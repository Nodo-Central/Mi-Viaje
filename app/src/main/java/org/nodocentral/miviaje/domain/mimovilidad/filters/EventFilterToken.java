package org.nodocentral.miviaje.domain.mimovilidad.filters;

import org.nodocentral.miviaje.domain.mimovilidad.Operator;
import org.nodocentral.miviaje.domain.mimovilidad.Route;
import org.nodocentral.miviaje.domain.mimovilidad.Station;

import java.util.Locale;
import java.util.Objects;

public final class EventFilterToken {
    public enum Category {
        ROUTE,
        STATION_VALIDATOR,
        OPERATOR
    }

    public enum Kind {
        ROUTE,
        STATION,
        OPERATOR,
        TEXT
    }

    private final Category category;
    private final Kind kind;
    private final Route route;
    private final Station station;
    private final Operator operator;
    private final String text;
    private final String label;

    private EventFilterToken(Category category,
                             Kind kind,
                             Route route,
                             Station station,
                             Operator operator,
                             String text,
                             String label) {
        this.category = Objects.requireNonNull(category, "category");
        this.kind = Objects.requireNonNull(kind, "kind");
        this.route = route;
        this.station = station;
        this.operator = operator;
        this.text = text != null ? text.trim() : null;
        this.label = label != null ? label.trim() : this.text;
    }

    public static EventFilterToken route(Route route, String label) {
        return new EventFilterToken(Category.ROUTE, Kind.ROUTE, route, null, null, null, label);
    }

    public static EventFilterToken station(Station station, String label) {
        return new EventFilterToken(Category.STATION_VALIDATOR, Kind.STATION, null, station, null, null, label);
    }

    public static EventFilterToken operator(Operator operator, String label) {
        return new EventFilterToken(Category.OPERATOR, Kind.OPERATOR, null, null, operator, null, label);
    }

    public static EventFilterToken text(Category category, String text) {
        return new EventFilterToken(category, Kind.TEXT, null, null, null, text, text);
    }

    public Category getCategory() {
        return category;
    }

    public Kind getKind() {
        return kind;
    }

    public Route getRoute() {
        return route;
    }

    public Station getStation() {
        return station;
    }

    public Operator getOperator() {
        return operator;
    }

    public String getText() {
        return text;
    }

    public String getLabel() {
        return label;
    }

    public boolean hasText() {
        return text != null && !text.trim().isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof EventFilterToken)) {
            return false;
        }
        EventFilterToken token = (EventFilterToken) other;
        return category == token.category
                && kind == token.kind
                && route == token.route
                && station == token.station
                && operator == token.operator
                && Objects.equals(canonicalText(text), canonicalText(token.text));
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, kind, route, station, operator, canonicalText(text));
    }

    @Override
    public String toString() {
        return label;
    }

    private static String canonicalText(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }
}
