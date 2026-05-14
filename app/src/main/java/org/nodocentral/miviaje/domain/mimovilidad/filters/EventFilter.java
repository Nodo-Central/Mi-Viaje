package org.nodocentral.miviaje.domain.mimovilidad.filters;

import org.nodocentral.miviaje.domain.mimovilidad.Route;
import org.nodocentral.miviaje.domain.mimovilidad.RouteMapper;
import org.nodocentral.miviaje.domain.mimovilidad.StationMapper;
import org.nodocentral.miviaje.domain.mimovilidad.card.Event;

import java.util.ArrayList;
import java.util.List;

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
        if (!activeCriteria.getRoutes().isEmpty()
                && !activeCriteria.getRoutes().contains(resolveRoute(event))) {
            return false;
        }
        if (!activeCriteria.getOperators().isEmpty()
                && !activeCriteria.getOperators().contains(event.getOperator())) {
            return false;
        }
        if (!activeCriteria.getEventTypes().isEmpty()
                && !activeCriteria.getEventTypes().contains(event.getType())) {
            return false;
        }
        EventFilterCriteria.DateRange dateRange = activeCriteria.getDateRange();
        return dateRange == null || dateRange.contains(event.getEventDateTime());
    }

    private static Route resolveRoute(Event event) {
        Route route = RouteMapper.fromId(
                event.getOperator(),
                event.getRouteId(),
                event.getDeviceId(),
                event.getTransportType()
        );
        return route != null ? route : StationMapper.getRoute(event);
    }
}
