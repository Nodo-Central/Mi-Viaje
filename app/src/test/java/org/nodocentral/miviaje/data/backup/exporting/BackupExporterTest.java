package org.nodocentral.miviaje.data.backup.exporting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.room.Room;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import org.nodocentral.miviaje.data.repository.ArtworkRepository;
import org.nodocentral.miviaje.data.room.CardEntity;
import org.nodocentral.miviaje.data.room.EventEntity;
import org.nodocentral.miviaje.data.room.MiViajeDatabase;
import org.nodocentral.miviaje.data.room.ProductEntity;
import org.nodocentral.miviaje.domain.artwork.Artwork;

@RunWith(RobolectricTestRunner.class)
public class BackupExporterTest {
    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-04-24T12:00:00Z"), ZoneId.of("UTC"));
    private MiViajeDatabase database;
    private TestArtworkRepository artworkRepository;

    @Before
    public void setUp() {
        database = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), MiViajeDatabase.class)
                .allowMainThreadQueries()
                .build();
        artworkRepository = new TestArtworkRepository(database);
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void buildCardExportFileName_prefersAndSanitizesAlias() {
        BackupExporter exporter = new BackupExporter(fixedClock);

        String fileName = exporter.buildCardExportFileName(" My Card / 2026 ", 0x1234L);

        assertEquals("card_My_Card_2026_2026-04-24.json", fileName);
    }

    @Test
    public void buildCardExportFileName_fallsBackToCardUidWhenAliasBlank() {
        BackupExporter exporter = new BackupExporter(fixedClock);

        String fileName = exporter.buildCardExportFileName("   ", 0x1234L);

        assertEquals("card_00000000001234_2026-04-24.json", fileName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exportCard_throwsWhenCardDoesNotExist() {
        new BackupExporter(fixedClock).exportCard(database, artworkRepository, 0x1234L);
    }

    @Test
    public void exportData_containsSchemaVersionAndArrays() {
        database.cardDao().upsert(buildCardEntity());
        database.productDao().insert(buildProductEntity());
        database.eventDao().insert(buildEventEntity());
        artworkRepository.returnedArtworks = List.of(
                new Artwork("art-1", "Artwork", "art.png", "image/png", "abc", LocalDateTime.of(2026, 4, 24, 10, 0))
        );

        String json = new BackupExporter(fixedClock).exportData(database, artworkRepository);
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        assertEquals(3, root.get("schemaVersion").getAsInt());
        assertTrue(root.getAsJsonArray("cards").size() > 0);
        assertTrue(root.getAsJsonArray("products").size() > 0);
        assertTrue(root.getAsJsonArray("events").size() > 0);
        assertTrue(root.getAsJsonArray("artworks").size() > 0);

        JsonObject card = root.getAsJsonArray("cards").get(0).getAsJsonObject();
        assertEquals("Daily card", card.get("alias").getAsString());
        assertEquals("imported:art-1", card.get("artworkRef").getAsString());
        assertEquals(LocalDate.of(2024, 2, 3).toEpochDay(), card.get("productionDate").getAsLong());
        assertEquals(
                LocalDateTime.of(2026, 4, 24, 10, 0).toEpochSecond(ZoneOffset.ofHours(-6)),
                card.get("lastUpdated").getAsLong()
        );

        JsonObject product = root.getAsJsonArray("products").get(0).getAsJsonObject();
        assertEquals("00000001FFFFFFFF", product.get("distributionSamId").getAsString());
    }

    private static CardEntity buildCardEntity() {
        CardEntity entity = new CardEntity();
        entity.uid = 1L;
        entity.country = "MX";
        entity.serialNumber = 1234;
        entity.applicationNetworkId = 1;
        entity.applicationCompanyId = 2;
        entity.issuerNetworkId = 3;
        entity.issuerDistributorId = 4;
        entity.samUid = 5L;
        entity.algorithmId = 6;
        entity.keyVersion = 7;
        entity.environmentNetworkId = 8;
        entity.applicationVersion = 16;
        entity.userProfileType = 6;
        entity.applicationStatus = 1;
        entity.applicationEventCount = 2;
        entity.applicationActionsApplied = 3;
        entity.alias = "Daily card";
        entity.lastUpdated = LocalDateTime.of(2026, 4, 24, 10, 0);
        entity.productionDate = LocalDate.of(2024, 2, 3);
        entity.artworkRef = "imported:art-1";
        return entity;
    }

    private static ProductEntity buildProductEntity() {
        ProductEntity entity = new ProductEntity();
        entity.cardId = 1L;
        entity.productId = 0x1234;
        entity.value = 500;
        entity.valuePointer = 9;
        entity.priority = 1;
        entity.pointer = 7;
        entity.serial = 8;
        entity.priceCents = 900;
        entity.valueUnit = 0;
        entity.distributorNetworkId = 50;
        entity.distributorCompanyId = 60;
        entity.distributionSamId = 0x00000001FFFFFFFFL;
        entity.state = 1;
        entity.weekOfYear = 17;
        entity.tripsPerDayOfWeek = 3;
        entity.totalUsages = 4;
        return entity;
    }

    private static EventEntity buildEventEntity() {
        EventEntity entity = new EventEntity();
        entity.cardId = 1L;
        entity.eventSequence = 1;
        entity.productId = 0x1234;
        entity.productPointer = 7;
        entity.entityId = 20;
        entity.eventDateTime = LocalDateTime.of(2026, 4, 24, 9, 0);
        entity.eventType = 4;
        entity.amount = 250;
        entity.samId = 1L;
        entity.samSequence = 2L;
        entity.deviceId = 30;
        entity.locationId = 31;
        entity.transportType = 1;
        entity.routeStationId = 32;
        entity.transferCount = 1;
        entity.transferLimit = 3600;
        entity.passbackCount = 0;
        entity.refundReason = 0;
        entity.deviceType = 1;
        return entity;
    }

    private static final class TestArtworkRepository extends ArtworkRepository {
        List<Artwork> returnedArtworks = List.of();

        TestArtworkRepository(MiViajeDatabase database) {
            super(database, null, null);
        }

        @Override
        public List<Artwork> getAll() {
            return returnedArtworks;
        }

        @Override
        public Artwork getByRef(String artworkRef) {
            return returnedArtworks.isEmpty() ? null : returnedArtworks.get(0);
        }
    }
}
