package org.nodocentral.miviaje.domain.artwork;

import androidx.annotation.NonNull;

import java.time.LocalDateTime;

public class Artwork {
    private final String id;
    private final String displayName;
    private final String relativePath;
    private final String mimeType;
    private final String sha256;
    private final LocalDateTime createdAt;

    public Artwork(String id,
                   String displayName,
                   String relativePath,
                   String mimeType,
                   String sha256,
                   LocalDateTime createdAt) {
        this.id = id;
        this.displayName = displayName;
        this.relativePath = relativePath;
        this.mimeType = mimeType;
        this.sha256 = sha256;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getSha256() {
        return sha256;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    @NonNull
    public String toString() {
        return "Artwork[" +
                "id=" + id +
                ", displayName=" + displayName +
                ", relativePath=" + relativePath +
                ", mimeType=" + mimeType +
                ", sha256=" + sha256 +
                ", createdAt=" + createdAt +
                "]";
    }
}
