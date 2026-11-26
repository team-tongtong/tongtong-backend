package com.likelion.tongtong.domain.room.service;

import com.likelion.tongtong.domain.order.entity.Order;
import com.likelion.tongtong.domain.order.entity.OrderItem;
import com.likelion.tongtong.domain.order.entity.OrderStatus;
import com.likelion.tongtong.domain.order.repository.OrderItemRepository;
import com.likelion.tongtong.domain.order.repository.OrderRepository;
import com.likelion.tongtong.domain.order.repository.OrderRepository.RoomParticipantCount;
import com.likelion.tongtong.domain.order.service.OrderAmountCalculator;
import com.likelion.tongtong.domain.order.service.OrderAmountCalculator.MenuLine;
import com.likelion.tongtong.domain.order.service.OrderAmountCalculator.OrderAmounts;
import com.likelion.tongtong.domain.room.dto.CreateRoomRequest;
import com.likelion.tongtong.domain.room.dto.CreateRoomRequest.MenuItem;
import com.likelion.tongtong.domain.room.dto.JoinRoomRequest;
import com.likelion.tongtong.domain.room.dto.JoinRoomResponse;
import com.likelion.tongtong.domain.room.dto.RoomCreatedResponse;
import com.likelion.tongtong.domain.room.dto.RoomListResponse;
import com.likelion.tongtong.domain.room.dto.RoomSummaryResponse;
import com.likelion.tongtong.domain.room.entity.Room;
import com.likelion.tongtong.domain.room.entity.RoomStatus;
import com.likelion.tongtong.domain.room.entity.Track;
import com.likelion.tongtong.domain.room.repository.RoomRepository;
import com.likelion.tongtong.domain.spot.entity.Spot;
import com.likelion.tongtong.domain.spot.repository.SpotRepository;
import com.likelion.tongtong.domain.store.entity.Menu;
import com.likelion.tongtong.domain.store.entity.Store;
import com.likelion.tongtong.domain.store.repository.MenuRepository;
import com.likelion.tongtong.domain.store.repository.StoreRepository;
import com.likelion.tongtong.domain.user.entity.User;
import com.likelion.tongtong.domain.user.service.CurrentUserService;
import com.likelion.tongtong.global.exception.BusinessException;
import com.likelion.tongtong.global.response.ErrorCode;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private static final SecureRandom PICKUP_CODE_RANDOM = new SecureRandom();

    private final RoomRepository roomRepository;
    private final OrderRepository orderRepository;
    private final Clock clock;
    private final StoreRepository storeRepository;
    private final SpotRepository spotRepository;
    private final MenuRepository menuRepository;
    private final OrderItemRepository orderItemRepository;
    private final CurrentUserService currentUserService;
    private final OrderAmountCalculator orderAmountCalculator;

    public RoomListResponse getRooms(Track track) {
        LocalDateTime now = LocalDateTime.now(clock);
        List<Room> rooms = roomRepository.findRecruitingRooms(RoomStatus.RECRUITING, now, track);
        if (rooms.isEmpty()) {
            return new RoomListResponse(List.of());
        }

        List<Long> roomIds = rooms.stream().map(Room::getId).toList();
        Map<Long, Long> participantsByRoom = orderRepository.countByRoomIdsAndStatus(roomIds, OrderStatus.JOINED)
                .stream()
                .collect(Collectors.toMap(RoomParticipantCount::getRoomId, RoomParticipantCount::getParticipantCount));

        List<RoomSummaryResponse> summaries = rooms.stream()
                .map(room -> RoomSummaryResponse.from(room, participantsByRoom.getOrDefault(room.getId(), 0L)))
                .toList();
        return new RoomListResponse(summaries);
    }

    @Transactional
    public RoomCreatedResponse createRoom(CreateRoomRequest request) {
        Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        Spot spot = spotRepository.findById(request.spotId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SPOT_NOT_FOUND));

        Map<Long, Menu> menus = findRoomMenus(request, store.getId());
        Menu sharedMenu = request.sharedMenu() == null ? null : menus.get(request.sharedMenu().menuId());
        if (sharedMenu != null && !sharedMenu.isPortionable()) {
            throw new BusinessException(ErrorCode.NOT_PORTIONABLE_MENU);
        }

        User host = currentUserService.getCurrentUser();
        OrderAmounts amounts = calculateHostAmounts(request, store, sharedMenu, menus);
        Room room = roomRepository.save(Room.builder()
                .store(store)
                .spot(spot)
                .track(request.track())
                .sharedMenu(sharedMenu)
                .maxParticipants(request.maxParticipants())
                .recruitDeadline(LocalDateTime.now(clock).plusMinutes(request.recruitMinutes()))
                .build());

        Order order = orderRepository.save(Order.builder()
                .room(room)
                .user(host)
                .paymentMethod(request.paymentMethod())
                .foodAmount(amounts.foodAmount())
                .deliveryFeeShare(amounts.deliveryFeeShare())
                .portionFee(amounts.portionFee())
                .totalAmount(amounts.totalAmount())
                .build());

        List<OrderItem> orderItems = new ArrayList<>();
        if (sharedMenu != null) {
            orderItems.add(toOrderItem(order, sharedMenu, 1, true));
        }
        for (MenuItem item : request.items()) {
            orderItems.add(toOrderItem(order, menus.get(item.menuId()), item.quantity(), false));
        }
        orderItemRepository.saveAll(orderItems);
        return RoomCreatedResponse.from(room, order);
    }

    private Map<Long, Menu> findRoomMenus(CreateRoomRequest request, Long storeId) {
        Set<Long> menuIds = new HashSet<>();
        for (MenuItem item : request.items()) {
            menuIds.add(item.menuId());
        }
        if (request.sharedMenu() != null) {
            menuIds.add(request.sharedMenu().menuId());
        }
        return findStoreMenus(menuIds, storeId);
    }

    private Map<Long, Menu> findStoreMenus(Set<Long> menuIds, Long storeId) {
        Map<Long, Menu> menus = menuRepository.findAllById(menuIds).stream()
                .collect(Collectors.toMap(Menu::getId, Function.identity()));
        if (menus.size() != menuIds.size()) {
            throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
        }
        if (menus.values().stream().anyMatch(menu -> !menu.getStore().getId().equals(storeId))) {
            throw new BusinessException(ErrorCode.MENU_STORE_MISMATCH);
        }
        return menus;
    }

    private OrderAmounts calculateHostAmounts(CreateRoomRequest request, Store store, Menu sharedMenu,
                                             Map<Long, Menu> menus) {
        List<MenuLine> personalItems = request.items().stream()
                .map(item -> new MenuLine(menus.get(item.menuId()).getPrice(), item.quantity()))
                .toList();
        Integer sharedMenuPrice = sharedMenu == null ? null : sharedMenu.getPrice();
        try {
            return orderAmountCalculator.calculate(request.track(), request.maxParticipants(),
                    store.getDeliveryFee(), sharedMenuPrice, personalItems);
        } catch (ArithmeticException exception) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    private OrderItem toOrderItem(Order order, Menu menu, int quantity, boolean shared) {
        return OrderItem.builder()
                .order(order)
                .menu(menu)
                .quantity(quantity)
                .shared(shared)
                .build();
    }

    @Transactional
    public JoinRoomResponse joinRoom(Long roomId, JoinRoomRequest request) {
        Room room = roomRepository.findByIdForUpdate(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
        if (!room.isJoinable(LocalDateTime.now(clock))) {
            throw new BusinessException(ErrorCode.RECRUIT_CLOSED);
        }

        User user = currentUserService.getCurrentUser();
        // 취소하지 않은 주문만 참여 인원으로 센다
        List<Order> activeOrders = orderRepository.findAllByRoomIdAndStatusNot(roomId, OrderStatus.CANCELED);
        if (activeOrders.stream().anyMatch(order -> order.getUser().getId().equals(user.getId()))) {
            throw new BusinessException(ErrorCode.ALREADY_JOINED);
        }
        if (activeOrders.size() >= room.getMaxParticipants()) {
            throw new BusinessException(ErrorCode.RECRUIT_CLOSED);
        }
        if (room.getTrack() == Track.INDIVIDUAL && request.items().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "아껴통은 메뉴를 1개 이상 담아 주세요.");
        }

        Store store = room.getStore();
        Set<Long> menuIds = request.items().stream()
                .map(JoinRoomRequest.MenuItem::menuId)
                .collect(Collectors.toSet());
        Map<Long, Menu> menus = findStoreMenus(menuIds, store.getId());
        Menu sharedMenu = room.getSharedMenu();
        OrderAmounts amounts = calculateJoinAmounts(room, store, sharedMenu, request.items(), menus);

        Order order = orderRepository.save(Order.builder()
                .room(room)
                .user(user)
                .paymentMethod(request.paymentMethod())
                .foodAmount(amounts.foodAmount())
                .deliveryFeeShare(amounts.deliveryFeeShare())
                .portionFee(amounts.portionFee())
                .totalAmount(amounts.totalAmount())
                .build());

        List<OrderItem> orderItems = new ArrayList<>();
        if (sharedMenu != null) {
            orderItems.add(toOrderItem(order, sharedMenu, 1, true));
        }
        for (JoinRoomRequest.MenuItem item : request.items()) {
            orderItems.add(toOrderItem(order, menus.get(item.menuId()), item.quantity(), false));
        }
        orderItemRepository.saveAll(orderItems);

        List<Order> participants = new ArrayList<>(activeOrders);
        participants.add(order);
        completeRecruitIfFilled(room, store, participants);
        return JoinRoomResponse.from(room, order);
    }

    private OrderAmounts calculateJoinAmounts(Room room, Store store, Menu sharedMenu,
                                              List<JoinRoomRequest.MenuItem> items, Map<Long, Menu> menus) {
        List<MenuLine> personalItems = items.stream()
                .map(item -> new MenuLine(menus.get(item.menuId()).getPrice(), item.quantity()))
                .toList();
        Integer sharedMenuPrice = sharedMenu == null ? null : sharedMenu.getPrice();
        try {
            return orderAmountCalculator.calculate(room.getTrack(), room.getMaxParticipants(),
                    store.getDeliveryFee(), sharedMenuPrice, personalItems);
        } catch (ArithmeticException exception) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "메뉴 수량을 줄여 주세요.");
        }
    }

    // 정원이 차고 음식값 합계가 최소주문금액 이상이면 모집 완료, 참여자 전원 결제 완료와 픽업 코드 발급
    private void completeRecruitIfFilled(Room room, Store store, List<Order> participants) {
        if (participants.size() < room.getMaxParticipants()) {
            return;
        }
        long foodTotal = participants.stream().mapToLong(Order::getFoodAmount).sum();
        Menu sharedMenu = room.getSharedMenu();
        if (sharedMenu != null) {
            // 1인 몫을 내림해서 생긴 잔액도 가게에는 결제되므로 합계에 더한다 (정원이 다 찼을 때만 여기 온다)
            foodTotal += sharedMenu.getPrice() % room.getMaxParticipants();
        }
        if (foodTotal < store.getMinOrderAmount()) {
            return;
        }

        room.completeRecruit();
        Set<String> issuedCodes = new HashSet<>();
        for (Order participant : participants) {
            participant.pay(issuePickupCode(issuedCodes));
        }
    }

    private String issuePickupCode(Set<String> issuedCodes) {
        String code;
        do {
            code = String.format("%05d", PICKUP_CODE_RANDOM.nextInt(100_000));
        } while (!issuedCodes.add(code));
        return code;
    }
}
