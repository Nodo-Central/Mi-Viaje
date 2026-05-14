package org.nodocentral.miviaje.data.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ProductEntity product);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ProductEntity> products);

    @Query("DELETE FROM products")
    void deleteAll();

    @Query("SELECT * FROM products WHERE cardId = :cardId")
    List<ProductEntity> getProductsForCard(int cardId);

    @Query("SELECT * FROM products")
    List<ProductEntity> getAll();

    @Query("SELECT * FROM products WHERE cardId = :cardId")
    List<ProductEntity> getAllForCard(long cardId);
}

