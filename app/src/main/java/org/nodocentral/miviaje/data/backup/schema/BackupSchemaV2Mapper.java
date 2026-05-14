package org.nodocentral.miviaje.data.backup.schema;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.nodocentral.miviaje.data.backup.models.BackupSnapshot;
import org.nodocentral.miviaje.data.room.CardEntity;
import org.nodocentral.miviaje.data.room.EventEntity;
import org.nodocentral.miviaje.data.room.ProductEntity;

import java.util.ArrayList;
import java.util.List;

public class BackupSchemaV2Mapper extends BackupMapperSupport implements BackupSchemaMapper {
    @Override
    public BackupSnapshot map(JsonObject root) {
        List<CardEntity> cards = mapCards(root.getAsJsonArray("cards"));
        List<ProductEntity> products = mapProducts(root.getAsJsonArray("products"));
        List<EventEntity> events = mapEvents(root.getAsJsonArray("events"));
        return new BackupSnapshot(2, cards, products, events, new ArrayList<>());
    }

    protected List<CardEntity> mapCards(JsonArray cards) {
        List<CardEntity> entities = new ArrayList<>();
        for (int i = 0; i < cards.size(); i++) {
            JsonElement el = cards.get(i);
            if (!el.isJsonObject()) {
                throw new BackupFieldException("cards[" + i + "]", "card entry must be an object");
            }
            JsonObject obj = el.getAsJsonObject();
            CardEntity cardEntity = new CardEntity();
            String path = "cards[" + i + "]";

            cardEntity.uid = hexToLong(getRequiredString(obj, "uid", path));
            cardEntity.alias = normalizeEmpty(getNullableString(obj, "alias"));
            cardEntity.lastUpdated = intToDateTime(getNullableInt(obj, "lastUpdated"));
            cardEntity.productionDate = intToDate(getNullableInt(obj, "productionDate"));
            cardEntity.country = getRequiredString(obj, "country", path);
            cardEntity.serialNumber = hexToInt(getRequiredString(obj, "serialNumber", path));
            cardEntity.emissionExpiration = intToDate(getRequiredInt(obj, "emissionExpiration", path));
            cardEntity.applicationNetworkId = getRequiredInt(obj, "applicationNetworkId", path);
            cardEntity.applicationCompanyId = getRequiredInt(obj, "applicationCompanyId", path);
            cardEntity.issuerNetworkId = getRequiredInt(obj, "issuerNetworkId", path);
            cardEntity.issuerDistributorId = getRequiredInt(obj, "issuerDistributorId", path);
            cardEntity.samUid = hexToLong(getRequiredString(obj, "samUid", path));
            cardEntity.algorithmId = getRequiredInt(obj, "algorithmId", path);
            cardEntity.keyVersion = (byte) getRequiredInt(obj, "keyVersion", path);
            cardEntity.environmentNetworkId = getRequiredInt(obj, "environmentNetworkId", path);
            cardEntity.applicationVersion = getRequiredInt(obj, "applicationVersion", path);
            cardEntity.environmentExpiration = intToDate(getRequiredInt(obj, "environmentExpiration", path));
            cardEntity.userBirthDate = intToDate(getNullableInt(obj, "userBirthDate"));
            cardEntity.userProfileType = getRequiredInt(obj, "userProfileType", path);
            cardEntity.userProfileExpiration = intToDate(getRequiredInt(obj, "userProfileExpiration", path));
            cardEntity.userName = normalizeEmpty(getNullableString(obj, "userName"));
            cardEntity.userCredential = normalizeEmpty(getNullableString(obj, "userCitizenId"));
            cardEntity.applicationStatus = getRequiredInt(obj, "applicationStatus", path);
            cardEntity.applicationEventCount = getRequiredInt(obj, "applicationEventCount", path);
            cardEntity.applicationActionsApplied = getRequiredInt(obj, "applicationActionsApplied", path);
            cardEntity.artworkRef = getNullableString(obj, "artworkRef");

            entities.add(cardEntity);
        }
        return entities;
    }

    protected List<ProductEntity> mapProducts(JsonArray products) {
        List<ProductEntity> entities = new ArrayList<>();
        for (int i = 0; i < products.size(); i++) {
            JsonElement el = products.get(i);
            if (!el.isJsonObject()) {
                throw new BackupFieldException("products[" + i + "]", "product entry must be an object");
            }
            JsonObject obj = el.getAsJsonObject();
            String path = "products[" + i + "]";
            ProductEntity productEntity = new ProductEntity();

            productEntity.cardId = hexToLong(getRequiredString(obj, "cardId", path));
            productEntity.productId = hexToShort(getRequiredString(obj, "productId", path));
            productEntity.value = getRequiredInt(obj, "value", path);
            productEntity.valuePointer = getRequiredInt(obj, "valuePointer", path);
            productEntity.priority = getRequiredInt(obj, "priority", path);
            productEntity.pointer = getRequiredInt(obj, "pointer", path);
            productEntity.serial = getRequiredInt(obj, "serial", path);
            productEntity.priceCents = (short) getRequiredInt(obj, "priceCents", path);
            productEntity.valueUnit = getRequiredInt(obj, "valueUnit", path);
            productEntity.minAmountLimit = getRequiredInt(obj, "minAmountLimit", path);
            productEntity.maxAmountLimit = getRequiredInt(obj, "maxAmountLimit", path);
            productEntity.reactivationCount = (short) getRequiredInt(obj, "reactivationCount", path);
            productEntity.lastAppliedActionNumber = (short) getRequiredInt(obj, "lastAppliedActionNumber", path);
            productEntity.distributorNetworkId = getRequiredInt(obj, "distributorNetworkId", path);
            productEntity.distributorCompanyId = (short) getRequiredInt(obj, "distributorCompanyId", path);
            productEntity.distributionDateTime = intToDateTime(getNullableInt(obj, "distributionDateTime"));
            productEntity.distributionSamId = hexToInt(getRequiredString(obj, "distributionSamId", path));
            productEntity.distributingDeviceId = (short) getRequiredInt(obj, "distributingDeviceId", path);
            productEntity.validFrom = intToDateTime(getNullableInt(obj, "validFrom"));
            productEntity.validTo = intToDateTime(getNullableInt(obj, "validTo"));
            productEntity.validDailyStartTime = intToTime(getNullableInt(obj, "validDailyStartTime"));
            productEntity.validDailyEndTime = intToTime(getNullableInt(obj, "validDailyEndTime"));
            productEntity.restrictionRestrictedDays = (byte) getRequiredInt(obj, "restrictionRestrictedDays", path);
            productEntity.restrictionMaxTripsPerDayOfWeek = getRequiredInt(obj, "restrictionMaxTripsPerDayOfWeek", path);
            productEntity.restrictionPassbackTimeMinutes = (short) getRequiredInt(obj, "restrictionPassbackTimeMinutes", path);
            productEntity.restrictionAllowedPassbacks = (byte) getRequiredInt(obj, "restrictionAllowedPassbacks", path);
            productEntity.restrictionTransferTimeLimitMinutes = (short) getRequiredInt(obj, "restrictionTransferTimeLimitMinutes", path);
            productEntity.restrictionAllowedInterchanges = (byte) getRequiredInt(obj, "restrictionAllowedInterchanges", path);
            productEntity.state = getRequiredInt(obj, "state", path);
            productEntity.weekOfYear = getRequiredInt(obj, "weekOfYear", path);
            productEntity.tripsPerDayOfWeek = getRequiredInt(obj, "tripsPerDayOfWeek", path);
            productEntity.totalUsages = getRequiredInt(obj, "totalUsages", path);
            productEntity.lastDebitDateTime = intToDateTime(getNullableInt(obj, "lastDebitDateTime"));
            productEntity.lastDebitEntityId = getRequiredInt(obj, "lastDebitEntityId", path);
            productEntity.lastDebitRouteStationId = getRequiredInt(obj, "lastDebitRouteStationId", path);
            productEntity.lastDebitDeviceId = getRequiredInt(obj, "lastDebitDeviceId", path);

            entities.add(productEntity);
        }
        return entities;
    }

    protected List<EventEntity> mapEvents(JsonArray events) {
        List<EventEntity> entities = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            JsonElement el = events.get(i);
            if (!el.isJsonObject()) {
                throw new BackupFieldException("events[" + i + "]", "event entry must be an object");
            }
            JsonObject obj = el.getAsJsonObject();
            String path = "events[" + i + "]";
            EventEntity eventEntity = new EventEntity();

            eventEntity.cardId = hexToLong(getRequiredString(obj, "cardId", path));
            eventEntity.eventSequence = getRequiredInt(obj, "eventSequence", path);
            eventEntity.productId = hexToInt(getRequiredString(obj, "productId", path));
            eventEntity.productPointer = getRequiredInt(obj, "productPointer", path);
            eventEntity.entityId = getRequiredInt(obj, "entityId", path);
            eventEntity.eventDateTime = intToDateTime(getRequiredInt(obj, "eventDateTime", path));
            eventEntity.eventType = getRequiredInt(obj, "eventType", path);
            eventEntity.amount = getRequiredInt(obj, "amount", path);
            eventEntity.samId = hexToLong(getRequiredString(obj, "samId", path));
            eventEntity.samSequence = hexToLong(getRequiredString(obj, "samSequence", path));
            eventEntity.deviceId = getRequiredInt(obj, "deviceId", path);
            eventEntity.locationId = getRequiredInt(obj, "locationId", path);
            eventEntity.transportType = getRequiredInt(obj, "transportType", path);
            eventEntity.routeStationId = getRequiredInt(obj, "routeStationId", path);
            eventEntity.transferCount = getRequiredInt(obj, "transferCount", path);
            eventEntity.transferLimit = getRequiredInt(obj, "transferLimit", path);
            eventEntity.passbackCount = getRequiredInt(obj, "passbackCount", path);
            eventEntity.refundReason = getRequiredInt(obj, "refundReason", path);
            eventEntity.deviceType = getRequiredInt(obj, "deviceType", path);

            entities.add(eventEntity);
        }
        return entities;
    }

    private String normalizeEmpty(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
