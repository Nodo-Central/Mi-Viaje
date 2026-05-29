package org.nodocentral.miviaje.domain.mimovilidad.analytics;

import org.nodocentral.miviaje.domain.mimovilidad.Operator;
import org.nodocentral.miviaje.domain.mimovilidad.Route;
import org.nodocentral.miviaje.domain.mimovilidad.RouteMapper;
import org.nodocentral.miviaje.domain.mimovilidad.Station;
import org.nodocentral.miviaje.domain.mimovilidad.StationMapper;
import org.nodocentral.miviaje.domain.mimovilidad.analytics.MobilityAnalyticsSummary.Count;
import org.nodocentral.miviaje.domain.mimovilidad.analytics.MobilityAnalyticsSummary.DateRange;
import org.nodocentral.miviaje.domain.mimovilidad.analytics.MobilityAnalyticsSummary.HourBlockCount;
import org.nodocentral.miviaje.domain.mimovilidad.analytics.MobilityAnalyticsSummary.PaymentUnitFlow;
import org.nodocentral.miviaje.domain.mimovilidad.card.Event;
import org.nodocentral.miviaje.domain.mimovilidad.card.Product;
import org.nodocentral.miviaje.domain.mimovilidad.card.ProductContract;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

public final class MobilityAnalyticsCalculator {
    private static final int HOUR_BLOCK_SIZE = 3;

    private MobilityAnalyticsCalculator() {
    }

    public static MobilityAnalyticsSummary calculate(List<Event> events, DateRange dateRange) {
        List<Event> sourceEvents = events != null ? events : new ArrayList<>();
        DateRange effectiveDateRange = dateRange != null ? dateRange : DateRange.allTime();

        int totalSavedEvents = sourceEvents.size();
        int eventsInRange = 0;
        int eventsOutsideRange = 0;
        int missingDateEvents = 0;
        int tripCount = 0;
        int transferCount = 0;
        MutablePaymentUnitFlow cashFlow = new MutablePaymentUnitFlow();
        MutablePaymentUnitFlow ticketFlow = new MutablePaymentUnitFlow();
        int unknownValueMovementCount = 0;
        int unknownTransportCount = 0;
        int unknownRouteCount = 0;
        int unknownStationCount = 0;
        int invalidOrUnspecifiedEventTypeCount = 0;

        EnumMap<DayOfWeek, Integer> weekdayTripCounts = emptyWeekdayCounts();
        int[] hourBlockCounts = new int[24 / HOUR_BLOCK_SIZE];
        Map<Event.TransportType, Integer> transportCounts = new HashMap<>();
        Map<Route, Integer> routeCounts = new HashMap<>();
        Map<Station, Integer> stationCounts = new HashMap<>();
        Map<Operator, Integer> operatorCounts = new HashMap<>();
        Set<LocalDate> activeDates = new TreeSet<>();
        Set<Operator> distinctOperators = new HashSet<>();
        Set<Integer> distinctValidators = new HashSet<>();
        Set<Integer> distinctDevices = new HashSet<>();
        Set<Long> distinctSams = new HashSet<>();

        for (Event event : sourceEvents) {
            if (event == null) {
                continue;
            }
            LocalDateTime eventDateTime = event.getEventDateTime();
            if (eventDateTime == null) {
                missingDateEvents++;
                continue;
            }
            if (!effectiveDateRange.contains(eventDateTime)) {
                eventsOutsideRange++;
                continue;
            }
            eventsInRange++;

            Event.Type type = event.getType();
            if (type == null || type == Event.Type.UNSPECIFIED) {
                invalidOrUnspecifiedEventTypeCount++;
            }

            Operator operator = event.getOperator();
            if (operator != null && operator != Operator.UNSPECIFIED) {
                increment(operatorCounts, operator);
                distinctOperators.add(operator);
            }
            int validator = StationMapper.getValidator(event);
            if (validator > 0) {
                distinctValidators.add(validator);
            }
            if (event.getDeviceId() > 0) {
                distinctDevices.add(event.getDeviceId());
            }
            if (event.getSamId() > 0) {
                distinctSams.add(event.getSamId());
            }

            PaymentUnit paymentUnit = resolvePaymentUnit(event);
            if (isValueMovementEvent(type) && event.getAmount() > 0) {
                MutablePaymentUnitFlow flow = flowForUnit(paymentUnit, cashFlow, ticketFlow);
                if (flow == null) {
                    unknownValueMovementCount++;
                } else if (isConsumptionEvent(type)) {
                    flow.addConsumed(event.getAmount());
                } else if (type == Event.Type.PRODUCT_TOP_UP) {
                    flow.addTopUp(event.getAmount(), eventDateTime);
                } else if (isRefundEvent(type)) {
                    flow.addRefund(event.getAmount());
                }
            }

            if (!isTripEvent(type)) {
                continue;
            }

            tripCount++;
            if (type == Event.Type.TRANSFER) {
                transferCount++;
            }

            activeDates.add(eventDateTime.toLocalDate());
            increment(weekdayTripCounts, eventDateTime.getDayOfWeek());
            hourBlockCounts[eventDateTime.getHour() / HOUR_BLOCK_SIZE]++;

            Event.TransportType transportType = event.getTransportType();
            if (transportType == null || transportType == Event.TransportType.UNSPECIFIED) {
                unknownTransportCount++;
            } else {
                increment(transportCounts, transportType);
            }

            Route route = resolveRoute(event);
            if (route == null || route == Route.NONE) {
                unknownRouteCount++;
            } else {
                increment(routeCounts, route);
            }

            Station station = resolveStation(event);
            if (station == null) {
                unknownStationCount++;
            } else {
                increment(stationCounts, station);
            }
        }

        int activeDays = activeDates.size();
        double averageTripsPerActiveDay = activeDays == 0 ? 0 : (double) tripCount / activeDays;

        return new MobilityAnalyticsSummary(
                effectiveDateRange,
                totalSavedEvents,
                eventsInRange,
                eventsOutsideRange,
                missingDateEvents,
                tripCount,
                transferCount,
                cashFlow.toImmutable(),
                ticketFlow.toImmutable(),
                activeDays,
                averageTripsPerActiveDay,
                calculateLongestStreak(activeDates),
                weekdayTripCounts,
                toHourBlockCounts(hourBlockCounts),
                toSortedCounts(transportCounts, Enum::name),
                toSortedCounts(routeCounts, Enum::name),
                toSortedCounts(stationCounts, Enum::name),
                toSortedCounts(operatorCounts, operator -> {
                    String name = operator.getName();
                    return name != null ? name : operator.name();
                }),
                unknownTransportCount,
                unknownRouteCount,
                unknownStationCount,
                unknownValueMovementCount,
                invalidOrUnspecifiedEventTypeCount,
                distinctOperators.size(),
                distinctValidators.size(),
                distinctDevices.size(),
                distinctSams.size()
        );
    }

    private static boolean isTripEvent(Event.Type type) {
        return type == Event.Type.PRODUCT_USE || type == Event.Type.TRANSFER;
    }

    private static boolean isValueMovementEvent(Event.Type type) {
        return isConsumptionEvent(type) || type == Event.Type.PRODUCT_TOP_UP || isRefundEvent(type);
    }

    private static boolean isConsumptionEvent(Event.Type type) {
        return type == Event.Type.PRODUCT_USE || type == Event.Type.TRANSFER;
    }

    private static boolean isRefundEvent(Event.Type type) {
        return type == Event.Type.REFUND || type == Event.Type.FARE_REFUND;
    }

    private static MutablePaymentUnitFlow flowForUnit(PaymentUnit unit,
                                                      MutablePaymentUnitFlow cashFlow,
                                                      MutablePaymentUnitFlow ticketFlow) {
        if (unit == PaymentUnit.CASH) {
            return cashFlow;
        }
        if (unit == PaymentUnit.TICKET) {
            return ticketFlow;
        }
        return null;
    }

    private static PaymentUnit resolvePaymentUnit(Event event) {
        Product product = event.getProduct();
        if (product != null) {
            ProductContract contract = product.getContract();
            if (contract != null) {
                ProductContract.ValueUnit valueUnit = contract.getValueUnit();
                if (valueUnit == ProductContract.ValueUnit.MXN_CENT) {
                    return PaymentUnit.CASH;
                }
                if (valueUnit == ProductContract.ValueUnit.TICKET) {
                    return PaymentUnit.TICKET;
                }
            }

            PaymentUnit unitFromType = resolvePaymentUnit(product.getType());
            if (unitFromType != PaymentUnit.UNKNOWN) {
                return unitFromType;
            }
        }

        return resolvePaymentUnit(Product.Type.fromInt(event.getProductId()));
    }

    private static PaymentUnit resolvePaymentUnit(Product.Type type) {
        if (type == Product.Type.WALLET || type == Product.Type.CREDIT) {
            return PaymentUnit.CASH;
        }
        if (type == Product.Type.DISCOUNT_TICKETS_1 || type == Product.Type.DISCOUNT_TICKETS_2) {
            return PaymentUnit.TICKET;
        }
        return PaymentUnit.UNKNOWN;
    }

    private static Route resolveRoute(Event event) {
        Route displayedRoute = StationMapper.getRoute(event);
        if (displayedRoute != null) {
            return displayedRoute;
        }
        return RouteMapper.fromInt(
                event.getEntityId(),
                event.getRouteId(),
                event.getDeviceId(),
                event.getTransportType()
        );
    }

    private static Station resolveStation(Event event) {
        Station station = StationMapper.getStation(event);
        if (station != null) {
            return station;
        }
        return StationMapper.getLocation(event);
    }

    private static EnumMap<DayOfWeek, Integer> emptyWeekdayCounts() {
        EnumMap<DayOfWeek, Integer> counts = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            counts.put(dayOfWeek, 0);
        }
        return counts;
    }

    private static <T> void increment(Map<T, Integer> counts, T item) {
        counts.put(item, counts.getOrDefault(item, 0) + 1);
    }

    private static List<HourBlockCount> toHourBlockCounts(int[] counts) {
        List<HourBlockCount> result = new ArrayList<>(counts.length);
        for (int i = 0; i < counts.length; i++) {
            int startHour = i * HOUR_BLOCK_SIZE;
            result.add(new HourBlockCount(startHour, startHour + HOUR_BLOCK_SIZE, counts[i]));
        }
        return result;
    }

    private static <T> List<Count<T>> toSortedCounts(Map<T, Integer> counts, Function<T, String> sortKey) {
        List<Count<T>> result = new ArrayList<>();
        for (Map.Entry<T, Integer> entry : counts.entrySet()) {
            result.add(new Count<>(entry.getKey(), entry.getValue()));
        }
        result.sort(
                Comparator.<Count<T>>comparingInt(Count::getCount)
                        .reversed()
                        .thenComparing(count -> sortKey.apply(count.getItem()))
        );
        return result;
    }

    private static int calculateLongestStreak(Set<LocalDate> activeDates) {
        int best = 0;
        int current = 0;
        LocalDate previous = null;
        for (LocalDate date : activeDates) {
            if (previous != null && date.equals(previous.plusDays(1))) {
                current++;
            } else {
                current = 1;
            }
            best = Math.max(best, current);
            previous = date;
        }
        return best;
    }

    private enum PaymentUnit {
        CASH,
        TICKET,
        UNKNOWN
    }

    private static final class MutablePaymentUnitFlow {
        private int consumed;
        private int added;
        private int refunded;
        private int consumedCount;
        private int addedCount;
        private int refundCount;
        private int highestTopUp;
        private int lastTopUpAmount;
        private LocalDateTime lastTopUpDateTime;

        void addConsumed(int amount) {
            consumed += amount;
            consumedCount++;
        }

        void addTopUp(int amount, LocalDateTime dateTime) {
            added += amount;
            addedCount++;
            highestTopUp = Math.max(highestTopUp, amount);
            if (lastTopUpDateTime == null || dateTime.isAfter(lastTopUpDateTime)) {
                lastTopUpDateTime = dateTime;
                lastTopUpAmount = amount;
            }
        }

        void addRefund(int amount) {
            refunded += amount;
            refundCount++;
        }

        PaymentUnitFlow toImmutable() {
            return new PaymentUnitFlow(
                    consumed,
                    added,
                    refunded,
                    consumedCount,
                    addedCount,
                    refundCount,
                    highestTopUp,
                    lastTopUpAmount,
                    lastTopUpDateTime
            );
        }
    }
}
