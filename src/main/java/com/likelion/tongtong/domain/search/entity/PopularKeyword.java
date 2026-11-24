package com.likelion.tongtong.domain.search.entity;

import com.likelion.tongtong.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "popular_keyword")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularKeyword extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "popular_keyword_id")
    private Long id;

    // RANK는 MySQL 예약어라 ranking으로 쓴다
    @Column(nullable = false)
    private Integer ranking;

    @Column(nullable = false, length = 30)
    private String keyword;

    @Builder
    private PopularKeyword(Integer ranking, String keyword) {
        this.ranking = ranking;
        this.keyword = keyword;
    }
}
