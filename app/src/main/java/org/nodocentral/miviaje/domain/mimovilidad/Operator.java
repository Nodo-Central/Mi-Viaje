package org.nodocentral.miviaje.domain.mimovilidad;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public enum Operator {
    UNSPECIFIED(0),
    TISA("TISA/Viaxer", 1),
    BEA("BEA", 4, 255),
    EB_JALISCO("EB Jalisco", 13),
    MI_MACRO_CALZADA("Mi Macro Calzada", 83),
    MI_MACRO_PERIFERICO_COMPLEMENTARIO("Mi Macro Periférico Complementarias y Alimentadoras", 1059, 39754),
    MI_MACRO_PERIFERICO_TRONCAL("Mi Macro Periférico Troncal", 1060),
    TRANSBUS("Transbus El Salto", 1062),
    RUTA_LOPEZ_MATEOS("Ruta López Mateos", 1070, 2580);

    private static final Map<Integer, Operator> BY_VALUE;

    static {
        Map<Integer, Operator> values = new LinkedHashMap<>();
        for (Operator operator : Operator.values()) {
            for (int value : operator.values) {
                Operator previous = values.put(value, operator);
                if (previous != null) {
                    throw new IllegalStateException(
                            "Duplicate operator value " + value + ": " + previous + " and " + operator);
                }
            }
        }
        BY_VALUE = Collections.unmodifiableMap(values);
    }

    private final String name;
    private final int[] values;

    Operator(int value, int... aliases) {
        this(null, value, aliases);
    }

    Operator(String name, int value, int... aliases) {
        this.name = name;
        this.values = new int[aliases.length + 1];
        this.values[0] = value;
        System.arraycopy(aliases, 0, this.values, 1, aliases.length);
    }

    public String getName() {
        return this.name;
    }

    public int getValue() {
        return this.values[0];
    }

    public int[] getValues() {
        return this.values.clone();
    }

    public boolean matches(int entityId) {
        for (int value : values) {
            if (value == entityId) {
                return true;
            }
        }
        return false;
    }

    public static Operator fromInt(int entityId) {
        return BY_VALUE.get(entityId);
    }
}
