package com.likelion.tongtong.domain.order.entity;

import com.likelion.tongtong.domain.room.entity.Room;
import com.likelion.tongtong.domain.room.entity.RoomStatus;
import com.likelion.tongtong.domain.user.entity.User;
import com.likelion.tongtong.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(nullable = false)
    private Integer foodAmount;

    @Column(nullable = false)
    private Integer deliveryFeeShare;

    @Column(nullable = false)
    private Integer portionFee;

    @Column(nullable = false)
    private Integer totalAmount;

    // 모집 완료 때 발급
    @Column(columnDefinition = "CHAR(5)")
    private String pickupCode;

    @Builder
    private Order(Room room, User user, OrderStatus status, PaymentMethod paymentMethod, PaymentStatus paymentStatus,
                  Integer foodAmount, Integer deliveryFeeShare, Integer portionFee, Integer totalAmount,
                  String pickupCode) {
        this.room = room;
        this.user = user;
        this.status = status != null ? status : OrderStatus.JOINED;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus != null ? paymentStatus : PaymentStatus.PENDING;
        this.foodAmount = foodAmount;
        this.deliveryFeeShare = deliveryFeeShare;
        this.portionFee = portionFee;
        this.totalAmount = totalAmount;
        this.pickupCode = pickupCode;
    }

    // 모집중인 방에 참여 중일 때만 취소할 수 있다
    public boolean isCancelable() {
        return status == OrderStatus.JOINED && room.getStatus() == RoomStatus.RECRUITING;
    }

    // 모집 완료 순간 결제 완료로 바꾸고 픽업 코드를 발급한다
    public void pay(String pickupCode) {
        this.paymentStatus = PaymentStatus.PAID;
        this.pickupCode = pickupCode;
    }

    // 모집 완료 전이라 결제된 금액이 없어 결제 상태도 함께 취소한다
    public void cancel() {
        this.status = OrderStatus.CANCELED;
        this.paymentStatus = PaymentStatus.CANCELED;
    }

    public boolean matchesPickupCode(String code) {
        return pickupCode != null && pickupCode.equals(code);
    }

    public void pickUp() {
        this.status = OrderStatus.PICKED_UP;
    }
}
