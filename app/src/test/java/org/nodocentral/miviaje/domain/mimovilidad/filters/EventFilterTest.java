package org.nodocentral.miviaje.domain.mimovilidad.filters;

import org.junit.Test;
import org.nodocentral.miviaje.domain.mimovilidad.Operator;
import org.nodocentral.miviaje.domain.mimovilidad.Route;
import org.nodocentral.miviaje.domain.mimovilidad.card.Event;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EventFilterTest {
    private static final LocalDateTime NOON = LocalDateTime.of(2026, 1, 12, 12, 0);

    @Test
    public void emptyCriteriaReturnsAllEvents() {
        Event first = event(Event.Type.PRODUCT_USE, NOON, Event.TransportType.BUS, 0, 0);
        Event second = event(Event.Type.PRODUCT_TOP_UP, NOON, Event.TransportType.BRT, 0, 0);

        List<Event> filteredEvents = EventFilter.filter(
                Arrays.asList(first, second),
                EventFilterCriteria.empty()
        );

        assertEquals(Arrays.asList(first, second), filteredEvents);
    }

    @Test
    public void multipleValuesWithinCategoryMatchWithOr() {
        Event bus = event(Event.Type.PRODUCT_USE, NOON, Event.TransportType.BUS, 0, 0);
        Event brt = event(Event.Type.PRODUCT_USE, NOON, Event.TransportType.BRT, 0, 0);
        Event train = event(Event.Type.PRODUCT_USE, NOON, Event.TransportType.TRAIN, 0b1000, 0);
        EventFilterCriteria criteria = EventFilterCriteria.builder()
                .transportTypes(EnumSet.of(Event.TransportType.BUS, Event.TransportType.BRT))
                .build();

        List<Event> filteredEvents = EventFilter.filter(Arrays.asList(bus, brt, train), criteria);

        assertEquals(Arrays.asList(bus, brt), filteredEvents);
    }

    @Test
    public void categoriesCombineWithAnd() {
        Event busUse = event(Event.Type.PRODUCT_USE, NOON, Event.TransportType.BUS, 0, 0);
        Event busTopUp = event(Event.Type.PRODUCT_TOP_UP, NOON, Event.TransportType.BUS, 0, 0);
        Event brtUse = event(Event.Type.PRODUCT_USE, NOON, Event.TransportType.BRT, 0, 0);
        EventFilterCriteria criteria = EventFilterCriteria.builder()
                .transportTypes(EnumSet.of(Event.TransportType.BUS))
                .eventTypes(EnumSet.of(Event.Type.PRODUCT_USE))
                .build();

        List<Event> filteredEvents = EventFilter.filter(Arrays.asList(busUse, busTopUp, brtUse), criteria);

        assertEquals(Arrays.asList(busUse), filteredEvents);
    }

    @Test
    public void routeAndOperatorFiltersUseDomainGetters() {
        Event route3 = event(Event.Type.PRODUCT_USE, NOON, Event.TransportType.TRAIN, 0b1000, 0);
        Event route4 = event(Event.Type.PRODUCT_USE, NOON, Event.TransportType.TRAIN, 0b1111, 0);
        Event macroCalzada = event(
                Event.Type.PRODUCT_USE,
                NOON,
                Event.TransportType.BUS,
                0,
                Operator.MI_MACRO_CALZADA.getValue()
        );

        List<Event> routeFilteredEvents = EventFilter.filter(
                Arrays.asList(route3, route4, macroCalzada),
                EventFilterCriteria.builder().routes(EnumSet.of(Route.LINE_3)).build()
        );
        List<Event> operatorFilteredEvents = EventFilter.filter(
                Arrays.asList(route3, route4, macroCalzada),
                EventFilterCriteria.builder().operators(EnumSet.of(Operator.MI_MACRO_CALZADA)).build()
        );

        assertEquals(Arrays.asList(route3), routeFilteredEvents);
        assertEquals(Arrays.asList(macroCalzada), operatorFilteredEvents);
    }

    @Test
    public void dateRangeUsesInclusiveStartAndExclusiveEnd() {
        Event atStart = event(
                Event.Type.PRODUCT_USE,
                LocalDateTime.of(2026, 1, 12, 0, 0),
                Event.TransportType.BUS,
                0,
                0
        );
        Event inside = event(
                Event.Type.PRODUCT_USE,
                LocalDateTime.of(2026, 1, 12, 23, 59),
                Event.TransportType.BUS,
                0,
                0
        );
        Event atEnd = event(
                Event.Type.PRODUCT_USE,
                LocalDateTime.of(2026, 1, 13, 0, 0),
                Event.TransportType.BUS,
                0,
                0
        );
        EventFilterCriteria criteria = EventFilterCriteria.builder()
                .dateRange(EventFilterCriteria.DateRange.forDay(LocalDate.of(2026, 1, 12)))
                .build();

        List<Event> filteredEvents = EventFilter.filter(Arrays.asList(atStart, inside, atEnd), criteria);

        assertEquals(Arrays.asList(atStart, inside), filteredEvents);
    }

    @Test
    public void dateSpanIncludesSelectedEndDate() {
        Event beforeStart = event(
                Event.Type.PRODUCT_USE,
                LocalDateTime.of(2026, 1, 11, 23, 59),
                Event.TransportType.BUS,
                0,
                0
        );
        Event atStart = event(
                Event.Type.PRODUCT_USE,
                LocalDateTime.of(2026, 1, 12, 0, 0),
                Event.TransportType.BUS,
                0,
                0
        );
        Event onEndDate = event(
                Event.Type.PRODUCT_USE,
                LocalDateTime.of(2026, 1, 19, 23, 59),
                Event.TransportType.BUS,
                0,
                0
        );
        Event afterEnd = event(
                Event.Type.PRODUCT_USE,
                LocalDateTime.of(2026, 1, 20, 0, 0),
                Event.TransportType.BUS,
                0,
                0
        );
        EventFilterCriteria criteria = EventFilterCriteria.builder()
                .dateRange(EventFilterCriteria.DateRange.forDateSpan(
                        LocalDate.of(2026, 1, 12),
                        LocalDate.of(2026, 1, 19)
                ))
                .build();

        List<Event> filteredEvents = EventFilter.filter(
                Arrays.asList(beforeStart, atStart, onEndDate, afterEnd),
                criteria
        );

        assertEquals(Arrays.asList(atStart, onEndDate), filteredEvents);
    }

    @Test
    public void weekAndMonthDateRangesUseExpectedBoundaries() {
        EventFilterCriteria.DateRange week = EventFilterCriteria.DateRange.forWeek(
                LocalDate.of(2026, 1, 14),
                DayOfWeek.MONDAY
        );
        EventFilterCriteria.DateRange month = EventFilterCriteria.DateRange.forMonth(
                LocalDate.of(2026, 1, 14)
        );

        assertTrue(week.contains(LocalDateTime.of(2026, 1, 12, 0, 0)));
        assertTrue(week.contains(LocalDateTime.of(2026, 1, 18, 23, 59)));
        assertTrue(!week.contains(LocalDateTime.of(2026, 1, 19, 0, 0)));
        assertTrue(month.contains(LocalDateTime.of(2026, 1, 1, 0, 0)));
        assertTrue(month.contains(LocalDateTime.of(2026, 1, 31, 23, 59)));
        assertTrue(!month.contains(LocalDateTime.of(2026, 2, 1, 0, 0)));
    }

    @Test
    public void nullEventsAndMissingFieldsDoNotMatchActiveCriteria() {
        Event missingFields = event(null, null, null, 0, 9999);
        EventFilterCriteria criteria = EventFilterCriteria.builder()
                .transportTypes(EnumSet.of(Event.TransportType.BUS))
                .eventTypes(EnumSet.of(Event.Type.PRODUCT_USE))
                .dateRange(EventFilterCriteria.DateRange.forDay(LocalDate.of(2026, 1, 12)))
                .build();

        assertTrue(EventFilter.filter(null, criteria).isEmpty());
        assertTrue(EventFilter.filter(Arrays.asList(null, missingFields), criteria).isEmpty());
    }

    private Event event(Event.Type type,
                        LocalDateTime dateTime,
                        Event.TransportType transportType,
                        int routeId,
                        int entityId) {
        return new Event(
                0,
                0,
                null,
                entityId,
                dateTime,
                type,
                0,
                0,
                0,
                0,
                0,
                0,
                transportType,
                routeId,
                0,
                0,
                0,
                Event.RefundReason.NO_REFUND,
                Event.DeviceType.UNSPECIFIED
        );
    }
}
