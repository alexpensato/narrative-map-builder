package com.manus.gamemap.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "connections")
public class Connection {

    @Id

    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_place_id")
    @JsonIgnore
    private Place sourcePlace;

    @Column(name = "target_place_id")
    private UUID targetId;

    private String direction;
    private boolean bidirectional;
    
    @Column(name = "travel_action")
    private String action;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Place getSourcePlace() { return sourcePlace; }
    public void setSourcePlace(Place sourcePlace) { this.sourcePlace = sourcePlace; }
    public UUID getTargetId() { return targetId; }
    public void setTargetId(UUID targetId) { this.targetId = targetId; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public boolean isBidirectional() { return bidirectional; }
    public void setBidirectional(boolean bidirectional) { this.bidirectional = bidirectional; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}
