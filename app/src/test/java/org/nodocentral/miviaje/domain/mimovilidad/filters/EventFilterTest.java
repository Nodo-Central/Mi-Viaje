package org.nodocentral.miviaje.domain.mimovilidad.filters;

import org.junit.Test;
import org.nodocentral.miviaje.domain.mimovilidad.Operator;
import org.nodocentral.miviaje.domain.mimovilidad.Route;
import org.nodocentral.miviaje.domain.mimovilidad.Station;
import org.nodocentral.miviaje.domain.mimovilidad.card.Event;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
    public void brtRouteFiltersUseDisplayedRouteWhenRawRouteIdIsAmbiguous() {
        Event line6 = event(
                Event.Type.PRODUCT_USE,
                NOON,
                Event.TransportType.BUS,
                1,
                Operator.MI_MACRO_CALZADA.getValue(),
                0L,
                1111,
                0
        );
        Event line7 = event(
                Event.Type.PRODUCT_USE,
                NOON,
                Event.TransportType.BUS,
                1,
                Operator.MI_MACRO_PERIFERICO_TRONCAL.getValue(),
                0x0000000100000000L,
                1,
                0
        );

        List<Event> line6Events = EventFilter.filter(
                Arrays.asList(line6, line7),
                EventFilterCriteria.builder().routes(EnumSet.of(Route.LINE_6)).build()
        );
        List<Event> line7Events = EventFilter.filter(
                Arrays.asList(line6, line7),
                EventFilterCriteria.builder().routes(EnumSet.of(Route.LINE_7)).build()
        );

        assertEquals(Arrays.asList(line6), line6Events);
        assertEquals(Arrays.asList(line7), line7Events);
    }

    @Test
    public void routeTextTokensMatchRawRouteIds() {
        Event route114 = event(Event.Type.PRODUCT_USE, NOON, Event.TransportType.BUS, 114, 0);
        Event route14 = event(Event.Type.PRODUCT_USE, NOON, Event.TransportType.BUS, 14, 0);
        EventFilterCriteria criteria = EventFilterCriteria.builder()
                .routeTokens(tokens(EventFilterToken.text(EventFilterToken.Category.ROUTE, "114")))
                .build();

        List<Event> filteredEvents = EventFilter.filter(Arrays.asList(route114, route14), criteria);

        assertEquals(Arrays.asList(route114), filteredEvents);
    }

    @Test
    public void knownStationTokensMatchStationIdentity() {
        Event urdaneta = event(
                Event.Type.PRODUCT_USE,
                NOON,
                Event.TransportType.TRAIN,
                1,
                0,
                0L,
                7000,
                0
        );
        Event washington = event(
                Event.Type.PRODUCT_USE,
                NOON,
                Event.TransportType.TRAIN,
                1,
                0,
                0L,
                10000,
                0
        );
        EventFilterCriteria criteria = EventFilterCriteria.builder()
                .stationValidatorTokens(tokens(EventFilterToken.station(Station.URDANETA, "Urdaneta")))
                .build();

        List<Event> filteredEvents = EventFilter.filter(Arrays.asList(urdaneta, washington), criteria);

        assertEquals(Arrays.asList(urdaneta), filteredEvents);
    }

    @Test
    public void stationValidatorTextTokensMatchStationValidatorDeviceAndLocationIds() {
        Event station7 = event(
                Event.Type.PRODUCT_USE,
                NOON,
                Event.TransportType.TRAIN,
                1,
                0,
                0L,
                7000,
                0
        );
        Event validator11420 = event(
                Event.Type.PRODUCT_USE,
                NOON,
                Event.TransportType.BUS,
                0,
                0,
                0L,
                11420,
                0
        );
        Event location42 = event(
                Event.Type.PRODUCT_TOP_UP,
                NOON,
                Event.TransportType.BUS,
                0,
                0,
                0L,
                0,
                42
        );
        Event other = event(Event.Type.PRODUCT_USE, NOON, Event.TransportType.BUS, 0, 0);

        assertEquals(Arrays.asList(station7), EventFilter.filter(
                Arrays.asList(station7, validator11420, location42, other),
                EventFilterCriteria.builder()
                        .stationValidatorTokens(tokens(EventFilterToken.text(EventFilterToken.Category.STATION_VALIDATOR, "Urdaneta")))
                        .build()
        ));
        assertEquals(Arrays.asList(validator11420), EventFilter.filter(
                Arrays.asList(station7, validator11420, location42, other),
                EventFilterCriteria.builder()
                        .stationValidatorTokens(tokens(EventFilterToken.text(EventFilterToken.Category.STATION_VALIDATOR, "U-11420")))
                        .build()
        ));
        assertEquals(Arrays.asList(location42), EventFilter.filter(
                Arrays.asList(station7, validator11420, location42, other),
                EventFilterCriteria.builder()
                        .stationValidatorTokens(tokens(EventFilterToken.text(EventFilterToken.Category.STATION_VALIDATOR, "42")))
                        .build()
        ));
    }

    @Test
    public void operatorTokensMatchKnownOperatorsNamesAndIds() {
        Event macroCalzada = event(Event.Type.PRODUCT_USE, NOON, Event.TransportType.BUS, 0, Operator.MI_MACRO_CALZADA.getValue());
        Event bea = event(Event.Type.PRODUCT_USE, NOON, Event.TransportType.BUS, 0, Operator.BEA.getValue());
        Event unknown83Name = event(Event.Type.PRODUCT_USE, NOON, Event.TransportType.BUS, 0, Operator.MI_MACRO_CALZADA.getValue());

        assertEquals(Arrays.asList(macroCalzada, unknown83Name), EventFilter.filter(
                Arrays.asList(macroCalzada, bea, unknown83Name),
                EventFilterCriteria.builder()
                        .operatorTokens(tokens(EventFilterToken.operator(Operator.MI_MACRO_CALZADA, "Mi Macro Calzada")))
                        .build()
        ));
        assertEquals(Arrays.asList(macroCalzada, unknown83Name), EventFilter.filter(
                Arrays.asList(macroCalzada, bea, unknown83Name),
                EventFilterCriteria.builder()
                        .operatorTokens(tokens(EventFilterToken.text(EventFilterToken.Category.OPERATOR, "Macro Calzada")))
                        .build()
        ));
        assertEquals(Arrays.asList(macroCalzada, unknown83Name), EventFilter.filter(
                Arrays.asList(macroCalzada, bea, unknown83Name),
                EventFilterCriteria.builder()
                        .operatorTokens(tokens(EventFilterToken.text(EventFilterToken.Category.OPERATOR, "83")))
                        .build()
        ));
    }

    @Test
    public void operatorAliasIdsMatchTheSameKnownOperator() {
        Event primary = event(Event.Type.PRODUCT_USE, NOON, Event.TransportType.BUS, 0, Operator.RUTA_LOPEZ_MATEOS.getValue());
        Event alias = event(Event.Type.PRODUCT_USE, NOON, Event.TransportType.BUS, 0, 2580);
        Event bea = event(Event.Type.PRODUCT_USE, NOON, Event.TransportType.BUS, 0, Operator.BEA.getValue());

        assertEquals(Operator.RUTA_LOPEZ_MATEOS, alias.getOperator());
        assertEquals(Arrays.asList(primary, alias), EventFilter.filter(
                Arrays.asList(primary, alias, bea),
                EventFilterCriteria.builder()
                        .operatorTokens(tokens(EventFilterToken.operator(Operator.RUTA_LOPEZ_MATEOS, "Ruta López Mateos")))
                        .build()
        ));
        assertEquals(Arrays.asList(primary, alias), EventFilter.filter(
                Arrays.asList(primary, alias, bea),
                EventFilterCriteria.builder()
                        .operatorTokens(tokens(EventFilterToken.text(EventFilterToken.Category.OPERATOR, "2580")))
                        .build()
        ));
    }

    @Test
    public void tokenCategoriesUseOrWithinCategoryAndAndAcrossCategories() {
        Event line6Macro = event(
                Event.Type.PRODUCT_USE,
                NOON,
                Event.TransportType.BUS,
                1,
                Operator.MI_MACRO_CALZADA.getValue(),
                0L,
                1111,
                0
        );
        Event route114Macro = event(Event.Type.PRODUCT_USE, NOON, Event.TransportType.BUS, 114, Operator.MI_MACRO_CALZADA.getValue());
        Event route114Bea = event(Event.Type.PRODUCT_USE, NOON, Event.TransportType.BUS, 114, Operator.BEA.getValue());
        EventFilterCriteria criteria = EventFilterCriteria.builder()
                .routeTokens(tokens(
                        EventFilterToken.route(Route.LINE_6, "L6"),
                        EventFilterToken.text(EventFilterToken.Category.ROUTE, "114")
                ))
                .operatorTokens(tokens(EventFilterToken.operator(Operator.MI_MACRO_CALZADA, "Mi Macro Calzada")))
                .build();

        List<Event> filteredEvents = EventFilter.filter(Arrays.asList(line6Macro, route114Macro, route114Bea), criteria);

        assertEquals(Arrays.asList(line6Macro, route114Macro), filteredEvents);
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
        return event(type, dateTime, transportType, routeId, entityId, 0L, 0, 0);
    }

    private Event event(Event.Type type,
                        LocalDateTime dateTime,
                        Event.TransportType transportType,
                        int routeId,
                        int entityId,
                        long samId,
                        int deviceId,
                        int locationId) {
        return new Event(
                0,
                0,
                null,
                entityId,
                dateTime,
                type,
                0,
                0,
                samId,
                0,
                deviceId,
                locationId,
                transportType,
                routeId,
                0,
                0,
                0,
                Event.RefundReason.NO_REFUND,
                Event.DeviceType.UNSPECIFIED
        );
    }

    private Set<EventFilterToken> tokens(EventFilterToken... tokens) {
        return new LinkedHashSet<>(Arrays.asList(tokens));
    }
}
