package org.nodocentral.miviaje.domain.mimovilidad.card;

import org.nodocentral.miviaje.domain.mimovilidad.card.Product.State;

import androidx.annotation.NonNull;

import java.time.LocalDateTime;

public class ProductService {
     /**
     * state: 0 = Initialized, 1 = Activated, 2 = Suspended
     * (Integer (0..3), 2 bits)
     */
    private final State state;

     /**
     * weekOfYear: ISO-8601 week number of the year when the last validation occurred
     * (INTM, 6 bits)
     */
    private final int weekOfYear;

    /**
     * tripsPerDayOfWeek: count of trips made each day of the week with this product
     * (TripsPerDayOfWeek, 16 bits
     */
    private final int tripsPerDayOfWeek;

    /**
     * totalUsages: total number of usages of the product (Quantity, 12 bits)
     */
    private final int totalUsages;

    /**
     * lastDebitDateTime: date and time of the last validation
     * (DateTimeCompact, 32 bits)
     */
    private final LocalDateTime lastDebitDateTime;

    /**
     * lastDebitEntityId: ID of the entity where the last validation occurred
     * (CompanyId, 16 bits)
     */
    private final int lastDebitEntityId;

    /**
     * lastDebitRouteStationId: route or station identifier of the last validation
     * (ReferenceIdentifier, 16 bits)
     */
    private final int lastDebitRouteStationId;

    /**
     * lastDebitDeviceId: identifier of the device that performed the last validation
     * (DeviceId, 16 bits)
     */
    private final int lastDebitDeviceId;

    public ProductService(State state,
                          int weekOfYear,
                          int tripsPerDayOfWeek,
                          int totalUsages,
                          LocalDateTime lastDebitDateTime,
                          int lastDebitEntityId,
                          int lastDebitRouteStationId,
                          int lastDebitDeviceId) {
        this.state = state;
        this.weekOfYear = weekOfYear;
        this.tripsPerDayOfWeek = tripsPerDayOfWeek;
        this.totalUsages = totalUsages;
        this.lastDebitDateTime = lastDebitDateTime;
        this.lastDebitEntityId = lastDebitEntityId;
        this.lastDebitRouteStationId = lastDebitRouteStationId;
        this.lastDebitDeviceId = lastDebitDeviceId;
    }

    @NonNull
    @Override
    public String toString() {
        return "ProductService[" +
                "state=" + state +
                ", weekOfYear=" + weekOfYear +
                ", tripsPerDayOfWeek=" + tripsPerDayOfWeek +
                ", totalUsages=" + totalUsages +
                ", lastDebitDateTime=" + lastDebitDateTime +
                ", lastDebitEntityId=" + lastDebitEntityId +
                ", lastDebitRouteStationId=" + lastDebitRouteStationId +
                ", lastDebitDeviceId=" + lastDebitDeviceId +
                "]";
    }

    public State getState() {
        return state;
    }

    public int getWeekOfYear() {
        return weekOfYear;
    }

    public int getTripsPerDayOfWeek() {
        return tripsPerDayOfWeek;
    }

    public int getTotalUsages() {
        return totalUsages;
    }

    public LocalDateTime getLastDebitDateTime() {
        return lastDebitDateTime;
    }

    public int getLastDebitEntityId() {
        return lastDebitEntityId;
    }

    public int getLastDebitRouteStationId() {
        return lastDebitRouteStationId;
    }

    public int getLastDebitDeviceId() {
        return lastDebitDeviceId;
    }
}
