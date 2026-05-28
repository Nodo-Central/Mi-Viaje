package org.nodocentral.miviaje.domain.mimovilidad.filters;

import org.nodocentral.miviaje.domain.mimovilidad.Operator;
import org.nodocentral.miviaje.domain.mimovilidad.Route;
import org.nodocentral.miviaje.domain.mimovilidad.card.Event;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

public final class EventFilterCriteria {
    private final EnumSet<Event.TransportType> transportTypes;
    private final Set<EventFilterToken> routeTokens;
    private final Set<EventFilterToken> stationValidatorTokens;
    private final Set<EventFilterToken> operatorTokens;
    private final EnumSet<Event.Type> eventTypes;
    private final DateRange dateRange;

    private EventFilterCriteria(Builder builder) {
        this.transportTypes = copyOf(Event.TransportType.class, builder.transportTypes);
        this.routeTokens = copyTokens(EventFilterToken.Category.ROUTE, builder.routeTokens);
        this.stationValidatorTokens = copyTokens(EventFilterToken.Category.STATION_VALIDATOR, builder.stationValidatorTokens);
        this.operatorTokens = copyTokens(EventFilterToken.Category.OPERATOR, builder.operatorTokens);
        this.eventTypes = copyOf(Event.Type.class, builder.eventTypes);
        this.dateRange = builder.dateRange;
    }

    public static EventFilterCriteria empty() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public Set<Event.TransportType> getTransportTypes() {
        return Collections.unmodifiableSet(transportTypes);
    }

    public Set<EventFilterToken> getRouteTokens() {
        return Collections.unmodifiableSet(routeTokens);
    }

    public Set<EventFilterToken> getStationValidatorTokens() {
        return Collections.unmodifiableSet(stationValidatorTokens);
    }

    public Set<EventFilterToken> getOperatorTokens() {
        return Collections.unmodifiableSet(operatorTokens);
    }

    public Set<Event.Type> getEventTypes() {
        return Collections.unmodifiableSet(eventTypes);
    }

    public DateRange getDateRange() {
        return dateRange;
    }

    public boolean isEmpty() {
        return transportTypes.isEmpty()
                && routeTokens.isEmpty()
                && stationValidatorTokens.isEmpty()
                && operatorTokens.isEmpty()
                && eventTypes.isEmpty()
                && dateRange == null;
    }

    private static <T extends Enum<T>> EnumSet<T> copyOf(Class<T> enumClass, Set<T> source) {
        EnumSet<T> values = EnumSet.noneOf(enumClass);
        if (source != null) {
            values.addAll(source);
        }
        return values;
    }

    private static Set<EventFilterToken> copyTokens(EventFilterToken.Category category, Set<EventFilterToken> source) {
        LinkedHashSet<EventFilterToken> values = new LinkedHashSet<>();
        if (source != null) {
            for (EventFilterToken token : source) {
                if (token != null && token.getCategory() == category) {
                    values.add(token);
                }
            }
        }
        return values;
    }

    public static final class Builder {
        private Set<Event.TransportType> transportTypes;
        private Set<EventFilterToken> routeTokens;
        private Set<EventFilterToken> stationValidatorTokens;
        private Set<EventFilterToken> operatorTokens;
        private Set<Event.Type> eventTypes;
        private DateRange dateRange;

        private Builder() {
        }

        public Builder transportTypes(Set<Event.TransportType> transportTypes) {
            this.transportTypes = transportTypes;
            return this;
        }

        public Builder routes(Set<Route> routes) {
            LinkedHashSet<EventFilterToken> tokens = new LinkedHashSet<>();
            if (routes != null) {
                for (Route route : routes) {
                    if (route != null) {
                        tokens.add(EventFilterToken.route(route, route.name()));
                    }
                }
            }
            this.routeTokens = tokens;
            return this;
        }

        public Builder routeTokens(Set<EventFilterToken> routeTokens) {
            this.routeTokens = routeTokens;
            return this;
        }

        public Builder stationValidatorTokens(Set<EventFilterToken> stationValidatorTokens) {
            this.stationValidatorTokens = stationValidatorTokens;
            return this;
        }

        public Builder operators(Set<Operator> operators) {
            LinkedHashSet<EventFilterToken> tokens = new LinkedHashSet<>();
            if (operators != null) {
                for (Operator operator : operators) {
                    if (operator != null) {
                        tokens.add(EventFilterToken.operator(operator, operator.name()));
                    }
                }
            }
            this.operatorTokens = tokens;
            return this;
        }

        public Builder operatorTokens(Set<EventFilterToken> operatorTokens) {
            this.operatorTokens = operatorTokens;
            return this;
        }

        public Builder eventTypes(Set<Event.Type> eventTypes) {
            this.eventTypes = eventTypes;
            return this;
        }

        public Builder dateRange(DateRange dateRange) {
            this.dateRange = dateRange;
            return this;
        }

        public EventFilterCriteria build() {
            return new EventFilterCriteria(this);
        }
    }

    public static final class DateRange {
        private final LocalDateTime startInclusive;
        private final LocalDateTime endExclusive;

        private DateRange(LocalDateTime startInclusive, LocalDateTime endExclusive) {
            if (startInclusive == null || endExclusive == null) {
                throw new IllegalArgumentException("Date range bounds cannot be null");
            }
            if (!startInclusive.isBefore(endExclusive)) {
                throw new IllegalArgumentException("Date range start must be before end");
            }
            this.startInclusive = startInclusive;
            this.endExclusive = endExclusive;
        }

        public static DateRange of(LocalDateTime startInclusive, LocalDateTime endExclusive) {
            return new DateRange(startInclusive, endExclusive);
        }

        public static DateRange forDay(LocalDate date) {
            LocalDateTime start = date.atStartOfDay();
            return new DateRange(start, start.plusDays(1));
        }

        public static DateRange forWeek(LocalDate date, DayOfWeek firstDayOfWeek) {
            LocalDate startDate = date.with(TemporalAdjusters.previousOrSame(firstDayOfWeek));
            LocalDateTime start = startDate.atStartOfDay();
            return new DateRange(start, start.plusWeeks(1));
        }

        public static DateRange forMonth(LocalDate date) {
            LocalDateTime start = date.withDayOfMonth(1).atStartOfDay();
            return new DateRange(start, start.plusMonths(1));
        }

        public static DateRange forDateSpan(LocalDate startDateInclusive, LocalDate endDateInclusive) {
            if (startDateInclusive == null || endDateInclusive == null) {
                throw new IllegalArgumentException("Date range bounds cannot be null");
            }
            return new DateRange(
                    startDateInclusive.atStartOfDay(),
                    endDateInclusive.plusDays(1).atStartOfDay()
            );
        }

        public LocalDateTime getStartInclusive() {
            return startInclusive;
        }

        public LocalDateTime getEndExclusive() {
            return endExclusive;
        }

        public boolean contains(LocalDateTime dateTime) {
            return dateTime != null
                    && !dateTime.isBefore(startInclusive)
                    && dateTime.isBefore(endExclusive);
        }
    }
}
