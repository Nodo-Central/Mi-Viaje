package org.nodocentral.miviaje.data.room;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import java.time.LocalDateTime;

@Entity(
        primaryKeys = {"cardId", "eventSequence", "samId", "samSequence"},
        tableName = "events",
        foreignKeys = @ForeignKey(
                entity = CardEntity.class,
                parentColumns = "uid",
                childColumns = "cardId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index("cardId"),
                @Index(value = {"cardId", "eventSequence"})
        }
)
public class EventEntity {
    public long cardId;
    public int eventSequence;

    public int productId;
    public int productPointer;
    public int entityId;
    public LocalDateTime eventDateTime;
    public int eventType;
    public int amount;

    public long samId;
    public long samSequence;

    public int deviceId;
    public int locationId;
    public int transportType;
    public int routeStationId;
    public int transferCount;
    public int transferLimit;
    public int passbackCount;
    public int refundReason;
    public int deviceType;
}
