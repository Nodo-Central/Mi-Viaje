package org.nodocentral.miviaje.data.mappers;

import org.nodocentral.miviaje.data.room.CardEntity;
import org.nodocentral.miviaje.data.room.EventEntity;
import org.nodocentral.miviaje.data.room.ProductEntity;
import org.nodocentral.miviaje.domain.mimovilidad.card.ApplicationStatus;
import org.nodocentral.miviaje.domain.mimovilidad.card.Card;
import org.nodocentral.miviaje.domain.mimovilidad.card.Emission;
import org.nodocentral.miviaje.domain.mimovilidad.card.Environment;
import org.nodocentral.miviaje.domain.mimovilidad.card.Event;
import org.nodocentral.miviaje.domain.mimovilidad.card.Product;
import org.nodocentral.miviaje.domain.mimovilidad.card.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CardMapper {
    private CardMapper() {
    }

    public static Card toDomain(CardEntity entity,
                                List<ProductEntity> products,
                                List<EventEntity> events) {
        if (entity == null) return null;

        Emission emission = new Emission(
                entity.country,
                entity.serialNumber,
                entity.emissionExpiration,
                new Emission.ApplicationOwner(entity.applicationNetworkId, entity.applicationCompanyId),
                new Emission.Issuer(entity.issuerNetworkId, entity.issuerDistributorId),
                new Emission.SecurityVersion(entity.samUid, entity.algorithmId, entity.keyVersion)
        );

        Environment environment = new Environment(
                entity.applicationVersion,
                entity.environmentNetworkId,
                entity.environmentExpiration
        );

        User.Profile profile = new User.Profile(
                User.Profile.Type.fromInt(entity.userProfileType),
                entity.userProfileExpiration,
                normalize(entity.userName),
                normalize(entity.userCredential)
        );
        User user = new User(entity.userBirthDate, profile);

        ApplicationStatus status = new ApplicationStatus(
                ApplicationStatus.State.fromInt(entity.applicationStatus),
                entity.applicationEventCount,
                entity.applicationActionsApplied
        );

        Map<Integer, Product> productMap = new HashMap<>();
        if (products != null) {
            for (ProductEntity pe : products) {
                if (pe.cardId == entity.uid) {
                    Product p = ProductMapper.toDomain(pe);
                    productMap.put((int) p.getId(), p);
                }
            }
        }

        List<Event> eventList = new ArrayList<>();
        if (events != null) {
            for (EventEntity ee : events) {
                if (ee.cardId == entity.uid) {
                    eventList.add(EventMapper.toDomain(ee, productMap));
                }
            }
        }

        return new Card(
                entity.uid,
                emission,
                environment,
                user,
                status,
                productMap,
                eventList,
                entity.alias,
                entity.artworkRef,
                entity.lastUpdated,
                entity.productionDate
        );
    }

    public static CardEntity toEntity(Card card) {
        if (card == null) return null;
        CardEntity entity = new CardEntity();

        entity.uid = card.getUid();
        entity.alias = card.getAlias();
        entity.artworkRef = card.getArtworkRef();
        entity.lastUpdated = card.getLastUpdated();
        entity.productionDate = card.getProductionDate();

        Emission emission = card.getEmission();
        entity.country = emission.getCountry().getCountry();
        entity.serialNumber = emission.getSerialNumber();
        entity.emissionExpiration = emission.getExpirationDate();
        entity.applicationNetworkId = emission.getApplicationOwner().getNetworkId();
        entity.applicationCompanyId = emission.getApplicationOwner().getCompanyId();
        entity.issuerNetworkId = emission.getIssuer().getNetworkId();
        entity.issuerDistributorId = emission.getIssuer().getDistributorId();
        entity.samUid = emission.getSecurityVersion().getSamUid();
        entity.algorithmId = emission.getSecurityVersion().getAlgorithmId();
        entity.keyVersion = emission.getSecurityVersion().getKeyVersion();

        Environment environment = card.getEnvironment();
        entity.environmentNetworkId = environment.getNetworkId();
        entity.applicationVersion = environment.getApplicationVersion();
        entity.environmentExpiration = environment.getExpirationDate();

        User user = card.getUser();
        entity.userBirthDate = user.getBirthDate();
        entity.userName = normalize(user.getProfile().getName());
        entity.userProfileType = user.getProfile().getType().getValue();
        entity.userProfileExpiration = user.getProfile().getExpirationDate();
        entity.userCredential = normalize(user.getProfile().getCredential());

        ApplicationStatus status = card.getApplicationStatus();
        entity.applicationStatus = status.getState().getValue();
        entity.applicationEventCount = status.getEventCount();
        entity.applicationActionsApplied = status.getActionsApplied();

        return entity;
    }

    private static String normalize(String string) {
        return (string == null || string.isBlank()) ? null : string;
    }
}
