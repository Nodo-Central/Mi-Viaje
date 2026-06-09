package org.nodocentral.miviaje.data.mappers;

import org.nodocentral.miviaje.data.room.EventEntity;
import org.nodocentral.miviaje.domain.mimovilidad.card.Event;
import org.nodocentral.miviaje.domain.mimovilidad.card.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class EventMapper {
    private EventMapper() { }

    public static List<Event> toDomain(List<EventEntity> eventEntities, Map<Integer, Product> products) {
        List<Event> events = new ArrayList<>();
        for (EventEntity event : eventEntities) {
            events.add(EventMapper.toDomain(event, products));
        }
        return events;
    }

    public static Event toDomain(EventEntity entity, Map<Integer, Product> products) {
        if (entity == null) return null;
        return new Event(
                entity.productId,
                entity.productPointer,
                products.get(entity.productId),
                Short.toUnsignedInt((short) entity.entityId),  // TODO: Make a migration to remove signs from short values
                entity.eventDateTime,
                Event.Type.fromInt(entity.eventType),
                entity.amount,
                entity.eventSequence,
                entity.samId,
                entity.samSequence,
                Short.toUnsignedInt((short) entity.deviceId),
                entity.locationId,
                Event.TransportType.fromInt(entity.transportType),
                entity.routeStationId,
                entity.transferCount,
                entity.transferLimit,
                entity.passbackCount,
                Event.RefundReason.fromInt(entity.refundReason),
                Event.DeviceType.fromInt(entity.deviceType)
        );
    }

    public static EventEntity toEntity(Event event, long cardId) {
        if (event == null) return null;
        EventEntity entity = new EventEntity();

        entity.cardId = cardId;
        entity.productId = event.getProductId();
        entity.productPointer = event.getProductPointer();
        entity.entityId = event.getEntityId();
        entity.eventDateTime = event.getEventDateTime();
        entity.eventType = event.getType().getValue();
        entity.amount = event.getAmount();
        entity.deviceId = event.getDeviceId();
        entity.eventSequence = event.getEventSequence();
        entity.samId = event.getSamId();
        entity.samSequence = event.getSamSequence();
        entity.deviceId = event.getDeviceId();
        entity.locationId = event.getLocationId();
        entity.transportType = event.getTransportType().getValue();
        entity.routeStationId = event.getRouteId();
        entity.transferCount = event.getTransferCount();
        entity.transferLimit = event.getTransferLimitTimestamp();
        entity.passbackCount = event.getPassbackCount();
        entity.refundReason = event.getRefundReason().getValue();
        entity.deviceType = event.getDeviceType().getValue();

        return entity;
    }
}
