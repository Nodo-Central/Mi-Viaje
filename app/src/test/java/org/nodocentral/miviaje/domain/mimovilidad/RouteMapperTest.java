package org.nodocentral.miviaje.domain.mimovilidad;

import org.junit.Test;
import org.nodocentral.miviaje.domain.mimovilidad.card.Event.TransportType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RouteMapperTest {
    private static final int UNKNOWN_DEVICE_ID = 0;
    private static final int PRETREN_3_DEVICE_ID = 12000;

    @Test
    public void routeIdentityUsesPublicRouteId() {
        assertEquals(1, Route.T01.getId());
        assertEquals(6, Route.T06_2.getId());
        assertEquals(TransportType.BUS, Route.T06_2.getTransportType());
        assertTrue(Route.T06_2.isTrunk());

        assertEquals(18, Route.T18A.getId());
        assertEquals(TransportType.BUS, Route.T18A.getTransportType());
        assertTrue(Route.T18A.isTrunk());
    }

    @Test
    public void operatorIdDisambiguatesRouteCollisionsInsideTransportType() {
        assertEquals(
                Route.MC_A03,
                RouteMapper.fromId(
                        Operator.MI_MACRO_CALZADA.getValue(),
                        1,
                        UNKNOWN_DEVICE_ID,
                        TransportType.BRT_FEEDER_BUS)
        );
        assertEquals(
                Route.LM_V01,
                RouteMapper.fromId(
                        Operator.RUTA_LOPEZ_MATEOS.getValue(),
                        1,
                        UNKNOWN_DEVICE_ID,
                        TransportType.BRT_FEEDER_BUS)
        );
        assertEquals(
                Route.MP_C01,
                RouteMapper.fromId(
                        Operator.MI_MACRO_PERIFERICO_COMPLEMENTARIO.getValue(),
                        4,
                        UNKNOWN_DEVICE_ID,
                        TransportType.BRT_FEEDER_BUS)
        );
    }

    @Test
    public void operatorAliasesResolveToTheSameOperatorAndRoutes() {
        assertEquals(Operator.RUTA_LOPEZ_MATEOS, Operator.fromInt(1070));
        assertEquals(Operator.RUTA_LOPEZ_MATEOS, Operator.fromInt(2580));
        assertTrue(Operator.RUTA_LOPEZ_MATEOS.matches(2580));

        assertEquals(
                Route.LM_V01,
                RouteMapper.fromId(2580, 1, UNKNOWN_DEVICE_ID, TransportType.BRT_FEEDER_BUS)
        );
        assertEquals(
                Route.LM_V01,
                RouteMapper.fromId(Operator.RUTA_LOPEZ_MATEOS, 1, UNKNOWN_DEVICE_ID, TransportType.BRT_FEEDER_BUS)
        );
    }

    @Test
    public void operatorIdDisambiguatesBusRoutes() {
        assertEquals(
                Route.T11_1,
                RouteMapper.fromId(224, 11, UNKNOWN_DEVICE_ID, TransportType.BUS)
        );
        assertEquals(
                Route.T11_2,
                RouteMapper.fromId(56, 11, UNKNOWN_DEVICE_ID, TransportType.BUS)
        );
        assertEquals(
                Route.T18A,
                RouteMapper.fromId(1013, 18, UNKNOWN_DEVICE_ID, TransportType.BUS)
        );
    }

    @Test
    public void routesWithoutOperatorIgnoreOperatorId() {
        assertEquals(
                Route.T06_2,
                RouteMapper.fromId(
                        78,
                        113,
                        UNKNOWN_DEVICE_ID,
                        TransportType.BUS)
        );
        assertEquals(
                Route.PRETREN_1,
                RouteMapper.fromId(
                        Operator.UNSPECIFIED.getValue(),
                        3,
                        UNKNOWN_DEVICE_ID,
                        TransportType.TRAIN_FEEDER_BUS)
        );
        assertEquals(
                Route.MP_T01,
                RouteMapper.fromId(
                        Operator.MI_MACRO_PERIFERICO_TRONCAL.getValue(),
                        1,
                        UNKNOWN_DEVICE_ID,
                        TransportType.BRT)
        );
    }

    @Test
    public void trainFeederBusDeviceIdDisambiguatesPretren3() {
        assertEquals(
                Route.PRETREN_3,
                RouteMapper.fromId(
                        Operator.UNSPECIFIED.getValue(),
                        3,
                        PRETREN_3_DEVICE_ID,
                        TransportType.TRAIN_FEEDER_BUS)
        );
    }

    @Test
    public void fromIntAliasesFromIdAndKeepsRouteCompatibility() {
        assertEquals(
                RouteMapper.fromId(Operator.TISA, 18, UNKNOWN_DEVICE_ID, TransportType.BUS),
                RouteMapper.fromInt(Operator.TISA, 18, UNKNOWN_DEVICE_ID, TransportType.BUS)
        );
    }

    @Test
    public void multipleOperatorRouteAliasesCanResolveToTheSameRoute() {
        assertEquals(
                Route.T18A,
                RouteMapper.fromId(1013, 18, UNKNOWN_DEVICE_ID, TransportType.BUS)
        );
        assertEquals(
                Route.T18A,
                RouteMapper.fromId(1, 18, UNKNOWN_DEVICE_ID, TransportType.BUS)
        );
        assertEquals(
                Route.T18A,
                RouteMapper.fromId(49, 18, 18144, TransportType.BUS)
        );
        assertEquals(
                Route.T18A,
                RouteMapper.fromId(63, 1801, UNKNOWN_DEVICE_ID, TransportType.BUS)
        );
        assertEquals(
                Route.T18B,
                RouteMapper.fromId(Operator.TISA, 1801, UNKNOWN_DEVICE_ID, TransportType.BUS)
        );
    }

    @Test
    public void sampleComplementaryRoutesCanUseRawOperatorIds() {
        assertEquals(
                Route.C25,
                RouteMapper.fromId(24, 102, 3011, TransportType.BUS)
        );
    }

    @Test
    public void unknownValuesReturnNull() {
        assertNull(RouteMapper.fromId(9999, 18, UNKNOWN_DEVICE_ID, TransportType.BUS));
        assertNull(RouteMapper.fromId((Operator) null, 18, UNKNOWN_DEVICE_ID, TransportType.BUS));
        assertNull(RouteMapper.fromId(Operator.TISA, 18, UNKNOWN_DEVICE_ID, null));
        assertNull(RouteMapper.fromId(Operator.TISA, 9999, UNKNOWN_DEVICE_ID, TransportType.BUS));
    }
}
