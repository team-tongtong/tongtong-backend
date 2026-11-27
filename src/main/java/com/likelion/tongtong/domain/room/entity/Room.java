package com.likelion.tongtong.domain.room.entity;

import com.likelion.tongtong.domain.spot.entity.Spot;
import com.likelion.tongtong.domain.store.entity.Menu;
import com.likelion.tongtong.domain.store.entity.Store;
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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "room")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "spot_id", nullable = false)
    private Spot spot;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private Track track;

    // PORTION이면 함께 나눌 메뉴, INDIVIDUAL이면 null
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_menu_id", nullable = true)
    private Menu sharedMenu;

    @Column(nullable = false)
    private Integer maxParticipants;

    @Column(nullable = false)
    private LocalDateTime recruitDeadline;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private RoomStatus status;

    @Builder
    private Room(Store store, Spot spot, Track track, Menu sharedMenu,
                 Integer maxParticipants, LocalDateTime recruitDeadline, RoomStatus status) {
        this.store = store;
        this.spot = spot;
        this.track = track;
        this.sharedMenu = sharedMenu;
        this.maxParticipants = maxParticipants;
        this.recruitDeadline = recruitDeadline;
        this.status = status != null ? status : RoomStatus.RECRUITING;
    }

    // 모집중이고 마감 시각 전일 때만 참여할 수 있다
    public boolean isJoinable(LocalDateTime now) {
        return status == RoomStatus.RECRUITING && recruitDeadline.isAfter(now);
    }

    public void completeRecruit() {
        this.status = RoomStatus.RECRUIT_COMPLETED;
    }

    // 마감 시각이 지났는데 아직 모집중이면 자동 취소 대상이다
    public boolean isExpired(LocalDateTime now) {
        return status == RoomStatus.RECRUITING && !recruitDeadline.isAfter(now);
    }

    public void cancelRecruit() {
        this.status = RoomStatus.CANCELED;
    }
}
