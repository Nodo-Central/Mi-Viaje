package org.nodocentral.miviaje.domain.mimovilidad;

public enum Operator {
    UNSPECIFIED(0),
    TISA("TISA/Viaxer", 1),
    BEA("BEA", 4),
    C111("C111", 5),
    ALIANZA_DE_CAMIONEROS("Alianza", 13),
    T06("T06", 78),
    T09("T09", 14),
    T11_1("T11", 224),
    T11_2("T11", 56),
    T15("T15", 1015),
    T18A("T18A", 1013), // TODO: Revisar por el caso 18 (63/BEA)
    MI_MACRO_CALZADA("Mi Macro Calzada", 83),
    BEA_V2("BEA v2", 255),
    MI_MACRO_PERIFERICO_COMPLEMENTARIO("Mi Macro Periférico", 1059),
    MI_MACRO_PERIFERICO_ALIMENTADOR("Mi Macro Periférico", 39754),
    MI_MACRO_PERIFERICO_TRONCAL("Mi Macro Periférico", 1060),
    TRANSBUS("Transbus El Salto", 1062),
    RUTA_LOPEZ_MATEOS("Ruta López Mateos", 1070);

    private final String name;
    private final int value;

    Operator(int value) {
        this.name = null;
        this.value = value;
    }

    Operator(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public int getValue() {
        return this.value;
    }

    public static Operator fromInt(int entityId) {
        for (Operator operator : Operator.values()) {
            if (operator.value == entityId) {
                return operator;
            }
        }
        return null;
    }
}
