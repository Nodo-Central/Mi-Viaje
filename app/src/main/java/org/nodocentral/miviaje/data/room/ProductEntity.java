package org.nodocentral.miviaje.data.room;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity(
        tableName = "products",
        primaryKeys = {"cardId", "productId"},
        foreignKeys = @ForeignKey(
                entity = CardEntity.class,
                parentColumns = "uid",
                childColumns = "cardId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index("cardId")
)
public class ProductEntity {
    // Product info
    public long cardId;
    public short productId;
    public int value;
    public int valuePointer;
    public int priority;

    // Contract info
    public int pointer;
    public int serial;
    public short priceCents;
    public int valueUnit;
    public int minAmountLimit;
    public int maxAmountLimit;
    public short reactivationCount;
    public short lastAppliedActionNumber;
    public int distributorNetworkId;
    public short distributorCompanyId;
    public LocalDateTime distributionDateTime;
    public int distributionSamId;
    public short distributingDeviceId;
    public LocalDateTime validFrom;
    public LocalDateTime validTo;
    public LocalTime validDailyStartTime;
    public LocalTime validDailyEndTime;
    public byte restrictionRestrictedDays;
    public int restrictionMaxTripsPerDayOfWeek;
    public short restrictionPassbackTimeMinutes;
    public byte restrictionAllowedPassbacks;
    public short restrictionTransferTimeLimitMinutes;
    public byte restrictionAllowedInterchanges;

    // Service info
    public int state;
    public int weekOfYear;
    public int tripsPerDayOfWeek;
    public int totalUsages;
    public LocalDateTime lastDebitDateTime;
    public int lastDebitEntityId;
    public int lastDebitRouteStationId;
    public int lastDebitDeviceId;
}

