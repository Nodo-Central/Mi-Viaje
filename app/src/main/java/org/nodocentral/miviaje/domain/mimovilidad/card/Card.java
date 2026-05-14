package org.nodocentral.miviaje.domain.mimovilidad.card;

import android.util.Log;

import androidx.annotation.NonNull;

import org.nodocentral.miviaje.Helpers;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Card implements Serializable {
    private final String alias;
    private final String artworkRef;
    private final LocalDateTime lastUpdated;
    private final LocalDate productionDate;

    private final long uid;
    private final Emission emission;
    private final Environment environment;
    private final User user;
    private final ApplicationStatus applicationStatus;
    private final Map<Integer, Product> productList;
    private final List<Event> events;

    public final int WALLET_PRODUCT_ID = 19761;
    public final int CREDIT_PRODUCT_ID = 17201;
    public final int TICKET1_PRODUCT_ID = 16945;
    public final int TICKET2_PRODUCT_ID = 16946;

    public Card(long uid,
                Emission emission,
                Environment environment,
                User user,
                ApplicationStatus applicationStatus,
                Map<Integer, Product> productList,
                List<Event> events,
                String alias,
                String artworkRef,
                LocalDateTime lastUpdated,
                LocalDate productionDate) {
        this.uid = uid;
        this.emission = emission;
        this.environment = environment;
        this.user = user;
        this.applicationStatus = applicationStatus;
        this.productList = productList;
        this.events = events;
        this.alias = alias;
        this.artworkRef = artworkRef;
        this.lastUpdated = lastUpdated;
        this.productionDate = productionDate;
    }

    public int getWalletValue() {
        try {
            int credit = getCreditValue();
            return Objects.requireNonNull(productList.get(WALLET_PRODUCT_ID)).getValue() - credit;
        } catch (NullPointerException e) {
            Log.e("WALLET_ERROR", String.format(
                    "WalletValueError: %s\nproductList.keySet(): %s",
                    e.getMessage(),
                    productList.keySet()
            ));
            return 0;
        }
    }

    public int getCreditValue() {
        try {
            return Objects.requireNonNull(productList.get(CREDIT_PRODUCT_ID)).getValue();
        } catch (NullPointerException e) {
            return 0;
        }
    }

    public int getTicketValue() {
        int value = 0;
        try {
            value += Objects.requireNonNull(productList.get(TICKET1_PRODUCT_ID)).getValue();
        } catch (NullPointerException ignored) {}
        try {
            value += Objects.requireNonNull(productList.get(TICKET2_PRODUCT_ID)).getValue();
        } catch (NullPointerException ignored) {}
        return value;
    }

    public ProductService.State getBPDState() {
        ProductService.State state1;
        ProductService.State state2;
        try {
            state1 = Objects.requireNonNull(productList.get(TICKET1_PRODUCT_ID)).getService().getState();
        } catch (NullPointerException ignored) {
            state1 = ProductService.State.INITIALIZED;
        }
        try {
            state2 = Objects.requireNonNull(productList.get(TICKET2_PRODUCT_ID)).getService().getState();
        } catch (NullPointerException ignored) {
            state2 = ProductService.State.INITIALIZED;
        }
        if (state1.getValue() > state2.getValue())
            return state1;
        else
            return state2;
    }

    public long getUid() {
        return uid;
    }

    public String getUidString() {
        return Helpers.longToHex(uid);
    }

    public Emission getEmission() {
        return emission;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public User getUser() {
        return user;
    }

    public ApplicationStatus getApplicationStatus() {
        return applicationStatus;
    }

    public List<Product> getProductList() {
        ArrayList<Product> list = new ArrayList<>(productList.values());

        list.sort(
                Comparator.comparingInt((Product p) -> p.getState().getSortRank())
                        .reversed()
                        .thenComparing(
                                Comparator.comparingInt((Product p) -> p.getValue() != 0 ? 1 : 0)
                                        .reversed())
                        .thenComparing(
                                Comparator.comparingInt(Product::getPriority)
                                        .reversed()));

        return list;
    }

    public Product getProductById(int productId) {
        return productList.get(productId);
    }

    public List<Event> getEvents() {
        return new ArrayList<>(events);
    }

    /** Solo los eventos que tengan el mismo productId. */
    public List<Event> getEventsByProduct(int productId) {
        return events.stream()
                .filter(e -> e.getProductId() == productId)
                .collect(Collectors.toList());
    }

    public String getAlias() {
        return alias;
    }

    public String getArtworkRef() {
        return artworkRef;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public LocalDate getProductionDate() {
        return productionDate;
    }

    @Override
    @NonNull
    public String toString() {
        return "MiViaje.Card[" +
                "uid=" + getUidString() +
                ", emission=" + emission.toString() +
                ", environment=" + environment.toString() +
                ", user=" + user.toString() +
                ", applicationStatus=" + applicationStatus.toString() +
                ", productList=" + productList.toString() +
                ", events=" + events.toString() +
                ", alias=" + alias +
                ", artworkRef=" + artworkRef +
                ", lastUpdated=" + lastUpdated +
                ", productionDate=" + productionDate +
                "]";
    }
}
