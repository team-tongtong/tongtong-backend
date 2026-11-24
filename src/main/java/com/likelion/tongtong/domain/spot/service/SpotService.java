package com.likelion.tongtong.domain.spot.service;

import com.likelion.tongtong.domain.spot.dto.SpotListResponse;
import com.likelion.tongtong.domain.spot.repository.SpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpotService {

    private final SpotRepository spotRepository;

    public SpotListResponse getSpots() {
        return SpotListResponse.from(spotRepository.findAll(Sort.by(Sort.Direction.ASC, "id")));
    }
}
