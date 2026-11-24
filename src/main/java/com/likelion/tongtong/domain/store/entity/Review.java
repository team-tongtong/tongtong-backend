package com.likelion.tongtong.domain.store.entity;

import com.likelion.tongtong.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Getter
@Entity
@Table(name = "review")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private Integer rating;

    @Column(nullable = false, length = 300)
    private String content;

    @Builder
    private Review(Store store, String nickname, Integer rating, String content) {
        this.store = store;
        this.nickname = nickname;
        this.rating = rating;
        this.content = content;
    }
}
