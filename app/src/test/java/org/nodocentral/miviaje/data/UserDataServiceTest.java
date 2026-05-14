package org.nodocentral.miviaje.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import androidx.room.Room;

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
import java.util.ArrayList;
import java.util.List;

import org.nodocentral.miviaje.data.backup.models.BackupSnapshot;
import org.nodocentral.miviaje.data.repository.ArtworkRepository;
import org.nodocentral.miviaje.data.room.ArtworkEntity;
import org.nodocentral.miviaje.data.room.CardEntity;
import org.nodocentral.miviaje.data.room.EventEntity;
import org.nodocentral.miviaje.data.room.MiViajeDatabase;
import org.nodocentral.miviaje.data.room.ProductEntity;
import org.nodocentral.miviaje.domain.artwork.Artwork;

@RunWith(RobolectricTestRunner.class)
public class UserDataServiceTest {
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-05-04T12:00:00Z"),
            ZoneId.of("UTC")
    );
    private static final LocalDateTime IMPORT_TIME = LocalDateTime.of(2026, 5, 4, 12, 0);

    private MiViajeDatabase database;
    private TestArtworkRepository artworkRepository;
    private UserDataService service;

    @Before
    public void setUp() {
        database = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), MiViajeDatabase.class)
                .allowMainThreadQueries()
                .build();
        artworkRepository = new TestArtworkRepository(database);
        service = new UserDataService(database, artworkRepository, FIXED_CLOCK);
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void replaceData_rejectsNullSnapshot() {
        assertThrows(IllegalArgumentException.class, () -> service.replaceData(null));
    }

    @Test
    public void mergeData_rejectsNullSnapshot() {
        assertThrows(IllegalArgumentException.class, () -> service.mergeData(null));
    }

    @Test
    public void clearData_deletesDatabaseRowsAndArtworkFiles() {
        seedDatabase();
        artworkRepository.currentArtworks = List.of(
                new Artwork("old", "Old", "old.png", "image/png", "hash", LocalDateTime.now())
        );

        service.clearData();

        assertEquals(0, database.cardDao().getAll().size());
        assertEquals(0, database.productDao().getAll().size());
        assertEquals(0, database.eventDao().getAll().size());
        assertEquals(0, database.artworkDao().getAll().size());
        assertEquals(List.of("old.png"), artworkRepository.deletedPaths);
    }

    @Test
    public void replaceData_keepsRestoredArtworkFilesAndDeletesOrphans() {
        seedDatabase();
        artworkRepository.currentArtworks = List.of(
                new Artwork("old", "Old", "old.png", "image/png", "hash", LocalDateTime.now()),
                new Artwork("keep", "Keep", "keep.png", "image/png", "hash2", LocalDateTime.now())
        );
        BackupSnapshot snapshot = new BackupSnapshot(
                3,
                List.of(buildCardEntity(2L)),
                List.of(buildProductEntity(2L)),
                List.of(buildEventEntity(2L)),
                List.of(buildArtworkEntity("keep", "keep.png"), buildArtworkEntity("new", "new.png"))
        );

        service.replaceData(snapshot);

        assertEquals(1, database.cardDao().getAll().size());
        assertEquals(1, database.productDao().getAll().size());
        assertEquals(1, database.eventDao().getAll().size());
        assertEquals(2, database.artworkDao().getAll().size());
        assertEquals(List.of("old.png"), artworkRepository.deletedPaths);
    }

    @Test
    public void mergeData_upsertsImportedRowsWithoutDeletingExistingRows() {
        seedDatabase();
        BackupSnapshot snapshot = new BackupSnapshot(
                3,
                List.of(buildCardEntity(2L)),
                List.of(buildProductEntity(2L)),
                List.of(buildEventEntity(2L)),
                List.of(buildArtworkEntity("new", "new.png"))
        );

        service.mergeData(snapshot);

        assertEquals(2, database.cardDao().getAll().size());
        assertEquals(2, database.productDao().getAll().size());
        assertEquals(2, database.eventDao().getAll().size());
        assertEquals(2, database.artworkDao().getAll().size());
        assertEquals(List.of(), artworkRepository.deleteAttempts);
        assertEquals(IMPORT_TIME, database.cardDao().get(2L).lastUpdated);
    }

    @Test
    public void mergeData_preservesLocalAliasAndArtworkWhenPresent() {
        CardEntity existing = buildCardEntity(1L);
        existing.alias = "Local alias";
        existing.artworkRef = "local-art";
        existing.productionDate = LocalDate.of(2020, 1, 1);
        database.cardDao().upsert(existing);

        CardEntity imported = buildCardEntity(1L);
        imported.alias = "Backup alias";
        imported.artworkRef = "backup-art";
        imported.productionDate = LocalDate.of(2026, 4, 24);
        BackupSnapshot snapshot = new BackupSnapshot(3, List.of(imported), List.of(), List.of(), List.of());

        service.mergeData(snapshot);

        CardEntity merged = database.cardDao().get(1L);
        assertEquals("Local alias", merged.alias);
        assertEquals("local-art", merged.artworkRef);
        assertEquals(LocalDate.of(2026, 4, 24), merged.productionDate);
        assertEquals(IMPORT_TIME, merged.lastUpdated);
    }

    @Test
    public void mergeData_usesBackupAliasAndArtworkWhenLocalMissing() {
        database.cardDao().upsert(buildCardEntity(1L));

        CardEntity imported = buildCardEntity(1L);
        imported.alias = "Backup alias";
        imported.artworkRef = "backup-art";
        BackupSnapshot snapshot = new BackupSnapshot(3, List.of(imported), List.of(), List.of(), List.of());

        service.mergeData(snapshot);

        CardEntity merged = database.cardDao().get(1L);
        assertEquals("Backup alias", merged.alias);
        assertEquals("backup-art", merged.artworkRef);
    }

    @Test
    public void mergeData_usesBackupProductionDate() {
        CardEntity existing = buildCardEntity(1L);
        existing.productionDate = LocalDate.of(2020, 1, 1);
        database.cardDao().upsert(existing);

        CardEntity imported = buildCardEntity(1L);
        imported.productionDate = LocalDate.of(2026, 4, 24);
        BackupSnapshot snapshot = new BackupSnapshot(3, List.of(imported), List.of(), List.of(), List.of());

        service.mergeData(snapshot);

        assertEquals(LocalDate.of(2026, 4, 24), database.cardDao().get(1L).productionDate);
    }

    @Test
    public void mergeData_setsLastUpdatedToCurrentImportTime() {
        CardEntity existing = buildCardEntity(1L);
        existing.lastUpdated = LocalDateTime.of(2020, 1, 1, 1, 1);
        database.cardDao().upsert(existing);

        CardEntity imported = buildCardEntity(1L);
        imported.lastUpdated = LocalDateTime.of(2024, 1, 1, 1, 1);
        BackupSnapshot snapshot = new BackupSnapshot(3, List.of(imported), List.of(), List.of(), List.of());

        service.mergeData(snapshot);

        assertEquals(IMPORT_TIME, database.cardDao().get(1L).lastUpdated);
    }

    @Test
    public void mergeData_doesNotDeleteArtworkRowsOrFilesMissingFromBackup() {
        seedDatabase();
        artworkRepository.currentArtworks = List.of(
                new Artwork("old", "Old", "old.png", "image/png", "hash", LocalDateTime.now())
        );
        BackupSnapshot snapshot = new BackupSnapshot(3, List.of(), List.of(), List.of(), List.of());

        service.mergeData(snapshot);

        assertEquals(1, database.artworkDao().getAll().size());
        assertEquals(List.of(), artworkRepository.deleteAttempts);
        assertEquals(List.of(), artworkRepository.deletedPaths);
    }

    @Test
    public void clearData_ignoresArtworkDeleteFailures() {
        seedDatabase();
        artworkRepository.currentArtworks = List.of(
                new Artwork("old", "Old", "old.png", "image/png", "hash", LocalDateTime.now())
        );
        artworkRepository.failOnDeletePath = "old.png";

        service.clearData();

        assertEquals(0, database.cardDao().getAll().size());
        assertEquals(List.of("old.png"), artworkRepository.deleteAttempts);
    }

    private void seedDatabase() {
        database.cardDao().upsert(buildCardEntity(1L));
        database.productDao().insert(buildProductEntity(1L));
        database.eventDao().insert(buildEventEntity(1L));
        database.artworkDao().upsert(buildArtworkEntity("old", "old.png"));
    }

    private static CardEntity buildCardEntity(long uid) {
        CardEntity entity = new CardEntity();
        entity.uid = uid;
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
        return entity;
    }

    private static ProductEntity buildProductEntity(long cardId) {
        ProductEntity entity = new ProductEntity();
        entity.cardId = cardId;
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
        entity.state = 1;
        entity.weekOfYear = 17;
        entity.tripsPerDayOfWeek = 3;
        entity.totalUsages = 4;
        return entity;
    }

    private static EventEntity buildEventEntity(long cardId) {
        EventEntity entity = new EventEntity();
        entity.cardId = cardId;
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

    private static ArtworkEntity buildArtworkEntity(String id, String path) {
        ArtworkEntity entity = new ArtworkEntity();
        entity.id = id;
        entity.relativePath = path;
        return entity;
    }

    private static final class TestArtworkRepository extends ArtworkRepository {
        List<Artwork> currentArtworks = List.of();
        List<String> deletedPaths = new ArrayList<>();
        List<String> deleteAttempts = new ArrayList<>();
        String failOnDeletePath;

        TestArtworkRepository(MiViajeDatabase database) {
            super(database, null, null);
        }

        @Override
        public List<Artwork> getAll() {
            return currentArtworks;
        }

        @Override
        public void deleteFileByRelativePath(String relativePath) {
            deleteAttempts.add(relativePath);
            if (relativePath != null && relativePath.equals(failOnDeletePath)) {
                throw new RuntimeException("boom");
            }
            deletedPaths.add(relativePath);
        }
    }
}
