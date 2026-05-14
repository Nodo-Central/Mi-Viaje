package org.nodocentral.miviaje.data.repository;

import org.nodocentral.miviaje.data.mappers.ProductMapper;
import org.nodocentral.miviaje.data.room.MiViajeDatabase;
import org.nodocentral.miviaje.data.room.ProductDao;
import org.nodocentral.miviaje.data.room.ProductEntity;
import org.nodocentral.miviaje.domain.mimovilidad.card.Card;
import org.nodocentral.miviaje.domain.mimovilidad.card.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductRepository {
    private final ProductDao productDao;

    public ProductRepository(MiViajeDatabase database) {
        this.productDao = database.productDao();
    }

    public Map<Integer, Product> getDomainMapForCard(long cardUid) {
        return ProductMapper.toDomain(getEntitiesForCard(cardUid));
    }

    public List<Product> getDomainListForCard(long cardUid) {
        List<ProductEntity> entities = getEntitiesForCard(cardUid);
        List<Product> products = new ArrayList<>(entities.size());
        for (ProductEntity entity : entities) {
            products.add(ProductMapper.toDomain(entity));
        }
        return products;
    }

    public void saveForCard(Card card) {
        List<ProductEntity> entities = new ArrayList<>();
        for (Product product : card.getProductList()) {
            entities.add(ProductMapper.toEntity(product, card.getUid()));
        }
        productDao.insertAll(entities);
    }

    List<ProductEntity> getEntitiesForCard(long cardUid) {
        return productDao.getAllForCard(cardUid);
    }
}
