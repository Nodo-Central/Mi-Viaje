package org.nodocentral.miviaje.data.mappers;

import androidx.annotation.NonNull;

import org.nodocentral.miviaje.data.room.ProductEntity;
import org.nodocentral.miviaje.domain.mimovilidad.card.Product;
import org.nodocentral.miviaje.domain.mimovilidad.card.ProductContract;
import org.nodocentral.miviaje.domain.mimovilidad.card.ProductService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ProductMapper {
    private ProductMapper() { }

    public static Map<Integer, Product> toDomain(List<ProductEntity> productEntities) {
        Map<Integer, Product> products = new HashMap<>();
        for (ProductEntity pe : productEntities) {
            Product p = ProductMapper.toDomain(pe);
            products.put((int) p.getId(), p);
        }
        return products;
    }

    public static Product toDomain(ProductEntity entity) {
        if (entity == null) return null;

        ProductContract.Retailer retailer = new ProductContract.Retailer(
                entity.distributorNetworkId,
                entity.distributorCompanyId
        );

        ProductContract.DistributionInfo distributionInfo = new ProductContract.DistributionInfo(
            entity.distributionDateTime,
            entity.distributionSamId,
            entity.distributingDeviceId
        );

        ProductContract contract = getContract(entity, retailer, distributionInfo);

        ProductService service = new ProductService(
                ProductService.State.fromInt(entity.state),
                entity.weekOfYear,
                entity.tripsPerDayOfWeek,
                entity.totalUsages,
                entity.lastDebitDateTime,
                entity.lastDebitEntityId,
                entity.lastDebitRouteStationId,
                entity.lastDebitDeviceId
        );

        return new Product(
                entity.productId,
                entity.value,
                entity.valuePointer,
                entity.priority,
                contract,
                service
        );
    }

    @NonNull
    private static ProductContract getContract(ProductEntity entity, ProductContract.Retailer retailer, ProductContract.DistributionInfo distributionInfo) {
        ProductContract.Validity validity = new ProductContract.Validity(
                entity.validFrom,
                entity.validTo,
                entity.validDailyStartTime,
                entity.validDailyEndTime
        );

        ProductContract.Restrictions restrictions = new ProductContract.Restrictions(
                entity.restrictionRestrictedDays,
                entity.restrictionMaxTripsPerDayOfWeek,
                entity.restrictionPassbackTimeMinutes,
                entity.restrictionAllowedPassbacks,
                entity.restrictionTransferTimeLimitMinutes,
                entity.restrictionAllowedInterchanges
        );

        return new ProductContract(
                entity.productId,
                entity.serial,
                entity.pointer,
                entity.reactivationCount,
                entity.lastAppliedActionNumber,
                entity.priceCents,
                ProductContract.ValueUnit.fromInt(entity.valueUnit),
                entity.minAmountLimit,
                entity.maxAmountLimit,
                retailer,
                distributionInfo,
                validity,
                restrictions
        );
    }

    public static ProductEntity toEntity(Product product, long cardId) {
        if (product == null) return null;
        ProductEntity entity = new ProductEntity();
        entity.productId = product.getId();
        entity.value = product.getValue();
        entity.valuePointer = product.getValuePointer();
        entity.priority = product.getPriority();
        entity.cardId = cardId;

        ProductContract contract = product.getContract();
        entity.pointer = contract.getProductPointer();
        entity.serial = contract.getProductSerial();
        entity.priceCents = contract.getPriceCents();
        entity.valueUnit = contract.getValueUnit().getValue();
        entity.minAmountLimit = contract.getMinAmountLimit();
        entity.maxAmountLimit = contract.getMaxAmountLimit();
        entity.reactivationCount = contract.getReactivationCount();
        entity.lastAppliedActionNumber = contract.getLastAppliedActionNumber();
        entity.distributorNetworkId = contract.getRetailer().getDistributorNetworkId();
        entity.distributorCompanyId = contract.getRetailer().getDistributorCompanyId();
        entity.distributionDateTime = contract.getDistributionInfo().getDistributionDateTime();
        entity.distributionSamId = contract.getDistributionInfo().getSamId();
        entity.distributingDeviceId = contract.getDistributionInfo().getDistributingDeviceId();
        entity.validFrom = contract.getValidity().getValidFrom();
        entity.validTo = contract.getValidity().getValidTo();
        entity.validDailyStartTime = contract.getValidity().getDailyStartTime();
        entity.validDailyEndTime = contract.getValidity().getDailyEndTime();
        entity.restrictionRestrictedDays = contract.getRestrictions().getRestrictedDays();
        entity.restrictionMaxTripsPerDayOfWeek = contract.getRestrictions().getMaxTripsPerDayOfWeek();
        entity.restrictionPassbackTimeMinutes = contract.getRestrictions().getPassbackTimeMinutes();
        entity.restrictionAllowedPassbacks = contract.getRestrictions().getAllowedPassbacks();
        entity.restrictionTransferTimeLimitMinutes = contract.getRestrictions().getTransferTimeLimitMinutes();
        entity.restrictionAllowedInterchanges = contract.getRestrictions().getAllowedInterchanges();

        ProductService service = product.getService();
        entity.state = service.getState().getValue();
        entity.weekOfYear = service.getWeekOfYear();
        entity.tripsPerDayOfWeek = service.getTripsPerDayOfWeek();
        entity.totalUsages = service.getTotalUsages();
        entity.lastDebitDateTime = service.getLastDebitDateTime();
        entity.lastDebitEntityId = service.getLastDebitEntityId();
        entity.lastDebitRouteStationId = service.getLastDebitRouteStationId();
        entity.lastDebitDeviceId = service.getLastDebitDeviceId();

        return entity;
    }
}
