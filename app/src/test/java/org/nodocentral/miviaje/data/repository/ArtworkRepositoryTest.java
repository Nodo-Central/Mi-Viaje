package org.nodocentral.miviaje.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import androidx.room.Room;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nodocentral.miviaje.data.room.ArtworkEntity;
import org.nodocentral.miviaje.data.room.MiViajeDatabase;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.time.LocalDateTime;

@RunWith(RobolectricTestRunner.class)
public class ArtworkRepositoryTest {
    private MiViajeDatabase database;
    private ArtworkRepository repository;

    @Before
    public void setUp() {
        database = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), MiViajeDatabase.class)
                .allowMainThreadQueries()
                .build();
        repository = new ArtworkRepository(RuntimeEnvironment.getApplication(), database);
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void updateDisplayName_savesNormalName() {
        seedArtwork("artwork-1");

        repository.updateDisplayName("artwork-1", "Front card");

        assertEquals("Front card", database.artworkDao().getById("artwork-1").displayName);
    }

    @Test
    public void updateDisplayName_trimsName() {
        seedArtwork("artwork-1");

        repository.updateDisplayName("artwork-1", "  Front card  ");

        assertEquals("Front card", database.artworkDao().getById("artwork-1").displayName);
    }

    @Test
    public void updateDisplayName_storesBlankNameAsNull() {
        seedArtwork("artwork-1");

        repository.updateDisplayName("artwork-1", "   ");

        assertNull(database.artworkDao().getById("artwork-1").displayName);
    }

    @Test
    public void updateDisplayName_ignoresMissingArtwork() {
        repository.updateDisplayName("missing", "Missing");

        assertNull(database.artworkDao().getById("missing"));
    }

    private void seedArtwork(String id) {
        ArtworkEntity entity = new ArtworkEntity();
        entity.id = id;
        entity.displayName = "Original";
        entity.relativePath = id + ".jpg";
        entity.mimeType = "image/jpeg";
        entity.sha256 = "hash";
        entity.createdAt = LocalDateTime.of(2026, 5, 10, 12, 0);
        database.artworkDao().upsert(entity);
    }
}
