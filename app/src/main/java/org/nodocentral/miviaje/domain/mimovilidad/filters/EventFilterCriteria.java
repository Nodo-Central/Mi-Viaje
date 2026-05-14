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
import java.util.Set;

public final class EventFilterCriteria {
    private final EnumSet<Event.TransportType> transportTypes;
    private final EnumSet<Route> routes;
    private final EnumSet<Operator> operators;
    private final EnumSet<Event.Type> eventTypes;
    private final DateRange dateRange;

    private EventFilterCriteria(Builder builder) {
        this.transportTypes = copyOf(Event.TransportType.class, builder.transportTypes);
        this.routes = copyOf(Route.class, builder.routes);
        this.operators = copyOf(Operator.class, builder.operators);
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

    public Set<Route> getRoutes() {
        return Collections.unmodifiableSet(routes);
    }

    public Set<Operator> getOperators() {
        return Collections.unmodifiableSet(operators);
    }

    public Set<Event.Type> getEventTypes() {
        return Collections.unmodifiableSet(eventTypes);
    }

    public DateRange getDateRange() {
        return dateRange;
    }

    public boolean isEmpty() {
        return transportTypes.isEmpty()
                && routes.isEmpty()
                && operators.isEmpty()
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

    public static final class Builder {
        private Set<Event.TransportType> transportTypes;
        private Set<Route> routes;
        private Set<Operator> operators;
        private Set<Event.Type> eventTypes;
        private DateRange dateRange;

        private Builder() {
        }

        public Builder transportTypes(Set<Event.TransportType> transportTypes) {
            this.transportTypes = transportTypes;
            return this;
        }

        public Builder routes(Set<Route> routes) {
            this.routes = routes;
            return this;
        }

        public Builder operators(Set<Operator> operators) {
            this.operators = operators;
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
