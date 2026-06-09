package org.nodocentral.miviaje.data.mappers;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.nodocentral.miviaje.data.room.ProductEntity;
import org.nodocentral.miviaje.domain.mimovilidad.card.Product;
import org.nodocentral.miviaje.domain.mimovilidad.card.ProductContract;
import org.nodocentral.miviaje.domain.mimovilidad.card.ProductService;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class ProductMapperTest {
    private static final long DISTRIBUTION_SAM_ID = 0x00000001FFFFFFFFL;

    @Test
    public void toDomain_preservesLongDistributionSamId() {
        Product product = ProductMapper.toDomain(buildProductEntity());

        assertEquals(DISTRIBUTION_SAM_ID, product.getContract().getDistributionInfo().getSamId());
    }

    @Test
    public void toEntity_preservesLongDistributionSamId() {
        ProductEntity entity = ProductMapper.toEntity(buildProduct(), 1L);

        assertEquals(DISTRIBUTION_SAM_ID, entity.distributionSamId);
    }

    private static ProductEntity buildProductEntity() {
        ProductEntity entity = new ProductEntity();
        entity.cardId = 1L;
        entity.productId = 0x1234;
        entity.value = 500;
        entity.valuePointer = 9;
        entity.priority = 1;
        entity.pointer = 7;
        entity.serial = 8;
        entity.priceCents = 900;
        entity.valueUnit = ProductContract.ValueUnit.MXN_CENT.getValue();
        entity.minAmountLimit = 0;
        entity.maxAmountLimit = 1000;
        entity.reactivationCount = 1;
        entity.lastAppliedActionNumber = 2;
        entity.distributorNetworkId = 50;
        entity.distributorCompanyId = 60;
        entity.distributionDateTime = LocalDateTime.of(2026, 4, 20, 9, 30);
        entity.distributionSamId = DISTRIBUTION_SAM_ID;
        entity.distributingDeviceId = 80;
        entity.validFrom = LocalDateTime.of(2026, 4, 21, 0, 0);
        entity.validTo = LocalDateTime.of(2026, 5, 21, 23, 59);
        entity.validDailyStartTime = LocalTime.of(5, 0);
        entity.validDailyEndTime = LocalTime.of(23, 0);
        entity.restrictionRestrictedDays = 0;
        entity.restrictionMaxTripsPerDayOfWeek = 20;
        entity.restrictionPassbackTimeMinutes = 30;
        entity.restrictionAllowedPassbacks = 2;
        entity.restrictionTransferTimeLimitMinutes = 45;
        entity.restrictionAllowedInterchanges = 1;
        entity.state = ProductService.State.ACTIVATED.getValue();
        entity.weekOfYear = 17;
        entity.tripsPerDayOfWeek = 5;
        entity.totalUsages = 9;
        entity.lastDebitDateTime = LocalDateTime.of(2026, 4, 24, 11, 22, 10);
        entity.lastDebitEntityId = 10;
        entity.lastDebitRouteStationId = 11;
        entity.lastDebitDeviceId = 12;
        return entity;
    }

    private static Product buildProduct() {
        return new Product(
                (short) 0x1234,
                500,
                9,
                1,
                new ProductContract(
                        (short) 0x1234,
                        8,
                        7,
                        (short) 1,
                        (short) 2,
                        (short) 900,
                        ProductContract.ValueUnit.MXN_CENT,
                        0,
                        1000,
                        new ProductContract.Retailer(50, (short) 60),
                        new ProductContract.DistributionInfo(
                                LocalDateTime.of(2026, 4, 20, 9, 30),
                                DISTRIBUTION_SAM_ID,
                                (short) 80
                        ),
                        new ProductContract.Validity(
                                LocalDateTime.of(2026, 4, 21, 0, 0),
                                LocalDateTime.of(2026, 5, 21, 23, 59),
                                LocalTime.of(5, 0),
                                LocalTime.of(23, 0)
                        ),
                        new ProductContract.Restrictions((byte) 0, 20, (short) 30, (byte) 2, (short) 45, (byte) 1)
                ),
                new ProductService(
                        ProductService.State.ACTIVATED,
                        17,
                        5,
                        9,
                        LocalDateTime.of(2026, 4, 24, 11, 22, 10),
                        10,
                        11,
                        12
                )
        );
    }
}
