package org.nodocentral.miviaje.domain.mimovilidad.analytics;

import org.nodocentral.miviaje.domain.mimovilidad.Operator;
import org.nodocentral.miviaje.domain.mimovilidad.Route;
import org.nodocentral.miviaje.domain.mimovilidad.Station;
import org.nodocentral.miviaje.domain.mimovilidad.card.Event;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class MobilityAnalyticsSummary {
    private final DateRange dateRange;
    private final int totalSavedEvents;
    private final int eventsInRange;
    private final int eventsOutsideRange;
    private final int missingDateEvents;
    private final int tripCount;
    private final int transferCount;
    private final PaymentUnitFlow cashFlow;
    private final PaymentUnitFlow ticketFlow;
    private final int activeDays;
    private final double averageTripsPerActiveDay;
    private final int longestActivityStreakDays;
    private final EnumMap<DayOfWeek, Integer> weekdayTripCounts;
    private final List<HourBlockCount> hourBlockCounts;
    private final List<Count<Event.TransportType>> transportCounts;
    private final List<Count<Route>> routeCounts;
    private final List<Count<Station>> stationCounts;
    private final List<Count<Operator>> operatorCounts;
    private final int unknownTransportCount;
    private final int unknownRouteCount;
    private final int unknownStationCount;
    private final int unknownValueMovementCount;
    private final int invalidOrUnspecifiedEventTypeCount;
    private final int distinctOperatorCount;
    private final int distinctValidatorCount;
    private final int distinctDeviceCount;
    private final int distinctSamCount;

    MobilityAnalyticsSummary(DateRange dateRange,
                             int totalSavedEvents,
                             int eventsInRange,
                             int eventsOutsideRange,
                             int missingDateEvents,
                             int tripCount,
                             int transferCount,
                             PaymentUnitFlow cashFlow,
                             PaymentUnitFlow ticketFlow,
                             int activeDays,
                             double averageTripsPerActiveDay,
                             int longestActivityStreakDays,
                             EnumMap<DayOfWeek, Integer> weekdayTripCounts,
                             List<HourBlockCount> hourBlockCounts,
                             List<Count<Event.TransportType>> transportCounts,
                             List<Count<Route>> routeCounts,
                             List<Count<Station>> stationCounts,
                             List<Count<Operator>> operatorCounts,
                             int unknownTransportCount,
                             int unknownRouteCount,
                             int unknownStationCount,
                             int unknownValueMovementCount,
                             int invalidOrUnspecifiedEventTypeCount,
                             int distinctOperatorCount,
                             int distinctValidatorCount,
                             int distinctDeviceCount,
                             int distinctSamCount) {
        this.dateRange = dateRange;
        this.totalSavedEvents = totalSavedEvents;
        this.eventsInRange = eventsInRange;
        this.eventsOutsideRange = eventsOutsideRange;
        this.missingDateEvents = missingDateEvents;
        this.tripCount = tripCount;
        this.transferCount = transferCount;
        this.cashFlow = cashFlow != null ? cashFlow : PaymentUnitFlow.empty();
        this.ticketFlow = ticketFlow != null ? ticketFlow : PaymentUnitFlow.empty();
        this.activeDays = activeDays;
        this.averageTripsPerActiveDay = averageTripsPerActiveDay;
        this.longestActivityStreakDays = longestActivityStreakDays;
        this.weekdayTripCounts = new EnumMap<>(weekdayTripCounts);
        this.hourBlockCounts = Collections.unmodifiableList(hourBlockCounts);
        this.transportCounts = Collections.unmodifiableList(transportCounts);
        this.routeCounts = Collections.unmodifiableList(routeCounts);
        this.stationCounts = Collections.unmodifiableList(stationCounts);
        this.operatorCounts = Collections.unmodifiableList(operatorCounts);
        this.unknownTransportCount = unknownTransportCount;
        this.unknownRouteCount = unknownRouteCount;
        this.unknownStationCount = unknownStationCount;
        this.unknownValueMovementCount = unknownValueMovementCount;
        this.invalidOrUnspecifiedEventTypeCount = invalidOrUnspecifiedEventTypeCount;
        this.distinctOperatorCount = distinctOperatorCount;
        this.distinctValidatorCount = distinctValidatorCount;
        this.distinctDeviceCount = distinctDeviceCount;
        this.distinctSamCount = distinctSamCount;
    }

    public DateRange getDateRange() {
        return dateRange;
    }

    public int getTotalSavedEvents() {
        return totalSavedEvents;
    }

    public int getEventsInRange() {
        return eventsInRange;
    }

    public int getEventsOutsideRange() {
        return eventsOutsideRange;
    }

    public int getMissingDateEvents() {
        return missingDateEvents;
    }

    public int getTripCount() {
        return tripCount;
    }

    public int getTransferCount() {
        return transferCount;
    }

    public int getSpentCents() {
        return cashFlow.getConsumed();
    }

    public PaymentUnitFlow getCashFlow() {
        return cashFlow;
    }

    public PaymentUnitFlow getTicketFlow() {
        return ticketFlow;
    }

    public int getActiveDays() {
        return activeDays;
    }

    public double getAverageTripsPerActiveDay() {
        return averageTripsPerActiveDay;
    }

    public int getLongestActivityStreakDays() {
        return longestActivityStreakDays;
    }

    public Map<DayOfWeek, Integer> getWeekdayTripCounts() {
        return Collections.unmodifiableMap(weekdayTripCounts);
    }

    public List<HourBlockCount> getHourBlockCounts() {
        return hourBlockCounts;
    }

    public List<Count<Event.TransportType>> getTransportCounts() {
        return transportCounts;
    }

    public Event.TransportType getTopTransportType() {
        return transportCounts.isEmpty() ? null : transportCounts.get(0).getItem();
    }

    public List<Count<Route>> getRouteCounts() {
        return routeCounts;
    }

    public Route getTopRoute() {
        return routeCounts.isEmpty() ? null : routeCounts.get(0).getItem();
    }

    public List<Count<Station>> getStationCounts() {
        return stationCounts;
    }

    public Station getTopStation() {
        return stationCounts.isEmpty() ? null : stationCounts.get(0).getItem();
    }

    public List<Count<Operator>> getOperatorCounts() {
        return operatorCounts;
    }

    public Operator getTopOperator() {
        return operatorCounts.isEmpty() ? null : operatorCounts.get(0).getItem();
    }

    public int getReloadCount() {
        return cashFlow.getAddedCount();
    }

    public int getReloadTotalCents() {
        return cashFlow.getAdded();
    }

    public int getAverageReloadCents() {
        return cashFlow.getAverageTopUp();
    }

    public int getHighestReloadCents() {
        return cashFlow.getHighestTopUp();
    }

    public int getLastReloadCents() {
        return cashFlow.getLastTopUpAmount();
    }

    public LocalDateTime getLastReloadDateTime() {
        return cashFlow.getLastTopUpDateTime();
    }

    public int getUnknownTransportCount() {
        return unknownTransportCount;
    }

    public int getUnknownRouteCount() {
        return unknownRouteCount;
    }

    public int getUnknownStationCount() {
        return unknownStationCount;
    }

    public int getUnknownValueMovementCount() {
        return unknownValueMovementCount;
    }

    public int getInvalidOrUnspecifiedEventTypeCount() {
        return invalidOrUnspecifiedEventTypeCount;
    }

    public int getDistinctOperatorCount() {
        return distinctOperatorCount;
    }

    public int getDistinctValidatorCount() {
        return distinctValidatorCount;
    }

    public int getDistinctDeviceCount() {
        return distinctDeviceCount;
    }

    public int getDistinctSamCount() {
        return distinctSamCount;
    }

    public Count<DayOfWeek> getMostActiveWeekday() {
        Count<DayOfWeek> result = null;
        for (Map.Entry<DayOfWeek, Integer> entry : weekdayTripCounts.entrySet()) {
            if (result == null
                    || entry.getValue() > result.getCount()
                    || (entry.getValue() == result.getCount()
                    && entry.getKey().getValue() < result.getItem().getValue())) {
                result = new Count<>(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    public HourBlockCount getPeakHourBlock() {
        HourBlockCount result = null;
        for (HourBlockCount block : hourBlockCounts) {
            if (result == null
                    || block.getCount() > result.getCount()
                    || (block.getCount() == result.getCount()
                    && block.getStartHour() < result.getStartHour())) {
                result = block;
            }
        }
        return result;
    }

    public static final class DateRange {
        private final LocalDate startDateInclusive;
        private final LocalDate endDateInclusive;
        private final boolean allTime;

        private DateRange(LocalDate startDateInclusive, LocalDate endDateInclusive, boolean allTime) {
            if (!allTime) {
                if (startDateInclusive == null || endDateInclusive == null) {
                    throw new IllegalArgumentException("Date range bounds cannot be null");
                }
                if (endDateInclusive.isBefore(startDateInclusive)) {
                    throw new IllegalArgumentException("Date range end cannot be before start");
                }
            }
            this.startDateInclusive = startDateInclusive;
            this.endDateInclusive = endDateInclusive;
            this.allTime = allTime;
        }

        public static DateRange lastDays(LocalDate today, int days) {
            if (days <= 0) {
                throw new IllegalArgumentException("days must be positive");
            }
            return custom(today.minusDays(days - 1L), today);
        }

        public static DateRange currentMonth(LocalDate today) {
            return custom(today.withDayOfMonth(1), today);
        }

        public static DateRange custom(LocalDate startDateInclusive, LocalDate endDateInclusive) {
            return new DateRange(startDateInclusive, endDateInclusive, false);
        }

        public static DateRange allTime() {
            return new DateRange(null, null, true);
        }

        public boolean contains(LocalDateTime dateTime) {
            if (dateTime == null) {
                return false;
            }
            if (allTime) {
                return true;
            }
            LocalDate date = dateTime.toLocalDate();
            return !date.isBefore(startDateInclusive) && !date.isAfter(endDateInclusive);
        }

        public LocalDate getStartDateInclusive() {
            return startDateInclusive;
        }

        public LocalDate getEndDateInclusive() {
            return endDateInclusive;
        }

        public boolean isAllTime() {
            return allTime;
        }
    }

    public static final class Count<T> {
        private final T item;
        private final int count;

        public Count(T item, int count) {
            this.item = item;
            this.count = count;
        }

        public T getItem() {
            return item;
        }

        public int getCount() {
            return count;
        }
    }

    public static final class HourBlockCount {
        private final int startHour;
        private final int endHour;
        private final int count;

        public HourBlockCount(int startHour, int endHour, int count) {
            this.startHour = startHour;
            this.endHour = endHour;
            this.count = count;
        }

        public int getStartHour() {
            return startHour;
        }

        public int getEndHour() {
            return endHour;
        }

        public int getCount() {
            return count;
        }
    }

    public static final class PaymentUnitFlow {
        private final int consumed;
        private final int added;
        private final int refunded;
        private final int consumedCount;
        private final int addedCount;
        private final int refundCount;
        private final int highestTopUp;
        private final int lastTopUpAmount;
        private final LocalDateTime lastTopUpDateTime;

        public PaymentUnitFlow(int consumed,
                               int added,
                               int refunded,
                               int consumedCount,
                               int addedCount,
                               int refundCount,
                               int highestTopUp,
                               int lastTopUpAmount,
                               LocalDateTime lastTopUpDateTime) {
            this.consumed = consumed;
            this.added = added;
            this.refunded = refunded;
            this.consumedCount = consumedCount;
            this.addedCount = addedCount;
            this.refundCount = refundCount;
            this.highestTopUp = highestTopUp;
            this.lastTopUpAmount = lastTopUpAmount;
            this.lastTopUpDateTime = lastTopUpDateTime;
        }

        public static PaymentUnitFlow empty() {
            return new PaymentUnitFlow(0, 0, 0, 0, 0, 0, 0, 0, null);
        }

        public int getConsumed() {
            return consumed;
        }

        public int getAdded() {
            return added;
        }

        public int getRefunded() {
            return refunded;
        }

        public int getNetChange() {
            return added + refunded - consumed;
        }

        public int getConsumedCount() {
            return consumedCount;
        }

        public int getAddedCount() {
            return addedCount;
        }

        public int getRefundCount() {
            return refundCount;
        }

        public int getHighestTopUp() {
            return highestTopUp;
        }

        public int getAverageTopUp() {
            return addedCount == 0 ? 0 : Math.round((float) added / addedCount);
        }

        public int getLastTopUpAmount() {
            return lastTopUpAmount;
        }

        public LocalDateTime getLastTopUpDateTime() {
            return lastTopUpDateTime;
        }

        public boolean hasMovement() {
            return consumedCount > 0 || addedCount > 0 || refundCount > 0;
        }
    }
}
