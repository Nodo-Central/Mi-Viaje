package org.nodocentral.miviaje.data.backup.models;

import org.nodocentral.miviaje.data.room.ArtworkEntity;
import org.nodocentral.miviaje.data.room.CardEntity;
import org.nodocentral.miviaje.data.room.EventEntity;
import org.nodocentral.miviaje.data.room.ProductEntity;

import java.util.List;

public class BackupSnapshot {
    public final int schemaVersion;
    public final List<CardEntity> cards;
    public final List<ProductEntity> products;
    public final List<EventEntity> events;
    public final List<ArtworkEntity> artworks;

    public BackupSnapshot(int schemaVersion,
                          List<CardEntity> cards,
                          List<ProductEntity> products,
                          List<EventEntity> events,
                          List<ArtworkEntity> artworks) {
        this.schemaVersion = schemaVersion;
        this.cards = cards;
        this.products = products;
        this.events = events;
        this.artworks = artworks;
    }
}
