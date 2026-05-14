package org.nodocentral.miviaje.data;

import android.util.Log;

import org.nodocentral.miviaje.data.backup.models.BackupSnapshot;
import org.nodocentral.miviaje.data.repository.ArtworkRepository;
import org.nodocentral.miviaje.data.room.ArtworkEntity;
import org.nodocentral.miviaje.data.room.CardEntity;
import org.nodocentral.miviaje.data.room.MiViajeDatabase;
import org.nodocentral.miviaje.domain.artwork.Artwork;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class UserDataService {
    private static final String TAG = "USER_DATA";

    private final MiViajeDatabase database;
    private final ArtworkRepository artworkRepository;
    private final Clock clock;

    public UserDataService(MiViajeDatabase database, ArtworkRepository artworkRepository) {
        this(database, artworkRepository, Clock.systemDefaultZone());
    }

    public UserDataService(MiViajeDatabase database, ArtworkRepository artworkRepository, Clock clock) {
        this.database = database;
        this.artworkRepository = artworkRepository;
        this.clock = clock;
    }

    public void clearData() {
        Set<String> existingArtworkPaths = collectCurrentArtworkPaths();

        database.runInTransaction(() -> {
            database.eventDao().deleteAll();
            database.productDao().deleteAll();
            database.cardDao().deleteAll();
            database.artworkDao().deleteAll();
        });

        deleteArtworkFiles(existingArtworkPaths);
    }

    public void replaceData(BackupSnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Backup snapshot is null");
        }

        Set<String> previousArtworkPaths = collectCurrentArtworkPaths();
        Set<String> restoredArtworkPaths = collectSnapshotArtworkPaths(snapshot);

        database.runInTransaction(() -> {
            database.eventDao().deleteAll();
            database.productDao().deleteAll();
            database.cardDao().deleteAll();
            database.artworkDao().deleteAll();

            database.cardDao().insertAll(snapshot.cards);
            database.productDao().insertAll(snapshot.products);
            database.eventDao().insertAll(snapshot.events);
            for (ArtworkEntity artwork : snapshot.artworks) {
                database.artworkDao().upsert(artwork);
            }
        });

        previousArtworkPaths.removeAll(restoredArtworkPaths);
        deleteArtworkFiles(previousArtworkPaths);
    }

    public void mergeData(BackupSnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Backup snapshot is null");
        }

        LocalDateTime importTime = LocalDateTime.now(clock);
        database.runInTransaction(() -> {
            for (CardEntity importedCard : snapshot.cards) {
                database.cardDao().upsert(mergeCard(importedCard, importTime));
            }
            database.productDao().insertAll(snapshot.products);
            database.eventDao().insertAll(snapshot.events);
            for (ArtworkEntity artwork : snapshot.artworks) {
                database.artworkDao().upsert(artwork);
            }
        });
    }

    private CardEntity mergeCard(CardEntity importedCard, LocalDateTime importTime) {
        CardEntity existingCard = database.cardDao().get(importedCard.uid);
        importedCard.lastUpdated = importTime;
        if (existingCard == null) {
            return importedCard;
        }

        importedCard.alias = chooseLocalWhenPresent(existingCard.alias, importedCard.alias);
        importedCard.artworkRef = chooseLocalWhenPresent(existingCard.artworkRef, importedCard.artworkRef);
        return importedCard;
    }

    private String chooseLocalWhenPresent(String localValue, String backupValue) {
        if (localValue != null && !localValue.isBlank()) {
            return localValue;
        }
        return backupValue;
    }

    private Set<String> collectCurrentArtworkPaths() {
        List<Artwork> artworks = artworkRepository.getAll();
        Set<String> paths = new LinkedHashSet<>();
        for (Artwork artwork : artworks) {
            if (artwork != null && artwork.getRelativePath() != null && !artwork.getRelativePath().isBlank()) {
                paths.add(artwork.getRelativePath());
            }
        }
        return paths;
    }

    private Set<String> collectSnapshotArtworkPaths(BackupSnapshot snapshot) {
        Set<String> paths = new LinkedHashSet<>();
        for (ArtworkEntity artwork : snapshot.artworks) {
            if (artwork != null && artwork.relativePath != null && !artwork.relativePath.isBlank()) {
                paths.add(artwork.relativePath);
            }
        }
        return paths;
    }

    private void deleteArtworkFiles(Set<String> relativePaths) {
        List<String> failedPaths = new ArrayList<>();
        for (String relativePath : relativePaths) {
            try {
                artworkRepository.deleteFileByRelativePath(relativePath);
            } catch (Exception exception) {
                failedPaths.add(relativePath);
                Log.w(TAG, "Could not delete artwork file: " + relativePath, exception);
            }
        }

        if (!failedPaths.isEmpty()) {
            Log.w(TAG, "Some artwork files were not deleted: " + failedPaths.size());
        }
    }
}
