package org.nodocentral.miviaje.domain.mimovilidad.card;

import androidx.annotation.NonNull;

import org.nodocentral.miviaje.Helpers;
import org.nodocentral.miviaje.domain.mimovilidad.Operator;

import java.time.LocalDateTime;

public class Event {
    /** IdProducto: código del producto usado en el evento (16 bits) */
    private final int productId; // TODO: Pass the Product instance to this?
    /** PunteroProducto: identificador de archivo del contrato correspondiente (8 bits) */
    private final int productPointer;
    private final Product product;

    /** IdEntidad: código de la entidad que generó el evento (16 bits) */
    private final int entityId;
    /** FechaHoraEvento: fecha y hora de ocurrencia del evento (32-bit compact) */
    private final LocalDateTime eventDateTime;
    /** TipoEvento: tipo de evento registrado (0..255) */
    private final Type type;

    /** MontoEvento: monto intercambiado, en centavos o según producto (16 bits) */
    private final int amount;
    /** ConsecutivoEvento: secuencia de aplicación en EstadoAplicación_EF (24 bits) */
    private final int eventSequence; // TODO: Link ApplicationStatus instance?
    /** IdSAM: UID del SAM usado en el evento (7 bytes) */
    private final long samId;
    /** ConsecutivoSAM: número de transacción en el SAM (64 bits) */
    private final long samSequence;
    /** IdDispositivo: identificador del dispositivo que ejecutó el evento (16 bits) */
    private final int deviceId;

    /** IdUbicación: punto donde ocurrió el evento (16 bits) */
    private final int locationId;

    /** TipoTransporte: modo de transporte usado (5 bits) */
    private final TransportType transportType;
    /** IdRuta_Estación: identificación de ruta o estación (16 bits) */
    private final int routeId;
    /** NúmeroTransbordos: transbordos en este viaje (4 bits) */
    private final int transferCount;
    /** LímiteTransbordo: fecha y hora límite para considerarse transbordo */
    private final int transferLimit; // TODO: Convert to LocalDateTime
    /** NúmeroPassbacks: número de passbacks acumulados (4 bits) */
    private final int passbackCount;

    /** MotivoDevolución: razón de devolución de tarifa en última validación (0..255) */
    private final RefundReason refundReason;

    /** IdTipoDispositivo: tipo de dispositivo usado (e.g., 1=Validador…) (16 bits) */
    private final DeviceType deviceType;

    public enum Type {
        UNSPECIFIED(0),
        PRODUCT_DISTRIBUTION(1),
        RFU_2(2),
        RFU_3(3),
        PRODUCT_USE(4),
        RFU_5(5),
        PRODUCT_TOP_UP(6),
        TRANSFER(7),
        REFUND(8),
        RFU_9(9),
        RFU_10(10),
        RFU_11(11),
        RFU_12(12),
        RFU_13(13),
        FARE_REFUND(14),
        RFU_15(15),
        RFU_16(16),
        RFU_17(17),
        RFU_18(18),
        RFU_19(19),
        PAYMENT_METHOD_EMISSION(20);

        private final int value;
        Type(int value) {
            this.value = value;
        }

        public static Type fromInt(int transportId) {
            for (Type transportType : Type.values()) {
                if (transportType.value == transportId) {
                    return transportType;
                }
            }
            return null;
        }

        public int getValue() {
            return value;
        }
    }

    public enum TransportType {
        UNSPECIFIED(0),
        BUS(1),
        TRAIN(3),
        TRAIN_FEEDER_BUS(29),
        BRT_FEEDER_BUS(31),
        BRT(33);

        private final int value;
        TransportType(int value) {
            this.value = value;
        }

        public static TransportType fromInt(int transportId) {
            for (TransportType transportType : TransportType.values()) {
                if (transportType.value == transportId) {
                    return transportType;
                }
            }
            return null;
        }

        public int getValue() {
            return value;
        }
    }

    public enum RefundReason {
        NO_REFUND(0),
        TECHNICAL_FAILURE(1),
        LOGISTIC_FAILURE(2),
        EXTERNAL_AGENT(3);

        private final int value;
        RefundReason(int value) {
            this.value = value;
        }

        public static RefundReason fromInt(int reasonId) {
            for (RefundReason refundReason : RefundReason.values()) {
                if (refundReason.value == reasonId) {
                    return refundReason;
                }
            }
            return null;
        }

        public int getValue() {
            return value;
        }
    }

    public enum DeviceType {
        UNSPECIFIED(0),
        FARE_VALIDATOR(1),
        TICKET_MACHINE(2),
        CUSTOMER_SERVICE(3),
        SMARTPHONE(4),
        POS_MACHINE(5);

        private final int value;
        DeviceType(int value) {
            this.value = value;
        }

        public static DeviceType fromInt(int deviceId) {
            for (DeviceType deviceType : DeviceType.values()) {
                if (deviceType.value == deviceId) {
                    return deviceType;
                }
            }
            return null;
        }

        public int getValue() {
            return value;
        }
    }

    public Event(int productId,
                 int productPointer,
                 Product product,
                 int entityId,
                 LocalDateTime eventDateTime,
                 Type type,
                 int amount,
                 int eventSequence,
                 long samId,
                 long samSequence,
                 int deviceId,
                 int locationId,
                 TransportType transportType,
                 int routeId,
                 int transferCount,
                 int transferLimit,
                 int passbackCount,
                 RefundReason refundReason,
                 DeviceType deviceType) {
        this.type = type;
        this.eventDateTime = eventDateTime;
        this.eventSequence = eventSequence;
        this.amount = amount;

        // TODO: Remove this and make the eventList an attribute of Product
        this.productId = productId;
        this.productPointer = productPointer;
        this.product = product;

        this.transportType = transportType;
        this.routeId = routeId;
        this.locationId = locationId;
        this.deviceId = deviceId;
        this.deviceType = deviceType;
        this.entityId = entityId;
        this.samId = samId;

        this.transferCount = transferCount;
        this.transferLimit = transferLimit;
        this.passbackCount = passbackCount;
        this.refundReason = refundReason;
        this.samSequence = samSequence;
    }

    @NonNull
    @Override
    public String toString() {
        return "Event" +
                "[\n productId=" + productId +
                ",\n product=" + (product != null ? product.getId() : "NULL") +
                ",\n entityId=" + entityId +
                ",\n eventDateTime=" + eventDateTime +
                ",\n type=" + type +
                ",\n amount=" + amount +
                ",\n eventSequence=" + eventSequence +
                ",\n samId=" + Long.toHexString(samId) +
                ",\n samSequence=" + samSequence +
                ",\n deviceId=" + deviceId +
                ",\n locationId=" + locationId +
                ",\n transportType=" + getTransportType() +
                ",\n rawTransportType=" + transportType +
                ",\n routeStationId=" + routeId +
                ",\n transferCount=" + transferCount +
                ",\n transferLimit=" + transferLimit +
                ",\n passbackCount=" + passbackCount +
                ",\n refundReason=" + refundReason +
                ",\n deviceType=" + deviceType +
                "]";
    }

    public int getProductId() {
        return productId;
    }

    public int getProductPointer() {
        return productPointer;
    }

    public ProductContract.ValueUnit getValueUnit() {
        if (product != null && product.getContract() != null) {
            return product.getContract().getValueUnit();
        } else {
            return ProductContract.ValueUnit.MXN_CENT;
        }
    }

    public Product getProduct() {
        return product;
    }

    public int getEntityId() {
        return entityId;
    }

    public LocalDateTime getEventDateTime() {
        return eventDateTime;
    }

    public Type getType() {
        return type;
    }

    public boolean isPayment() {
        return (getType() != Type.PRODUCT_TOP_UP &&
                getType() != Type.FARE_REFUND &&
                getType() != Type.REFUND);
    }

    public int getAmount() {
        return amount;
    }

    public int getEventSequence() {
        return eventSequence;
    }

    public long getSamId() {
        return samId;
    }

    public long getSamSequence() {
        return samSequence;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public int getLocationId() {
        return locationId;
    }

    public Operator getOperator() {
        return Operator.fromInt(entityId);
    }

    public TransportType getRawTransportType() {
        return transportType;
    }

    public TransportType getTransportType() {
        Operator operator = getOperator();
        if (operator != null &&
                (transportType == TransportType.BUS ||
                transportType == TransportType.TRAIN ||
                transportType == TransportType.TRAIN_FEEDER_BUS)) {
            switch (operator) {
                case MI_MACRO_CALZADA:
                case MI_MACRO_PERIFERICO_TRONCAL:
                    return TransportType.BRT;
                case RUTA_LOPEZ_MATEOS:
                case MI_MACRO_PERIFERICO_COMPLEMENTARIO:
                    return TransportType.BRT_FEEDER_BUS;
                default:
                    if ((samId & 0xFFFFFFFFL) == 0xfa4d7a80L ||
                            (samId & 0xFFFFFFFL) == 0xa077c80 ||
                            (samId & 0xFFFFFFFL) == 0xade7980) {
                        if (routeId > 20) {
                            return TransportType.BRT;
                        } else {
                            return TransportType.TRAIN_FEEDER_BUS;
                        }
                    } else if ((samId & 0xFFFFFFFFL) == 0x42294f80) {
                        return TransportType.TRAIN;
                    } else if ((samId & 0xFFFFFFFL) == 0xa057180 || (samId & 0xFFFFFFFL) == 0xa294f80) {
                        return TransportType.BRT_FEEDER_BUS;
                    } else {
                        return transportType;
                    }
            }
        } else {
            return transportType;
        }
    }

    public int getRouteId() {
        return routeId;
    }

    public int getTransferCount() {
        return transferCount;
    }

    public int getTransferLimitTimestamp() {
        return transferLimit;
    }

    public LocalDateTime getTransferLimit() {
        return Helpers.parseDateTimeCompact(transferLimit);
    }

    public int getPassbackCount() {
        return passbackCount;
    }

    public RefundReason getRefundReason() {
        return refundReason;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }
}
