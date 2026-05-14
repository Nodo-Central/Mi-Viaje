package org.nodocentral.miviaje.domain.mimovilidad;

import org.nodocentral.miviaje.domain.mimovilidad.card.Event.TransportType;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class RouteMapper {
    private static final Map<Key, Route> ID_MAP;
    private static final Map<OperatorKey, Route> OPERATOR_ID_MAP;

    static {
        Map<Key, Route> ids = new LinkedHashMap<>();
        Map<OperatorKey, Route> operatorIds = new LinkedHashMap<>();

        registerTrainRoutes(ids);
        registerTrainFeederBusRoutes(ids);
        registerBrtRoutes(ids);
        registerBrtFeederBusRoutes(ids, operatorIds);
        registerBusRoutes(ids, operatorIds);

        ID_MAP = freeze(ids);
        OPERATOR_ID_MAP = freezeOperatorIds(operatorIds);
    }

    private RouteMapper() {
    }

    public static Route fromId(int routeId, TransportType transportType) {
        return find(Operator.UNSPECIFIED, routeId, 0, transportType);
    }

    public static Route fromId(int operatorId, int routeId, int deviceId, TransportType transportType) {
        return find(operatorId, routeId, deviceId, transportType);
    }

    public static Route fromId(Operator operator, int routeId, int deviceId, TransportType transportType) {
        return find(operator, routeId, deviceId, transportType);
    }

    public static Route fromInt(int operatorId, int routeId, int deviceId, TransportType transportType) {
        return fromId(operatorId, routeId, deviceId, transportType);
    }

    public static Route fromInt(Operator operator, int routeId, int deviceId, TransportType transportType) {
        return fromId(operator, routeId, deviceId, transportType);
    }

    private static void registerTrainRoutes(Map<Key, Route> ids) {
        registerId(ids, Route.LINE_1, 1);
        registerId(ids, Route.LINE_2, 2);
        registerId(ids, Route.LINE_3, 8);
        registerId(ids, Route.LINE_4, 4);
        registerId(ids, Route.LINE_4, 15);
    }

    private static void registerTrainFeederBusRoutes(Map<Key, Route> ids) {
        registerId(ids, Route.PRETREN_1, 1);
        registerId(ids, Route.PRETREN_1_B, 9);
        registerId(ids, Route.PRETREN_1, 3);
        registerId(ids, Route.PRETREN_2, 4); // Went to Tetlán and it was 4, not 2
        //registerId(ids, Route.PRETREN_3, 3); // Shares 3 as well; check devId 12XXX instead
        registerId(ids, Route.PRETREN_4, 6);  // Go to Belenes and check
        registerId(ids, Route.PRETREN_5, 5);  // 5-B uses just 5 as well
        registerId(ids, Route.PRETREN_6, 10); // Verify near La Normal or Ixcatán
        registerId(ids, Route.PRETREN_7, 11); // Same as Route 6
        //registerId(ids, Route.PRETREN_8, 13); // Doesn't event operate yet
        registerId(ids, Route.PRETREN_9, 12);

        // All of these have been verified
        registerId(ids, Route.PRETREN_A01, 16);
        registerId(ids, Route.PRETREN_A02, 17);
        registerId(ids, Route.PRETREN_A03, 18);
        registerId(ids, Route.PRETREN_A04, 19);
        registerId(ids, Route.PRETREN_A05, 20);
    }

    private static void registerBrtRoutes(Map<Key, Route> ids) {
        registerId(ids, Route.MP_T01, 1);
        registerId(ids, Route.MP_T02, 2);
        registerId(ids, Route.MP_T03, 3);
    }

    private static void registerBrtFeederBusRoutes(Map<Key, Route> ids,
                                                   Map<OperatorKey, Route> operatorIds) {
        registerOperatorId(operatorIds, Route.MC_A03, 1, Operator.MI_MACRO_CALZADA);
        registerOperatorId(operatorIds, Route.MC_A05, 2, Operator.MI_MACRO_CALZADA);
        registerOperatorId(operatorIds, Route.MC_A06, 3, Operator.MI_MACRO_CALZADA);
        registerOperatorId(operatorIds, Route.MC_A07, 4, Operator.MI_MACRO_CALZADA);
        registerOperatorId(operatorIds, Route.MC_A08, 5, Operator.MI_MACRO_CALZADA);
        registerOperatorId(operatorIds, Route.MC_A09, 6, Operator.MI_MACRO_CALZADA);
        registerOperatorId(operatorIds, Route.MC_A10, 7, Operator.MI_MACRO_CALZADA);
        registerOperatorId(operatorIds, Route.MC_A11, 8, Operator.MI_MACRO_CALZADA);
        registerOperatorId(operatorIds, Route.MC_A12, 9, Operator.MI_MACRO_CALZADA);
        registerOperatorId(operatorIds, Route.MC_A13, 10, Operator.MI_MACRO_CALZADA);
        registerOperatorId(operatorIds, Route.MC_A14, 11, Operator.MI_MACRO_CALZADA);
        registerOperatorId(operatorIds, Route.MC_A15, 12, Operator.MI_MACRO_CALZADA);
        registerOperatorId(operatorIds, Route.MC_A16, 13, Operator.MI_MACRO_CALZADA);
        registerOperatorId(operatorIds, Route.MC_A17, 14, Operator.MI_MACRO_CALZADA);
        registerOperatorId(operatorIds, Route.MC_A18, 15, Operator.MI_MACRO_CALZADA);
        registerOperatorId(operatorIds, Route.MC_A19, 16, Operator.MI_MACRO_CALZADA);
        registerOperatorId(operatorIds, Route.MC_A20, 17, Operator.MI_MACRO_CALZADA);
        registerOperatorId(operatorIds, Route.MC_A21, 18, Operator.MI_MACRO_CALZADA);
        registerOperatorId(operatorIds, Route.MC_A22, 19, Operator.MI_MACRO_CALZADA);

        registerOperatorId(operatorIds, Route.MP_C01, 4, Operator.MI_MACRO_PERIFERICO_COMPLEMENTARIO);
        registerOperatorId(operatorIds, Route.MP_C02, 5, Operator.MI_MACRO_PERIFERICO_COMPLEMENTARIO);
        registerOperatorId(operatorIds, Route.MP_C03, 6, Operator.MI_MACRO_PERIFERICO_COMPLEMENTARIO);
        registerOperatorId(operatorIds, Route.MP_A01, 7, Operator.MI_MACRO_PERIFERICO_COMPLEMENTARIO);
        registerOperatorId(operatorIds, Route.MP_A02, 8, Operator.MI_MACRO_PERIFERICO_COMPLEMENTARIO);
        registerOperatorId(operatorIds, Route.MP_A03, 9, Operator.MI_MACRO_PERIFERICO_COMPLEMENTARIO);
        registerOperatorId(operatorIds, Route.MP_A04, 10, Operator.MI_MACRO_PERIFERICO_COMPLEMENTARIO);
        registerOperatorId(operatorIds, Route.MP_A05_1, 11, Operator.MI_MACRO_PERIFERICO_COMPLEMENTARIO);
        registerOperatorId(operatorIds, Route.MP_A05_1, 51, 39754);
        registerOperatorId(operatorIds, Route.MP_A05_2, 12, Operator.MI_MACRO_PERIFERICO_COMPLEMENTARIO);
        registerOperatorId(operatorIds, Route.MP_A05_2, 52, Operator.MI_MACRO_PERIFERICO_COMPLEMENTARIO);
        registerOperatorId(operatorIds, Route.MP_A06, 13, Operator.MI_MACRO_PERIFERICO_COMPLEMENTARIO);
        registerOperatorId(operatorIds, Route.MP_A06, 106, Operator.BEA_V2);  // Reported as 106 without operator 1060
        registerOperatorId(operatorIds, Route.MP_A07, 14, Operator.MI_MACRO_PERIFERICO_COMPLEMENTARIO);
        registerOperatorId(operatorIds, Route.MP_A08, 15, Operator.MI_MACRO_PERIFERICO_COMPLEMENTARIO);
        registerOperatorId(operatorIds, Route.MP_A09, 16, Operator.MI_MACRO_PERIFERICO_COMPLEMENTARIO);
        registerOperatorId(operatorIds, Route.MP_A10, 17, Operator.MI_MACRO_PERIFERICO_COMPLEMENTARIO);
        registerOperatorId(operatorIds, Route.MP_A11, 18, Operator.MI_MACRO_PERIFERICO_COMPLEMENTARIO);
        registerOperatorId(operatorIds, Route.MP_A12, 19, Operator.MI_MACRO_PERIFERICO_COMPLEMENTARIO);
        registerOperatorId(operatorIds, Route.MP_A13, 20, Operator.MI_MACRO_PERIFERICO_COMPLEMENTARIO);

        registerOperatorId(operatorIds, Route.LM_V01, 1, Operator.RUTA_LOPEZ_MATEOS);
        registerOperatorId(operatorIds, Route.LM_V02, 2, Operator.RUTA_LOPEZ_MATEOS);
        registerOperatorId(operatorIds, Route.LM_V03, 3, Operator.RUTA_LOPEZ_MATEOS);
        registerOperatorId(operatorIds, Route.LM_V04, 4, Operator.RUTA_LOPEZ_MATEOS);
        registerOperatorId(operatorIds, Route.LM_V05, 5, Operator.RUTA_LOPEZ_MATEOS);
        registerOperatorId(operatorIds, Route.LM_C01, 6, Operator.RUTA_LOPEZ_MATEOS);
        registerOperatorId(operatorIds, Route.LM_C02, 7, Operator.RUTA_LOPEZ_MATEOS);
        registerOperatorId(operatorIds, Route.LM_C03, 8, Operator.RUTA_LOPEZ_MATEOS);
    }

    private static void registerBusRoutes(Map<Key, Route> ids,
                                          Map<OperatorKey, Route> operatorIds) {
        registerId(ids, Route.C14, 175);
        registerId(ids, Route.C40_41, 4041);
        registerId(ids, Route.C52_87, 5287);
        registerId(ids, Route.C67_1, 671);
        registerId(ids, Route.C67_2, 672);
        registerOperatorId(operatorIds, Route.C111_V2, 2, Operator.C111);
        registerId(ids, Route.C114_V1, 1141);

        registerOperatorId(operatorIds, Route.T01, 100, Operator.ALIANZA_DE_CAMIONEROS);
        registerOperatorId(operatorIds, Route.T06_2, 113, Operator.T06);
        registerId(ids, Route.T08, 8);
        registerOperatorId(operatorIds, Route.T09_O, 1, Operator.T09);
        registerOperatorId(operatorIds, Route.T09_B, 2, Operator.T09);
        registerOperatorId(operatorIds, Route.T11_1, 11, Operator.T11_1);
        registerOperatorId(operatorIds, Route.T11_2, 11, Operator.T11_2);
        registerOperatorId(operatorIds, Route.T13A, 1301, Operator.TRANSBUS);
        registerOperatorId(operatorIds, Route.T15, 15, Operator.T15);
        registerOperatorId(operatorIds, Route.T15, 1501, Operator.TISA);
        registerOperatorId(operatorIds, Route.T18A, 18, 1, 255, 1013);
        registerOperatorId(operatorIds, Route.T18A, 1801, 63);
        registerOperatorId(operatorIds, Route.T18B, 1801, Operator.TISA);
        registerOperatorId(operatorIds, Route.T21_C01, 211, Operator.BEA_V2);
    }

    private static Route find(int operatorId, int routeId, int deviceId, TransportType transportType) {
        if (transportType == null) {
            return null;
        }

        // PreTren 3 shares route id 3, so identify it by validator range.
        if (transportType == TransportType.TRAIN_FEEDER_BUS && deviceId / 1000 == 12) {
            return Route.PRETREN_3;
        }

        Route route = OPERATOR_ID_MAP.get(new OperatorKey(operatorId, routeId, transportType));
        if (route != null) {
            return route;
        }

        return ID_MAP.get(new Key(routeId, transportType));
    }

    private static Route find(Operator operator, int routeId, int deviceId, TransportType transportType) {
        int operatorId = operator != null ? operator.getValue() : Operator.UNSPECIFIED.getValue();
        return find(operatorId, routeId, deviceId, transportType);
    }

    private static void registerId(Map<Key, Route> map, Route route, int routeId) {
        Key key = new Key(routeId, route.getTransportType());
        Route previous = map.put(key, route);
        if (previous != null) {
            throw new IllegalStateException(
                    "Duplicate route mapping for " + key + ": " + previous + " and " + route);
        }
    }

    private static void registerOperatorId(Map<OperatorKey, Route> map,
                                           Route route,
                                           int routeId,
                                           int... operatorIds) {
        for (int operatorId : operatorIds) {
            registerOperatorKey(map, route, operatorId, routeId);
        }
    }

    private static void registerOperatorId(Map<OperatorKey, Route> map,
                                           Route route,
                                           int routeId,
                                           Operator... operators) {
        for (Operator operator : operators) {
            if (operator == null) {
                throw new IllegalArgumentException("operator must not be null");
            }
            registerOperatorKey(map, route, operator.getValue(), routeId);
        }
    }

    private static void registerOperatorKey(Map<OperatorKey, Route> map,
                                            Route route,
                                            int operatorId,
                                            int routeId) {
        OperatorKey key = new OperatorKey(operatorId, routeId, route.getTransportType());
        Route previous = map.put(key, route);
        if (previous != null) {
            throw new IllegalStateException(
                    "Duplicate operator route mapping for " + key + ": " + previous + " and " + route);
        }
    }

    private static Map<Key, Route> freeze(Map<Key, Route> source) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }

    private static Map<OperatorKey, Route> freezeOperatorIds(Map<OperatorKey, Route> source) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }

    private static final class Key {
        final int routeId;
        final TransportType transportType;

        Key(int routeId, TransportType transportType) {
            this.routeId = routeId;
            this.transportType = transportType;
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
            return routeId == key.routeId && transportType == key.transportType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(routeId, transportType);
        }

        @Override
        public String toString() {
            return "Key{routeId=" + routeId + ", transportType=" + transportType + "}";
        }
    }

    private static final class OperatorKey {
        final int operatorId;
        final int routeId;
        final TransportType transportType;

        OperatorKey(int operatorId, int routeId, TransportType transportType) {
            this.operatorId = operatorId;
            this.routeId = routeId;
            this.transportType = transportType;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof OperatorKey)) {
                return false;
            }
            OperatorKey key = (OperatorKey) other;
            return operatorId == key.operatorId &&
                    routeId == key.routeId &&
                    transportType == key.transportType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(operatorId, routeId, transportType);
        }

        @Override
        public String toString() {
            return "OperatorKey{operatorId=" + operatorId +
                    ", routeId=" + routeId +
                    ", transportType=" + transportType + "}";
        }
    }

}
