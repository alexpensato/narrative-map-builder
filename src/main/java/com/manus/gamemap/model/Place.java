package com.manus.gamemap.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "places")
public class Place {

    @Id

    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "map_id")
    @JsonIgnore
    private GameMap map;

    private int x;
    private int y;
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    @CollectionTable(name = "place_actions", joinColumns = @JoinColumn(name = "place_id"))
    @Column(name = "action_text")
    @OrderColumn(name = "sort_order")
    private List<String> actionInPlace = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "place_objects", joinColumns = @JoinColumn(name = "place_id"))
    @Column(name = "object_name")
    @OrderColumn(name = "sort_order")
    private List<String> objects = new ArrayList<>();

    @OneToMany(mappedBy = "sourcePlace", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Connection> connections = new ArrayList<>();

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public GameMap getMap() { return map; }
    public void setMap(GameMap map) { this.map = map; }
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getActionInPlace() { return actionInPlace; }
    public void setActionInPlace(List<String> actionInPlace) { this.actionInPlace = actionInPlace; }
    public List<String> getObjects() { return objects; }
    public void setObjects(List<String> objects) { this.objects = objects; }
    public List<Connection> getConnections() { return connections; }
    public void setConnections(List<Connection> connections) {
        this.connections.clear();
        if (connections != null) {
            this.connections.addAll(connections);
        }
    }
}
