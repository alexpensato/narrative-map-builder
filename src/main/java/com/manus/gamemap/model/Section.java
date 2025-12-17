package com.manus.gamemap.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "sections")
public class Section {
    @Id
    private String id;

    private String name;
    private String color;

    @ManyToOne
    @JoinColumn(name = "map_id")
    @JsonBackReference
    private GameMap map;

    public Section() {}

    public Section(String id, String name, String color, GameMap map) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.map = map;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public GameMap getMap() { return map; }
    public void setMap(GameMap map) { this.map = map; }
}
