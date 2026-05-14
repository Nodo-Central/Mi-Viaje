package org.nodocentral.miviaje.data.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;

@Entity(tableName = "artworks")
public class ArtworkEntity {
    @PrimaryKey
    @NonNull
    public String id;
    public String displayName;
    public String relativePath;
    public String mimeType;
    public String sha256;
    public LocalDateTime createdAt;
}
