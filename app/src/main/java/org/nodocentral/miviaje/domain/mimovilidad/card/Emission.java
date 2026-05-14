package org.nodocentral.miviaje.domain.mimovilidad.card;

import androidx.annotation.NonNull;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Represents the data stored in the Emisión_EF elementary file.
 */
public class Emission {
    /**
     * ISO 3166‐1 numeric country code (10 bits)
     */
    private final Locale country;
    /**
     * Serial number of the medium (32 bits)
     */
    private final int serialNumber;
    /**
     * End‐date (16 bits)
     */
    private final LocalDate expirationDate;

    private final ApplicationOwner applicationOwner;
    private final Issuer issuer;
    private final SecurityVersion securityVersion;

    public static class ApplicationOwner {
        private final int networkId;   // 24 bits
        private final int companyId;   // 16 bits

        public ApplicationOwner(int networkId, int companyId) {
            this.networkId = networkId;
            this.companyId = companyId;
        }

        @NonNull
        @Override
        public String toString() {
            return "ApplicationOwner[" +
                    "networkId=" + networkId +
                    ", companyId=" + companyId +
                    "]";
        }

        public int getNetworkId() {
            return networkId;
        }

        public int getCompanyId() {
            return companyId;
        }
    }

    public static class Issuer {
        private final int networkId;       // 24 bits
        private final int distributorId;   // 16 bits

        public Issuer(int networkId, int distributorId) {
            this.networkId = networkId;
            this.distributorId = distributorId;
        }

        @NonNull
        @Override
        public String toString() {
            return "Issuer[" +
                    "networkId=" + networkId +
                    ", distributorId=" + distributorId +
                    "]";
        }

        public int getNetworkId() {
            return networkId;
        }

        public int getDistributorId() {
            return distributorId;
        }
    }

    public static class SecurityVersion {
        private final long samUid;             // 56 bits
        private final int algorithmId;         // 12 bits
        private final byte keyVersion;         // 8 bits

        public SecurityVersion(long samUid, int algorithmId, byte keyVersion) {
            this.samUid = samUid;
            this.algorithmId = algorithmId;
            this.keyVersion = keyVersion;
        }

        @NonNull
        @Override
        public String toString() {
            return "SecurityVersion[" +
                    "samUid=" + samUid +
                    ", algorithmId=" + algorithmId +
                    ", keyVersion=" + keyVersion +
                    "]";
        }

        public long getSamUid() {
            return samUid;
        }

        public int getAlgorithmId() {
            return algorithmId;
        }

        public byte getKeyVersion() {
            return keyVersion;
        }
    }

    public Emission(String country,
                    int serialNumber,
                    LocalDate expirationDate,
                    ApplicationOwner applicationOwner,
                    Issuer issuer,
                    SecurityVersion securityVersion) {
        this.country = new Locale("", country);
        this.serialNumber = serialNumber;
        this.expirationDate = expirationDate;
        this.applicationOwner = applicationOwner;
        this.issuer = issuer;
        this.securityVersion = securityVersion;
    }

    @NonNull
    @Override
    public String toString() {
        return "Emission[" +
                "countryCode=" + getCountry() +
                ", serialNumber=" + getSerialNumber() +
                ", expirationDate=" + getExpirationDate() +
                ", " + getApplicationOwner() +
                ", " + getIssuer() +
                ", " + getSecurityVersion() +
                "]";
    }

    public Locale getCountry() {
        return country;
    }

    public int getSerialNumber() {
        return serialNumber;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public ApplicationOwner getApplicationOwner() {
        return applicationOwner;
    }

    public Issuer getIssuer() {
        return issuer;
    }

    public SecurityVersion getSecurityVersion() {
        return securityVersion;
    }
}
