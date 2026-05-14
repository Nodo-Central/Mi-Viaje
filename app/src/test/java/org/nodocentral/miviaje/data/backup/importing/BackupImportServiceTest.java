package org.nodocentral.miviaje.data.backup.importing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.nodocentral.miviaje.data.backup.models.BackupImportResult;

public class BackupImportServiceTest {
    private final BackupImportService service = new BackupImportService(new BackupImporter());

    @Test
    public void importData_returnsInvalidJsonError() {
        BackupImportResult result = service.importData("not json");

        assertFalse(result.isSuccess());
        assertEquals("INVALID_JSON", result.getErrors().get(0).code);
    }

    @Test
    public void importData_requiresSchemaVersion() {
        BackupImportResult result = service.importData("{\"cards\":[],\"products\":[],\"events\":[]}");

        assertFalse(result.isSuccess());
        assertEquals("$.schemaVersion", result.getErrors().get(0).fieldPath);
        assertEquals("REQUIRED", result.getErrors().get(0).code);
    }

    @Test
    public void importData_rejectsUnsupportedSchemaVersion() {
        BackupImportResult result = service.importData("{\"schemaVersion\":4,\"cards\":[],\"products\":[],\"events\":[]}");

        assertFalse(result.isSuccess());
        assertEquals("UNSUPPORTED_VERSION", result.getErrors().get(0).code);
    }

    @Test
    public void importData_rejectsNonArrayFields() {
        BackupImportResult result = service.importData("{\"schemaVersion\":3,\"cards\":{},\"products\":[],\"events\":[],\"artworks\":{}}");

        assertFalse(result.isSuccess());
        assertEquals(2, result.getErrors().size());
        assertEquals("$.cards", result.getErrors().get(0).fieldPath);
        assertEquals("$.artworks", result.getErrors().get(1).fieldPath);
    }

    @Test
    public void importData_mapsSchemaVersion2() {
        BackupImportResult result = service.importData(buildValidBackupJson(2));

        assertTrue(result.isSuccess());
        assertEquals(2, result.getSnapshot().schemaVersion);
        assertEquals(0, result.getSnapshot().artworks.size());
    }

    @Test
    public void importData_mapsSchemaVersion3() {
        BackupImportResult result = service.importData(buildValidBackupJson(3));

        assertTrue(result.isSuccess());
        assertEquals(3, result.getSnapshot().schemaVersion);
        assertEquals(1, result.getSnapshot().artworks.size());
        assertEquals("Daily card", result.getSnapshot().cards.get(0).alias);
        assertEquals(
                LocalDateTime.ofEpochSecond(1713924000, 0, ZoneOffset.ofHours(-6)),
                result.getSnapshot().cards.get(0).lastUpdated
        );
        assertEquals(LocalDate.ofEpochDay(19756), result.getSnapshot().cards.get(0).productionDate);
        assertEquals("imported:art-1", result.getSnapshot().cards.get(0).artworkRef);
    }

    private static String buildValidBackupJson(int schemaVersion) {
        String artworks = schemaVersion == 3
                ? ",\"artworks\":[{\"id\":\"art-1\",\"displayName\":\"Artwork\",\"relativePath\":\"art.png\",\"mimeType\":\"image/png\",\"sha256\":\"abc\",\"createdAt\":1713924000}]"
                : "";
        return "{"
                + "\"schemaVersion\":" + schemaVersion + ","
                + "\"cards\":[{"
                + "\"uid\":\"0000000000000001\","
                + "\"alias\":\"Daily card\","
                + "\"lastUpdated\":1713924000,"
                + "\"productionDate\":19756,"
                + "\"country\":\"MX\","
                + "\"serialNumber\":\"000001\","
                + "\"emissionExpiration\":20567,"
                + "\"applicationNetworkId\":1,"
                + "\"applicationCompanyId\":2,"
                + "\"issuerNetworkId\":3,"
                + "\"issuerDistributorId\":4,"
                + "\"samUid\":\"0000000000000001\","
                + "\"algorithmId\":5,"
                + "\"keyVersion\":6,"
                + "\"environmentNetworkId\":7,"
                + "\"applicationVersion\":16,"
                + "\"environmentExpiration\":20567,"
                + "\"userProfileType\":6,"
                + "\"userProfileExpiration\":20567,"
                + "\"userName\":\"Ada\","
                + "\"userCitizenId\":\"CURP\","
                + "\"applicationStatus\":1,"
                + "\"applicationEventCount\":2,"
                + "\"applicationActionsApplied\":3,"
                + "\"artworkRef\":\"imported:art-1\""
                + "}],"
                + "\"products\":[{"
                + "\"cardId\":\"0000000000000001\","
                + "\"productId\":\"1234\","
                + "\"value\":500,"
                + "\"valuePointer\":9,"
                + "\"priority\":1,"
                + "\"pointer\":7,"
                + "\"serial\":8,"
                + "\"priceCents\":900,"
                + "\"valueUnit\":0,"
                + "\"minAmountLimit\":0,"
                + "\"maxAmountLimit\":1000,"
                + "\"reactivationCount\":1,"
                + "\"lastAppliedActionNumber\":2,"
                + "\"distributorNetworkId\":50,"
                + "\"distributorCompanyId\":60,"
                + "\"distributionDateTime\":1713924000,"
                + "\"distributionSamId\":\"0000000A\","
                + "\"distributingDeviceId\":70,"
                + "\"validFrom\":1713924000,"
                + "\"validTo\":1714010400,"
                + "\"validDailyStartTime\":3600,"
                + "\"validDailyEndTime\":7200,"
                + "\"restrictionRestrictedDays\":0,"
                + "\"restrictionMaxTripsPerDayOfWeek\":4,"
                + "\"restrictionPassbackTimeMinutes\":5,"
                + "\"restrictionAllowedPassbacks\":1,"
                + "\"restrictionTransferTimeLimitMinutes\":10,"
                + "\"restrictionAllowedInterchanges\":2,"
                + "\"state\":1,"
                + "\"weekOfYear\":17,"
                + "\"tripsPerDayOfWeek\":3,"
                + "\"totalUsages\":8,"
                + "\"lastDebitDateTime\":1713924000,"
                + "\"lastDebitEntityId\":11,"
                + "\"lastDebitRouteStationId\":12,"
                + "\"lastDebitDeviceId\":13"
                + "}],"
                + "\"events\":[{"
                + "\"cardId\":\"0000000000000001\","
                + "\"eventSequence\":1,"
                + "\"productId\":\"1234\","
                + "\"productPointer\":7,"
                + "\"entityId\":20,"
                + "\"eventDateTime\":1713924000,"
                + "\"eventType\":4,"
                + "\"amount\":250,"
                + "\"samId\":\"0000000000000001\","
                + "\"samSequence\":\"0000000000000002\","
                + "\"deviceId\":30,"
                + "\"locationId\":31,"
                + "\"transportType\":1,"
                + "\"routeStationId\":32,"
                + "\"transferCount\":1,"
                + "\"transferLimit\":3600,"
                + "\"passbackCount\":0,"
                + "\"refundReason\":0,"
                + "\"deviceType\":1"
                + "}]"
                + artworks
                + "}";
    }
}
