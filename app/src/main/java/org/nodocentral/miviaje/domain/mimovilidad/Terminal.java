package org.nodocentral.miviaje.domain.mimovilidad;

public enum Terminal {
    PERIFERICO_SUR(Station.PERIFERICO_SUR, Route.LINE_1, Station.AUDITORIO, 1),
    AUDITORIO(Station.AUDITORIO, Route.LINE_1, Station.PERIFERICO_SUR, 2),
    JUAREZ(Station.JUAREZ_II, Route.LINE_2, Station.TETLAN, 1),
    TETLAN(Station.TETLAN, Route.LINE_2, Station.JUAREZ_II, 2),
    ARCOS_DE_ZAPOPAN(Station.ARCOS_DE_ZAPOPAN, Route.LINE_3, Station.CENTRAL_DE_AUTOBUSES, 1),
    CENTRAL_DE_AUTOBUSES(Station.CENTRAL_DE_AUTOBUSES, Route.LINE_3, Station.ARCOS_DE_ZAPOPAN, 2),
    LAS_JUNTAS(Station.LAS_JUNTAS, Route.LINE_4, Station.TLAJOMULCO_CENTRO, 1),
    TLAJOMULCO_CENTRO(Station.TLAJOMULCO_CENTRO, Route.LINE_4, Station.LAS_JUNTAS, 2),
    LAS_JUNTAS_II(Station.FRAY_ANGELICO, Route.LINE_6, Station.MIRADOR, 1),
    MIRADOR(Station.MIRADOR, Route.LINE_6, Station.LAS_JUNTAS_II, 2),
    CARRETERA_A_CHAPALA(Station.CARRETERA_A_CHAPALA, Route.LINE_7, Station.BARRANCA_DE_HUENTITAN, 1),
    BARRANCA_DE_HUENTITAN(Station.BARRANCA_DE_HUENTITAN, Route.LINE_7, Station.CARRETERA_A_CHAPALA, 2),
    LA_CANTERA(Station.LA_CANTERA, Route.LINE_7, Station.LOS_CONEJOS, 3),
    LOS_CONEJOS(Station.LOS_CONEJOS, Route.LINE_7, Station.LA_CANTERA, 4);

    public final Station station;
    private final Station opposite;
    public final Route route;
    public final int id;

    Terminal(Station station, Route route, Station opposite, int id) {
        this.station = station;
        this.route = route;
        this.opposite = opposite;
        this.id = id;
    }

    public static Terminal opposite(Terminal terminal) {
        for (Terminal candidate : Terminal.values()) {
            if (terminal.opposite == candidate.station) {
                return candidate;
            }
        }
        return null;
    }

    public static Terminal fromInt(Route route, int value) {
        if (route == null) {
            return null;
        }
        return fromInt(route.getId(), value);
    }

    public static Terminal fromInt(int routeId, int value) {
        for (Terminal terminal : Terminal.values()) {
            if (terminal.id == value && terminal.route.getId() == routeId) {
                return terminal;
            }
        }
        return null;
    }
}
