package org.nodocentral.miviaje.data.backup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;

import org.nodocentral.miviaje.data.UserDataService;
import org.nodocentral.miviaje.data.backup.exporting.BackupExporter;
import org.nodocentral.miviaje.data.backup.importing.BackupImportService;
import org.nodocentral.miviaje.data.backup.importing.BackupImporter;
import org.nodocentral.miviaje.data.backup.models.BackupImportResult;
import org.nodocentral.miviaje.data.backup.models.BackupSnapshot;

public class BackupServiceTest {
    @Test
    public void importData_doesNotApplySnapshotWhenValidationFails() {
        BackupImportResult failure = BackupImportResult.failure(
                List.of(new org.nodocentral.miviaje.data.backup.models.BackupValidationError(
                        "$.schemaVersion",
                        "REQUIRED",
                        "schemaVersion is required"
                ))
        );
        StubBackupImportService importService = new StubBackupImportService(failure);
        RecordingUserDataService userDataService = new RecordingUserDataService();
        BackupService service = new BackupService(null, null, new BackupExporter(), importService, userDataService);

        BackupImportResult result = service.importData("bad");

        assertFalse(result.isSuccess());
        assertEquals("REQUIRED", result.getErrors().get(0).code);
        assertNull(userDataService.receivedSnapshot);
    }

    @Test
    public void importData_mergesSnapshotWhenValidationSucceeds() {
        BackupSnapshot snapshot = new BackupSnapshot(3, List.of(), List.of(), List.of(), List.of());
        StubBackupImportService importService = new StubBackupImportService(BackupImportResult.success(snapshot));
        RecordingUserDataService userDataService = new RecordingUserDataService();
        BackupService service = new BackupService(null, null, new BackupExporter(), importService, userDataService);

        BackupImportResult result = service.importData("ok");

        assertTrue(result.isSuccess());
        assertSame(snapshot, userDataService.receivedSnapshot);
    }

    @Test
    public void importData_convertsApplyFailuresIntoValidationErrors() {
        BackupSnapshot snapshot = new BackupSnapshot(3, List.of(), List.of(), List.of(), List.of());
        StubBackupImportService importService = new StubBackupImportService(BackupImportResult.success(snapshot));
        RecordingUserDataService userDataService = new RecordingUserDataService();
        userDataService.throwOnMerge = new RuntimeException("boom");
        BackupService service = new BackupService(null, null, new BackupExporter(), importService, userDataService);

        BackupImportResult result = service.importData("ok");

        assertFalse(result.isSuccess());
        assertEquals("$.payload", result.getErrors().get(0).fieldPath);
        assertEquals("APPLY_FAILED", result.getErrors().get(0).code);
        assertEquals("boom", result.getErrors().get(0).message);
    }

    private static final class StubBackupImportService extends BackupImportService {
        private final BackupImportResult result;

        StubBackupImportService(BackupImportResult result) {
            super(new BackupImporter());
            this.result = result;
        }

        @Override
        public BackupImportResult importData(String json) {
            return result;
        }
    }

    private static final class RecordingUserDataService extends UserDataService {
        BackupSnapshot receivedSnapshot;
        RuntimeException throwOnMerge;

        RecordingUserDataService() {
            super(null, null);
        }

        @Override
        public void mergeData(BackupSnapshot snapshot) {
            if (throwOnMerge != null) {
                throw throwOnMerge;
            }
            receivedSnapshot = snapshot;
        }
    }
}
