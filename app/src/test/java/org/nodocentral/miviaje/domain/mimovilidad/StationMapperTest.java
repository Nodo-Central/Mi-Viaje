package org.nodocentral.miviaje.domain.mimovilidad;

import org.junit.Test;
import org.nodocentral.miviaje.domain.mimovilidad.card.Event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class StationMapperTest {
    @Test
    public void fromIdResolvesUniqueStation() {
        assertEquals(
                Station.PERIFERICO_SUR,
                StationMapper.fromId(1, Route.LINE_1)
        );
    }

    @Test
    public void fromIntAliasesFromId() {
        assertEquals(
                StationMapper.fromId(1, Route.LINE_6),
                StationMapper.fromInt(1, Route.LINE_6)
        );
    }

    @Test
    public void routeDisambiguatesCrossRouteCollisions() {
        assertEquals(
                Station.REVOLUCION,
                StationMapper.fromId(35, Route.LINE_3)
        );
        assertEquals(
                Station.REVOLUCION,
                StationMapper.fromId(35, Route.LINE_1)
        );
        assertEquals(
                Station.ARTES_PLASTICAS,
                StationMapper.fromId(35, Route.LINE_6)
        );
    }

    @Test
    public void trainRoutesResolveAnyTrainStationId() {
        assertEquals(
                Station.TETLAN,
                StationMapper.fromId(20, Route.LINE_1)
        );
    }

    @Test
    public void trainEventsResolveStationEvenWhenRouteIsUnknown() {
        assertEquals(
                Station.TETLAN,
                StationMapper.getStation(trainEvent(9999, 20005))
        );
    }

    @Test
    public void brtRoutesStillUseTheirOwnStationIds() {
        assertEquals(
                Station.CIUDAD_GRANJA,
                StationMapper.fromId(20, Route.LINE_7)
        );
    }

    @Test
    public void aliasesResolveOnRouteFour() {
        assertEquals(
                Station.LAS_JUNTAS,
                StationMapper.fromId(1051, Route.LINE_4)
        );
        assertEquals(
                Station.JALISCO_200_ANNOS,
                StationMapper.fromId(1053, Route.LINE_4)
        );
    }

    @Test
    public void locationLookupDoesNotRequireRoute() {
        assertEquals(
                Station.PERIFERICO_SUR,
                StationMapper.fromLocation(1)
        );
    }

    @Test
    public void unknownValuesReturnNull() {
        assertNull(StationMapper.fromId(9999, Route.LINE_7));
        assertNull(StationMapper.fromLocation(9999));
        assertNull(StationMapper.fromId(1, Route.LINE_5));
        assertNull(StationMapper.fromId(1, null));
    }

    private Event trainEvent(int routeId, int deviceId) {
        return new Event(
                0,
                0,
                null,
                0,
                null,
                Event.Type.PRODUCT_USE,
                0,
                0,
                0,
                0,
                deviceId,
                0,
                Event.TransportType.TRAIN,
                routeId,
                0,
                0,
                0,
                Event.RefundReason.NO_REFUND,
                Event.DeviceType.UNSPECIFIED
        );
    }
}
