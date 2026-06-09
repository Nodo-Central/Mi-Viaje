package org.nodocentral.miviaje.domain.mimovilidad;

import android.util.Log;

import org.nodocentral.miviaje.domain.mimovilidad.card.Event;
import org.nodocentral.miviaje.domain.mimovilidad.card.Event.TransportType;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class StationMapper {
    private static final int ROUTE_1_2_TERMINAL_OFFSET = 6;
    private static final int DIVIDER_ROUTES_1_2_3 = 1000;
    private static final long DIVIDER_ROUTE_4 = 0x10000000000L;
    private static final int DIVIDER_ROUTE_6 = 1000;
    private static final int DIVIDER_ROUTE_6_LOCATION = 100;

    private static final Map<Key, Station> ID_MAP;
    private static final Map<Integer, Station> TRAIN_ID_MAP;
    private static final Map<Integer, Station> LOCATION_MAP;

    static {
        Map<Key, Station> ids = new LinkedHashMap<>();
        Map<Integer, Station> locations = new LinkedHashMap<>();

        register(ids, Station.PERIFERICO_SUR, 1);
        registerLocation(locations, Station.PERIFERICO_SUR, 1);
        register(ids, Station.SANTUARIO_MARTIRES, 2);
        registerLocation(locations, Station.SANTUARIO_MARTIRES, 2);
        register(ids, Station.ESPANNA, 3);
        registerLocation(locations, Station.ESPANNA, 3);
        register(ids, Station.PATRIA, 4);
        registerLocation(locations, Station.PATRIA, 4);
        register(ids, Station.ISLA_RAZA, 5);
        registerLocation(locations, Station.ISLA_RAZA, 5);
        register(ids, Station._18_DE_MARZO, 6);
        registerLocation(locations, Station._18_DE_MARZO, 6);
        register(ids, Station.URDANETA, 7);
        registerLocation(locations, Station.URDANETA, 7);
        register(ids, Station.UNIDAD_DEPORTIVA, 8);
        registerLocation(locations, Station.UNIDAD_DEPORTIVA, 8);
        register(ids, Station.SANTA_FILOMENA, 9);
        registerLocation(locations, Station.SANTA_FILOMENA, 9);
        register(ids, Station.WASHINGTON, 10);
        registerLocation(locations, Station.WASHINGTON, 10);
        register(ids, Station.MEXICALTZINGO, 11);
        registerLocation(locations, Station.MEXICALTZINGO, 11);
        register(ids, Station.JUAREZ_I, 12);
        registerLocation(locations, Station.JUAREZ_I, 12);
        register(ids, Station.REFUGIO, 13);
        registerLocation(locations, Station.REFUGIO, 13);
        register(ids, Station.MEZQUITAN, 14);
        registerLocation(locations, Station.MEZQUITAN, 14);
        register(ids, Station.AVILA_CAMACHO, 15);
        registerLocation(locations, Station.AVILA_CAMACHO, 15);
        register(ids, Station.DIVISION_DEL_NORTE, 16);
        registerLocation(locations, Station.DIVISION_DEL_NORTE, 16);
        register(ids, Station.ATEMAJAC, 17);
        registerLocation(locations, Station.ATEMAJAC, 17);
        register(ids, Station.DERMATOLOGICO, 18);
        registerLocation(locations, Station.DERMATOLOGICO, 18);
        register(ids, Station.PERIFERICO_NORTE, 19);
        registerLocation(locations, Station.PERIFERICO_NORTE, 19);
        register(ids, Station.AUDITORIO, 30);
        registerLocation(locations, Station.AUDITORIO, 30);

        register(ids, Station.TETLAN, 20);
        registerLocation(locations, Station.TETLAN, 20);
        register(ids, Station.LA_AURORA, 21);
        registerLocation(locations, Station.LA_AURORA, 21);
        register(ids, Station.SAN_JACINTO, 22);
        registerLocation(locations, Station.SAN_JACINTO, 22);
        register(ids, Station.SAN_ANDRES, 23);
        registerLocation(locations, Station.SAN_ANDRES, 23);
        register(ids, Station.CRISTOBAL_DE_ONNATE, 24);
        registerLocation(locations, Station.CRISTOBAL_DE_ONNATE, 24);
        register(ids, Station.OBLATOS, 25);
        registerLocation(locations, Station.OBLATOS, 25);
        register(ids, Station.BELISARIO_DOMINGUEZ, 26);
        registerLocation(locations, Station.BELISARIO_DOMINGUEZ, 26);
        register(ids, Station.SAN_JUAN_DE_DIOS, 27);
        registerLocation(locations, Station.SAN_JUAN_DE_DIOS, 27);
        register(ids, Station.PLAZA_UNIVERSIDAD, 28);
        registerLocation(locations, Station.PLAZA_UNIVERSIDAD, 28);
        register(ids, Station.JUAREZ_II, 29);
        registerLocation(locations, Station.JUAREZ_II, 29);

        register(ids, Station.CENTRAL_DE_AUTOBUSES, 31);
        registerLocation(locations, Station.CENTRAL_DE_AUTOBUSES, 31);
        register(ids, Station.LAZARO_CARDENAS, 32);
        registerLocation(locations, Station.LAZARO_CARDENAS, 32);
        register(ids, Station.TLAQUEPAQUE_CENTRO, 33);
        registerLocation(locations, Station.TLAQUEPAQUE_CENTRO, 33);
        register(ids, Station.RIO_NILO, 34);
        registerLocation(locations, Station.RIO_NILO, 34);
        register(ids, Station.REVOLUCION, 35);
        registerLocation(locations, Station.REVOLUCION, 35);
        register(ids, Station.CUCEI, 36);
        registerLocation(locations, Station.CUCEI, 36);
        register(ids, Station.PLAZA_DE_LA_BANDERA, 37);
        registerLocation(locations, Station.PLAZA_DE_LA_BANDERA, 37);
        register(ids, Station.INDEPENDENCIA, 38);
        registerLocation(locations, Station.INDEPENDENCIA, 38);
        register(ids, Station.GUADALAJARA_CENTRO, 39);
        registerLocation(locations, Station.GUADALAJARA_CENTRO, 39);
        register(ids, Station.SANTUARIO, 40);
        registerLocation(locations, Station.SANTUARIO, 40);
        register(ids, Station.LA_NORMAL, 41);
        registerLocation(locations, Station.LA_NORMAL, 41);
        register(ids, Station.AVILA_CAMACHO_II, 42);
        registerLocation(locations, Station.AVILA_CAMACHO_II, 42);
        register(ids, Station.CIRCUNVALACION_COUNTRY, 43);
        registerLocation(locations, Station.CIRCUNVALACION_COUNTRY, 43);
        register(ids, Station.PLAZA_PATRIA, 44);
        registerLocation(locations, Station.PLAZA_PATRIA, 44);
        register(ids, Station.ZAPOPAN_CENTRO, 45);
        registerLocation(locations, Station.ZAPOPAN_CENTRO, 45);
        register(ids, Station.MERCADO_DEL_MAR, 46);
        registerLocation(locations, Station.MERCADO_DEL_MAR, 46);
        register(ids, Station.PERIFERICO_BELENES, 47);
        registerLocation(locations, Station.PERIFERICO_BELENES, 47);
        register(ids, Station.ARCOS_DE_ZAPOPAN, 48);
        registerLocation(locations, Station.ARCOS_DE_ZAPOPAN, 48);

        register(ids, Station.LAS_JUNTAS, 49, 1051, 1061);
        registerLocation(locations, Station.LAS_JUNTAS, 49);
        //register(ids, Station.LOMAS_DEL_CUATRO, 57); // Fill-in station, still unbuilt
        //registerLocation(locations, Station.LOMAS_DEL_CUATRO, 57);
        register(ids, Station.JALISCO_200_ANNOS, 50, 1053, 1047);
        registerLocation(locations, Station.JALISCO_200_ANNOS, 50);
        register(ids, Station.REAL_DEL_VALLE, 51, 1042, 1046);
        registerLocation(locations, Station.REAL_DEL_VALLE, 51);
        register(ids, Station.CONCEPCION_DEL_VALLE, 52, 1058, 1082); // Find out if 1082 is correct
        registerLocation(locations, Station.CONCEPCION_DEL_VALLE, 52);
        register(ids, Station.EL_CUERVO, 53, 1055, 1056);
        registerLocation(locations, Station.EL_CUERVO, 53);
        register(ids, Station.LOMAS_DEL_SUR, 54, 1057, 1059, 1066, 1069);
        registerLocation(locations, Station.LOMAS_DEL_SUR, 54);
        register(ids, Station.CUTLAJO, 55, 1035, 1054);
        registerLocation(locations, Station.CUTLAJO, 55);
        register(ids, Station.TLAJOMULCO_CENTRO, 56, 1060, 1067);
        registerLocation(locations, Station.TLAJOMULCO_CENTRO, 56);

        register(ids, Station.CARRETERA_A_CHAPALA_II, 11, 1);
        register(ids, Station.LAS_LIEBRES, 12, 2);
        register(ids, Station.PARQUE_MONTENEGRO, 13, 3);
        register(ids, Station.LAS_TORRES, 14, 4);
        register(ids, Station.LAS_PINTITAS, 15, 5);
        register(ids, Station.LA_GIGANTERA, 16, 6);
        register(ids, Station.LA_PIEDRERA, 17, 7);
        register(ids, Station.SAN_JOSE_DEL_QUINCE, 18, 8);
        register(ids, Station.AEROPUERTO, 19, 9);

        register(ids, Station.MIRADOR, 11);
        register(ids, Station.HUENTITAN, 12);
        register(ids, Station.ZOOLOGICO, 13);
        register(ids, Station.INDEPENDENCIA_NORTE, 14);
        register(ids, Station.SAN_PATRICIO, 15);
        register(ids, Station.IGUALDAD, 16);
        register(ids, Station.MONUMENTAL, 17);
        register(ids, Station.MONTE_OLIVETE, 18);
        register(ids, Station.CIRCUNVALACION, 19);
        register(ids, Station.CIENCIAS_DE_LA_SALUD, 20);
        register(ids, Station.JUAN_ALVAREZ, 21);
        register(ids, Station.ALAMEDA, 22);
        register(ids, Station.SAN_JUAN_DE_DIOS_II, 23);
        register(ids, Station.BICENTENARIO, 24);
        register(ids, Station.LA_PAZ, 25);
        register(ids, Station.NINOS_HEROES, 26);
        register(ids, Station.AGUA_AZUL, 27);
        register(ids, Station.CIPRES, 28);
        register(ids, Station.HEROES_DE_NACOZARI, 29);
        register(ids, Station.COLON_INDUSTRIAL, 30);
        register(ids, Station.EL_DEAN, 31);
        register(ids, Station.ZONA_INDUSTRIAL, 32);
        register(ids, Station.LOPEZ_DE_LEGAZPI, 33);
        register(ids, Station.CLEMENTE_OROZCO, 34);
        register(ids, Station.ARTES_PLASTICAS, 35);
        register(ids, Station.ESCULTURAS, 36);
        register(ids, Station.FRAY_ANGELICO, 37);
        register(ids, Station.LAS_JUNTAS_II, 38);

        // TODO: Use both location and detected route.
        register(ids, Station.CARRETERA_A_CHAPALA, 1);
        register(ids, Station.LAS_PINTAS, 2, 0x43b294);
        register(ids, Station.ARTESANOS, 3);
        register(ids, Station.JALISCO_200_ANNOS_II, 0x4152b4, 0x4172b4);
        register(ids, Station.ADOLF_HORN, 4, 0x413294, 0x40d294);
        register(ids, Station.TOLUQUILLA, 5);
        register(ids, Station._8_DE_JULIO, 6);
        register(ids, Station.SAN_SEBASTIANITO, 7);
        register(ids, Station.PERIFERICO_SUR_II, 8, 0x42f284, 0x431284, 0x432274, 0x429294);
        register(ids, Station.TERMINAL_SUR_DE_AUTOBUSES, 9);
        register(ids, Station.ITESO, 10);
        register(ids, Station.LOPEZ_MATEOS, 11, 0x439284);
        register(ids, Station.AGRICOLA, 12);
        register(ids, Station.EL_BRISENNO, 13);
        register(ids, Station.MARIANO_OTERO, 14);
        register(ids, Station.MIRAMAR, 15);
        register(ids, Station.FELIPE_RUVALCABA, 16);
        register(ids, Station.EL_COLLI, 17, 0x413284);
        register(ids, Station.CHAPALITA_INN, 18, 0x40d284, 0x40e284); // confirm ids 0x40c284 and 0x40f284

        register(ids, Station.PARQUE_METROPOLITANO, 19, 0x40a284);  // confirm id 0x40b284
        register(ids, Station.CIUDAD_GRANJA, 20);
        register(ids, Station.CIUDAD_JUDICIAL, 21);
        register(ids, Station.ESTADIO_CHIVAS, 22);
        register(ids, Station.VALLARTA, 23);
        register(ids, Station.SAN_JUAN_DE_OCOTAN, 24);
        register(ids, Station._5_DE_MAYO, 25);
        register(ids, Station.ACUEDUCTO, 26);
        register(ids, Station.SANTA_MARGARITA, 27);
        register(ids, Station.LA_TUZANIA, 28);

        register(ids, Station.PERIFERICO_BELENES_II, 29, 0x436274, 0x4242b4);
        register(ids, Station.SAN_ISIDRO, 30);
        register(ids, Station.CENTRO_CULTURAL_UNIVERSITARIO, 31, 0x42b2b4, 0x42d2b4);
        register(ids, Station.CONSTITUCION, 32);
        register(ids, Station.TABACHINES, 33);
        register(ids, Station.LA_CANTERA, 34);
        register(ids, Station.PERIFERICO_NORTE_II, 35, 0x42b284); // Another validator missing here
        register(ids, Station.EL_BATAN, 36);
        register(ids, Station.LA_EXPERIENCIA, 37);
        register(ids, Station.RANCHO_NUEVO, 38);
        register(ids, Station.LOMAS_DEL_PARAISO, 39);
        register(ids, Station.INDEPENDENCIA_NORTE_II, 40, 0x417284, 0x418284);
        register(ids, Station.ARENA_GUADALAJARA, 41);
        register(ids, Station.BARRANCA_DE_HUENTITAN, 42);
        //register(ids, Station.VICENTE_FERNANDEZ, 43);  // doesn't exist yet
        // TODO: Verify these locations
        register(ids, Station.COMISARIA_DE_GUADALAJARA, 44);
        register(ids, Station.BETANIA, 45);
        register(ids, Station.COLONIA_JALISCO, 46);
        register(ids, Station.ZAPOTLANEJO, 47);
        register(ids, Station.LOS_CONEJOS, 48);

        ID_MAP = freeze(ids);
        TRAIN_ID_MAP = freezeTrainIds(ids);
        LOCATION_MAP = freezeLocations(locations);
    }

    private StationMapper() {
    }

    public static Station fromId(int id, Route route) {
        return fromId(id, route != null ? route.getTransportType() : null, route);
    }

    public static Station fromInt(int id, Route route) {
        return fromId(id, route);
    }

    public static Station fromLocation(int locationId) {
        return LOCATION_MAP.get(locationId);
    }

    public static Station getLocation(Event event) {
        if (event == null) {
            return null;
        }
        int location = event.getLocationId();

        if (event.getOperator() == Operator.MI_MACRO_CALZADA) {
            int stationId = event.getDeviceId() % DIVIDER_ROUTE_6_LOCATION;
            return fromId(stationId, Route.LINE_6);
        } else if (event.getOperator() == Operator.MI_MACRO_PERIFERICO_TRONCAL) {
            return fromId(location, Route.LINE_7);
        } else if ((event.getSamId() & 0xFFFFF) == 0xD7A80 && location != 0) {  // Is it a SamUID from a Line 4 VRT?
            if (event.getRouteId() == 0)
                return fromLocation(location + 48);
            else
                return fromId(location, Route.LINE_5);
        }

        return fromLocation(location);
    }

    public static int getValidator(Event event) {
        if (event == null) {
            return 0;
        }

        TransportType transportType = event.getTransportType();
        if (transportType == TransportType.TRAIN) {
            Route route = getRoute(event);
            int rawValidator = Math.abs(event.getDeviceId() % DIVIDER_ROUTES_1_2_3);

            if (route == Route.LINE_4) {
                return rawValidator + 1;
            } else {
                return rawValidator;
            }
        } else if (transportType == TransportType.BRT) {
            if (event.getOperator() == Operator.MI_MACRO_PERIFERICO_TRONCAL) {
                return event.getDeviceId() + 1;
            } else {
                return (event.getDeviceId() / 100) % 10;
            }
        } else {
            // TODO: Handle BEA cases like route 114, device ID 11407
            return event.getDeviceId();
        }
    }

    public static int getStationId(Event event) {
        if (event == null) {
            return 0;
        }

        TransportType transportType = event.getTransportType();
        if (transportType == TransportType.TRAIN) {
            Route route = getRawRoute(event);
            if (route == Route.LINE_4) {
                return (int) (event.getSamId() / DIVIDER_ROUTE_4); // 0x2C42294F80L?
            } else {
                return event.getDeviceId() / DIVIDER_ROUTES_1_2_3;
            }
        } else if (transportType == TransportType.BRT) {
            if (event.getOperator() == Operator.MI_MACRO_PERIFERICO_TRONCAL) {
                return (int) (event.getSamId() >> 28); // 0x43B2BFA4D7A80;
            } else {
                return event.getDeviceId() % 100;
            }
        } else {
            return 0;
        }
    }

    public static Station getStation(Event event) {
        if (event == null) {
            return null;
        }

        return fromId(getStationId(event), event.getTransportType(), getRawRoute(event));
    }

    public static Terminal getTerminal(Event event) {
        if (event == null) {
            return null;
        }

        Route route = getRoute(event);
        Terminal terminal;
        int divider = route == Route.LINE_6 ? DIVIDER_ROUTE_6 : DIVIDER_ROUTES_1_2_3;
        int validator = Math.abs(event.getDeviceId() % divider);

        if (route != null) {
            switch (route) {
                case LINE_1:
                    terminal = validator >= ROUTE_1_2_TERMINAL_OFFSET ? Terminal.PERIFERICO_SUR : Terminal.AUDITORIO;
                    break;
                case LINE_2:
                    terminal = validator >= ROUTE_1_2_TERMINAL_OFFSET ? Terminal.JUAREZ : Terminal.TETLAN;
                    break;
                case LINE_3:
                    terminal = validator >= ROUTE_1_2_TERMINAL_OFFSET ? Terminal.CENTRAL_DE_AUTOBUSES : Terminal.ARCOS_DE_ZAPOPAN;
                    break;
                case LINE_6:
                    terminal = validator == 21 ? Terminal.MIRADOR : Terminal.LAS_JUNTAS_II;
                    break;
                default:
                    terminal = null;
                    break;
            }

            if (terminal != null && terminal.station == getStation(event)) {
                return Terminal.opposite(terminal);
            }
        } else {
            terminal = null;
        }
        return terminal;
    }

    private static Route getRawRoute(Event event) {
        TransportType transportType = event.getTransportType();

        // TODO: Normalize these route exceptions with a DB migration.
        if (transportType == TransportType.TRAIN) {
            return RouteMapper.fromId(event.getRouteId(), TransportType.TRAIN);
        } else if (transportType == TransportType.BRT) {
            Operator operator = event.getOperator();
            if (operator == Operator.MI_MACRO_CALZADA) {
                return Route.LINE_6;
            } else if (operator == Operator.MI_MACRO_PERIFERICO_TRONCAL) {
                return Route.LINE_7;
            } else {
                return Route.LINE_5;
            }
        }
        return null;
    }

    public static Route getRoute(Event event) {
        if (event == null) {
            return null;
        }
        Station station = getStation(event);
        if (station != null) {
            return station.getRoute();
        } else {
            return getRawRoute(event);
        }
    }

    private static Station fromId(int id, TransportType transportType, Route route) {
        if (transportType == TransportType.TRAIN) {
            Station trainStation = TRAIN_ID_MAP.get(id);
            if (trainStation != null) {
                return trainStation;
            }
        }

        return find(ID_MAP, id, route);
    }

    private static Station find(Map<Key, Station> map, int value, Route route) {
        if (route == null) {
            return null;
        }

        return map.get(new Key(value, route));
    }

    private static Map<Integer, Station> freezeTrainIds(Map<Key, Station> source) {
        Map<Integer, Station> trainIds = new LinkedHashMap<>();
        for (Map.Entry<Key, Station> entry : source.entrySet()) {
            Station station = entry.getValue();
            if (station.getTransportType() != TransportType.TRAIN) {
                continue;
            }

            Station previous = trainIds.put(entry.getKey().value, station);
            if (previous != null) {
                throw new IllegalStateException(
                        "Duplicate train station mapping for " + entry.getKey().value + ": " +
                                previous + " and " + station);
            }
        }
        return Collections.unmodifiableMap(trainIds);
    }

    private static void register(Map<Key, Station> map, Station station, int... values) {
        for (int value : values) {
            Key key = new Key(value, station.getRoute());
            Station previous = map.put(key, station);
            if (previous != null) {
                throw new IllegalStateException(
                        "Duplicate station mapping for " + key + ": " + previous + " and " + station);
            }
        }
    }

    private static void registerLocation(Map<Integer, Station> map, Station station, int... values) {
        for (int value : values) {
            Station previous = map.put(value, station);
            if (previous != null) {
                throw new IllegalStateException(
                        "Duplicate station location mapping for " + value + ": " +
                                previous + " and " + station);
            }
        }
    }

    private static Map<Key, Station> freeze(Map<Key, Station> source) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }

    private static Map<Integer, Station> freezeLocations(Map<Integer, Station> source) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }

    private static final class Key {
        final int value;
        final Route route;

        Key(int value, Route route) {
            this.value = value;
            this.route = route;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Key)) {
                return false;
            }
            Key key = (Key) other;
            return value == key.value && route == key.route;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, route);
        }

        @Override
        public String toString() {
            return "Key{value=" + value + ", route=" + route + "}";
        }
    }
}
