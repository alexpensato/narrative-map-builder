package com.manus.gamemap.repository;

import com.manus.gamemap.model.GameMap;
import com.manus.gamemap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface GameMapRepository extends JpaRepository<GameMap, UUID> {
    List<GameMap> findByUser(User user);
}
