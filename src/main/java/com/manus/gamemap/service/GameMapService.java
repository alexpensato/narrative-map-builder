package com.manus.gamemap.service;

import com.manus.gamemap.dto.MapDataDTO;
import com.manus.gamemap.model.Connection;
import com.manus.gamemap.dto.MapSummaryDTO;
import com.manus.gamemap.model.GameMap;
import com.manus.gamemap.model.Place;
import com.manus.gamemap.repository.GameMapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GameMapService {

    @Autowired
    private GameMapRepository gameMapRepository;

    public List<MapSummaryDTO> getAllMapSummaries() {
        return gameMapRepository.findAll().stream()
                .map(map -> new MapSummaryDTO(map.getId(), map.getName(), map.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public MapDataDTO getMap(UUID id) {
        GameMap map = gameMapRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Map not found"));
        return new MapDataDTO(map.getId(), map.getName(), map.getWidth(), map.getHeight(), map.getPlaces());
    }

    @Transactional
    public MapDataDTO createOrUpdateMap(UUID id, MapDataDTO dto) {
        GameMap map;
        if (id != null && gameMapRepository.existsById(id)) {
            map = gameMapRepository.findById(id).get();
        } else {
            map = new GameMap();
            if (id != null) map.setId(id);
            map.setName("New Map"); // Default name
        }

        map.setWidth(dto.getGridSize().getWidth());
        map.setHeight(dto.getGridSize().getHeight());
        if (dto.getName() != null) {
            map.setName(dto.getName());
        }

        // Clear existing places and re-add (simple replacement strategy for full sync)
        map.getPlaces().clear();
        
        if (dto.getPlaces() != null) {
            for (Place placeDto : dto.getPlaces()) {
                Place place = new Place();
                place.setId(placeDto.getId()); // Preserve frontend IDs
                place.setX(placeDto.getX());
                place.setY(placeDto.getY());
                place.setName(placeDto.getName());
                place.setDescription(placeDto.getDescription());
                place.setActionInPlace(placeDto.getActionInPlace());
                place.setObjects(placeDto.getObjects());
                place.setMap(map);

                if (placeDto.getConnections() != null) {
                    for (Connection connDto : placeDto.getConnections()) {
                        Connection conn = new Connection();
                        if (connDto.getId() != null) {
                            conn.setId(connDto.getId());
                        } else {
                            conn.setId(UUID.randomUUID());
                        }
                        conn.setTargetId(connDto.getTargetId());
                        conn.setDirection(connDto.getDirection());
                        conn.setBidirectional(connDto.isBidirectional());
                        conn.setAction(connDto.getAction());
                        conn.setSourcePlace(place);
                        place.getConnections().add(conn);
                    }
                }
                map.addPlace(place);
            }
        }

        GameMap savedMap = gameMapRepository.save(map);
        return new MapDataDTO(savedMap.getId(), savedMap.getName(), savedMap.getWidth(), savedMap.getHeight(), savedMap.getPlaces());
    }
}
