package org.nodocentral.miviaje.domain.mimovilidad.card;

import androidx.annotation.NonNull;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class ProductContract {
    private final int productPointer;                 // PunteroContrato
    private final short productId;                 // IdProducto (16 bits)
    private final int productSerial;            // SerialProducto (32 bits)
    private final short priceCents;                     // PrecioProducto (16 bits)
    private final ValueUnit valueUnit;                 // UnidadValorProducto (4 bits)
    private final int minAmountLimit;            // MínimoValor (32 bits)
    private final int maxAmountLimit;            // MáximoValor (32 bits)
    private final short reactivationCount;         // NúmeroReactivaciónProducto (16 bits)
    private final short lastAppliedActionNumber;   // NúmeroAcciónAplicadaProducto (16 bits)

    private final Retailer retailer;
    private final DistributionInfo distributionInfo;
    private final Validity validity;
    private final Restrictions restrictions;

    public ProductContract(short productId,
                           int productSerial,
                           int productPointer,
                           short reactivationCount,
                           short lastAppliedActionNumber,
                           short priceCents,
                           ValueUnit valueUnit,
                           int minAmountLimit,
                           int maxAmountLimit,
                           Retailer retailer,
                           DistributionInfo distributionInfo,
                           Validity validity,
                           Restrictions restrictions) {
        this.productPointer = productPointer;
        this.productId = productId;
        this.productSerial = productSerial;
        this.priceCents = priceCents;
        this.valueUnit = valueUnit;
        this.minAmountLimit = minAmountLimit;
        this.maxAmountLimit = maxAmountLimit;
        this.reactivationCount = reactivationCount;
        this.lastAppliedActionNumber = lastAppliedActionNumber;
        this.retailer = retailer;
        this.distributionInfo = distributionInfo;
        this.validity = validity;
        this.restrictions = restrictions;
    }

    public static class Retailer {
        private final int distributorNetworkId;      // IdRedProducto (24 bits)
        private final short distributorCompanyId;      // IdDistribuidorProducto (16 bits)

        public Retailer(int distributorNetworkId,
                        short distributorCompanyId) {
            this.distributorNetworkId = distributorNetworkId;
            this.distributorCompanyId = distributorCompanyId;
        }

        public int getDistributorNetworkId() {
            return distributorNetworkId;
        }

        public short getDistributorCompanyId() {
            return distributorCompanyId;
        }

        @NonNull
        @Override
        public String toString() {
            return "Retailer[" +
                    "distributorNetworkId=" + distributorNetworkId +
                    ", distributorCompanyId=" + distributorCompanyId +
                    "]";
        }
    }

    public enum ValueUnit {
        MXN_CENT(0),
        TICKET(1),
        RFU_1(2),
        RFU_2(3);

        private final int value;
        ValueUnit(int value) {
            this.value = value;
        }

        public static ValueUnit fromInt(int valueUnitId) {
            for (ValueUnit valueUnit : ValueUnit.values()) {
                if (valueUnit.value == valueUnitId) {
                    return valueUnit;
                }
            }
            return null;
        }

        public int getValue() {
            return value;
        }
    }

    public static class DistributionInfo {
        private final LocalDateTime distributionDateTime;  // FechaDistribución (32-bit compact)
        private final long samId;              // IdSAMDistribución
        private final short distributingDeviceId;            // IdDispositivoDistribución (16 bits)

        public DistributionInfo(LocalDateTime distributionDateTime,
                                long samId,
                                short distributingDeviceId
        ) {
            this.distributionDateTime = distributionDateTime;
            this.samId = samId;
            this.distributingDeviceId = distributingDeviceId;
        }

        public LocalDateTime getDistributionDateTime() {
            return distributionDateTime;
        }

        public long getSamId() {
            return samId;
        }

        public short getDistributingDeviceId() {
            return distributingDeviceId;
        }

        @NonNull
        @Override
        public String toString() {
            return "DistributionInfo[" +
                    "distributionDateTime=" + distributionDateTime +
                    ", samIdDistribution=" + samId +
                    ", deviceIdDistribution=" + distributingDeviceId +
                    "]";
        }
    }

    public static class Validity {
        private final LocalDateTime validFrom;    // InicioValidezProducto (32-bit compact)
        private final LocalDateTime validTo;      // FinValidezProducto (32-bit compact)
        private final LocalTime dailyStartTime;         // InicioValidezDía (minutes since midnight)
        private final LocalTime dailyEndTime;           // FinValidezDía (minutes since midnight)

        public Validity(LocalDateTime validFrom,
                        LocalDateTime validTo,
                        LocalTime dailyStartTime,
                        LocalTime dailyEndTime) {
            this.validFrom = validFrom;
            this.validTo = validTo;
            this.dailyStartTime = dailyStartTime;
            this.dailyEndTime = dailyEndTime;
        }

        public LocalDateTime getValidFrom() {
            return validFrom;
        }

        public LocalDateTime getValidTo() {
            return validTo;
        }

        public LocalTime getDailyStartTime() {
            return dailyStartTime;
        }

        public LocalTime getDailyEndTime() {
            return dailyEndTime;
        }

        @NonNull
        @Override
        public String toString() {
            return "Validity[" +
                    "validFrom=" + validFrom +
                    ", validTo=" + validTo +
                    ", dailyStartTime=" + dailyStartTime +
                    ", dailyEndTime=" + dailyEndTime +
                    "]";
        }
    }

    public static class Restrictions {
        private final byte restrictedDays;         // DíasRestringidos (8 bits)
        private final int maxTripsPerDayOfWeek;    // MaxViajesDíaSemana (32 bits)
        private final short passbackTimeMinutes;            // TiempoPassback (16 bits)
        private final byte allowedPassbacks;        // PassbacksPermitidos (4 bits)
        private final short transferTimeLimitMinutes;       // TiempoTransbordo (16 bits)
        private final byte allowedInterchanges;     // TransbordosPermitidos (4 bits)

        public Restrictions(byte restrictedDays,
                            int maxTripsPerDayOfWeek,
                            short passbackTimeMinutes,
                            byte allowedPassbacks,
                            short transferTimeLimitMinutes,
                            byte allowedInterchanges) {
            this.restrictedDays = restrictedDays;
            this.maxTripsPerDayOfWeek = maxTripsPerDayOfWeek;
            this.passbackTimeMinutes = passbackTimeMinutes;
            this.allowedPassbacks = allowedPassbacks;
            this.transferTimeLimitMinutes = transferTimeLimitMinutes;
            this.allowedInterchanges = allowedInterchanges;
        }

        public byte getRestrictedDays() {
            return restrictedDays;
        }

        public int getMaxTripsPerDayOfWeek() {
            return maxTripsPerDayOfWeek;
        }

        public short getPassbackTimeMinutes() {
            return passbackTimeMinutes;
        }

        public byte getAllowedPassbacks() {
            return allowedPassbacks;
        }

        public short getTransferTimeLimitMinutes() {
            return transferTimeLimitMinutes;
        }

        public byte getAllowedInterchanges() {
            return allowedInterchanges;
        }

        @NonNull
        @Override
        public String toString() {
            return "Restrictions[" +
                    "restrictedDays=" + restrictedDays +
                    ", maxTripsPerDayOfWeek=" + maxTripsPerDayOfWeek +
                    ", passbackTime=" + passbackTimeMinutes +
                    ", allowedPassbacks=" + allowedPassbacks +
                    ", transferTimeLimit=" + transferTimeLimitMinutes +
                    ", allowedInterchanges=" + allowedInterchanges +
                    "]";
        }
    }

    public int getProductPointer() {
        return productPointer;
    }

    public short getProductId() {
        return productId;
    }

    public int getProductSerial() {
        return productSerial;
    }

    public short getPriceCents() {
        return priceCents;
    }

    public ValueUnit getValueUnit() {
        return valueUnit;
    }

    public int getMinAmountLimit() {
        return minAmountLimit;
    }

    public int getMaxAmountLimit() {
        return maxAmountLimit;
    }

    public short getReactivationCount() {
        return reactivationCount;
    }

    public short getLastAppliedActionNumber() {
        return lastAppliedActionNumber;
    }

    public Retailer getRetailer() {
        return retailer;
    }

    public DistributionInfo getDistributionInfo() {
        return distributionInfo;
    }

    public Validity getValidity() {
        return validity;
    }

    public Restrictions getRestrictions() {
        return restrictions;
    }

    @NonNull
    @Override
    public String toString() {
        return "ProductContract[" +
                "productId=" + productId +
                ", productSerial=" + productSerial +
                ", productPointer=" + productPointer +
                ", price=" + priceCents +
                ", valueUnit=" + valueUnit +
                ", minAmountLimit=" + minAmountLimit +
                ", maxAmountLimit=" + maxAmountLimit +
                ", reactivationCount=" + reactivationCount +
                ", lastAppliedActionNumber=" + lastAppliedActionNumber +
                ", retailer=" + retailer +
                ", distributionInfo=" + distributionInfo +
                ", validity=" + validity +
                ", restrictions=" + restrictions +
                "]";
    }
}
