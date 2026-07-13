package com.baemin_mini.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class FeeService {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final BigDecimal BASE_FEE = BigDecimal.valueOf(15000);
    private static final BigDecimal EXTRA_FEE_PER_KM = BigDecimal.valueOf(5000);
    private static final BigDecimal SMALL_ORDER_FEE = BigDecimal.valueOf(5000);
    private static final BigDecimal SMALL_ORDER_THRESHOLD = BigDecimal.valueOf(50000);
    private static final BigDecimal MAX_DELIVERY_FEE = BigDecimal.valueOf(60000);
    private static final double ROAD_MULTIPLIER = 1.25;

    public BigDecimal calculateDistanceKm(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        double lat1Rad = Math.toRadians(lat1.doubleValue());
        double lon1Rad = Math.toRadians(lon1.doubleValue());
        double lat2Rad = Math.toRadians(lat2.doubleValue());
        double lon2Rad = Math.toRadians(lon2.doubleValue());

        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;

        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.pow(Math.sin(dLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double rawDistance = EARTH_RADIUS_KM * c;
        double actualDistance = rawDistance * ROAD_MULTIPLIER;

        return BigDecimal.valueOf(actualDistance).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateDeliveryFee(BigDecimal distanceKm, BigDecimal itemsTotal) {
        BigDecimal fee = BASE_FEE;

        if (distanceKm.compareTo(BigDecimal.valueOf(2)) > 0) {
            BigDecimal extraKm = distanceKm.subtract(BigDecimal.valueOf(2));
            // ceil(extraKm)
            BigDecimal extraKmCeil = extraKm.setScale(0, RoundingMode.CEILING);
            fee = fee.add(extraKmCeil.multiply(EXTRA_FEE_PER_KM));
        }

        if (itemsTotal.compareTo(SMALL_ORDER_THRESHOLD) < 0) {
            fee = fee.add(SMALL_ORDER_FEE);
        }

        if (fee.compareTo(MAX_DELIVERY_FEE) > 0) {
            fee = MAX_DELIVERY_FEE;
        }

        return fee;
    }
}
