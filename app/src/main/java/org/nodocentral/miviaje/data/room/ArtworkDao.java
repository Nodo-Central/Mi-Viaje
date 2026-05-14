package org.nodocentral.miviaje.data.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ArtworkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(ArtworkEntity artwork);

    @Delete
    void delete(ArtworkEntity artwork);

    @Query("DELETE FROM artworks WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM artworks WHERE id = :id")
    ArtworkEntity getById(String id);

    @Query("UPDATE artworks SET displayName = :displayName WHERE id = :id")
    void updateDisplayName(String id, String displayName);

    @Query("DELETE FROM artworks")
    void deleteAll();

    @Query("SELECT * FROM artworks ORDER BY createdAt DESC")
    List<ArtworkEntity> getAll();
}
