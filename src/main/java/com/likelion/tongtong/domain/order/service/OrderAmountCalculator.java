package com.likelion.tongtong.domain.order.service;

import com.likelion.tongtong.domain.room.entity.Track;
import com.likelion.tongtong.global.exception.BusinessException;
import com.likelion.tongtong.global.response.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OrderAmountCalculator {

    private static final int PORTION_FEE = 500;

    public record MenuLine(int unitPrice, int quantity) {
    }

    public record OrderShares(int sharedMenuPortionPrice, int deliveryFeeShare, int portionFee) {
    }

    public record OrderAmounts(int foodAmount, int deliveryFeeShare, int portionFee, int totalAmount) {
    }

    /** 금액은 원 단위이며, 아껴통의 sharedMenuPrice는 null이다. */
    public OrderShares calculateShares(Track track, int maxParticipants, int deliveryFee, Integer sharedMenuPrice) {
        if (maxParticipants <= 0 || deliveryFee < 0 || (sharedMenuPrice != null && sharedMenuPrice < 0)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        int deliveryFeeShare = deliveryFee / maxParticipants;

        return switch (track) {
            case INDIVIDUAL -> new OrderShares(0, deliveryFeeShare, 0);
            case PORTION -> {
                if (sharedMenuPrice == null) {
                    throw new BusinessException(ErrorCode.INVALID_INPUT);
                }
                yield new OrderShares(sharedMenuPrice / maxParticipants, deliveryFeeShare, PORTION_FEE);
            }
        };
    }

    /** personalItems에는 공유 메뉴의 1인 몫을 포함하지 않는다. */
    public OrderAmounts calculate(Track track, int maxParticipants, int deliveryFee, Integer sharedMenuPrice,
                                  List<MenuLine> personalItems) {
        if (track == Track.INDIVIDUAL && personalItems.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        OrderShares shares = calculateShares(track, maxParticipants, deliveryFee, sharedMenuPrice);
        int foodAmount = shares.sharedMenuPortionPrice();
        for (MenuLine item : personalItems) {
            if (item.unitPrice() < 0 || item.quantity() <= 0) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }
            int lineAmount = Math.multiplyExact(item.unitPrice(), item.quantity());
            foodAmount = Math.addExact(foodAmount, lineAmount);
        }

        int fees = Math.addExact(shares.deliveryFeeShare(), shares.portionFee());
        int totalAmount = Math.addExact(foodAmount, fees);
        return new OrderAmounts(foodAmount, shares.deliveryFeeShare(), shares.portionFee(), totalAmount);
    }
}
