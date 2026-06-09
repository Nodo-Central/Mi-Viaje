package org.nodocentral.miviaje.domain.mimovilidad;

import org.nodocentral.miviaje.domain.mimovilidad.card.Event.TransportType;

import java.util.EnumSet;
import java.util.Objects;

public enum Route {
    NONE(-1, TransportType.UNSPECIFIED),
    // Train
    LINE_1(1, TransportType.TRAIN, true),
    LINE_2(2, TransportType.TRAIN, true),
    LINE_3(3, TransportType.TRAIN, true),
    LINE_4(4, TransportType.TRAIN, true),
    LINE_5(5, TransportType.BRT, true),
    LINE_6(6, TransportType.BRT, true),
    LINE_7(7, TransportType.BRT, true),

    // Train feeder
    PRETREN_1(1, TransportType.TRAIN_FEEDER_BUS, true),
    PRETREN_1_B(1, "-B", TransportType.TRAIN_FEEDER_BUS, true),
    PRETREN_2(2, TransportType.TRAIN_FEEDER_BUS, true),
    PRETREN_3(3, TransportType.TRAIN_FEEDER_BUS, true),
    PRETREN_4(4, TransportType.TRAIN_FEEDER_BUS, true),
    PRETREN_5(5, TransportType.TRAIN_FEEDER_BUS, true),
    PRETREN_6(6, TransportType.TRAIN_FEEDER_BUS, true),
    PRETREN_7(7, TransportType.TRAIN_FEEDER_BUS, true),
    PRETREN_8(8, TransportType.TRAIN_FEEDER_BUS, true),
    PRETREN_9(9, TransportType.TRAIN_FEEDER_BUS, true),
    PRETREN_A01(1, TransportType.TRAIN_FEEDER_BUS),
    PRETREN_A02(2, TransportType.TRAIN_FEEDER_BUS),
    PRETREN_A03(3, TransportType.TRAIN_FEEDER_BUS),
    PRETREN_A04(4, TransportType.TRAIN_FEEDER_BUS),
    PRETREN_A05(5, TransportType.TRAIN_FEEDER_BUS),

    // Mi Macro Calzada
    MC_A03(3, TransportType.BRT_FEEDER_BUS),
    MC_A05(5, TransportType.BRT_FEEDER_BUS),
    MC_A06(6, TransportType.BRT_FEEDER_BUS),
    MC_A07(7, TransportType.BRT_FEEDER_BUS),
    MC_A08(8, TransportType.BRT_FEEDER_BUS),
    MC_A09(9, TransportType.BRT_FEEDER_BUS),
    MC_A10(10, TransportType.BRT_FEEDER_BUS),
    MC_A11(11, TransportType.BRT_FEEDER_BUS),
    MC_A12(12, TransportType.BRT_FEEDER_BUS),
    MC_A13(13, TransportType.BRT_FEEDER_BUS),
    MC_A14(14, TransportType.BRT_FEEDER_BUS),
    MC_A15(15, TransportType.BRT_FEEDER_BUS),
    MC_A16(16, TransportType.BRT_FEEDER_BUS),
    MC_A17(17, TransportType.BRT_FEEDER_BUS),
    MC_A18(18, TransportType.BRT_FEEDER_BUS),
    MC_A19(19, TransportType.BRT_FEEDER_BUS),
    MC_A20(20, TransportType.BRT_FEEDER_BUS),
    MC_A21(21, TransportType.BRT_FEEDER_BUS),
    MC_A22(22, TransportType.BRT_FEEDER_BUS),

    // Mi Macro Periférico
    MP_T01(1, TransportType.BRT, true),
    MP_T02(2, TransportType.BRT, true),
    MP_T03(3, TransportType.BRT, true),
    MP_T04(4, TransportType.BRT, true),
    MP_C01(1, TransportType.BRT_FEEDER_BUS, true),
    MP_C02(2, TransportType.BRT_FEEDER_BUS, true),
    MP_C03(3, TransportType.BRT_FEEDER_BUS, true),
    MP_A01(1, TransportType.BRT_FEEDER_BUS),
    MP_A02(2, TransportType.BRT_FEEDER_BUS),
    MP_A03(3, TransportType.BRT_FEEDER_BUS),
    MP_A04(4, TransportType.BRT_FEEDER_BUS),
    MP_A05_1(5, "-1", TransportType.BRT_FEEDER_BUS),
    MP_A05_2(5, "-2", TransportType.BRT_FEEDER_BUS),
    MP_A06(6, TransportType.BRT_FEEDER_BUS),
    MP_A07(7, TransportType.BRT_FEEDER_BUS),
    MP_A08(8, TransportType.BRT_FEEDER_BUS),
    MP_A09(9, TransportType.BRT_FEEDER_BUS),
    MP_A10(10, TransportType.BRT_FEEDER_BUS),
    MP_A11(11, TransportType.BRT_FEEDER_BUS),
    MP_A12(12, TransportType.BRT_FEEDER_BUS),
    MP_A13(13, TransportType.BRT_FEEDER_BUS),

    // Ruta López Mateos (BRS)/BRT feeder
    LM_V01(1, TransportType.BRT_FEEDER_BUS, true),
    LM_V02(2, TransportType.BRT_FEEDER_BUS, true),
    LM_V03(3, TransportType.BRT_FEEDER_BUS, true),
    LM_V04(4, TransportType.BRT_FEEDER_BUS, true),
    LM_V05(5, TransportType.BRT_FEEDER_BUS, true),
    LM_C01(1, TransportType.BRT_FEEDER_BUS),
    LM_C02(2, TransportType.BRT_FEEDER_BUS),
    LM_C03(3, TransportType.BRT_FEEDER_BUS),

    // Colectivo Troncal
    T01(1, TransportType.BUS, true),
    T02(2, TransportType.BUS, true),
    T03(3, TransportType.BUS, true),
    T04A(4, "A", TransportType.BUS, true),
    T04B(4, "B", TransportType.BUS, true),
    // T05 is Ruta Lopez Mateos!
    T06_1(6, "-V1", TransportType.BUS, true),
    T06_2(6, "-V2", TransportType.BUS, true),
    T06_3(6, "-V3", TransportType.BUS, true),
    T07(7, TransportType.BUS, true),
    T08(8, TransportType.BUS, true),
    T09_O(9, " Oblatos", TransportType.BUS, true),
    T09_B(9, " Belisario", TransportType.BUS, true),
    T10(10, TransportType.BUS, true),
    T11_1(11, "A", TransportType.BUS, true),
    T11_2(11, "A-C01", TransportType.BUS, true),
    T11_3(11, "A-C02", TransportType.BUS, true),
    // T12 is Vallarta corridor, i.e. PreTren 1
    T13A(13, "A", TransportType.BUS, true),
    T13A_5(13, "A-C05", TransportType.BUS, true),
    T13A_6(13, "A-C06", TransportType.BUS, true),
    T13B(13, "B", TransportType.BUS, true),
    T13C(13, "C", TransportType.BUS, true),
    T14A(14, "A", TransportType.BUS, true),
    T14B(14, "B", TransportType.BUS, true),
    T15(15, TransportType.BUS, true),
    T16(16, TransportType.BUS, true),
    T17(17, TransportType.BUS, true),
    T17_B(17, "B", TransportType.BUS, true),
    T18A(18, "A", TransportType.BUS, true),
    T18B(18, "B", TransportType.BUS, true),
    // T19 now is Mi Macro Periférico!
    T20(20, TransportType.BUS, true), // It used to operate as T13D but it's actually T20 according to Imeplan's Adolf Horn corridor, however it doesn't operate anymore
    T21(21, TransportType.BUS, true),
    T21_C01(21, "-C01", TransportType.BUS, true),

    // Colectivo complementario
    /**
     * Let's try to keep this section as small as possible.
     * There are over 140 complementary routes only in Guadalajara
     * and that's without counting variants. It's unreasonable
     * to keep them all in this specific enum since it's out of scope
     * for the function it accomplishes.
     * <p>
     * This enum is meant only to tag down major routes in the city
     * as well as some odd ones that don't register their route id
     * properly in the routeId field.
     * <p>
     * It is NOT meant as a source of truth for all routes that exist
     * in the city. Only exceptional routes should be put below this comment
     * and with a very good argument, such as C14, writing 175
     * instead of 14 in its routeId field. If it already works as-is,
     * don't include it.
     */
    C14(14, TransportType.BUS),
    C25(25, TransportType.BUS),
    C40_41(40, "/41", TransportType.BUS),
    C49_50(49, "/50/51", TransportType.BUS),
    C52_87(52, "/87", TransportType.BUS),
    C67_1(67, "-V1", TransportType.BUS),
    C67_2(67, "-V2", TransportType.BUS),
    C111_V2(111, "-V2", TransportType.BUS),
    C114_V1(114, "-V1", TransportType.BUS),
    ;

    private final int id;
    private final TransportType transportType;
    private final boolean isTrunk;
    private final String variantTag;

    Route(int id, TransportType transportType) {
        this(id, transportType, false);
    }

    Route(int id, TransportType transportType, boolean isTrunk) {
        this(id, null, transportType, isTrunk);
    }

    Route(int id, String variantTag, TransportType transportType) {
        this(id, variantTag, transportType, false);
    }

    Route(int id, String variantTag, TransportType transportType, boolean isTrunk) {
        this.id = id;
        this.transportType = Objects.requireNonNull(transportType, "transportType");
        this.isTrunk = isTrunk;
        this.variantTag = variantTag;
    }

    public static EnumSet<Route> getRapidTransitLines() {
        return EnumSet.of(LINE_1, LINE_2, LINE_3, LINE_4, LINE_5, LINE_6, LINE_7);
    }

    public int getId() {
        return id;
    }

    public String getVariantTag() {
        return variantTag;
    }

    public String getVariantTagSafe() {
        return variantTag != null ? variantTag : "";
    }

    public TransportType getTransportType() {
        return transportType;
    }

    public boolean isTrunk() {
        return isTrunk;
    }
}
