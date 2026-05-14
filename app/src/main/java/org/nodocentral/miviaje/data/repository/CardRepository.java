package org.nodocentral.miviaje.data.repository;

import org.nodocentral.miviaje.data.mappers.CardMapper;
import org.nodocentral.miviaje.data.room.CardDao;
import org.nodocentral.miviaje.data.room.CardEntity;
import org.nodocentral.miviaje.data.room.EventEntity;
import org.nodocentral.miviaje.data.room.MiViajeDatabase;
import org.nodocentral.miviaje.data.room.ProductEntity;
import org.nodocentral.miviaje.domain.mimovilidad.card.Card;

import java.util.ArrayList;
import java.util.List;

public class CardRepository {
    private final MiViajeDatabase database;
    private final CardDao cardDao;
    private final ProductRepository productRepository;
    private final EventRepository eventRepository;

    public CardRepository(MiViajeDatabase database) {
        this(database, new ProductRepository(database), null);
    }

    public CardRepository(MiViajeDatabase database,
                          ProductRepository productRepository,
                          EventRepository eventRepository) {
        this.database = database;
        this.cardDao = database.cardDao();
        this.productRepository = productRepository;
        this.eventRepository = eventRepository != null
                ? eventRepository
                : new EventRepository(database, productRepository);
    }

    public List<Card> getAllCards(int recentEventLimitPerCard) {
        List<CardEntity> cardEntities = cardDao.getAll();
        List<Card> cards = new ArrayList<>(cardEntities.size());
        for (CardEntity cardEntity : cardEntities) {
            cards.add(toDomain(cardEntity, recentEventLimitPerCard));
        }
        return cards;
    }

    public Card getCard(long cardUid, int recentEventLimit) {
        CardEntity cardEntity = cardDao.get(cardUid);
        return toDomain(cardEntity, recentEventLimit);
    }

    public boolean save(Card card) {
        CardEntity updatedCard = CardMapper.toEntity(card);
        CardEntity existingCard = cardDao.get(card.getUid());
        preserveEditableCardFields(existingCard, updatedCard);

        database.runInTransaction(() -> {
            cardDao.upsert(updatedCard);
            eventRepository.saveForCard(card);
            productRepository.saveForCard(card);
        });

        return existingCard == null;
    }

    public void delete(long cardUid) {
        cardDao.deleteByUid(cardUid);
    }

    public void updateAlias(long cardUid, String alias) {
        cardDao.updateAlias(cardUid, alias);
    }

    public void updateArtworkRef(long cardUid, String artworkRef) {
        cardDao.updateArtworkRef(cardUid, artworkRef);
    }

    public void clearArtworkRef(String artworkRef) {
        cardDao.clearArtworkRef(artworkRef);
    }

    public int countByArtworkRef(String artworkRef) {
        return cardDao.countByArtworkRef(artworkRef);
    }

    private Card toDomain(CardEntity cardEntity, int recentEventLimit) {
        if (cardEntity == null) {
            return null;
        }
        List<ProductEntity> productEntities = productRepository.getEntitiesForCard(cardEntity.uid);
        List<EventEntity> eventEntities = eventRepository.getEntitiesForCard(cardEntity.uid, recentEventLimit);
        return CardMapper.toDomain(cardEntity, productEntities, eventEntities);
    }

    private void preserveEditableCardFields(CardEntity existingCard, CardEntity updatedCard) {
        if (existingCard == null || updatedCard == null) {
            return;
        }

        if (updatedCard.alias == null) {
            updatedCard.alias = existingCard.alias;
        }
        if (updatedCard.artworkRef == null) {
            updatedCard.artworkRef = existingCard.artworkRef;
        }
        if (updatedCard.lastUpdated == null) {
            updatedCard.lastUpdated = existingCard.lastUpdated;
        }
    }
}
