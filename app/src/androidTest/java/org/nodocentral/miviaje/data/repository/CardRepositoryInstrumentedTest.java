package org.nodocentral.miviaje.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.room.Room;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.nodocentral.miviaje.data.room.CardEntity;
import org.nodocentral.miviaje.data.room.MiViajeDatabase;
import org.nodocentral.miviaje.domain.mimovilidad.card.ApplicationStatus;
import org.nodocentral.miviaje.domain.mimovilidad.card.Card;
import org.nodocentral.miviaje.domain.mimovilidad.card.Emission;
import org.nodocentral.miviaje.domain.mimovilidad.card.Environment;
import org.nodocentral.miviaje.domain.mimovilidad.card.Event;
import org.nodocentral.miviaje.domain.mimovilidad.card.Product;
import org.nodocentral.miviaje.domain.mimovilidad.card.ProductContract;
import org.nodocentral.miviaje.domain.mimovilidad.card.ProductService;
import org.nodocentral.miviaje.domain.mimovilidad.card.User;

@RunWith(AndroidJUnit4.class)
public class CardRepositoryInstrumentedTest {
    private MiViajeDatabase database;
    private CardRepository repository;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        database = Room.inMemoryDatabaseBuilder(context, MiViajeDatabase.class)
                .allowMainThreadQueries()
                .build();
        repository = new CardRepository(database);
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void save_newCardInsertsCardProductsAndEvents() {
        Card card = buildCard(0x1111L, "Alpha", "imported:art-a", LocalDateTime.of(2026, 4, 24, 8, 0), 3, 3);

        boolean inserted = repository.save(card);
        Card loaded = repository.getCard(card.getUid(), 10);

        assertTrue(inserted);
        assertEquals("Alpha", loaded.getAlias());
        assertEquals("imported:art-a", loaded.getArtworkRef());
        assertEquals(2, loaded.getProductList().size());
        assertEquals(3, loaded.getEvents().size());
    }

    @Test
    public void save_existingCardPreservesEditableFieldsWhenIncomingValuesAreNull() {
        LocalDateTime originalUpdatedAt = LocalDateTime.of(2026, 4, 24, 8, 0);
        repository.save(buildCard(0x1111L, "Alpha", "imported:art-a", originalUpdatedAt, 2, 2));

        boolean inserted = repository.save(buildCard(0x1111L, null, null, null, 7, 2));
        Card loaded = repository.getCard(0x1111L, 10);

        assertFalse(inserted);
        assertEquals("Alpha", loaded.getAlias());
        assertEquals("imported:art-a", loaded.getArtworkRef());
        assertEquals(originalUpdatedAt, loaded.getLastUpdated());
        assertEquals(7, loaded.getApplicationStatus().getEventCount());
    }

    @Test
    public void getCard_reconstructsFullDomainObjectAndLimitsRecentEvents() {
        Card card = buildCard(0x1111L, "Alpha", "imported:art-a", LocalDateTime.of(2026, 4, 24, 8, 0), 3, 3);
        repository.save(card);

        Card loaded = repository.getCard(card.getUid(), 2);

        assertEquals(2, loaded.getProductList().size());
        assertEquals(2, loaded.getEvents().size());
        assertEquals(3, loaded.getEvents().get(0).getEventSequence());
        assertEquals(2, loaded.getEvents().get(1).getEventSequence());
        assertEquals(LocalDateTime.of(2026, 4, 24, 12, 3), loaded.getProductById(0x1234).getService().getLastDebitDateTime());
    }

    @Test
    public void getAllCards_appliesRecentEventLimitPerCard() {
        repository.save(buildCard(0x1111L, "Alpha", "imported:art-a", LocalDateTime.of(2026, 4, 24, 8, 0), 3, 3));
        repository.save(buildCard(0x2222L, "Beta", "imported:art-b", LocalDateTime.of(2026, 4, 25, 8, 0), 1, 1));

        List<Card> cards = repository.getAllCards(1);

        assertEquals(2, cards.size());
        assertEquals(1, cards.get(0).getEvents().size());
        assertEquals(1, cards.get(1).getEvents().size());
    }

    @Test
    public void updateAndDeleteOperations_modifyStoredCard() {
        repository.save(buildCard(0x1111L, "Alpha", "imported:art-a", LocalDateTime.of(2026, 4, 24, 8, 0), 2, 1));

        repository.updateAlias(0x1111L, "Renamed");
        repository.updateArtworkRef(0x1111L, "imported:art-b");
        assertEquals("Renamed", repository.getCard(0x1111L, 10).getAlias());
        assertEquals("imported:art-b", repository.getCard(0x1111L, 10).getArtworkRef());

        repository.clearArtworkRef("imported:art-b");
        assertNull(repository.getCard(0x1111L, 10).getArtworkRef());

        repository.delete(0x1111L);
        assertNull(repository.getCard(0x1111L, 10));
    }

    @Test
    public void cardDaoGetAll_keepsCurrentApplicationEventCountOrderingForEqualTimestamps() {
        repository.save(buildCard(0x1111L, "Alpha", "imported:art-a", LocalDateTime.of(2026, 4, 24, 8, 0), 2, 1));
        repository.save(buildCard(0x2222L, "Beta", "imported:art-b", LocalDateTime.of(2026, 4, 24, 8, 0), 5, 1));

        List<CardEntity> cards = database.cardDao().getAll();

        assertEquals(0x2222L, cards.get(0).uid);
        assertEquals(0x1111L, cards.get(1).uid);
    }

    private static Card buildCard(long uid,
                                  String alias,
                                  String artworkRef,
                                  LocalDateTime lastUpdated,
                                  int applicationEventCount,
                                  int eventCount) {
        Product firstProduct = buildProduct((short) 0x1234, 500, 9, 1, LocalDateTime.of(2026, 4, 24, 12, 3));
        Product secondProduct = buildProduct((short) 0x1235, 300, 10, 2, LocalDateTime.of(2026, 4, 24, 12, 4));
        Map<Integer, Product> products = new LinkedHashMap<>();
        products.put((int) firstProduct.getId(), firstProduct);
        products.put((int) secondProduct.getId(), secondProduct);

        return new Card(
                uid,
                new Emission(
                        "MX",
                        123456,
                        LocalDate.of(2030, 1, 1),
                        new Emission.ApplicationOwner(1, 2),
                        new Emission.Issuer(3, 4),
                        new Emission.SecurityVersion(5L, 6, (byte) 7)
                ),
                new Environment(16, 8, LocalDate.of(2030, 1, 2)),
                new User(
                        LocalDate.of(2000, 1, 1),
                        new User.Profile(User.Profile.Type.STUDENT, LocalDate.of(2030, 1, 3), "Ada", "CURP")
                ),
                new ApplicationStatus(ApplicationStatus.State.ACTIVATED, applicationEventCount, 9),
                products,
                buildEvents(firstProduct, secondProduct, eventCount),
                alias,
                artworkRef,
                lastUpdated,
                LocalDate.of(2024, 1, 1)
        );
    }

    private static Product buildProduct(short productId,
                                        int value,
                                        int valuePointer,
                                        int pointer,
                                        LocalDateTime lastDebitDateTime) {
        return new Product(
                productId,
                value,
                valuePointer,
                1,
                new ProductContract(
                        productId,
                        200,
                        pointer,
                        (short) 1,
                        (short) 2,
                        (short) 300,
                        ProductContract.ValueUnit.MXN_CENT,
                        10,
                        1000,
                        new ProductContract.Retailer(50, (short) 60),
                        new ProductContract.DistributionInfo(LocalDateTime.of(2026, 4, 20, 9, 30), 70, (short) 80),
                        new ProductContract.Validity(
                                LocalDateTime.of(2026, 4, 21, 0, 0),
                                LocalDateTime.of(2026, 5, 21, 23, 59),
                                LocalTime.of(5, 0),
                                LocalTime.of(23, 0)
                        ),
                        new ProductContract.Restrictions((byte) 0, 20, (short) 30, (byte) 2, (short) 45, (byte) 1)
                ),
                new ProductService(Product.State.ACTIVE, 17, 5, 9, lastDebitDateTime, 10, 11, 12)
        );
    }

    private static List<Event> buildEvents(Product firstProduct, Product secondProduct, int count) {
        java.util.ArrayList<Event> events = new java.util.ArrayList<>();
        if (count >= 1) {
            events.add(buildEvent(firstProduct, 1, LocalDateTime.of(2026, 4, 24, 12, 1)));
        }
        if (count >= 2) {
            events.add(buildEvent(secondProduct, 2, LocalDateTime.of(2026, 4, 24, 12, 2)));
        }
        if (count >= 3) {
            events.add(buildEvent(firstProduct, 3, LocalDateTime.of(2026, 4, 24, 12, 3)));
        }
        return events;
    }

    private static Event buildEvent(Product product, int sequence, LocalDateTime dateTime) {
        return new Event(
                product.getId(),
                product.getContract().getProductPointer(),
                product,
                20,
                dateTime,
                Event.Type.PRODUCT_USE,
                250,
                sequence,
                1L,
                sequence,
                30,
                31,
                Event.TransportType.BUS,
                32,
                1,
                3600,
                0,
                Event.RefundReason.NO_REFUND,
                Event.DeviceType.FARE_VALIDATOR
        );
    }
}
