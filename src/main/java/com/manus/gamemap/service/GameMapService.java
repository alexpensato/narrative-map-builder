package com.manus.gamemap.service;

import com.manus.gamemap.dto.MapDataDTO;
import com.manus.gamemap.model.Connection;
import com.manus.gamemap.dto.MapSummaryDTO;
import com.manus.gamemap.model.GameMap;
import com.manus.gamemap.model.Place;
import com.manus.gamemap.model.User;
import com.manus.gamemap.repository.GameMapRepository;
import com.manus.gamemap.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameMapService {

    @Autowired
    private GameMapRepository gameMapRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<MapSummaryDTO> getAllMapSummaries() {
        User currentUser = getCurrentUser();
        return gameMapRepository.findByUser(currentUser).stream()
                .map(map -> new MapSummaryDTO(map.getId(), map.getName(), map.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public MapDataDTO getMap(UUID id) {
        User currentUser = getCurrentUser();
        GameMap map = gameMapRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Map not found"));
        
        if (!map.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied");
        }

        return new MapDataDTO(map.getId(), map.getName(), map.getWidth(), map.getHeight(), map.getPlaces(), map.getSections());
    }

    @Transactional
    public MapDataDTO createOrUpdateMap(UUID id, MapDataDTO dto) {
        User currentUser = getCurrentUser();
        GameMap map;
        
        if (id != null && gameMapRepository.existsById(id)) {
            map = gameMapRepository.findById(id).get();
            if (!map.getUser().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Access denied");
            }
            
            // Check if name is being changed to something that already exists for this user
            // But allow keeping the same name for the same map
            if (dto.getName() != null && !dto.getName().equals(map.getName())) {
                 if (gameMapRepository.findByUser(currentUser).stream()
                         .anyMatch(m -> m.getName().equals(dto.getName()) && !m.getId().equals(id))) {
                     throw new RuntimeException("A map with this name already exists.");
                 }
            }
        } else {
            // Check for duplicate name for this user
            if (gameMapRepository.findByUser(currentUser).stream().anyMatch(m -> m.getName().equals(dto.getName()))) {
                 // If ID is null (new map) but name exists, we could throw error or update existing.
                 // For simplicity, let's throw error if they try to create a NEW map with same name.
                 // But if they are saving an existing map (id != null), we allow name change unless it conflicts with ANOTHER map.
                 throw new RuntimeException("A map with this name already exists.");
            }
            
            map = new GameMap();
            if (id != null) map.setId(id);
            map.setName("New Map"); // Default name
            map.setUser(currentUser);
        }

        map.setWidth(dto.getGridSize().getWidth());
        map.setHeight(dto.getGridSize().getHeight());
        if (dto.getName() != null) {
            map.setName(dto.getName());
        }

        // Smart update of places to avoid duplicate key errors
        updatePlaces(map, dto.getPlaces());
        updateSections(map, dto.getSections());

        GameMap savedMap = gameMapRepository.save(map);
        return new MapDataDTO(savedMap.getId(), savedMap.getName(), savedMap.getWidth(), savedMap.getHeight(), savedMap.getPlaces(), savedMap.getSections());
    }

    private void updatePlaces(GameMap map, List<Place> placeDtos) {
        if (placeDtos == null) {
            map.getPlaces().clear();
            return;
        }

        Map<UUID, Place> existingPlaces = new HashMap<>();
        for (Place p : map.getPlaces()) {
            existingPlaces.put(p.getId(), p);
        }

        // Map to track old ID -> new ID for remapping connections
        Map<UUID, UUID> idMapping = new HashMap<>();
        List<Place> finalPlaces = new ArrayList<>();
        
        // First pass: Create/Update places and build ID mapping
        for (Place dto : placeDtos) {
            Place place = existingPlaces.get(dto.getId());
            if (place == null) {
                // New place (or imported from another map)
                place = new Place();
                UUID newId = UUID.randomUUID();
                place.setId(newId);
                place.setMap(map);
                idMapping.put(dto.getId(), newId);
            } else {
                // Existing place, keep ID
                idMapping.put(dto.getId(), dto.getId());
            }
            
            // Update fields
            place.setX(dto.getX());
            place.setY(dto.getY());
            place.setName(dto.getName());
            place.setDescription(dto.getDescription());
            place.setActionInPlace(dto.getActionInPlace());
            place.setObjects(dto.getObjects());
            place.setSectionId(dto.getSectionId());
            
            finalPlaces.add(place);
        }

        // Second pass: Update connections with remapped IDs
        for (int i = 0; i < placeDtos.size(); i++) {
            Place dto = placeDtos.get(i);
            Place place = finalPlaces.get(i);
            updateConnections(place, dto.getConnections(), idMapping);
        }

        map.getPlaces().clear();
        map.getPlaces().addAll(finalPlaces);
    }

    private void updateSections(GameMap map, List<com.manus.gamemap.model.Section> sectionDtos) {
        if (sectionDtos == null) {
            map.getSections().clear();
            return;
        }
        
        Map<String, com.manus.gamemap.model.Section> existingSections = new HashMap<>();
        for (com.manus.gamemap.model.Section s : map.getSections()) {
            existingSections.put(s.getId(), s);
        }
        
        // We also need to remap section IDs if we are regenerating them
        Map<String, String> sectionIdMapping = new HashMap<>();
        List<com.manus.gamemap.model.Section> finalSections = new ArrayList<>();
        
        for (com.manus.gamemap.model.Section dto : sectionDtos) {
            com.manus.gamemap.model.Section section = existingSections.get(dto.getId());
            if (section == null) {
                section = new com.manus.gamemap.model.Section();
                String newId = UUID.randomUUID().toString();
                section.setId(newId);
                section.setMap(map);
                sectionIdMapping.put(dto.getId(), newId);
            } else {
                sectionIdMapping.put(dto.getId(), dto.getId());
            }
            section.setName(dto.getName());
            section.setColor(dto.getColor());
            finalSections.add(section);
        }
        
        map.getSections().clear();
        map.getSections().addAll(finalSections);
        
        // Update places with new section IDs
        for (Place place : map.getPlaces()) {
            if (place.getSectionId() != null && sectionIdMapping.containsKey(place.getSectionId())) {
                place.setSectionId(sectionIdMapping.get(place.getSectionId()));
            } else if (place.getSectionId() != null && !existingSections.containsKey(place.getSectionId())) {
                 place.setSectionId(null);
            }
        }
    }

    private void updateConnections(Place place, List<Connection> connectionDtos, Map<UUID, UUID> idMapping) {
        if (connectionDtos == null) {
            place.getConnections().clear();
            return;
        }
        
        place.getConnections().clear();
        
        for (Connection dto : connectionDtos) {
            // Remap target ID if it exists in our mapping (which it should if it's in the map)
            // If not in mapping, it might be a reference to a deleted place or external? 
            // For safety, only add if target is valid.
            UUID targetUuid = dto.getTargetId();
            
            if (targetUuid != null && idMapping.containsKey(targetUuid)) {
                Connection conn = new Connection();
                conn.setId(UUID.randomUUID());
                conn.setTargetId(idMapping.get(targetUuid)); 
                conn.setDirection(dto.getDirection());
                conn.setBidirectional(dto.isBidirectional());
                conn.setAction(dto.getAction());
                conn.setSourcePlace(place);
                place.getConnections().add(conn);
            }
        }
    }
}
