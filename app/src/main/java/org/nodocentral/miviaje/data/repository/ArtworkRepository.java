package org.nodocentral.miviaje.data.repository;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.nodocentral.miviaje.data.artwork.CardArtworkResolver;
import org.nodocentral.miviaje.data.files.ArtworkImageManager;
import org.nodocentral.miviaje.data.mappers.ArtworkMapper;
import org.nodocentral.miviaje.data.room.ArtworkDao;
import org.nodocentral.miviaje.data.room.ArtworkEntity;
import org.nodocentral.miviaje.data.room.MiViajeDatabase;
import org.nodocentral.miviaje.domain.artwork.Artwork;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ArtworkRepository {
    private static final String TAG = "ArtworkRepository";

    private final MiViajeDatabase database;
    private final ArtworkDao artworkDao;
    private final ArtworkImageManager artworkImageManager;
    private final CardRepository cardRepository;

    public ArtworkRepository(Context context, MiViajeDatabase database) {
        this(database, new ArtworkImageManager(context), new CardRepository(database));
    }

    public ArtworkRepository(MiViajeDatabase database,
                             ArtworkImageManager artworkImageManager,
                             CardRepository cardRepository) {
        this.database = database;
        this.artworkDao = database.artworkDao();
        this.artworkImageManager = artworkImageManager;
        this.cardRepository = cardRepository;
    }

    public List<Artwork> getAll() {
        List<ArtworkEntity> entities = artworkDao.getAll();
        List<Artwork> artworks = new ArrayList<>(entities.size());
        for (ArtworkEntity entity : entities) {
            Artwork artwork = ArtworkMapper.toDomain(entity);
            if (artwork != null) {
                artworks.add(artwork);
            }
        }
        return artworks;
    }

    public Map<String, Artwork> getMapById() {
        Map<String, Artwork> artworkMap = new HashMap<>();
        for (Artwork artwork : getAll()) {
            if (artwork != null && artwork.getId() != null) {
                artworkMap.put(artwork.getId(), artwork);
            }
        }
        return artworkMap;
    }

    public Artwork getById(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return ArtworkMapper.toDomain(artworkDao.getById(id));
    }

    public Artwork getByRef(String artworkRef) {
        return getById(CardArtworkResolver.getArtworkId(artworkRef));
    }

    public int countCardUsage(String artworkId) {
        if (artworkId == null || artworkId.isBlank()) {
            return 0;
        }
        return cardRepository.countByArtworkRef(CardArtworkResolver.toArtworkRef(artworkId));
    }

    public void assignToCard(long cardUid, String artworkRef) {
        cardRepository.updateArtworkRef(cardUid, artworkRef);
    }

    public void updateDisplayName(String artworkId, String displayName) {
        if (artworkId == null || artworkId.isBlank()) {
            return;
        }
        artworkDao.updateDisplayName(artworkId, normalizeOptionalText(displayName));
    }

    public void importAndAssign(Uri sourceUri, long cardUid) throws Exception {
        importAndAssign(sourceUri, cardUid, null);
    }

    public void importAndAssign(Uri sourceUri, long cardUid, String displayNameOverride) throws Exception {
        if (sourceUri == null) {
            throw new IllegalArgumentException("sourceUri is null");
        }

        String artworkId = UUID.randomUUID().toString();
        String displayName = displayNameOverride == null
                ? artworkImageManager.queryDisplayName(sourceUri)
                : displayNameOverride;
        ArtworkEntity entity;
        try {
            ArtworkImageManager.ImportedImageData importedImageData =
                    artworkImageManager.importFromUri(sourceUri, artworkId);
            entity = createArtworkEntity(
                    artworkId,
                    displayName,
                    importedImageData.fileName,
                    importedImageData.mimeType,
                    importedImageData.sha256
            );
        } catch (ArtworkImageManager.InvalidImageException e) {
            throw new ArtworkImportException(ArtworkImportException.Reason.INVALID_IMAGE, e);
        }

        try {
            database.runInTransaction(() -> {
                artworkDao.upsert(entity);
                assignToCard(cardUid, CardArtworkResolver.toArtworkRef(entity.id));
            });
        } catch (RuntimeException e) {
            artworkImageManager.deleteByRelativePath(entity.relativePath);
            throw e;
        }
    }

    public String queryImportDisplayName(Uri sourceUri) {
        return sourceUri == null ? null : artworkImageManager.queryDisplayName(sourceUri);
    }

    public void delete(Artwork artwork) {
        if (artwork == null || artwork.getId() == null) {
            return;
        }

        artworkDao.deleteById(artwork.getId());
        if (artwork.getRelativePath() != null) {
            artworkImageManager.deleteByRelativePath(artwork.getRelativePath());
        }
    }

    public void deleteAndClearReferences(Artwork artwork) {
        if (artwork == null || artwork.getId() == null) {
            return;
        }

        String artworkRef = CardArtworkResolver.toArtworkRef(artwork.getId());
        database.runInTransaction(() -> {
            cardRepository.clearArtworkRef(artworkRef);
            artworkDao.deleteById(artwork.getId());
        });

        if (artwork.getRelativePath() != null) {
            artworkImageManager.deleteByRelativePath(artwork.getRelativePath());
        }
    }

    public void deleteFileByRelativePath(String relativePath) {
        artworkImageManager.deleteByRelativePath(relativePath);
    }

    public ArtworkSanitizationResult sanitizeImportedArtworks() {
        int optimizedCount = 0;
        int removedCount = 0;
        boolean hadErrors = false;

        List<Artwork> artworks = getAll();
        File artworksDir = artworkImageManager.requireArtworksDirectory();

        for (Artwork artwork : artworks) {
            if (artwork == null || artwork.getId() == null || artwork.getRelativePath() == null) {
                deleteAndClearReferences(artwork);
                removedCount++;
                continue;
            }

            File artworkFile = new File(artworksDir, artwork.getRelativePath());
            if (!artworkFile.exists() || !artworkFile.isFile()) {
                deleteAndClearReferences(artwork);
                removedCount++;
                continue;
            }

            ArtworkImageManager.ImageDimensions dimensions = artworkImageManager.readDimensions(artworkFile);
            if (dimensions == null) {
                deleteAndClearReferences(artwork);
                removedCount++;
                continue;
            }

            if (!dimensions.exceedsMaxSide(ArtworkImageManager.MAX_ARTWORK_SIDE_PX)) {
                continue;
            }

            try {
                ArtworkImageManager.ImportedImageData normalized =
                        artworkImageManager.normalizeExistingFile(artwork.getId(), artworkFile);
                Artwork normalizedArtwork = new Artwork(
                        artwork.getId(),
                        artwork.getDisplayName(),
                        normalized.fileName,
                        normalized.mimeType,
                        normalized.sha256,
                        artwork.getCreatedAt() == null ? LocalDateTime.now() : artwork.getCreatedAt()
                );
                artworkDao.upsert(ArtworkMapper.toEntity(normalizedArtwork));
                optimizedCount++;
            } catch (Exception e) {
                hadErrors = true;
                Log.w(TAG, "Failed to sanitize imported artwork: " + artwork.getId(), e);
                deleteAndClearReferences(artwork);
                removedCount++;
            }
        }

        return new ArtworkSanitizationResult(optimizedCount, removedCount, hadErrors);
    }

    private static ArtworkEntity createArtworkEntity(String artworkId,
                                                     String displayName,
                                                     String fileName,
                                                     String mimeType,
                                                     String sha256) {
        ArtworkEntity entity = new ArtworkEntity();
        entity.id = artworkId;
        entity.displayName = normalizeOptionalText(displayName);
        entity.relativePath = fileName;
        entity.mimeType = mimeType;
        entity.sha256 = sha256;
        entity.createdAt = LocalDateTime.now();
        return entity;
    }

    private static String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }

    public static final class ArtworkSanitizationResult {
        public final int optimizedCount;
        public final int removedCount;
        public final boolean hadErrors;

        public ArtworkSanitizationResult(int optimizedCount, int removedCount, boolean hadErrors) {
            this.optimizedCount = optimizedCount;
            this.removedCount = removedCount;
            this.hadErrors = hadErrors;
        }

        public boolean hasVisibleChanges() {
            return optimizedCount > 0 || removedCount > 0;
        }
    }

    public static final class ArtworkImportException extends Exception {
        public enum Reason {
            INVALID_IMAGE
        }

        public final Reason reason;

        public ArtworkImportException(Reason reason, Throwable cause) {
            super(cause);
            this.reason = reason;
        }
    }
}
