package com.manus.gamemap.repository;

import com.manus.gamemap.model.GameMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface GameMapRepository extends JpaRepository<GameMap, UUID> {
}
