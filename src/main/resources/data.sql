-- 시연용 초기 데이터. 이미 있는 PK의 행은 기존 값을 유지한다.
START TRANSACTION;

INSERT INTO `users` (`user_id`, `created_at`, `updated_at`) VALUES
    (1, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (2, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (3, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (4, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (5, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (6, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR))
ON DUPLICATE KEY UPDATE `user_id` = `user_id`;

INSERT INTO `store` (`store_id`, `name`, `category`, `image_url`, `rating`, `review_count`, `order_count`, `min_order_amount`, `delivery_fee`, `delivery_minutes`, `distance_meters`, `last_order_time`, `created_at`, `updated_at`) VALUES
    (1, '두찜 안산한대앞역점', '찜닭', 'https://i.ibb.co/TqTWxsXg/images-1.png', 4.9, 259, 1200, 13500, 2000, 32, 650, '23:50:00', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (2, 'BHC치킨 안산사동점', '치킨', 'https://i.ibb.co/Q7gqxqH1/207910-207569-1231.jpg', 4.6, 152, 910, 21000, 3000, 40, 400, '23:30:00', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (3, '동대문엽기떡볶이 한양대에리카점', '떡볶이', 'https://i.ibb.co/2376fc4k/images.png', 4.5, 203, 1100, 14000, 3000, 35, 700, '23:30:00', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (4, '가장맛있는족발 한대앞역점', '족발', 'https://i.ibb.co/99ThMVhC/images.jpg', 4.4, 71, 380, 30000, 3000, 50, 850, '23:30:00', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (5, '텐버거 안산한양대점', '햄버거', 'https://i.ibb.co/k61zJ0cH/TENBURGER22-02-1019x1024.jpg', 4.8, 96, 540, 12000, 2500, 25, 250, '20:55:00', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (6, '피자스쿨 안산한양대점', '피자', 'https://i.ibb.co/d4XpYg0f/images-1.jpg', 4.4, 119, 640, 14000, 2500, 30, 500, '20:30:00', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (7, '밀플랜비 한양대에리카점', '부리또', 'https://i.ibb.co/d0kzmg5F/images-2.jpg', 4.1, 88, 830, 20000, 2000, 25, 450, '22:00:00', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR))
ON DUPLICATE KEY UPDATE `store_id` = `store_id`;

INSERT INTO `spot` (`spot_id`, `name`, `description`, `map_image_url`, `created_at`, `updated_at`) VALUES
    (1, '한양대 ERICA 행복관 1층', '1층 엘리베이터 옆 픽업 선반', 'https://i.ibb.co/2YMbHhFg/images-10.jpg', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (2, '학생회관 1층', '학생회관 정문 계단 옆 벤치 앞 통통 픽업존', 'https://i.ibb.co/Df1phR9x/1.jpg', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (3, '학술정보관 앞', '학술정보관 정문 계단 옆 벤치 앞 통통 픽업존', 'https://i.ibb.co/vxrZmy0n/images-4.jpg', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (4, '한양대 ERICA 정문', '정문 수위실 옆 배달 픽업 구역', 'https://i.ibb.co/tjzQ6gG/Capture-2022-11-15-09-13-24.jpg', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR))
ON DUPLICATE KEY UPDATE `spot_id` = `spot_id`;

INSERT INTO `menu` (`menu_id`, `store_id`, `name`, `description`, `price`, `image_url`, `portionable`, `created_at`, `updated_at`) VALUES
    (1, 1, '까만찜닭', '달콤 짭조름한 간장소스로 만든 찜닭', 27800, 'https://i.ibb.co/TxhGhLfb/a74ec1d259a7eb2e131e8707ce5ee0f9-niw8-QMZ5-7ee482c83316dfa93bfa966c04d6cc07b5b827a9.png', 1, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (2, 1, '두찜 실비한우곱찜닭', '통통한 한우대창에 매운 김치 다대기가 듬뿍 들어간 찜닭. 타피오카 99% 당면, 비트무, 김, 참기름 포함', 34800, 'https://i.ibb.co/gZ1DwXqT/images-5.jpg', 1, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (3, 1, '두찜 칼낙지찜닭 (절단낙지)', '매콤한 찜닭에 쫄깃한 절단낙지와 칼국수 사리를 더해 푸짐하게 즐기는 찜닭', 34800, 'https://i.ibb.co/LG7F336/images-6.jpg', 1, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (4, 1, '[원조K로제] 로제찜닭', 'K로제의 원조 두찜만의 특제 로제소스 타피오카 99% 당면으로 잘 불지않고 더 뱅글쫄깃합니다.', 30800, 'https://i.ibb.co/M5t31BsD/images-7.jpg', 1, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (5, 2, '뿌링클', '바삭한 치킨에 치즈 풍미의 뿌링클 시즈닝과 뿌링 소스를 곁들인 bhc 시그니처 치킨', 21000, 'https://i.ibb.co/JZGvX8j/images-8.jpg', 1, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (6, 2, '콰삭킹', '감자·쌀·옥수수 크럼블로 바삭함을 극대화한 치킨. 스윗 하바네로 소스 제공', 21000, 'https://i.ibb.co/SXmT4ptn/images-9.jpg', 1, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (7, 2, 'HOT후라이드', '명불허전 바삭함, 고소함 가득 매콤하게 튀긴 매운 맛 후라이드 치킨의 정석', 24000, 'https://i.ibb.co/FkbdZ5DR/20251102-163125-4a53ba3f.png', 1, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (8, 3, '엽기떡볶이', '치즈와 비엔나가 들어가는 엽떡 대표 메뉴. 반반·오뎅 선택 가능, 3인분 안팎', 14000, 'https://i.ibb.co/k609zPcf/6-1-1-1-1-1-1-2-1-1-1-1-1-1-1-1-1-1-1-1-1.png', 1, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (9, 3, '로제떡볶이', '부드러운 로제 크림에 엽떡의 매콤함을 더한 떡볶이. 반반·오뎅 선택 가능', 16000, 'https://i.ibb.co/YTTMHF6T/5-1-1-1-1-1-1-2-1-1-1-1-1-1-1-1-1-1-1-1-1.png', 1, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (10, 3, '마라로제떡볶이', '마라의 얼얼함과 로제 소스를 함께 즐기는 떡볶이', 18000, 'https://i.ibb.co/k2XpbKKz/24-02-22-500x500-2-1-1-1-1-1-2-1-1-1-1-1-1-1-1-1-1-1-1-1.png', 1, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (11, 3, '꿔바로우(5개)', '새콤달콤한 꿔바로우 5개. 떡볶이와 곁들이기 좋은 사이드', 5900, 'https://i.ibb.co/yLQ3Qd6/3-1-1-1-1-1-1-1-1-1-1-1-1-1.jpg', 0, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (12, 4, '가장맛있는족발 앞발 (대)', '앞발 족발 대 사이즈 (3~4인분). 부추무침, 겉절이, 백김치, 쌈장, 새우젓, 쌈채소 포함', 41000, 'https://i.ibb.co/XxybHpCY/IMG-1597.jpg', 1, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (13, 4, '반반족발 (2인분)', '족발 두 가지 맛을 반반 담은 2인분 단품. 겉절이, 부추무침, 백김치, 쌈채소 포함', 41000, 'https://i.ibb.co/snnmCp8/IMG-1595.jpg', 1, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (14, 4, '가족의 족보 (중)', '족발과 보쌈을 한 번에 즐기는 3~4인분 세트. 보쌈김치와 쌈채소 포함', 42000, 'https://i.ibb.co/67rhNCMw/IMG-1596.jpg', 1, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (15, 4, '날치알 주먹밥', '족발에 곁들이기 좋은 날치알 주먹밥', 3000, 'https://i.ibb.co/w3Z57KC/IMG-1549.png', 0, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (16, 5, '텐버거 세트', '100% 소고기 수제 패티와 발효요거트 수제소스 버거에 프렌치포테이토 또는 치즈스틱, 음료 포함', 8400, 'https://i.ibb.co/kV8T6mXd/IMG-1550.png', 0, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (17, 5, '텐베이컨치즈버거 세트', '수제 소고기 패티에 베이컨과 치즈를 더한 버거 세트. 감자튀김 또는 치즈스틱, 음료 포함', 10900, 'https://i.ibb.co/LX1StzgR/IMG-1551.png', 0, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (18, 5, '텐통살치킨버거 세트', '바삭한 닭다리살 패티와 마요소스 버거 세트. 감자튀김 또는 치즈스틱, 음료 포함', 9400, 'https://i.ibb.co/9md7svsB/IMG-1552.png', 0, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (19, 5, '버팔로치킨(봉) 4개', '매콤하게 양념한 치킨봉 4개. 여럿이 곁들여 먹기 좋은 사이드', 4900, 'https://i.ibb.co/chByrgDj/IMG-1553.png', 1, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (20, 6, '고구마피자', '사각 고구마 토핑에 마요머스터드 소스를 더한 달콤한 피자스쿨 대표 메뉴', 10900, 'https://i.ibb.co/rGH4mRVB/IMG-1554.png', 0, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (21, 6, '불고기피자', '불고기와 양파가 올라간 무난하고 인기 많은 피자', 11900, 'https://i.ibb.co/8RSwkv3/IMG-1555.png', 0, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (22, 6, '콤비네이션피자', '페퍼로니, 포크토핑, 야채가 올라간 클래식 콤비네이션 피자', 10900, 'https://i.ibb.co/nNtvTWnL/IMG-1556.png', 0, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (23, 6, '직화홀릭바이트피자', '직화 불고기와 홀그레인 머스터드 소스를 엣지 바이트까지 채운 피자', 14900, 'https://cdn.phototourl.com/anon/2026-11-27-663b5f6c-8798-4b6c-844f-9b3f38a4c0fb.png', 1, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (24, 7, '치킨감자 부리또 세트', '치킨과 감자가 듬뿍 들어간 시그니처 부리또에 사이드와 음료를 더한 세트', 9000, 'https://cdn.phototourl.com/anon/2026-11-27-92706d7b-db23-4d49-8b57-ecfe105721db.png', 0, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (25, 7, '소고기라이스 부리또 세트', '소고기와 라이스를 또띠아에 가득 채운 부리또 세트', 8500, 'https://cdn.phototourl.com/anon/2026-11-27-f8218ea1-b3ff-4ab0-932c-7c8a53714724.png', 0, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (26, 7, '새우감자 부리또 세트', '새우와 감자를 넣은 부리또 세트', 8500, 'https://i.ibb.co/pvbZ63Tx/IMG-1560.png', 0, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (27, 7, '메이크업치킨 400g', '치킨 400g 사이드. 부리또와 함께 나눠 먹기 좋아요', 8500, 'https://cdn.phototourl.com/anon/2026-11-27-5e447610-2de0-49b6-8aa3-7e75875b8e56.jpg', 1, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (28, 2, '사인드와인더감자&칩스', '겉은 콰삭, 속은 포슬한 웨지감자 튀김 사이드', 8000, 'https://cdn.phototourl.com/anon/2026-11-27-44ec00d8-a825-4663-8c32-fc10b7a85b22.jpg', 0, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR))
ON DUPLICATE KEY UPDATE `menu_id` = `menu_id`;

INSERT INTO `review` (`review_id`, `store_id`, `nickname`, `rating`, `content`, `created_at`, `updated_at`) VALUES
    (1, 1, '찜닭러버', 5, '양이 많아서 나눠 먹기 좋아요', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (2, 1, '자취생김군', 4, '룸메랑 셋이서 나눠 먹었는데 1인당 만원 안쪽이라 부담 없었어요. 양념이 진해서 밥 비벼 먹으면 끝!', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (3, 2, '치킨러버', 5, '뿌링클을 셋이 나눠 시켰더니 1인 7천원 정도라 부담이 없었어요. 치즈 시즈닝 덕분에 과제하다 야식으로 딱!', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (4, 2, '공대생민수', 4, '콰삭킹이 진짜 바삭해요. 다만 저녁 시간엔 배달이 조금 걸려서 모집 마감 시간을 넉넉히 잡는 게 좋아요.', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (5, 3, '떡볶이중독', 5, '로제떡볶이를 넷이 나눠 먹었는데 양이 넉넉해요. 한 명당 4천원이라 용돈 아끼기 최고!', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (6, 3, '새내기하윤', 4, '맵기 단계를 고를 수 있어서 매운 거 못 먹는 친구랑도 같이 시켰어요. 꿔바로우 사이드까지 추가하면 든든합니다.', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (7, 4, '야식요정', 5, '앞발 대짜가 3~4인분이라 같이 나눠 먹기 딱 좋아요. 쫀득하고 쌈채소도 넉넉하게 와서 만족했어요.', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (8, 4, '졸업반선배', 4, '날치알 주먹밥 하나 추가했는데 고기랑 같이 먹으니 든든했어요. 배달은 조금 걸려도 따뜻하게 도착했습니다.', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (9, 5, '아껴통러', 5, '혼자 시키면 최소주문금액이 안 넘는데 아껴통으로 친구랑 묶어 시켰더니 배달비도 아끼고 좋았어요. 수제 패티가 두툼하고 감튀가 맛있어요.', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (10, 5, '버거덕후', 4, '텐베이컨치즈버거 세트 먹었는데 패티 육즙이 좋아요. 사이즈는 아담한 편이라 배고프면 사이드 추가를 추천해요!', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (11, 6, '얼굴피자', 5, '혼자 시키면 최소주문금액이 안 넘는데 아껴통으로 친구랑 묶으니 배달비도 반반이라 좋았어요. 고구마피자는 달달해서 인기 많아요.', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (12, 6, '피자는사랑', 4, '불고기피자 무난하게 맛있고 가격이 착해요. 1인 한 판으로 가볍게 먹기 좋아요.', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (13, 7, '부리또파', 5, '하나만 먹어도 배불러요. 치킨감자 부리또 세트는 사이드랑 음료까지 나와서 든든합니다.', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (14, 7, '시험기간생', 4, '시험기간에 한 손으로 먹을 수 있어서 좋아요. 친구랑 치킨 사이드 하나 시켜서 나눠 먹으니 딱 좋았어요.', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR))
ON DUPLICATE KEY UPDATE `review_id` = `review_id`;

INSERT INTO `popular_keyword` (`popular_keyword_id`, `ranking`, `keyword`, `created_at`, `updated_at`) VALUES
    (1, 1, '찜닭', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (2, 2, '치킨', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (3, 3, '떡볶이', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (4, 4, '피자', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (5, 5, '햄버거', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (6, 6, '족발', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (7, 7, '부리또', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (8, 8, '뿌링클', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (9, 9, '로제떡볶이', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (10, 10, '고구마피자', DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR))
ON DUPLICATE KEY UPDATE `popular_keyword_id` = `popular_keyword_id`;

INSERT INTO `room` (`room_id`, `store_id`, `spot_id`, `track`, `shared_menu_id`, `max_participants`, `status`, `recruit_deadline`, `created_at`, `updated_at`) VALUES
    (1, 1, 1, 'PORTION', 1, 4, 'RECRUITING', DATE_ADD(DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), INTERVAL 600 MINUTE), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (2, 2, 2, 'PORTION', 5, 2, 'RECRUIT_COMPLETED', DATE_ADD(DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), INTERVAL 600 MINUTE), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (3, 5, 3, 'INDIVIDUAL', NULL, 3, 'RECRUITING', DATE_ADD(DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), INTERVAL 720 MINUTE), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (4, 6, 4, 'INDIVIDUAL', NULL, 2, 'RECRUIT_COMPLETED', DATE_ADD(DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), INTERVAL 600 MINUTE), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (5, 4, 1, 'PORTION', 13, 2, 'ARRIVED', DATE_ADD(DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), INTERVAL 600 MINUTE), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (6, 7, 2, 'INDIVIDUAL', NULL, 3, 'RECRUITING', DATE_ADD(DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), INTERVAL 25 MINUTE), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (7, 3, 3, 'PORTION', 8, 4, 'RECRUITING', DATE_ADD(DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), INTERVAL 15 MINUTE), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (8, 1, 4, 'PORTION', 3, 3, 'RECRUITING', DATE_ADD(DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), INTERVAL 40 MINUTE), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR))
ON DUPLICATE KEY UPDATE `room_id` = `room_id`;

INSERT INTO `orders` (`order_id`, `room_id`, `user_id`, `status`, `payment_method`, `payment_status`, `pickup_code`, `food_amount`, `delivery_fee_share`, `portion_fee`, `total_amount`, `created_at`, `updated_at`) VALUES
    (1, 1, 1, 'JOINED', 'KAKAO_PAY', 'PENDING', NULL, 6950, 500, 500, 7950, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (2, 2, 1, 'JOINED', 'TOSS_PAY', 'PAID', '10482', 10500, 1500, 500, 12500, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (3, 2, 2, 'JOINED', 'CARD', 'PAID', '57213', 18500, 1500, 500, 20500, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (4, 3, 1, 'JOINED', 'NAVER_PAY', 'PENDING', NULL, 8400, 833, 0, 9233, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (5, 3, 3, 'JOINED', 'CARD', 'PENDING', NULL, 10900, 833, 0, 11733, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (6, 4, 1, 'JOINED', 'KAKAO_PAY', 'PAID', '33091', 10900, 1250, 0, 12150, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (7, 4, 4, 'JOINED', 'TOSS_PAY', 'PAID', '84620', 21800, 1250, 0, 23050, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (8, 5, 1, 'PICKED_UP', 'CARD', 'PAID', '26157', 20500, 1500, 500, 22500, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (9, 5, 5, 'JOINED', 'KAKAO_PAY', 'PAID', '91734', 23500, 1500, 500, 25500, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (10, 6, 2, 'JOINED', 'CARD', 'PENDING', NULL, 8500, 666, 0, 9166, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (11, 6, 3, 'JOINED', 'TOSS_PAY', 'PENDING', NULL, 8500, 666, 0, 9166, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (12, 7, 4, 'JOINED', 'NAVER_PAY', 'PENDING', NULL, 3500, 750, 500, 4750, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (13, 7, 5, 'JOINED', 'CARD', 'PENDING', NULL, 9400, 750, 500, 10650, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (14, 8, 6, 'JOINED', 'KAKAO_PAY', 'PENDING', NULL, 11600, 666, 500, 12766, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR))
ON DUPLICATE KEY UPDATE `order_id` = `order_id`;

INSERT INTO `order_item` (`order_item_id`, `order_id`, `menu_id`, `quantity`, `shared`, `created_at`, `updated_at`) VALUES
    (1, 1, 1, 1, 1, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (2, 2, 5, 1, 1, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (3, 3, 5, 1, 1, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (4, 3, 28, 1, 0, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (5, 4, 16, 1, 0, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (6, 5, 17, 1, 0, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (7, 6, 20, 1, 0, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (8, 7, 20, 1, 0, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (9, 7, 22, 1, 0, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (10, 8, 13, 1, 1, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (11, 9, 13, 1, 1, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (12, 9, 15, 1, 0, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (13, 10, 25, 1, 0, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (14, 11, 26, 1, 0, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (15, 12, 8, 1, 1, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (16, 13, 8, 1, 1, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (17, 13, 11, 1, 0, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR)),
    (18, 14, 3, 1, 1, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR), DATE_ADD(UTC_TIMESTAMP(), INTERVAL 9 HOUR))
ON DUPLICATE KEY UPDATE `order_item_id` = `order_item_id`;

COMMIT;
