package org.nodocentral.miviaje.data.repository;

import org.nodocentral.miviaje.data.mappers.EventMapper;
import org.nodocentral.miviaje.data.room.EventDao;
import org.nodocentral.miviaje.data.room.EventEntity;
import org.nodocentral.miviaje.data.room.MiViajeDatabase;
import org.nodocentral.miviaje.domain.mimovilidad.card.Card;
import org.nodocentral.miviaje.domain.mimovilidad.card.Event;
import org.nodocentral.miviaje.domain.mimovilidad.card.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventRepository {
    private final EventDao eventDao;
    private final ProductRepository productRepository;

    public EventRepository(MiViajeDatabase database) {
        this(database, new ProductRepository(database));
    }

    public EventRepository(MiViajeDatabase database, ProductRepository productRepository) {
        this.eventDao = database.eventDao();
        this.productRepository = productRepository;
    }

    public List<Event> getEventsForCard(long cardUid) {
        Map<Integer, Product> products = productRepository.getDomainMapForCard(cardUid);
        return EventMapper.toDomain(getEntitiesForCard(cardUid), products);
    }

    public List<Event> getEventsForCard(long cardUid, int limit) {
        Map<Integer, Product> products = productRepository.getDomainMapForCard(cardUid);
        return EventMapper.toDomain(getEntitiesForCard(cardUid, limit), products);
    }

    public void saveForCard(Card card) {
        List<EventEntity> entities = new ArrayList<>();
        for (Event event : card.getEvents()) {
            entities.add(EventMapper.toEntity(event, card.getUid()));
        }
        eventDao.insertAll(entities);
    }

    List<EventEntity> getEntitiesForCard(long cardUid) {
        return eventDao.getAllForCard(cardUid);
    }

    List<EventEntity> getEntitiesForCard(long cardUid, int limit) {
        if (limit > 0) {
            return eventDao.getAllForCard(cardUid, limit);
        }
        return eventDao.getAllForCard(cardUid);
    }
}
