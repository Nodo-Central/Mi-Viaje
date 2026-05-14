#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MATERIAL_AAR="$(find "${HOME}/.gradle/caches/modules-2/files-2.1/com.google.android.material/material" \
  -path "*/material-*.aar" -type f | sort -V | tail -n 1)"

if [[ -z "${MATERIAL_AAR}" ]]; then
  echo "Material Components AAR not found. Run ./gradlew assembleDebug once, then retry." >&2
  exit 1
fi

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "${TMP_DIR}"' EXIT

(
  cd "${TMP_DIR}"
  jar xf "${MATERIAL_AAR}" classes.jar
  cat > PaletteXml.java <<'JAVA'
import com.google.android.material.color.utilities.DynamicScheme;
import com.google.android.material.color.utilities.Hct;
import com.google.android.material.color.utilities.SchemeTonalSpot;
import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class PaletteXml {
    record Palette(String style, String colorName) {}

    static final Palette[] PALETTES = {
            new Palette("MetroL1", "miviaje_mt_l1"),
            new Palette("MetroL2", "miviaje_mt_l2"),
            new Palette("MetroL3", "miviaje_mt_l3"),
            new Palette("MetroL4", "miviaje_mt_l4"),
            new Palette("MacroAeropuerto", "miviaje_mm_ma"),
            new Palette("MacroCalzada", "miviaje_mm_mc"),
            new Palette("MacroPeriferico", "miviaje_mm_mp"),
            new Palette("MiTransporte", "miviaje_tp_bus"),
            new Palette("Corporate", "miviaje_pro")
    };

    static final String[][] ROLES = {
            {"colorPrimary", "getPrimary"}, {"colorOnPrimary", "getOnPrimary"}, {"colorPrimaryContainer", "getPrimaryContainer"}, {"colorOnPrimaryContainer", "getOnPrimaryContainer"}, {"colorPrimaryInverse", "getInversePrimary"},
            {"colorSecondary", "getSecondary"}, {"colorOnSecondary", "getOnSecondary"}, {"colorSecondaryContainer", "getSecondaryContainer"}, {"colorOnSecondaryContainer", "getOnSecondaryContainer"},
            {"colorTertiary", "getTertiary"}, {"colorOnTertiary", "getOnTertiary"}, {"colorTertiaryContainer", "getTertiaryContainer"}, {"colorOnTertiaryContainer", "getOnTertiaryContainer"},
            {"colorError", "getError"}, {"colorOnError", "getOnError"}, {"colorErrorContainer", "getErrorContainer"}, {"colorOnErrorContainer", "getOnErrorContainer"},
            {"colorOnBackground", "getOnBackground"}, {"colorSurface", "getSurface"}, {"colorOnSurface", "getOnSurface"}, {"colorSurfaceVariant", "getSurfaceVariant"}, {"colorOnSurfaceVariant", "getOnSurfaceVariant"},
            {"colorSurfaceDim", "getSurfaceDim"}, {"colorSurfaceBright", "getSurfaceBright"}, {"colorSurfaceContainerLowest", "getSurfaceContainerLowest"}, {"colorSurfaceContainerLow", "getSurfaceContainerLow"}, {"colorSurfaceContainer", "getSurfaceContainer"}, {"colorSurfaceContainerHigh", "getSurfaceContainerHigh"}, {"colorSurfaceContainerHighest", "getSurfaceContainerHighest"},
            {"colorSurfaceInverse", "getInverseSurface"}, {"colorOnSurfaceInverse", "getInverseOnSurface"}, {"colorOutline", "getOutline"}, {"colorOutlineVariant", "getOutlineVariant"}, {"colorPrimaryFixed", "getPrimaryFixed"}, {"colorPrimaryFixedDim", "getPrimaryFixedDim"}, {"colorOnPrimaryFixed", "getOnPrimaryFixed"}, {"colorOnPrimaryFixedVariant", "getOnPrimaryFixedVariant"},
            {"colorSecondaryFixed", "getSecondaryFixed"}, {"colorSecondaryFixedDim", "getSecondaryFixedDim"}, {"colorOnSecondaryFixed", "getOnSecondaryFixed"}, {"colorOnSecondaryFixedVariant", "getOnSecondaryFixedVariant"}, {"colorTertiaryFixed", "getTertiaryFixed"}, {"colorTertiaryFixedDim", "getTertiaryFixedDim"}, {"colorOnTertiaryFixed", "getOnTertiaryFixed"}, {"colorOnTertiaryFixedVariant", "getOnTertiaryFixedVariant"},
            {"colorControlActivated", "getPrimary"}, {"colorControlNormal", "getControlNormal"}
    };

    static String hex(int argb) {
        return String.format("#%06X", argb & 0xFFFFFF);
    }

    static int get(DynamicScheme scheme, String method) throws Exception {
        Method m = DynamicScheme.class.getMethod(method);
        return (int) m.invoke(scheme);
    }

    static Map<String, Integer> readColors(String path) throws Exception {
        Map<String, Integer> colors = new HashMap<>();
        NodeList nodes = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new File(path))
                .getElementsByTagName("color");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            String name = element.getAttribute("name");
            String value = element.getTextContent().trim();
            if (value.startsWith("#")) {
                colors.put(name, parseColor(value));
            }
        }
        return colors;
    }

    static int parseColor(String value) {
        String hex = value.substring(1);
        if (hex.length() == 3) {
            hex = "FF" + "" + hex.charAt(0) + hex.charAt(0) + hex.charAt(1) + hex.charAt(1) + hex.charAt(2) + hex.charAt(2);
        } else if (hex.length() == 4) {
            hex = "" + hex.charAt(0) + hex.charAt(0) + hex.charAt(1) + hex.charAt(1) + hex.charAt(2) + hex.charAt(2) + hex.charAt(3) + hex.charAt(3);
        } else if (hex.length() == 6) {
            hex = "FF" + hex;
        } else if (hex.length() != 8) {
            throw new IllegalArgumentException("Unsupported color value: " + value);
        }
        return (int) Long.parseLong(hex, 16);
    }

    static void printStyles(Map<String, Integer> colors, boolean dark) throws Exception {
        System.out.println("<resources>");
        System.out.println("    <!-- Generated from Material Color Utilities SchemeTonalSpot seeds in values/colors.xml. -->");
        for (Palette palette : PALETTES) {
            Integer seed = colors.get(palette.colorName());
            if (seed == null) {
                throw new IllegalStateException("Missing color seed: " + palette.colorName());
            }
            DynamicScheme scheme = new SchemeTonalSpot(Hct.fromInt(seed), dark, 0.0);
            System.out.println("    <style name=\"Theme.MiViaje.Palette." + palette.style() + "\" parent=\"Theme.MiViaje\">");
            System.out.println("        <item name=\"android:colorBackground\">" + hex(scheme.getSurface()) + "</item>");
            System.out.println("        <item name=\"android:windowBackground\">" + hex(scheme.getSurface()) + "</item>");
            System.out.println("        <item name=\"android:statusBarColor\">" + hex(scheme.getPrimaryContainer()) + "</item>");
            System.out.println("        <item name=\"android:navigationBarColor\">" + hex(scheme.getSurface()) + "</item>");
            System.out.println("        <item name=\"colorAccent\">" + hex(scheme.getPrimary()) + "</item>");
            for (String[] role : ROLES) {
                System.out.println("        <item name=\"" + role[0] + "\">" + hex(get(scheme, role[1])) + "</item>");
            }
            System.out.println("    </style>");
            System.out.println();
        }
        System.out.println("</resources>");
    }

    public static void main(String[] args) throws Exception {
        Map<String, Integer> colors = readColors(args[0]);
        printStyles(colors, args.length > 1 && args[1].equals("dark"));
    }
}
JAVA

  javac -classpath classes.jar PaletteXml.java
  java -classpath .:classes.jar PaletteXml "${ROOT_DIR}/app/src/main/res/values/colors.xml" \
    > "${ROOT_DIR}/app/src/main/res/values/themes_palettes.xml"
  java -classpath .:classes.jar PaletteXml "${ROOT_DIR}/app/src/main/res/values/colors.xml" dark \
    > "${ROOT_DIR}/app/src/main/res/values-night/themes_palettes.xml"
)
