package org.nodocentral.miviaje.domain.mimovilidad.analytics;

import org.junit.Test;
import org.nodocentral.miviaje.domain.mimovilidad.Operator;
import org.nodocentral.miviaje.domain.mimovilidad.Route;
import org.nodocentral.miviaje.domain.mimovilidad.Station;
import org.nodocentral.miviaje.domain.mimovilidad.analytics.MobilityAnalyticsSummary.DateRange;
import org.nodocentral.miviaje.domain.mimovilidad.analytics.MobilityAnalyticsSummary.PaymentUnitFlow;
import org.nodocentral.miviaje.domain.mimovilidad.card.Event;
import org.nodocentral.miviaje.domain.mimovilidad.card.Product;
import org.nodocentral.miviaje.domain.mimovilidad.card.ProductContract;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MobilityAnalyticsCalculatorTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 5, 28);
    private static final Product CASH_PRODUCT = product(Product.Type.WALLET, ProductContract.ValueUnit.MXN_CENT);
    private static final Product TICKET_PRODUCT = product(Product.Type.DISCOUNT_TICKETS_1, ProductContract.ValueUnit.TICKET);

    @Test
    public void lastDaysRangeIncludesStartAndEndDates() {
        Event before = event(Event.Type.PRODUCT_USE, LocalDateTime.of(2026, 2, 26, 23, 59), Event.TransportType.BUS, 114, 0, 0);
        Event atStart = event(Event.Type.PRODUCT_USE, LocalDateTime.of(2026, 2, 27, 0, 0), Event.TransportType.BUS, 114, 0, 0);
        Event atEnd = event(Event.Type.PRODUCT_USE, LocalDateTime.of(2026, 5, 28, 23, 59), Event.TransportType.BUS, 114, 0, 0);
        Event after = event(Event.Type.PRODUCT_USE, LocalDateTime.of(2026, 5, 29, 0, 0), Event.TransportType.BUS, 114, 0, 0);

        MobilityAnalyticsSummary summary = MobilityAnalyticsCalculator.calculate(
                Arrays.asList(before, atStart, atEnd, after),
                DateRange.lastDays(TODAY, 91)
        );

        assertEquals(2, summary.getTripCount());
        assertEquals(2, summary.getEventsOutsideRange());
    }

    @Test
    public void allTimeRangeIncludesAllDatedEventsAndTracksMissingDates() {
        Event first = event(Event.Type.PRODUCT_USE, LocalDateTime.of(2024, 1, 1, 8, 0), Event.TransportType.BUS, 114, 0, 0);
        Event second = event(Event.Type.PRODUCT_USE, LocalDateTime.of(2026, 1, 1, 8, 0), Event.TransportType.BUS, 114, 0, 0);
        Event missingDate = event(Event.Type.PRODUCT_USE, null, Event.TransportType.BUS, 114, 0, 0);

        MobilityAnalyticsSummary summary = MobilityAnalyticsCalculator.calculate(
                Arrays.asList(first, second, missingDate),
                DateRange.allTime()
        );

        assertEquals(2, summary.getTripCount());
        assertEquals(0, summary.getEventsOutsideRange());
        assertEquals(1, summary.getMissingDateEvents());
    }

    @Test
    public void emptyRangeReturnsZeroedSummary() {
        MobilityAnalyticsSummary summary = MobilityAnalyticsCalculator.calculate(
                Collections.singletonList(event(
                        Event.Type.PRODUCT_USE,
                        LocalDateTime.of(2026, 1, 1, 8, 0),
                        Event.TransportType.BUS,
                        114,
                        0,
                        0
                )),
                DateRange.custom(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31))
        );

        assertEquals(0, summary.getTripCount());
        assertEquals(0, summary.getSpentCents());
        assertEquals(0, summary.getActiveDays());
        assertEquals(0, summary.getAverageTripsPerActiveDay(), 0.001);
        assertNull(summary.getTopTransportType());
    }

    @Test
    public void calculatesTripsTransfersSpendReloadsActiveDaysAndAverage() {
        Event trip = event(Event.Type.PRODUCT_USE, LocalDateTime.of(2026, 5, 26, 8, 0), Event.TransportType.BUS, 114, 0, 950, CASH_PRODUCT);
        Event transfer = event(Event.Type.TRANSFER, LocalDateTime.of(2026, 5, 26, 8, 30), Event.TransportType.BUS, 114, 0, 0, CASH_PRODUCT);
        Event secondDayTrip = event(Event.Type.PRODUCT_USE, LocalDateTime.of(2026, 5, 27, 9, 0), Event.TransportType.BUS, 114, 0, 950, CASH_PRODUCT);
        Event reload = event(Event.Type.PRODUCT_TOP_UP, LocalDateTime.of(2026, 5, 27, 10, 0), Event.TransportType.BUS, 0, 0, 5000, CASH_PRODUCT);

        MobilityAnalyticsSummary summary = MobilityAnalyticsCalculator.calculate(
                Arrays.asList(trip, transfer, secondDayTrip, reload),
                DateRange.custom(LocalDate.of(2026, 5, 26), LocalDate.of(2026, 5, 27))
        );

        assertEquals(3, summary.getTripCount());
        assertEquals(1, summary.getTransferCount());
        assertEquals(1900, summary.getSpentCents());
        assertEquals(2, summary.getActiveDays());
        assertEquals(1.5, summary.getAverageTripsPerActiveDay(), 0.001);
        assertEquals(1, summary.getReloadCount());
        assertEquals(5000, summary.getLastReloadCents());
        assertEquals(3100, summary.getCashFlow().getNetChange());
    }

    @Test
    public void separatesCashAndTicketMovement() {
        Event cashTrip = event(Event.Type.PRODUCT_USE, LocalDateTime.of(2026, 5, 26, 8, 0), Event.TransportType.BUS, 114, 0, 950, CASH_PRODUCT);
        Event cashTopUp = event(Event.Type.PRODUCT_TOP_UP, LocalDateTime.of(2026, 5, 26, 9, 0), Event.TransportType.BUS, 0, 0, 10000, CASH_PRODUCT);
        Event ticketTrip = event(Event.Type.PRODUCT_USE, LocalDateTime.of(2026, 5, 27, 8, 0), Event.TransportType.BUS, 114, 0, 1, TICKET_PRODUCT);
        Event ticketTransfer = event(Event.Type.TRANSFER, LocalDateTime.of(2026, 5, 27, 8, 20), Event.TransportType.BUS, 114, 0, 1, TICKET_PRODUCT);
        Event ticketTopUp = event(Event.Type.PRODUCT_TOP_UP, LocalDateTime.of(2026, 5, 28, 7, 0), Event.TransportType.BUS, 0, 0, 30, TICKET_PRODUCT);

        MobilityAnalyticsSummary summary = MobilityAnalyticsCalculator.calculate(
                Arrays.asList(cashTrip, cashTopUp, ticketTrip, ticketTransfer, ticketTopUp),
                DateRange.custom(LocalDate.of(2026, 5, 26), LocalDate.of(2026, 5, 28))
        );

        PaymentUnitFlow cash = summary.getCashFlow();
        PaymentUnitFlow tickets = summary.getTicketFlow();
        assertEquals(950, cash.getConsumed());
        assertEquals(10000, cash.getAdded());
        assertEquals(9050, cash.getNetChange());
        assertEquals(2, tickets.getConsumed());
        assertEquals(30, tickets.getAdded());
        assertEquals(28, tickets.getNetChange());
        assertEquals(3, summary.getTripCount());
    }

    @Test
    public void tracksRefundsByPaymentUnit() {
        Event cashTrip = event(Event.Type.PRODUCT_USE, LocalDateTime.of(2026, 5, 26, 8, 0), Event.TransportType.BUS, 114, 0, 950, CASH_PRODUCT);
        Event cashRefund = event(Event.Type.FARE_REFUND, LocalDateTime.of(2026, 5, 26, 8, 5), Event.TransportType.BUS, 114, 0, 950, CASH_PRODUCT);
        Event ticketTrip = event(Event.Type.PRODUCT_USE, LocalDateTime.of(2026, 5, 27, 8, 0), Event.TransportType.BUS, 114, 0, 1, TICKET_PRODUCT);
        Event ticketRefund = event(Event.Type.REFUND, LocalDateTime.of(2026, 5, 27, 8, 5), Event.TransportType.BUS, 114, 0, 1, TICKET_PRODUCT);

        MobilityAnalyticsSummary summary = MobilityAnalyticsCalculator.calculate(
                Arrays.asList(cashTrip, cashRefund, ticketTrip, ticketRefund),
                DateRange.custom(LocalDate.of(2026, 5, 26), LocalDate.of(2026, 5, 27))
        );

        assertEquals(950, summary.getCashFlow().getRefunded());
        assertEquals(0, summary.getCashFlow().getNetChange());
        assertEquals(1, summary.getTicketFlow().getRefunded());
        assertEquals(0, summary.getTicketFlow().getNetChange());
    }

    @Test
    public void fallsBackToProductTypeWhenValueUnitIsMissing() {
        Product cashWithoutUnit = product(Product.Type.CREDIT, null);
        Product ticketsWithoutUnit = product(Product.Type.DISCOUNT_TICKETS_2, null);

        MobilityAnalyticsSummary summary = MobilityAnalyticsCalculator.calculate(
                Arrays.asList(
                        event(Event.Type.PRODUCT_USE, LocalDateTime.of(2026, 5, 26, 8, 0), Event.TransportType.BUS, 114, 0, 950, cashWithoutUnit),
                        event(Event.Type.PRODUCT_USE, LocalDateTime.of(2026, 5, 26, 9, 0), Event.TransportType.BUS, 114, 0, 1, ticketsWithoutUnit)
                ),
                DateRange.custom(LocalDate.of(2026, 5, 26), LocalDate.of(2026, 5, 26))
        );

        assertEquals(950, summary.getCashFlow().getConsumed());
        assertEquals(1, summary.getTicketFlow().getConsumed());
    }

    @Test
    public void excludesUnknownValueUnitsFromVisibleTotalsAndCountsThemForDebug() {
        Event unknownMovement = event(Event.Type.PRODUCT_USE, LocalDateTime.of(2026, 5, 26, 8, 0), Event.TransportType.BUS, 114, 0, 950);

        MobilityAnalyticsSummary summary = MobilityAnalyticsCalculator.calculate(
                Collections.singletonList(unknownMovement),
                DateRange.custom(LocalDate.of(2026, 5, 26), LocalDate.of(2026, 5, 26))
        );

        assertEquals(0, summary.getCashFlow().getConsumed());
        assertEquals(0, summary.getTicketFlow().getConsumed());
        assertEquals(1, summary.getUnknownValueMovementCount());
    }

    @Test
    public void calculatesRankingsAndRhythm() {
        Event line2Station = event(Event.Type.PRODUCT_USE, LocalDateTime.of(2026, 5, 25, 8, 0), Event.TransportType.TRAIN, 2, 0, 950, 27000, 0, 0L);
        Event line2StationAgain = event(Event.Type.PRODUCT_USE, LocalDateTime.of(2026, 5, 26, 8, 30), Event.TransportType.TRAIN, 2, 0, 950, 27000, 0, 0L);
        Event macro = event(
                Event.Type.PRODUCT_USE,
                LocalDateTime.of(2026, 5, 26, 15, 0),
                Event.TransportType.BUS,
                1,
                Operator.MI_MACRO_CALZADA.getValue(),
                950
        );

        MobilityAnalyticsSummary summary = MobilityAnalyticsCalculator.calculate(
                Arrays.asList(line2Station, line2StationAgain, macro),
                DateRange.custom(LocalDate.of(2026, 5, 25), LocalDate.of(2026, 5, 26))
        );

        assertEquals(Event.TransportType.TRAIN, summary.getTopTransportType());
        assertEquals(Route.LINE_2, summary.getTopRoute());
        assertEquals(Station.SAN_JUAN_DE_DIOS, summary.getTopStation());
        assertEquals(DayOfWeek.TUESDAY, summary.getMostActiveWeekday().getItem());
        assertEquals(6, summary.getPeakHourBlock().getStartHour());
        assertEquals(2, summary.getLongestActivityStreakDays());
    }

    @Test
    public void calculatesAdvancedAndDebugCounts() {
        Event known = event(
                Event.Type.PRODUCT_USE,
                LocalDateTime.of(2026, 5, 26, 8, 0),
                Event.TransportType.BUS,
                1,
                Operator.MI_MACRO_CALZADA.getValue(),
                950,
                1111,
                0,
                123L
        );
        Event unknowns = event(Event.Type.PRODUCT_USE, LocalDateTime.of(2026, 5, 26, 9, 0), Event.TransportType.UNSPECIFIED, 0, 0, 0);
        Event invalid = event(Event.Type.UNSPECIFIED, LocalDateTime.of(2026, 5, 26, 10, 0), Event.TransportType.BUS, 0, 0, 0);

        MobilityAnalyticsSummary summary = MobilityAnalyticsCalculator.calculate(
                Arrays.asList(known, unknowns, invalid),
                DateRange.custom(LocalDate.of(2026, 5, 26), LocalDate.of(2026, 5, 26))
        );

        assertEquals(1, summary.getUnknownTransportCount());
        assertEquals(1, summary.getUnknownRouteCount());
        assertEquals(1, summary.getUnknownStationCount());
        assertEquals(1, summary.getInvalidOrUnspecifiedEventTypeCount());
        assertEquals(1, summary.getDistinctOperatorCount());
        assertEquals(1, summary.getDistinctValidatorCount());
        assertEquals(1, summary.getDistinctDeviceCount());
        assertEquals(1, summary.getDistinctSamCount());
    }

    @Test
    public void operatorAliasIdsAreCountedTogether() {
        Event primary = event(Event.Type.PRODUCT_USE, LocalDateTime.of(2026, 5, 26, 8, 0), Event.TransportType.BUS, 1, Operator.RUTA_LOPEZ_MATEOS.getValue(), 950);
        Event alias = event(Event.Type.PRODUCT_USE, LocalDateTime.of(2026, 5, 26, 9, 0), Event.TransportType.BUS, 1, 2580, 950);

        MobilityAnalyticsSummary summary = MobilityAnalyticsCalculator.calculate(
                Arrays.asList(primary, alias),
                DateRange.custom(LocalDate.of(2026, 5, 26), LocalDate.of(2026, 5, 26))
        );

        assertEquals(1, summary.getDistinctOperatorCount());
        assertEquals(Operator.RUTA_LOPEZ_MATEOS, summary.getTopOperator());
        assertEquals(2, summary.getOperatorCounts().get(0).getCount());
    }

    private Event event(Event.Type type,
                        LocalDateTime dateTime,
                        Event.TransportType transportType,
                        int routeId,
                        int entityId,
                        int amount) {
        return event(type, dateTime, transportType, routeId, entityId, amount, 0, 0, 0L);
    }

    private Event event(Event.Type type,
                        LocalDateTime dateTime,
                        Event.TransportType transportType,
                        int routeId,
                        int entityId,
                        int amount,
                        Product product) {
        return event(type, dateTime, transportType, routeId, entityId, amount, 0, 0, 0L, product);
    }

    private Event event(Event.Type type,
                        LocalDateTime dateTime,
                        Event.TransportType transportType,
                        int routeId,
                        int entityId,
                        int amount,
                        int deviceId,
                        int locationId,
                        long samId) {
        return event(type, dateTime, transportType, routeId, entityId, amount, deviceId, locationId, samId, null);
    }

    private Event event(Event.Type type,
                        LocalDateTime dateTime,
                        Event.TransportType transportType,
                        int routeId,
                        int entityId,
                        int amount,
                        int deviceId,
                        int locationId,
                        long samId,
                        Product product) {
        return new Event(
                product != null ? product.getId() : 0,
                0,
                product,
                entityId,
                dateTime,
                type,
                amount,
                0,
                samId,
                0,
                deviceId,
                locationId,
                transportType,
                routeId,
                type == Event.Type.TRANSFER ? 1 : 0,
                0,
                0,
                Event.RefundReason.NO_REFUND,
                Event.DeviceType.UNSPECIFIED
        );
    }

    private static Product product(Product.Type type, ProductContract.ValueUnit valueUnit) {
        ProductContract contract = new ProductContract(
                (short) type.getId(),
                0,
                0,
                (short) 0,
                (short) 0,
                (short) 0,
                valueUnit,
                0,
                0,
                null,
                null,
                null,
                null
        );
        return new Product((short) type.getId(), 0, 0, 0, contract, null);
    }
}
