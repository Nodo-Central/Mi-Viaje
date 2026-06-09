package org.nodocentral.miviaje.data.backup.exporting;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.nodocentral.miviaje.Helpers;
import org.nodocentral.miviaje.data.repository.ArtworkRepository;
import org.nodocentral.miviaje.data.room.CardEntity;
import org.nodocentral.miviaje.data.room.EventEntity;
import org.nodocentral.miviaje.data.room.MiViajeDatabase;
import org.nodocentral.miviaje.data.room.ProductEntity;
import org.nodocentral.miviaje.domain.artwork.Artwork;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class BackupExporter {
    private static final ZoneOffset EXPORT_ZONE_OFFSET = ZoneOffset.ofHours(-6);
    private final Clock clock;

    public BackupExporter() {
        this(Clock.systemDefaultZone());
    }

    BackupExporter(Clock clock) {
        this.clock = clock;
    }

    public String exportData(MiViajeDatabase database, ArtworkRepository artworkRepository) {
        return buildBackupJson(
                database.eventDao().getAll(),
                database.productDao().getAll(),
                database.cardDao().getAll(),
                artworkRepository.getAll()
        );
    }

    public String exportCard(MiViajeDatabase database,
                             ArtworkRepository artworkRepository,
                             long cardUid) {
        CardEntity card = database.cardDao().get(cardUid);
        if (card == null) {
            throw new IllegalArgumentException("Card not found for uid " + Helpers.longToHex(cardUid));
        }

        List<CardEntity> cards = new ArrayList<>();
        cards.add(card);

        List<Artwork> artworks = new ArrayList<>();
        Artwork artwork = artworkRepository.getByRef(card.artworkRef);
        if (artwork != null) {
            artworks.add(artwork);
        }

        return buildBackupJson(
                database.eventDao().getAllForCard(cardUid),
                database.productDao().getAllForCard(cardUid),
                cards,
                artworks
        );
    }

    public String buildCardExportFileName(String cardAlias, long cardUid) {
        String nameToken = normalizeText(cardAlias);
        if (nameToken == null) {
            nameToken = Helpers.longToHex(cardUid);
        }

        String sanitizedToken = sanitizeFileNameToken(nameToken);
        if (sanitizedToken.isBlank()) {
            sanitizedToken = Helpers.longToHex(cardUid);
        }

        return "card_" + sanitizedToken + "_" + LocalDate.now(clock) + ".json";
    }

    private String buildBackupJson(List<EventEntity> eventEntities,
                                   List<ProductEntity> productEntities,
                                   List<CardEntity> cardEntities,
                                   List<Artwork> artworkEntities) {
        JsonObject root = new JsonObject();
        root.addProperty("schemaVersion", 3);
        root.addProperty("exportedAt", System.currentTimeMillis());

        JsonArray events = new JsonArray();
        for (EventEntity event : eventEntities) {
            events.add(mapEvent(event));
        }

        JsonArray products = new JsonArray();
        for (ProductEntity product : productEntities) {
            products.add(mapProduct(product));
        }

        JsonArray cards = new JsonArray();
        for (CardEntity card : cardEntities) {
            cards.add(mapCard(card));
        }

        JsonArray artworks = new JsonArray();
        for (Artwork artwork : artworkEntities) {
            artworks.add(mapArtwork(artwork));
        }

        root.add("events", events);
        root.add("products", products);
        root.add("cards", cards);
        root.add("artworks", artworks);

        return new GsonBuilder().setPrettyPrinting().create().toJson(root);
    }

    private JsonObject mapEvent(EventEntity event) {
        JsonObject mapped = new JsonObject();
        mapped.addProperty("cardId", Helpers.longToHex(event.cardId));
        mapped.addProperty("eventSequence", event.eventSequence);
        mapped.addProperty("productId", Helpers.longToHex(event.productId, 4));
        mapped.addProperty("productPointer", event.productPointer);
        mapped.addProperty("entityId", event.entityId);
        mapped.addProperty("eventDateTime", event.eventDateTime.toEpochSecond(EXPORT_ZONE_OFFSET));
        mapped.addProperty("eventType", event.eventType);
        mapped.addProperty("amount", event.amount);
        mapped.addProperty("samId", Helpers.longToHex(event.samId));
        mapped.addProperty("samSequence", Helpers.longToHex(event.samSequence));
        mapped.addProperty("deviceId", event.deviceId);
        mapped.addProperty("locationId", event.locationId);
        mapped.addProperty("transportType", event.transportType);
        mapped.addProperty("routeStationId", event.routeStationId);
        mapped.addProperty("transferCount", event.transferCount);
        mapped.addProperty("transferLimit", event.transferLimit);
        mapped.addProperty("passbackCount", event.passbackCount);
        mapped.addProperty("refundReason", event.refundReason);
        mapped.addProperty("deviceType", event.deviceType);
        return mapped;
    }

    private JsonObject mapProduct(ProductEntity product) {
        JsonObject mapped = new JsonObject();
        mapped.addProperty("cardId", Helpers.longToHex(product.cardId));
        mapped.addProperty("productId", Helpers.longToHex(product.productId, 4));
        mapped.addProperty("value", product.value);
        mapped.addProperty("valuePointer", product.valuePointer);
        mapped.addProperty("priority", product.priority);
        mapped.addProperty("pointer", product.pointer);
        mapped.addProperty("serial", product.serial);
        mapped.addProperty("priceCents", product.priceCents);
        mapped.addProperty("valueUnit", product.valueUnit);
        mapped.addProperty("minAmountLimit", product.minAmountLimit);
        mapped.addProperty("maxAmountLimit", product.maxAmountLimit);
        mapped.addProperty("reactivationCount", product.reactivationCount);
        mapped.addProperty("lastAppliedActionNumber", product.lastAppliedActionNumber);
        mapped.addProperty("distributorNetworkId", product.distributorNetworkId);
        mapped.addProperty("distributorCompanyId", product.distributorCompanyId);
        mapped.addProperty("distributionDateTime", product.distributionDateTime != null ? product.distributionDateTime.toEpochSecond(EXPORT_ZONE_OFFSET) : null);
        mapped.addProperty("distributionSamId", Helpers.longToHex(product.distributionSamId, 16));
        mapped.addProperty("distributingDeviceId", product.distributingDeviceId);
        mapped.addProperty("validFrom", product.validFrom != null ? product.validFrom.toEpochSecond(EXPORT_ZONE_OFFSET) : null);
        mapped.addProperty("validTo", product.validTo != null ? product.validTo.toEpochSecond(EXPORT_ZONE_OFFSET) : null);
        mapped.addProperty("validDailyStartTime", product.validDailyStartTime != null ? product.validDailyStartTime.toSecondOfDay() : null);
        mapped.addProperty("validDailyEndTime", product.validDailyEndTime != null ? product.validDailyEndTime.toSecondOfDay() : null);
        mapped.addProperty("restrictionRestrictedDays", product.restrictionRestrictedDays);
        mapped.addProperty("restrictionMaxTripsPerDayOfWeek", product.restrictionMaxTripsPerDayOfWeek);
        mapped.addProperty("restrictionPassbackTimeMinutes", product.restrictionPassbackTimeMinutes);
        mapped.addProperty("restrictionAllowedPassbacks", product.restrictionAllowedPassbacks);
        mapped.addProperty("restrictionTransferTimeLimitMinutes", product.restrictionTransferTimeLimitMinutes);
        mapped.addProperty("restrictionAllowedInterchanges", product.restrictionAllowedInterchanges);
        mapped.addProperty("state", product.state);
        mapped.addProperty("weekOfYear", product.weekOfYear);
        mapped.addProperty("tripsPerDayOfWeek", product.tripsPerDayOfWeek);
        mapped.addProperty("totalUsages", product.totalUsages);
        mapped.addProperty("lastDebitDateTime", product.lastDebitDateTime != null ? product.lastDebitDateTime.toEpochSecond(EXPORT_ZONE_OFFSET) : null);
        mapped.addProperty("lastDebitEntityId", product.lastDebitEntityId);
        mapped.addProperty("lastDebitRouteStationId", product.lastDebitRouteStationId);
        mapped.addProperty("lastDebitDeviceId", product.lastDebitDeviceId);
        return mapped;
    }

    private JsonObject mapCard(CardEntity card) {
        JsonObject mapped = new JsonObject();
        mapped.addProperty("uid", Helpers.longToHex(card.uid));
        mapped.addProperty("alias", card.alias);
        mapped.addProperty("lastUpdated", card.lastUpdated != null ? card.lastUpdated.toEpochSecond(EXPORT_ZONE_OFFSET) : null);
        mapped.addProperty("productionDate", card.productionDate != null ? card.productionDate.toEpochDay() : null);
        mapped.addProperty("country", card.country);
        mapped.addProperty("serialNumber", Helpers.longToHex(card.serialNumber, 6));
        mapped.addProperty("emissionExpiration", card.emissionExpiration != null ? card.emissionExpiration.toEpochDay() : null);
        mapped.addProperty("applicationNetworkId", card.applicationNetworkId);
        mapped.addProperty("applicationCompanyId", card.applicationCompanyId);
        mapped.addProperty("issuerNetworkId", card.issuerNetworkId);
        mapped.addProperty("issuerDistributorId", card.issuerDistributorId);
        mapped.addProperty("samUid", Helpers.longToHex(card.samUid));
        mapped.addProperty("algorithmId", card.algorithmId);
        mapped.addProperty("keyVersion", card.keyVersion);
        mapped.addProperty("environmentNetworkId", card.environmentNetworkId);
        mapped.addProperty("applicationVersion", card.applicationVersion);
        mapped.addProperty("environmentExpiration", card.environmentExpiration != null ? card.environmentExpiration.toEpochDay() : null);
        mapped.addProperty("userBirthDate", card.userBirthDate != null ? card.userBirthDate.toEpochDay() : null);
        mapped.addProperty("userProfileType", card.userProfileType);
        mapped.addProperty("userProfileExpiration", card.userProfileExpiration != null ? card.userProfileExpiration.toEpochDay() : null);
        mapped.addProperty("userName", card.userName);
        mapped.addProperty("userCitizenId", card.userCredential);
        mapped.addProperty("applicationStatus", card.applicationStatus);
        mapped.addProperty("applicationEventCount", card.applicationEventCount);
        mapped.addProperty("applicationActionsApplied", card.applicationActionsApplied);
        mapped.addProperty("artworkRef", card.artworkRef);
        return mapped;
    }

    private JsonObject mapArtwork(Artwork artwork) {
        JsonObject mapped = new JsonObject();
        mapped.addProperty("id", artwork.getId());
        mapped.addProperty("displayName", artwork.getDisplayName());
        mapped.addProperty("relativePath", artwork.getRelativePath());
        mapped.addProperty("mimeType", artwork.getMimeType());
        mapped.addProperty("sha256", artwork.getSha256());
        mapped.addProperty("createdAt", artwork.getCreatedAt() != null ? artwork.getCreatedAt().toEpochSecond(EXPORT_ZONE_OFFSET) : null);
        return mapped;
    }

    private static String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private static String sanitizeFileNameToken(String input) {
        String sanitized = input.replaceAll("[^a-zA-Z0-9._-]+", "_");
        sanitized = sanitized.replaceAll("_+", "_");
        sanitized = sanitized.replaceAll("^[_\\.]+", "");
        sanitized = sanitized.replaceAll("[_\\.]+$", "");
        return sanitized;
    }
}
