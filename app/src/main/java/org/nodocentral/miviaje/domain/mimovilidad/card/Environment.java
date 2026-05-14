package org.nodocentral.miviaje.domain.mimovilidad.card;

import androidx.annotation.NonNull;

import java.time.LocalDate;

public class Environment {
    /**
     * VersionNumber: 8-bit value where the upper 4 bits are the major version
     * and the lower 4 bits the minor version. Initial value = 1.0 (0x10).
     */
    private final int applicationVersion;

    /** NetworkId: 24-bit identifier of the interoperable network origin. */
    private final int networkId;

    /**
     * EndDate: number of days since 1997-01-01 when the application
     * is no longer valid, mapped here to a LocalDate.
     */
    private final LocalDate expirationDate;

    public Environment(int applicationVersion,
                       int networkId,
                       LocalDate expirationDate) {
        this.applicationVersion = applicationVersion;
        this.networkId = networkId;
        this.expirationDate = expirationDate;
    }

    public int getApplicationVersion() {
        return applicationVersion;
    }

    public int getNetworkId() {
        return networkId;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    @NonNull
    @Override
    public String toString() {
        return "Environment[" +
                "applicationVersion=0x" + Integer.toHexString(applicationVersion) +
                ", networkId=" + networkId +
                ", expirationDate=" + expirationDate +
                "]";
    }
}
