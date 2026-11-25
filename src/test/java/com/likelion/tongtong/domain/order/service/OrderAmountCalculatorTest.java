package com.likelion.tongtong.domain.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.likelion.tongtong.domain.order.service.OrderAmountCalculator.MenuLine;
import com.likelion.tongtong.domain.order.service.OrderAmountCalculator.OrderAmounts;
import com.likelion.tongtong.domain.order.service.OrderAmountCalculator.OrderShares;
import com.likelion.tongtong.domain.room.entity.Track;
import com.likelion.tongtong.global.exception.BusinessException;
import com.likelion.tongtong.global.response.ErrorCode;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OrderAmountCalculatorTest {

    private final OrderAmountCalculator calculator = new OrderAmountCalculator();

    @Test
    @DisplayName("아껴통은 개인 메뉴의 단가와 수량을 곱해 합산하고 배달비 분담금을 더한다")
    void calculatesIndividualOrder() {
        // given
        List<MenuLine> personalItems = List.of(new MenuLine(7_000, 2), new MenuLine(1_500, 1));

        // when
        OrderAmounts amounts = calculator.calculate(Track.INDIVIDUAL, 4, 2_000, null, personalItems);

        // then
        assertThat(amounts).isEqualTo(new OrderAmounts(15_500, 500, 0, 16_000));
    }

    @Test
    @DisplayName("나눠통 기준 예시의 최종 주문 금액은 10,450원이다")
    void calculatesPortionOrder() {
        // given
        List<MenuLine> personalItems = List.of(new MenuLine(2_500, 1));

        // when
        OrderAmounts amounts = calculator.calculate(Track.PORTION, 4, 2_000, 27_800, personalItems);

        // then
        assertThat(amounts).isEqualTo(new OrderAmounts(9_450, 500, 500, 10_450));
    }

    @Test
    @DisplayName("공유 메뉴와 배달비를 모집 정원으로 각각 내림하고 잔액을 참여자에게 청구하지 않는다")
    void roundsDownEachShare() {
        // given
        int maxParticipants = 3;
        int sharedMenuPrice = 10_001;
        int deliveryFee = 2_000;

        // when
        OrderShares shares = calculator.calculateShares(Track.PORTION, maxParticipants, deliveryFee, sharedMenuPrice);

        // then
        assertThat(shares).isEqualTo(new OrderShares(3_333, 666, 500));
        assertThat(sharedMenuPrice - shares.sharedMenuPortionPrice() * maxParticipants).isEqualTo(2);
        assertThat(deliveryFee - shares.deliveryFeeShare() * maxParticipants).isEqualTo(2);
    }

    @Test
    @DisplayName("나눠통은 개인 메뉴가 비어 있어도 분담 조회와 같은 금액으로 계산한다")
    void calculatesPortionOrderWithoutPersonalItems() {
        // given
        OrderShares shares = calculator.calculateShares(Track.PORTION, 3, 2_000, 10_001);
        List<MenuLine> personalItems = List.of();

        // when
        OrderAmounts amounts = calculator.calculate(Track.PORTION, 3, 2_000, 10_001, personalItems);

        // then
        assertThat(amounts).isEqualTo(new OrderAmounts(3_333, 666, 500, 4_499));
        assertThat(amounts.foodAmount()).isEqualTo(shares.sharedMenuPortionPrice());
        assertThat(amounts.deliveryFeeShare()).isEqualTo(shares.deliveryFeeShare());
        assertThat(amounts.portionFee()).isEqualTo(shares.portionFee());
    }

    @Test
    @DisplayName("아껴통은 개인 메뉴가 비어 있으면 INVALID_INPUT으로 거절한다")
    void rejectsIndividualOrderWithoutPersonalItems() {
        // given
        List<MenuLine> personalItems = List.of();

        // when & then
        assertThatThrownBy(() -> calculator.calculate(Track.INDIVIDUAL, 4, 2_000, null, personalItems))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT));
    }

    @Test
    @DisplayName("나눠통은 공유 메뉴 가격이 없으면 INVALID_INPUT으로 거절한다")
    void rejectsPortionSharesWithoutSharedMenuPrice() {
        // given
        Integer sharedMenuPrice = null;

        // when & then
        assertThatThrownBy(() -> calculator.calculateShares(Track.PORTION, 4, 2_000, sharedMenuPrice))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT));
    }

    @Test
    @DisplayName("나눠통은 공유 메뉴 가격이 음수면 INVALID_INPUT으로 거절한다")
    void rejectsNegativeSharedMenuPrice() {
        // given
        int sharedMenuPrice = -1;

        // when & then
        assertThatThrownBy(() -> calculator.calculateShares(Track.PORTION, 4, 2_000, sharedMenuPrice))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    @DisplayName("모집 정원이 0 이하면 배달비를 나누기 전에 INVALID_INPUT으로 거절한다")
    void rejectsNonPositiveMaxParticipants(int maxParticipants) {
        // given
        int deliveryFee = 2_000;

        // when & then
        assertThatThrownBy(() -> calculator.calculateShares(Track.INDIVIDUAL, maxParticipants, deliveryFee, null))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT));
    }

    @Test
    @DisplayName("배달비가 음수면 INVALID_INPUT으로 거절한다")
    void rejectsNegativeDeliveryFee() {
        // given
        int deliveryFee = -1;

        // when & then
        assertThatThrownBy(() -> calculator.calculateShares(Track.INDIVIDUAL, 4, deliveryFee, null))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT));
    }

    @Test
    @DisplayName("개인 메뉴 단가가 음수면 INVALID_INPUT으로 거절한다")
    void rejectsNegativePersonalMenuPrice() {
        // given
        List<MenuLine> personalItems = List.of(new MenuLine(-1, 1));

        // when & then
        assertThatThrownBy(() -> calculator.calculate(Track.INDIVIDUAL, 4, 0, null, personalItems))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    @DisplayName("개인 메뉴 수량이 0 이하면 INVALID_INPUT으로 거절한다")
    void rejectsNonPositivePersonalMenuQuantity(int quantity) {
        // given
        List<MenuLine> personalItems = List.of(new MenuLine(1_000, quantity));

        // when & then
        assertThatThrownBy(() -> calculator.calculate(Track.INDIVIDUAL, 4, 0, null, personalItems))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT));
    }

    @Test
    @DisplayName("아껴통과 나눠통에서 메뉴 가격과 배달비 0원을 계산할 수 있다")
    void acceptsZeroAmounts() {
        // given
        List<MenuLine> personalItems = List.of(new MenuLine(0, 1));

        // when
        OrderAmounts individual = calculator.calculate(Track.INDIVIDUAL, 4, 0, null, personalItems);
        OrderAmounts portion = calculator.calculate(Track.PORTION, 4, 0, 0, personalItems);

        // then
        assertThat(individual).isEqualTo(new OrderAmounts(0, 0, 0, 0));
        assertThat(portion).isEqualTo(new OrderAmounts(0, 0, 500, 500));
    }

    @Test
    @DisplayName("개인 메뉴의 단가와 수량을 곱한 금액이 정수 범위를 넘으면 계산에 실패한다")
    void detectsMenuAmountOverflow() {
        // given
        List<MenuLine> personalItems = List.of(new MenuLine(Integer.MAX_VALUE, 2));

        // when & then
        assertThatThrownBy(() -> calculator.calculate(Track.INDIVIDUAL, 2, 0, null, personalItems))
                .isInstanceOf(ArithmeticException.class);
    }

    @Test
    @DisplayName("음식값과 배달비 분담금을 합친 금액이 정수 범위를 넘으면 계산에 실패한다")
    void detectsTotalAmountOverflow() {
        // given
        List<MenuLine> personalItems = List.of(new MenuLine(Integer.MAX_VALUE, 1));

        // when & then
        assertThatThrownBy(() -> calculator.calculate(Track.INDIVIDUAL, 2, 2, null, personalItems))
                .isInstanceOf(ArithmeticException.class);
    }
}
