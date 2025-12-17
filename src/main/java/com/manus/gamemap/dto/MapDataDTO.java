package com.manus.gamemap.dto;

import com.manus.gamemap.model.Place;
import com.manus.gamemap.model.Section;
import java.util.List;
import java.util.UUID;

public class MapDataDTO {
    private UUID id;
    private String name;
    private GridSizeDTO gridSize;
    private List<Place> places;
    private List<Section> sections;

    public MapDataDTO() {}

    public MapDataDTO(UUID id, String name, int width, int height, List<Place> places, List<Section> sections) {
        this.id = id;
        this.name = name;
        this.gridSize = new GridSizeDTO(width, height);
        this.places = places;
        this.sections = sections;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public GridSizeDTO getGridSize() { return gridSize; }
    public void setGridSize(GridSizeDTO gridSize) { this.gridSize = gridSize; }
    public List<Place> getPlaces() { return places; }
    public void setPlaces(List<Place> places) { this.places = places; }
    public List<Section> getSections() { return sections; }
    public void setSections(List<Section> sections) { this.sections = sections; }

    public static class GridSizeDTO {
        private int width;
        private int height;

        public GridSizeDTO() {}
        public GridSizeDTO(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }
        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }
    }
}
