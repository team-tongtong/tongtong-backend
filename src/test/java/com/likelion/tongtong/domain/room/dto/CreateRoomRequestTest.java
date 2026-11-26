package com.likelion.tongtong.domain.room.dto;

import static org.assertj.core.api.Assertions.assertThat;

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
        var request = request(Track.PORTION, new SharedMenu(null), List.of(new MenuItem(3L, 0)), 7, 15);

        // when and then
        assertThat(VALIDATOR_FACTORY.getValidator().validate(request))
                .extracting(error -> error.getPropertyPath().toString())
                .containsExactlyInAnyOrder("maxParticipants", "recruitMinutesStepValid", "items[0].quantity",
                        "sharedMenu.menuId");
    }

    private CreateRoomRequest request(Track track, SharedMenu sharedMenu, List<MenuItem> items, int capacity, int minutes) {
        return new CreateRoomRequest(1L, track, sharedMenu, items, 1L, capacity, minutes, PaymentMethod.KAKAO_PAY);
    }
}
