package com.manus.gamemap.controller;

import com.manus.gamemap.dto.MapDataDTO;
import com.manus.gamemap.dto.MapSummaryDTO;
import com.manus.gamemap.service.GameMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/maps")
public class GameMapController {

    @Autowired
    private GameMapService gameMapService;

    @GetMapping
    public List<MapSummaryDTO> getAllMaps() {
        return gameMapService.getAllMapSummaries();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MapDataDTO> getMap(@PathVariable UUID id) {
        return ResponseEntity.ok(gameMapService.getMap(id));
    }

    @PostMapping
    public ResponseEntity<MapDataDTO> createMap(@RequestBody MapDataDTO dto) {
        return ResponseEntity.ok(gameMapService.createOrUpdateMap(null, dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MapDataDTO> updateMap(@PathVariable UUID id, @RequestBody MapDataDTO dto) {
        return ResponseEntity.ok(gameMapService.createOrUpdateMap(id, dto));
    }
}
