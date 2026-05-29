package org.nodocentral.miviaje.presentation;

import android.content.Context;

import org.nodocentral.miviaje.R;
import org.nodocentral.miviaje.domain.mimovilidad.Route;
import org.nodocentral.miviaje.domain.mimovilidad.Station;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;

public final class TransitTextFormatter {
    private TransitTextFormatter() {
    }

    public static String getRouteName(Context context, Route route, boolean rebelMode) {
        switch (route.getTransportType()) {
            case TRAIN_FEEDER_BUS:
                if (route.isTrunk()) {
                    if (rebelMode) {
                        return context.getString(R.string.transport_sitren, route.getId(), route.getVariantTagSafe());
                    } else {
                        return context.getString(R.string.transport_pretren, route.getId(), route.getVariantTagSafe());
                    }
                } else {
                    if (rebelMode) {
                        return context.getString(R.string.transport_sitren_feeder, route.getId(), route.getVariantTagSafe());
                    } else {
                        return context.getString(R.string.transport_pretren_feeder, route.getId(), route.getVariantTagSafe());
                    }
                }
            case BRT_FEEDER_BUS:
                switch (route) {
                    case LM_V01:
                        return context.getString(R.string.route_lopezmateos_v01);
                    case LM_V02:
                        return context.getString(R.string.route_lopezmateos_v02);
                    case LM_V03:
                        return context.getString(R.string.route_lopezmateos_v03);
                    case LM_V04:
                        return context.getString(R.string.route_lopezmateos_v04);
                    case LM_V05:
                        return context.getString(R.string.route_lopezmateos_v05);
                    case LM_C01:
                        return context.getString(R.string.route_lopezmateos_c01);
                    case LM_C02:
                        return context.getString(R.string.route_lopezmateos_c02);
                    case LM_C03:
                        return context.getString(R.string.route_lopezmateos_c03);
                    default:
                        if (route.isTrunk()) {
                            return context.getString(R.string.transport_complementary, route.getId(), route.getVariantTagSafe());
                        } else {
                            return context.getString(R.string.transport_feeder, route.getId(), route.getVariantTagSafe());
                        }
                }
            case BUS:
            default:
                if (route.isTrunk()) {
                    return context.getString(R.string.transport_trunk, route.getId(), route.getVariantTagSafe());
                } else {
                    return context.getString(R.string.transport_complementary, route.getId(), route.getVariantTagSafe());
                }
        }
    }

    public static String getStationName(Context context, Station station) {
        String stationName = getRawStationName(context, station);
        return context.getString(R.string.transport_station, Objects.requireNonNullElseGet(
                stationName,
                () -> station.name().replace('_', ' '))
        );
    }

    public static String getStationSuggestionName(Context context, Station station) {
        String stationName = getRawStationName(context, station);
        return stationName != null ? stationName : station.name().replace('_', ' ');
    }

    public static String getRawStationName(Context context, Station station) {
        switch (station) {
            case PERIFERICO_SUR:
                return context.getString(R.string.station_periferico_sur);
            case SANTUARIO_MARTIRES:
                return context.getString(R.string.station_santuario_martires);
            case ESPANNA:
                return context.getString(R.string.station_espanna);
            case PATRIA:
                return context.getString(R.string.station_patria);
            case ISLA_RAZA:
                return context.getString(R.string.station_isla_raza);
            case _18_DE_MARZO:
                return context.getString(R.string.station_18_de_marzo);
            case URDANETA:
                return context.getString(R.string.station_urdaneta);
            case UNIDAD_DEPORTIVA:
                return context.getString(R.string.station_unidad_deportiva);
            case SANTA_FILOMENA:
                return context.getString(R.string.station_santa_filomena);
            case WASHINGTON:
                return context.getString(R.string.station_washington);
            case MEXICALTZINGO:
                return context.getString(R.string.station_mexicaltzingo);
            case JUAREZ_I:
                return context.getString(R.string.station_juarez_i);
            case REFUGIO:
                return context.getString(R.string.station_refugio);
            case MEZQUITAN:
                return context.getString(R.string.station_mezquitan);
            case AVILA_CAMACHO:
                return context.getString(R.string.station_avila_camacho);
            case DIVISION_DEL_NORTE:
                return context.getString(R.string.station_division_del_norte);
            case ATEMAJAC:
                return context.getString(R.string.station_atemajac);
            case DERMATOLOGICO:
                return context.getString(R.string.station_dermatologico);
            case PERIFERICO_NORTE:
                return context.getString(R.string.station_periferico_norte);
            case TETLAN:
                return context.getString(R.string.station_tetlan);
            case LA_AURORA:
                return context.getString(R.string.station_la_aurora);
            case SAN_JACINTO:
                return context.getString(R.string.station_san_jacinto);
            case SAN_ANDRES:
                return context.getString(R.string.station_san_andres);
            case CRISTOBAL_DE_ONNATE:
                return context.getString(R.string.station_cristobal_de_onnate);
            case OBLATOS:
                return context.getString(R.string.station_oblatos);
            case BELISARIO_DOMINGUEZ:
                return context.getString(R.string.station_belisario_dominguez);
            case SAN_JUAN_DE_DIOS:
                return context.getString(R.string.station_san_juan_de_dios);
            case PLAZA_UNIVERSIDAD:
                return context.getString(R.string.station_plaza_universidad);
            case JUAREZ_II:
                return context.getString(R.string.station_juarez_ii);
            case AUDITORIO:
                return context.getString(R.string.station_auditorio);
            case ARCOS_DE_ZAPOPAN:
                return context.getString(R.string.station_arcos_de_zapopan);
            case PERIFERICO_BELENES:
                return context.getString(R.string.station_periferico_belenes);
            case MERCADO_DEL_MAR:
                return context.getString(R.string.station_mercado_del_mar);
            case ZAPOPAN_CENTRO:
                return context.getString(R.string.station_zapopan_centro);
            case PLAZA_PATRIA:
                return context.getString(R.string.station_plaza_patria);
            case CIRCUNVALACION_COUNTRY:
                return context.getString(R.string.station_circunvalacion_country);
            case AVILA_CAMACHO_II:
                return context.getString(R.string.station_avila_camacho);
            case LA_NORMAL:
                return context.getString(R.string.station_la_normal);
            case SANTUARIO:
                return context.getString(R.string.station_santuario);
            case GUADALAJARA_CENTRO:
                return context.getString(R.string.station_guadalajara_centro);
            case INDEPENDENCIA:
                return context.getString(R.string.station_independencia);
            case PLAZA_DE_LA_BANDERA:
                return context.getString(R.string.station_plaza_de_la_bandera);
            case CUCEI:
                return context.getString(R.string.station_cucei);
            case REVOLUCION:
                return context.getString(R.string.station_revolucion);
            case RIO_NILO:
                return context.getString(R.string.station_rio_nilo);
            case TLAQUEPAQUE_CENTRO:
                return context.getString(R.string.station_tlaquepaque_centro);
            case LAZARO_CARDENAS:
                return context.getString(R.string.station_lazaro_cardenas);
            case CENTRAL_DE_AUTOBUSES:
                return context.getString(R.string.station_central_de_autobuses);
            case LAS_JUNTAS_II:
                return context.getString(R.string.station_las_juntas);
            case FRAY_ANGELICO:
                return context.getString(R.string.station_fray_angelico);
            case ESCULTURAS:
                return context.getString(R.string.station_esculturas);
            case ARTES_PLASTICAS:
                return context.getString(R.string.station_artes_plasticas);
            case CLEMENTE_OROZCO:
                return context.getString(R.string.station_clemente_orozco);
            case LOPEZ_DE_LEGAZPI:
                return context.getString(R.string.station_lopez_de_legazpi);
            case ZONA_INDUSTRIAL:
                return context.getString(R.string.station_zona_industrial);
            case EL_DEAN:
                return context.getString(R.string.station_el_dean);
            case COLON_INDUSTRIAL:
                return context.getString(R.string.station_colon_industrial);
            case HEROES_DE_NACOZARI:
                return context.getString(R.string.station_heroes_de_nacozari);
            case CIPRES:
                return context.getString(R.string.station_cipres);
            case AGUA_AZUL:
                return context.getString(R.string.station_agua_azul);
            case NINOS_HEROES:
                return context.getString(R.string.station_ninos_heroes);
            case LA_PAZ:
                return context.getString(R.string.station_la_paz);
            case BICENTENARIO:
                return context.getString(R.string.station_bicentenario);
            case SAN_JUAN_DE_DIOS_II:
                return context.getString(R.string.station_san_juan_de_dios);
            case ALAMEDA:
                return context.getString(R.string.station_alameda);
            case JUAN_ALVAREZ:
                return context.getString(R.string.station_juan_alvarez);
            case CIENCIAS_DE_LA_SALUD:
                return context.getString(R.string.station_ciencias_de_la_salud);
            case CIRCUNVALACION:
                return context.getString(R.string.station_circunvalacion);
            case MONTE_OLIVETE:
                return context.getString(R.string.station_monte_olivete);
            case MONUMENTAL:
                return context.getString(R.string.station_monumental);
            case IGUALDAD:
                return context.getString(R.string.station_igualdad);
            case SAN_PATRICIO:
                return context.getString(R.string.station_san_patricio);
            case INDEPENDENCIA_NORTE:
                return context.getString(R.string.station_independencia_norte);
            case ZOOLOGICO:
                return context.getString(R.string.station_zoologico);
            case HUENTITAN:
                return context.getString(R.string.station_huentitan);
            case MIRADOR:
                return context.getString(R.string.station_mirador);
            case CARRETERA_A_CHAPALA:
                return context.getString(R.string.station_carretera_a_chapala);
            case LAS_PINTAS:
                return context.getString(R.string.station_las_pintas);
            case ARTESANOS:
                return context.getString(R.string.station_artesanos);
            case JALISCO_200_ANNOS_II:
                return context.getString(R.string.station_jalisco_200_annos);
            case ADOLF_HORN:
                return context.getString(R.string.station_adolf_horn);
            case TOLUQUILLA:
                return context.getString(R.string.station_toluquilla);
            case _8_DE_JULIO:
                return context.getString(R.string.station_8_de_julio);
            case SAN_SEBASTIANITO:
                return context.getString(R.string.station_san_sebastianito);
            case PERIFERICO_SUR_II:
                return context.getString(R.string.station_periferico_sur);
            case TERMINAL_SUR_DE_AUTOBUSES:
                return context.getString(R.string.station_terminal_sur_de_autobuses);
            case ITESO:
                return context.getString(R.string.station_iteso);
            case LOPEZ_MATEOS:
                return context.getString(R.string.station_lopez_mateos);
            case AGRICOLA:
                return context.getString(R.string.station_agricola);
            case EL_BRISENNO:
                return context.getString(R.string.station_el_brisenno);
            case MARIANO_OTERO:
                return context.getString(R.string.station_mariano_otero);
            case MIRAMAR:
                return context.getString(R.string.station_miramar);
            case FELIPE_RUVALCABA:
                return context.getString(R.string.station_felipe_ruvalcaba);
            case EL_COLLI:
                return context.getString(R.string.station_el_colli);
            case CHAPALITA_INN:
                return context.getString(R.string.station_chapalita_inn);
            case PARQUE_METROPOLITANO:
                return context.getString(R.string.station_parque_metropolitano);
            case CIUDAD_GRANJA:
                return context.getString(R.string.station_ciudad_granja);
            case CIUDAD_JUDICIAL:
                return context.getString(R.string.station_ciudad_judicial);
            case ESTADIO_CHIVAS:
                return context.getString(R.string.station_estadio_chivas);
            case VALLARTA:
                return context.getString(R.string.station_vallarta);
            case SAN_JUAN_DE_OCOTAN:
                return context.getString(R.string.station_san_juan_de_ocotan);
            case _5_DE_MAYO:
                return context.getString(R.string.station_5_de_mayo);
            case ACUEDUCTO:
                return context.getString(R.string.station_acueducto);
            case SANTA_MARGARITA:
                return context.getString(R.string.station_santa_margarita);
            case LA_TUZANIA:
                return context.getString(R.string.station_la_tuzania);
            case PERIFERICO_BELENES_II:
                return context.getString(R.string.station_periferico_belenes);
            case SAN_ISIDRO:
                return context.getString(R.string.station_san_isidro);
            case CENTRO_CULTURAL_UNIVERSITARIO:
                return context.getString(R.string.station_centro_cultural_universitario);
            case CONSTITUCION:
                return context.getString(R.string.station_constitucion);
            case TABACHINES:
                return context.getString(R.string.station_tabachines);
            case LA_CANTERA:
                return context.getString(R.string.station_la_cantera);
            case PERIFERICO_NORTE_II:
                return context.getString(R.string.station_periferico_norte);
            case EL_BATAN:
                return context.getString(R.string.station_el_batan);
            case LA_EXPERIENCIA:
                return context.getString(R.string.station_la_experiencia);
            case RANCHO_NUEVO:
                return context.getString(R.string.station_rancho_nuevo);
            case LOMAS_DEL_PARAISO:
                return context.getString(R.string.station_lomas_del_paraiso);
            case INDEPENDENCIA_NORTE_II:
                return context.getString(R.string.station_independencia_norte);
            case ARENA_GUADALAJARA:
                return context.getString(R.string.station_arena_guadalajara);
            case BARRANCA_DE_HUENTITAN:
                return context.getString(R.string.station_barranca_de_huentitan);
            case VICENTE_FERNANDEZ:
                return context.getString(R.string.station_vicente_fernandez);
            case COMISARIA_DE_GUADALAJARA:
                return context.getString(R.string.station_comisaria_de_guadalajara);
            case BETANIA:
                return context.getString(R.string.station_betania);
            case COLONIA_JALISCO:
                return context.getString(R.string.station_colonia_jalisco);
            case ZAPOTLANEJO:
                return context.getString(R.string.station_zapotlanejo);
            case LOS_CONEJOS:
                return context.getString(R.string.station_los_conejos);
            case LAS_JUNTAS:
                return context.getString(R.string.station_las_juntas);
            case LOMAS_DEL_CUATRO:
                return context.getString(R.string.station_lomas_del_cuatro);
            case JALISCO_200_ANNOS:
                return context.getString(R.string.station_jalisco_200_annos);
            case REAL_DEL_VALLE:
                return context.getString(R.string.station_real_del_valle);
            case CONCEPCION_DEL_VALLE:
                return context.getString(R.string.station_concepcion_del_valle);
            case EL_CUERVO:
                return context.getString(R.string.station_el_cuervo);
            case LOMAS_DEL_SUR:
                return context.getString(R.string.station_lomas_del_sur);
            case CUTLAJO:
                return context.getString(R.string.station_cutlajo);
            case TLAJOMULCO_CENTRO:
                return context.getString(R.string.station_tlajomulco_centro);
            default:
                return null;
        }
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD);
        return decomposed
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('_', ' ')
                .replace('-', ' ')
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}
