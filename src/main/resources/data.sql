INSERT IGNORE INTO users (email, password, role)
VALUES
('user1@test.com', '$2a$10$Djt1NFxR/2FN.H4L6zcP5uRCCOxk3vXV0owU57mJtQQGRsT.cFX/i', 'USER'),
('user2@test.com', '$2a$10$Djt1NFxR/2FN.H4L6zcP5uRCCOxk3vXV0owU57mJtQQGRsT.cFX/i', 'USER'),
('user3@test.com', '$2a$10$Djt1NFxR/2FN.H4L6zcP5uRCCOxk3vXV0owU57mJtQQGRsT.cFX/i', 'USER');

INSERT IGNORE INTO article (title, content, user_id, created_at)
VALUES
('title1','content1',1, NOW() - INTERVAL 30 MINUTE),
('title2','content2',2, NOW() - INTERVAL 29 MINUTE),
('title3','content3',3, NOW() - INTERVAL 28 MINUTE),
('title4','content4',1, NOW() - INTERVAL 27 MINUTE),
('title5','content5',2, NOW() - INTERVAL 26 MINUTE),
('title6','content6',3, NOW() - INTERVAL 25 MINUTE),
('title7','content7',1, NOW() - INTERVAL 24 MINUTE),
('title8','content8',2, NOW() - INTERVAL 23 MINUTE),
('title9','content9',3, NOW() - INTERVAL 22 MINUTE),
('title10','content10',1, NOW() - INTERVAL 21 MINUTE),
('title11','content11',2, NOW() - INTERVAL 20 MINUTE),
('title12','content12',3, NOW() - INTERVAL 19 MINUTE),
('title13','content13',1, NOW() - INTERVAL 18 MINUTE),
('title14','content14',2, NOW() - INTERVAL 17 MINUTE),
('title15','content15',3, NOW() - INTERVAL 16 MINUTE),
('title16','content16',1, NOW() - INTERVAL 15 MINUTE),
('title17','content17',2, NOW() - INTERVAL 14 MINUTE),
('title18','content18',3, NOW() - INTERVAL 13 MINUTE),
('title19','content19',1, NOW() - INTERVAL 12 MINUTE),
('title20','content20',2, NOW() - INTERVAL 11 MINUTE),
('title21','content21',3, NOW() - INTERVAL 10 MINUTE),
('title22','content22',1, NOW() - INTERVAL 9 MINUTE),
('title23','content23',2, NOW() - INTERVAL 8 MINUTE),
('title24','content24',3, NOW() - INTERVAL 7 MINUTE),
('title25','content25',1, NOW() - INTERVAL 6 MINUTE),
('title26','content26',2, NOW() - INTERVAL 5 MINUTE),
('title27','content27',3, NOW() - INTERVAL 4 MINUTE),
('title28','content28',1, NOW() - INTERVAL 3 MINUTE),
('title29','content29',2, NOW() - INTERVAL 2 MINUTE),
('title30','content30',3, NOW() - INTERVAL 1 MINUTE);