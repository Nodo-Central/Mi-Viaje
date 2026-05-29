package org.nodocentral.miviaje.domain.mimovilidad.filters;

import org.nodocentral.miviaje.domain.mimovilidad.Operator;
import org.nodocentral.miviaje.domain.mimovilidad.Route;
import org.nodocentral.miviaje.domain.mimovilidad.RouteMapper;
import org.nodocentral.miviaje.domain.mimovilidad.Station;
import org.nodocentral.miviaje.domain.mimovilidad.StationMapper;
import org.nodocentral.miviaje.domain.mimovilidad.card.Event;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class EventFilter {
    private EventFilter() {
    }

    public static List<Event> filter(List<Event> events, EventFilterCriteria criteria) {
        List<Event> filteredEvents = new ArrayList<>();
        if (events == null || events.isEmpty()) {
            return filteredEvents;
        }

        EventFilterCriteria activeCriteria = criteria != null ? criteria : EventFilterCriteria.empty();
        for (Event event : events) {
            if (matches(event, activeCriteria)) {
                filteredEvents.add(event);
            }
        }
        return filteredEvents;
    }

    public static boolean matches(Event event, EventFilterCriteria criteria) {
        if (event == null) {
            return false;
        }

        EventFilterCriteria activeCriteria = criteria != null ? criteria : EventFilterCriteria.empty();
        if (!activeCriteria.getTransportTypes().isEmpty()
                && !activeCriteria.getTransportTypes().contains(event.getTransportType())) {
            return false;
        }
        if (!matchesRouteTokens(event, activeCriteria.getRouteTokens())) {
            return false;
        }
        if (!matchesStationValidatorTokens(event, activeCriteria.getStationValidatorTokens())) {
            return false;
        }
        if (!matchesOperatorTokens(event, activeCriteria.getOperatorTokens())) {
            return false;
        }
        if (!activeCriteria.getEventTypes().isEmpty()
                && !activeCriteria.getEventTypes().contains(event.getType())) {
            return false;
        }
        EventFilterCriteria.DateRange dateRange = activeCriteria.getDateRange();
        return dateRange == null || dateRange.contains(event.getEventDateTime());
    }

    private static boolean matchesRouteTokens(Event event, Set<EventFilterToken> tokens) {
        if (tokens.isEmpty()) {
            return true;
        }
        for (EventFilterToken token : tokens) {
            if (matchesRouteToken(event, token)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesRouteToken(Event event, EventFilterToken token) {
        Route route = resolveRoute(event);
        if (token.getKind() == EventFilterToken.Kind.ROUTE) {
            return route == token.getRoute();
        }
        String query = token.getText();
        return textMatches(query,
                route != null ? route.name() : null,
                route != null ? String.valueOf(route.getId()) : null,
                route != null ? "L" + route.getId() : null,
                event.getRouteId(),
                event.getTransportType() != null ? event.getTransportType().name() : null);
    }

    private static boolean matchesStationValidatorTokens(Event event, Set<EventFilterToken> tokens) {
        if (tokens.isEmpty()) {
            return true;
        }
        for (EventFilterToken token : tokens) {
            if (matchesStationValidatorToken(event, token)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesStationValidatorToken(Event event, EventFilterToken token) {
        Station station = StationMapper.getStation(event);
        Station location = StationMapper.getLocation(event);
        if (token.getKind() == EventFilterToken.Kind.STATION) {
            return station == token.getStation() || location == token.getStation();
        }
        String query = token.getText();
        return textMatches(query,
                station != null ? station.name() : null,
                location != null ? location.name() : null,
                station != null ? station.getRoute().name() : null,
                StationMapper.getStationId(event),
                StationMapper.getValidator(event),
                event.getDeviceId(),
                event.getLocationId());
    }

    private static boolean matchesOperatorTokens(Event event, Set<EventFilterToken> tokens) {
        if (tokens.isEmpty()) {
            return true;
        }
        for (EventFilterToken token : tokens) {
            if (matchesOperatorToken(event, token)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesOperatorToken(Event event, EventFilterToken token) {
        Operator operator = event.getOperator();
        if (token.getKind() == EventFilterToken.Kind.OPERATOR) {
            return operator == token.getOperator();
        }
        String query = token.getText();
        return textMatches(query, operatorSearchValues(operator, event.getEntityId()));
    }

    private static Object[] operatorSearchValues(Operator operator, int entityId) {
        if (operator == null) {
            return new Object[]{entityId};
        }
        int[] operatorIds = operator.getValues();
        Object[] values = new Object[operatorIds.length + 3];
        values[0] = operator.name();
        values[1] = operator.getName();
        for (int i = 0; i < operatorIds.length; i++) {
            values[i + 2] = operatorIds[i];
        }
        values[values.length - 1] = entityId;
        return values;
    }

    private static Route resolveRoute(Event event) {
        Route displayedRoute = StationMapper.getRoute(event);
        if (displayedRoute != null) {
            return displayedRoute;
        }
        Route route = RouteMapper.fromId(
                event.getEntityId(),
                event.getRouteId(),
                event.getDeviceId(),
                event.getTransportType()
        );
        return route;
    }

    private static boolean textMatches(String query, Object... values) {
        String normalizedQuery = normalize(query);
        if (normalizedQuery.isEmpty()) {
            return false;
        }
        String queryDigits = digitsOnly(normalizedQuery);
        for (Object value : values) {
            if (value == null) {
                continue;
            }
            String normalizedValue = normalize(String.valueOf(value));
            if (!normalizedValue.isEmpty()
                    && !normalizedValue.matches("\\d+")
                    && normalizedValue.contains(normalizedQuery)) {
                return true;
            }
            if (!queryDigits.isEmpty()) {
                String valueDigits = digitsOnly(normalizedValue);
                if (!valueDigits.isEmpty() && stripLeadingZeroes(queryDigits).equals(stripLeadingZeroes(valueDigits))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD);
        return decomposed
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('_', ' ')
                .replace('-', ' ')
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private static String digitsOnly(String value) {
        return value != null ? value.replaceAll("\\D+", "") : "";
    }

    private static String stripLeadingZeroes(String value) {
        String stripped = value.replaceFirst("^0+(?!$)", "");
        return stripped.isEmpty() ? "0" : stripped;
    }
}
