package com.likelion.tongtong.domain.room.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.likelion.tongtong.domain.order.entity.PaymentMethod;
import com.likelion.tongtong.domain.room.dto.CreateRoomRequest.MenuItem;
import com.likelion.tongtong.domain.room.dto.CreateRoomRequest.SharedMenu;
import com.likelion.tongtong.domain.room.entity.Track;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CreateRoomRequestTest {

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();

    @AfterAll
    static void closeValidatorFactory() {
        VALIDATOR_FACTORY.close();
    }

    @Test
    @DisplayName("아껴통 개인 메뉴와 나눠통 공유 메뉴가 있으면 요청 검증을 통과한다")
    void acceptsValidMenuSelections() {
        // given
        var individual = request(Track.INDIVIDUAL, null, List.of(new MenuItem(3L, 1)), 4, 10);
        var portion = request(Track.PORTION, new SharedMenu(2L), List.of(), 2, 60);
        var portionWithPersonalItem = request(Track.PORTION, new SharedMenu(2L), List.of(new MenuItem(3L, 1)), 6, 10);

        // when and then
        var validator = VALIDATOR_FACTORY.getValidator();
        assertThat(validator.validate(individual)).isEmpty();
        assertThat(validator.validate(portion)).isEmpty();
        assertThat(validator.validate(portionWithPersonalItem)).isEmpty();
    }

    @Test
    @DisplayName("트랙과 맞지 않는 메뉴 구성은 검증에 실패한다")
    void rejectsInvalidMenuSelections() {
        // given
        var individualWithoutItems = request(Track.INDIVIDUAL, null, List.of(), 4, 10);
        var individualWithSharedMenu = request(Track.INDIVIDUAL, new SharedMenu(2L), List.of(new MenuItem(3L, 1)), 4, 10);
        var portionWithoutSharedMenu = request(Track.PORTION, null, List.of(), 4, 10);

        // when and then
        var validator = VALIDATOR_FACTORY.getValidator();
        for (CreateRoomRequest request : List.of(individualWithoutItems, individualWithSharedMenu, portionWithoutSharedMenu)) {
            assertThat(validator.validate(request)).extracting(error -> error.getPropertyPath().toString())
                    .containsExactly("menuSelectionValid");
        }
    }

    @Test
    @DisplayName("정원 범위, 모집 시간 단위와 중첩 메뉴 수량을 검증한다")
    void rejectsInvalidCapacityDurationAndQuantity() {
        // given
        var request = new CreateRoomRequest(0L, Track.PORTION, new SharedMenu(0L), List.of(new MenuItem(-1L, 0)),
                -1L, 7, 15, PaymentMethod.KAKAO_PAY);

        // when and then
        assertThat(VALIDATOR_FACTORY.getValidator().validate(request))
                .extracting(error -> error.getPropertyPath().toString(), error -> error.getMessage())
                .containsExactlyInAnyOrder(
                        tuple("storeId", "가게 ID를 1 이상으로 입력해 주세요."),
                        tuple("spotId", "통통 스팟 ID를 1 이상으로 입력해 주세요."),
                        tuple("maxParticipants", "모집 인원은 2명 이상 6명 이하로 정해 주세요."),
                        tuple("recruitMinutesStepValid", "모집 시간을 10분 단위로 정해 주세요."),
                        tuple("sharedMenu.menuId", "함께 나눌 메뉴 ID를 1 이상으로 입력해 주세요."),
                        tuple("items[0].menuId", "개인 메뉴 ID를 1 이상으로 입력해 주세요."),
                        tuple("items[0].quantity", "메뉴 수량을 1개 이상으로 입력해 주세요."));
        assertThat(VALIDATOR_FACTORY.getValidator().validate(
                request(Track.PORTION, new SharedMenu(null), List.of(), 4, 10)))
                .extracting(error -> error.getPropertyPath().toString(), error -> error.getMessage())
                .containsExactly(tuple("sharedMenu.menuId", "함께 나눌 메뉴 ID를 입력해 주세요."));
    }

    @Test
    @DisplayName("정원과 모집 시간이 허용 범위를 벗어나면 해요체 안내 문구를 반환한다")
    void rejectsOutOfRangeValuesWithValidationMessages() {
        // given
        var validator = VALIDATOR_FACTORY.getValidator();
        var belowMinimum = request(Track.PORTION, new SharedMenu(2L), List.of(), 1, 0);
        var aboveMaximum = request(Track.PORTION, new SharedMenu(2L), List.of(), 7, 70);

        // when and then
        for (CreateRoomRequest request : List.of(belowMinimum, aboveMaximum)) {
            assertThat(validator.validate(request))
                    .extracting(error -> error.getPropertyPath().toString(), error -> error.getMessage())
                    .containsExactlyInAnyOrder(
                            tuple("maxParticipants", "모집 인원은 2명 이상 6명 이하로 정해 주세요."),
                            tuple("recruitMinutes", "모집 시간은 10분 이상 60분 이하로 정해 주세요."));
        }
    }

    private CreateRoomRequest request(Track track, SharedMenu sharedMenu, List<MenuItem> items, int capacity, int minutes) {
        return new CreateRoomRequest(1L, track, sharedMenu, items, 1L, capacity, minutes, PaymentMethod.KAKAO_PAY);
    }
}
