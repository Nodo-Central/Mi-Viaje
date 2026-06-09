package org.nodocentral.miviaje.data.room;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.nodocentral.miviaje.data.room.converters.DateConverters;

@Database(entities = {CardEntity.class, ProductEntity.class, EventEntity.class, ArtworkEntity.class}, version = 7)
@TypeConverters({DateConverters.class})
public abstract class MiViajeDatabase extends RoomDatabase {
    public abstract CardDao cardDao();
    public abstract ProductDao productDao();
    public abstract EventDao eventDao();
    public abstract ArtworkDao artworkDao();

    private static volatile MiViajeDatabase INSTANCE;

    public static MiViajeDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (MiViajeDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    MiViajeDatabase.class,
                                    "miviaje_db"
                            )
                            .addMigrations(MIGRATION_1_2)
                            .addMigrations(MIGRATION_2_3)
                            .addMigrations(MIGRATION_3_4)
                            .addMigrations(MIGRATION_4_5)
                            .addMigrations(MIGRATION_5_6)
                            .addMigrations(MIGRATION_6_7)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL(
                    "UPDATE `products` " +
                            "SET `distributionSamId` = `distributionSamId` + 4294967296 " +
                            "WHERE `distributionSamId` < 0"
            );
        }
    };

    public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `artworks` (" +
                            "`id` TEXT NOT NULL, " +
                            "`displayName` TEXT, " +
                            "`relativePath` TEXT, " +
                            "`mimeType` TEXT, " +
                            "`sha256` TEXT, " +
                            "`createdAt` INTEGER, " +
                            "PRIMARY KEY(`id`)" +
                            ")"
            );
        }
    };

    public static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("PRAGMA foreign_keys=OFF");

            // --- A) Add new column to cards ---
            // Nullable, so no default needed.
            db.execSQL("ALTER TABLE `cards` ADD COLUMN `artworkRef` TEXT");

            // --- B) Rebuild events with expanded primary key ---
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `events_new` (" +
                            "`cardId` INTEGER NOT NULL, " +
                            "`eventSequence` INTEGER NOT NULL, " +
                            "`samId` INTEGER NOT NULL, " +
                            "`samSequence` INTEGER NOT NULL, " +

                            "`productId` INTEGER NOT NULL, " +
                            "`productPointer` INTEGER NOT NULL, " +
                            "`entityId` INTEGER NOT NULL, " +
                            "`eventDateTime` INTEGER, " +
                            "`eventType` INTEGER NOT NULL, " +
                            "`amount` INTEGER NOT NULL, " +

                            "`deviceId` INTEGER NOT NULL, " +
                            "`locationId` INTEGER NOT NULL, " +
                            "`transportType` INTEGER NOT NULL, " +
                            "`routeStationId` INTEGER NOT NULL, " +
                            "`transferCount` INTEGER NOT NULL, " +
                            "`transferLimit` INTEGER NOT NULL, " +
                            "`passbackCount` INTEGER NOT NULL, " +
                            "`refundReason` INTEGER NOT NULL, " +
                            "`deviceType` INTEGER NOT NULL, " +

                            "PRIMARY KEY(`cardId`, `eventSequence`, `samId`, `samSequence`), " +
                            "FOREIGN KEY(`cardId`) REFERENCES `cards`(`uid`) ON DELETE CASCADE" +
                            ")"
            );

            db.execSQL(
                    "INSERT INTO `events_new` (" +
                            "`cardId`, `eventSequence`, `samId`, `samSequence`, " +
                            "`productId`, `productPointer`, `entityId`, `eventDateTime`, `eventType`, `amount`, " +
                            "`deviceId`, `locationId`, `transportType`, `routeStationId`, `transferCount`, `transferLimit`, " +
                            "`passbackCount`, `refundReason`, `deviceType`" +
                            ") " +
                            "SELECT " +
                            "`cardId`, `eventSequence`, `samId`, `samSequence`, " +
                            "`productId`, `productPointer`, `entityId`, `eventDateTime`, `eventType`, `amount`, " +
                            "`deviceId`, `locationId`, `transportType`, `routeStationId`, `transferCount`, `transferLimit`, " +
                            "`passbackCount`, `refundReason`, `deviceType` " +
                            "FROM `events`"
            );

            db.execSQL("DROP TABLE `events`");
            db.execSQL("ALTER TABLE `events_new` RENAME TO `events`");

            db.execSQL("CREATE INDEX IF NOT EXISTS `index_events_cardId` ON `events`(`cardId`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_events_cardId_eventSequence` ON `events`(`cardId`, `eventSequence`)");

            db.execSQL("PRAGMA foreign_keys=ON");
        }
    };

    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("PRAGMA foreign_keys=OFF"); // safer during rebuild

            // 1) Create new table with samId as INTEGER NOT NULL DEFAULT 0
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `events_new` (" +
                            "`cardId` INTEGER NOT NULL, " +
                            "`eventSequence` INTEGER NOT NULL, " +
                            "`productId` INTEGER NOT NULL, " +
                            "`productPointer` INTEGER NOT NULL, " +
                            "`entityId` INTEGER NOT NULL, " +
                            "`eventDateTime` INTEGER, " +
                            "`eventType` INTEGER NOT NULL, " +
                            "`amount` INTEGER NOT NULL, " +
                            "`samId` INTEGER NOT NULL, " +   // <-- changed
                            "`samSequence` INTEGER NOT NULL, " +
                            "`deviceId` INTEGER NOT NULL, " +
                            "`locationId` INTEGER NOT NULL, " +
                            "`transportType` INTEGER NOT NULL, " +
                            "`routeStationId` INTEGER NOT NULL, " +
                            "`transferCount` INTEGER NOT NULL, " +
                            "`transferLimit` INTEGER NOT NULL, " +
                            "`passbackCount` INTEGER NOT NULL, " +
                            "`refundReason` INTEGER NOT NULL, " +
                            "`deviceType` INTEGER NOT NULL, " +
                            "PRIMARY KEY(`cardId`, `eventSequence`), " +
                            "FOREIGN KEY(`cardId`) REFERENCES `cards`(`uid`) ON DELETE CASCADE" +
                            ")"
            );

            // 2) Copy rows with Java-side conversion (SQLite can't blob->int nicely)

            try (Cursor c = db.query(
                    "SELECT " +
                            "`cardId`, `eventSequence`, `productId`, `entityId`, `eventDateTime`, " +
                            "`eventType`, `amount`, `samId`, `samSequence`, `deviceId`, `locationId`, " +
                            "`transportType`, `routeStationId`, `transferCount`, `transferLimit`, " +
                            "`passbackCount`, `refundReason`, `deviceType`, `productPointer` " +
                            "FROM `events`"
            )) {
                while (c.moveToNext()) {
                    long cardId = c.getLong(0);
                    long eventSequence = c.getLong(1);
                    long productId = c.getLong(2);
                    long entityId = c.getLong(3);

                    Long eventDateTime = c.isNull(4) ? null : c.getLong(4);

                    long eventType = c.getLong(5);
                    long amount = c.getLong(6);

                    byte[] samBlob = c.isNull(7) ? null : c.getBlob(7);
                    long samLong = blobToLong56or64(samBlob); // <- your rule

                    long samSequence = c.getLong(8);
                    long deviceId = c.getLong(9);
                    long locationId = c.getLong(10);
                    long transportType = c.getLong(11);
                    long routeStationId = c.getLong(12);
                    long transferCount = c.getLong(13);
                    long transferLimit = c.getLong(14);
                    long passbackCount = c.getLong(15);
                    long refundReason = c.getLong(16);
                    long deviceType = c.getLong(17);
                    long productPointer = c.getLong(18);

                    db.execSQL(
                            "INSERT INTO `events_new` (" +
                                    "`cardId`, `eventSequence`, `productId`, `entityId`, `eventDateTime`, " +
                                    "`eventType`, `amount`, `samId`, `samSequence`, `deviceId`, `locationId`, " +
                                    "`transportType`, `routeStationId`, `transferCount`, `transferLimit`, " +
                                    "`passbackCount`, `refundReason`, `deviceType`, `productPointer`" +
                                    ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                            new Object[]{
                                    cardId, eventSequence, productId, entityId, eventDateTime,
                                    eventType, amount, samLong, samSequence, deviceId, locationId,
                                    transportType, routeStationId, transferCount, transferLimit,
                                    passbackCount, refundReason, deviceType, productPointer
                            }
                    );
                }
            }

            // 3) Swap tables + recreate indexes
            db.execSQL("DROP TABLE `events`");
            db.execSQL("ALTER TABLE `events_new` RENAME TO `events`");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_events_cardId` ON `events`(`cardId`)");

            db.execSQL("PRAGMA foreign_keys=ON");
            db.execSQL("ALTER TABLE `cards` ADD COLUMN `alias` TEXT");
            db.execSQL("ALTER TABLE `cards` ADD COLUMN `lastUpdated` INTEGER");
            db.execSQL("ALTER TABLE `cards` ADD COLUMN `productionDate` INTEGER");
        }
    };

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("PRAGMA foreign_keys=ON;");
            db.execSQL("ALTER TABLE `events` ADD `productPointer` INTEGER NOT NULL DEFAULT 0;");
        }
    };

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("PRAGMA foreign_keys=ON");

            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `events_new` (" +
                            "`cardId` INTEGER NOT NULL, " +
                            "`eventSequence` INTEGER NOT NULL, " +
                            "`productId` INTEGER NOT NULL, " +
                            "`entityId` INTEGER NOT NULL, " +
                            "`eventDateTime` INTEGER, " +      // <-- nullable (sin NOT NULL)
                            "`eventType` INTEGER NOT NULL, " +
                            "`amount` INTEGER NOT NULL, " +
                            "`samId` BLOB, " +                  // <-- nullable (sin NOT NULL)
                            "`samSequence` INTEGER NOT NULL, " +
                            "`deviceId` INTEGER NOT NULL, " +
                            "`locationId` INTEGER NOT NULL, " +
                            "`transportType` INTEGER NOT NULL, " +
                            "`routeStationId` INTEGER NOT NULL, " +
                            "`transferCount` INTEGER NOT NULL, " +
                            "`transferLimit` INTEGER NOT NULL, " +
                            "`passbackCount` INTEGER NOT NULL, " +
                            "`refundReason` INTEGER NOT NULL, " +
                            "`deviceType` INTEGER NOT NULL, " +
                            "PRIMARY KEY(`cardId`, `eventSequence`), " +
                            "FOREIGN KEY(`cardId`) REFERENCES `cards`(`uid`) ON DELETE CASCADE" +
                            ")"
            );

            db.execSQL(
                    "INSERT INTO `events_new` (" +
                            "`cardId`, `eventSequence`, `productId`, `entityId`, `eventDateTime`, " +
                            "`eventType`, `amount`, `samId`, `samSequence`, `deviceId`, `locationId`, " +
                            "`transportType`, `routeStationId`, `transferCount`, `transferLimit`, " +
                            "`passbackCount`, `refundReason`, `deviceType`" +
                            ") " +
                            "SELECT " +
                            "`cardId`, `eventSequence`, `productId`, `entityId`, `eventDateTime`, " +
                            "`eventType`, `amount`, `samId`, `samSequence`, `deviceId`, `locationId`, " +
                            "`transportType`, `routeStationId`, `transferCount`, `transferLimit`, " +
                            "`passbackCount`, `refundReason`, `deviceType` " +
                            "FROM `events`"
            );

            db.execSQL("DROP TABLE `events`");
            db.execSQL("ALTER TABLE `events_new` RENAME TO `events`");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_events_cardId` ON `events`(`cardId`)");
        }
    };

    /**
     * Interprets BLOB as an unsigned big-endian integer (common for IDs).
     * - null -> 0
     * - length 7 -> 56-bit
     * - length 8 -> 64-bit
     * - anything else -> 0 (or throw, if you prefer strict)
     */
    static long blobToLong56or64(byte[] b) {
        if (b == null || b.length == 0) return 0L;
        if (b.length > 8) return 0L;

        long v = 0L;
        for (byte value : b) {
            v = (v << 8) | (value & 0xFFL);
        }
        return v;
    }
}
