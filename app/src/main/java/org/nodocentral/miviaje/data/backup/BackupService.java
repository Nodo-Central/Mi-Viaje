package org.nodocentral.miviaje.data.backup;

import org.nodocentral.miviaje.data.UserDataService;
import org.nodocentral.miviaje.data.backup.exporting.BackupExporter;
import org.nodocentral.miviaje.data.backup.importing.BackupImportService;
import org.nodocentral.miviaje.data.backup.importing.BackupImporter;
import org.nodocentral.miviaje.data.backup.models.BackupImportResult;
import org.nodocentral.miviaje.data.backup.models.BackupValidationError;
import org.nodocentral.miviaje.data.repository.ArtworkRepository;
import org.nodocentral.miviaje.data.room.MiViajeDatabase;

import java.util.List;

public class BackupService {
    private final MiViajeDatabase database;
    private final ArtworkRepository artworkRepository;
    private final BackupExporter backupExporter;
    private final BackupImportService backupImportService;
    private final UserDataService userDataService;

    public BackupService(MiViajeDatabase database, ArtworkRepository artworkRepository) {
        this(
                database,
                artworkRepository,
                new BackupExporter(),
                new BackupImportService(new BackupImporter()),
                new UserDataService(database, artworkRepository)
        );
    }

    public BackupService(MiViajeDatabase database,
                         ArtworkRepository artworkRepository,
                         BackupExporter backupExporter,
                         BackupImportService backupImportService,
                         UserDataService userDataService) {
        this.database = database;
        this.artworkRepository = artworkRepository;
        this.backupExporter = backupExporter;
        this.backupImportService = backupImportService;
        this.userDataService = userDataService;
    }

    public String exportData() {
        return backupExporter.exportData(database, artworkRepository);
    }

    public String exportCard(long cardUid) {
        return backupExporter.exportCard(database, artworkRepository, cardUid);
    }

    public String buildCardExportFileName(String cardAlias, long cardUid) {
        return backupExporter.buildCardExportFileName(cardAlias, cardUid);
    }

    public BackupImportResult importData(String json) {
        BackupImportResult result = backupImportService.importData(json);
        if (!result.isSuccess()) {
            return result;
        }

        try {
            userDataService.mergeData(result.getSnapshot());
            return result;
        } catch (RuntimeException exception) {
            String message = exception.getMessage() == null
                    ? "Error applying backup snapshot"
                    : exception.getMessage();
            return BackupImportResult.failure(
                    List.of(new BackupValidationError("$.payload", "APPLY_FAILED", message))
            );
        }
    }
}
