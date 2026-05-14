package org.nodocentral.miviaje.data.mappers;

import org.nodocentral.miviaje.data.room.ArtworkEntity;
import org.nodocentral.miviaje.domain.artwork.Artwork;

public final class ArtworkMapper {
    private ArtworkMapper() {
    }

    public static Artwork toDomain(ArtworkEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Artwork(
                entity.id,
                entity.displayName,
                entity.relativePath,
                entity.mimeType,
                entity.sha256,
                entity.createdAt
        );
    }

    public static ArtworkEntity toEntity(Artwork artwork) {
        if (artwork == null) {
            return null;
        }

        ArtworkEntity entity = new ArtworkEntity();
        entity.id = artwork.getId();
        entity.displayName = artwork.getDisplayName();
        entity.relativePath = artwork.getRelativePath();
        entity.mimeType = artwork.getMimeType();
        entity.sha256 = artwork.getSha256();
        entity.createdAt = artwork.getCreatedAt();
        return entity;
    }
}
