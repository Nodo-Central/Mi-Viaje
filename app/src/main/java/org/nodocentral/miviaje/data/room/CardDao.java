package org.nodocentral.miviaje.data.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CardDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(CardEntity card);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<CardEntity> cards);

    @Update
    void update(CardEntity card);

    @Transaction
    default void upsert(CardEntity card) {
        long id = insert(card);
        if (id == -1) {
            update(card);
        }
    }

    @Transaction
    default void upsertAll(List<CardEntity> cards) {
        for (CardEntity card : cards) {
            upsert(card);
        }
    }

    @Delete
    void delete(CardEntity card);

    @Query("DELETE FROM cards WHERE uid = :uid")
    void deleteByUid(long uid);

    @Query("DELETE FROM cards")
    void deleteAll();

    @Query("SELECT * FROM cards ORDER BY `lastUpdated`, `applicationEventCount` DESC")
    List<CardEntity> getAll();

    @Query("SELECT * FROM cards WHERE uid = :uid")
    CardEntity get(long uid);

    @Query("UPDATE cards SET alias = :alias WHERE uid = :uid")
    void updateAlias(long uid, String alias);

    @Query("UPDATE cards SET artworkRef = :artworkRef WHERE uid = :uid")
    void updateArtworkRef(long uid, String artworkRef);

    @Query("UPDATE cards SET artworkRef = NULL WHERE artworkRef = :artworkRef")
    void clearArtworkRef(String artworkRef);

    @Query("SELECT COUNT(*) FROM cards WHERE artworkRef = :artworkRef")
    int countByArtworkRef(String artworkRef);
}
