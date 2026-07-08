package org.nodocentral.miviaje.domain.mimovilidad.card;

import androidx.annotation.NonNull;

public class Product {
    public enum Type {
        DISCOUNT_TICKETS_1(16945),
        DISCOUNT_TICKETS_2(16946),
        CREDIT(17201),
        WALLET(19761);
        private final int id;

        Type(int id) {
            this.id = id;
        }

        public static Type fromInt(int id) {
            for (Type type : Type.values()) {
                if (type.id == id) {
                    return type;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }

    /**
     * ProductId: código que identifica el producto (16 bits)
     */
    private final short id;
    private final Type type;
    private final ProductContract contract;
    private final ProductService service;
    private final int value;
    private final int valuePointer;

    /**
     * Priority: prioridad del producto, mayor = más prioridad (8 bits)
     */
    private final int priority;

    public Product(short id,
                   int value,
                   int valuePointer,
                   int priority,
                   ProductContract contract,
                   ProductService service) {
        this.id = id;
        this.type = Type.fromInt(id);
        this.priority = priority;
        this.contract = contract;
        this.service = service;
        this.value = value;
        this.valuePointer = valuePointer;
    }

    public boolean isEmpty() {
        return id == 0 &&
                contract == null &&
                service == null &&
                value == 0 &&
                priority == 0;
    }

    public short getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public ProductContract getContract() {
        return contract;
    }

    public ProductService getService() {
        return service;
    }

    public State getState() {
        if (service.getState() == State.ACTIVE && !contract.isValid())
            return State.EXPIRED;
        else
            return service.getState();
    }

    public int getValue() {
        return value;
    }

    public int getValuePointer() {
        return valuePointer;
    }

    public int getPriority() {
        return priority;
    }

    @NonNull
    @Override
    public String toString() {
        return "Product[" +
                "productId=" + id +
                ", contract=" + contract +
                ", service=" + service +
                ", value=" + value +
                ", priority=" + priority +
                "]";
    }

    public enum State {
        INITIALIZED(0, 1),
        ACTIVE(1, 3),
        EXPIRED(-1, 2),
        SUSPENDED(2, 0);

        private final int value;     // raw card value
        private final int sortRank;  // UI/business ordering

        State(int value, int sortRank) {
            this.value = value;
            this.sortRank = sortRank;
        }

        public static State fromInt(int valueUnitId) {
            for (State valueUnit : State.values()) {
                if (valueUnit.value == valueUnitId) {
                    return valueUnit;
                }
            }
            return null;
        }

        public int getValue() {
            return this.value;
        }

        public int getSortRank() {
            return this.sortRank;
        }
    }
}
