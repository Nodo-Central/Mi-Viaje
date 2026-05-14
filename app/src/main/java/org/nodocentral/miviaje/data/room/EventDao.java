package org.nodocentral.miviaje.data.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(EventEntity event);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<EventEntity> events);

    @Query("DELETE FROM events")
    void deleteAll();

    @Query("SELECT * FROM events")
    List<EventEntity> getAll();

    @Query("SELECT * FROM events WHERE cardId = :cardId ORDER BY eventSequence DESC")
    List<EventEntity> getAllForCard(long cardId);

    @Query("SELECT * FROM events WHERE cardId = :cardId ORDER BY eventSequence DESC LIMIT :limit")
    List<EventEntity> getAllForCard(long cardId, int limit);
}

