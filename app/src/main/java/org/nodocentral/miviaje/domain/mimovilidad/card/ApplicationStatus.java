package org.nodocentral.miviaje.domain.mimovilidad.card;

import androidx.annotation.NonNull;

public class ApplicationStatus {
    /**
     * Indicates the current state of the interoperable application.
     * Possible values (defined in the interoperable system catalog):
     * 0 = Inicializada, 1 = Activada, 2 = Desactivada, 3 = Bloqueada.
     */
    public final State state;

    /**
     * The count of events that have occurred on the payment medium.
     * May increment by more than one per operation (e.g., emission + product distribution).
     */
    public final int eventCount;

    /**
     * The number of actions applied to the payment medium via the LAM list.
     */
    public final int actionsApplied;

    public ApplicationStatus(State state,
                             int eventCount,
                             int actionsApplied) {
        this.state = state;
        this.eventCount = eventCount;
        this.actionsApplied = actionsApplied;
    }

    public enum State {
        INITIALIZED(0),
        ACTIVATED(1),
        BLOCKED(2),
        DEACTIVATED(3);

        private final int value;
        State(int value) {
            this.value = value;
        }

        public static State fromInt(int stateId) {
            for (State state : State.values()) {
                if (state.value == stateId) {
                    return state;
                }
            }
            throw new IllegalArgumentException("Invalid state: " + stateId);
        }

        public int getValue() {
            return value;
        }
    }

    public State getState() {
        return state;
    }

    public int getEventCount() {
        return eventCount;
    }

    public int getActionsApplied() {
        return actionsApplied;
    }

    @Override
    @NonNull
    public String toString() {
        return "ApplicationStatus[" +
                "state=" + state.name() +
                ", eventCount=" + eventCount +
                ", actionsApplied=" + actionsApplied +
                "]";
    }
}
