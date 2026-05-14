package org.nodocentral.miviaje.data.nfc.desfire;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.WeekFields;
import java.util.Date;

public class CardVersion {
    private final Version hardware;
    private final Version software;
    private final long uid;
    private final long batch;
    private final int week;
    private final int year;

    public static class Version {
        private final short vendorId;
        private final short type;
        private final short subType;
        private final short major;
        private final short minor;
        private final short size;
        private final short protocol;
        public Version(short vendorId,
                short type,
                short subType,
                short major,
                short minor,
                short size,
                short protocol) {
            this.vendorId = vendorId;
            this.type = type;
            this.subType = subType;
            this.major = major;
            this.minor = minor;
            this.size = size;
            this.protocol = protocol;
        }

        public short getVendorId() {
            return vendorId;
        }

        public short getType() {
            return type;
        }

        public short getSubType() {
            return subType;
        }

        public short getMajor() {
            return major;
        }

        public short getMinor() {
            return minor;
        }

        public short getSize() {
            return size;
        }

        public short getProtocol() {
            return protocol;
        }
    }

    public CardVersion(Version hardware, Version software, long batch, long uid, int week, int year) {
        this.hardware = hardware;
        this.software = software;
        this.batch = batch;
        this.uid = uid;
        this.week = week;
        this.year = year;
    }

    public Version getHardware() {
        return hardware;
    }

    public Version getSoftware() {
        return software;
    }

    public long getUid() {
        return uid;
    }

    public long getBatch() {
        return batch;
    }

    public int getWeek() {
        return week;
    }

    public int getYear() {
        return year;
    }

    public LocalDate getProductionDate() {
        int currentYear = LocalDate.now().getYear();
        int currentYearOffset = (currentYear / 100) * 100;
        int cardFullYear = getYear() + currentYearOffset;
        if (cardFullYear > (currentYear + 1)) {
            cardFullYear -= 100;
        }

        WeekFields wf = WeekFields.ISO;
        return LocalDate
                .of(cardFullYear, 1, 4)
                .with(wf.weekBasedYear(), cardFullYear)
                .with(wf.weekOfWeekBasedYear(), week)
                .with(wf.dayOfWeek(), 1);
    }
}
