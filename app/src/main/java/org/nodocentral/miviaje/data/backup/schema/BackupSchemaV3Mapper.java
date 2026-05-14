package org.nodocentral.miviaje.data.backup.schema;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.nodocentral.miviaje.data.backup.models.BackupSnapshot;
import org.nodocentral.miviaje.data.room.ArtworkEntity;

import java.util.ArrayList;
import java.util.List;

public class BackupSchemaV3Mapper extends BackupSchemaV2Mapper {
    @Override
    public BackupSnapshot map(JsonObject root) {
        BackupSnapshot base = super.map(root);
        List<ArtworkEntity> artworks = mapArtworks(root.has("artworks") ? root.getAsJsonArray("artworks") : new JsonArray());
        return new BackupSnapshot(3, base.cards, base.products, base.events, artworks);
    }

    private List<ArtworkEntity> mapArtworks(JsonArray artworks) {
        List<ArtworkEntity> entities = new ArrayList<>();
        for (int i = 0; i < artworks.size(); i++) {
            JsonElement el = artworks.get(i);
            if (!el.isJsonObject()) {
                throw new BackupFieldException("artworks[" + i + "]", "artwork entry must be an object");
            }
            JsonObject obj = el.getAsJsonObject();
            String path = "artworks[" + i + "]";

            ArtworkEntity artworkEntity = new ArtworkEntity();
            artworkEntity.id = getRequiredString(obj, "id", path);
            artworkEntity.displayName = getNullableString(obj, "displayName");
            artworkEntity.relativePath = getNullableString(obj, "relativePath");
            artworkEntity.mimeType = getNullableString(obj, "mimeType");
            artworkEntity.sha256 = getNullableString(obj, "sha256");
            artworkEntity.createdAt = intToDateTime(getNullableInt(obj, "createdAt"));
            entities.add(artworkEntity);
        }
        return entities;
    }
}
