INSERT IGNORE INTO users (email, password, role)
VALUES
('user1@test.com', '$2a$10$7/9gcAoI.k3ZRNLbQLYEtOhgaPyw.24OC8aejRhxBMBbl5kgWeZdC', 'USER'),
('user2@test.com', '$2a$10$7/9gcAoI.k3ZRNLbQLYEtOhgaPyw.24OC8aejRhxBMBbl5kgWeZdC', 'USER'),
('user3@test.com', '$2a$10$7/9gcAoI.k3ZRNLbQLYEtOhgaPyw.24OC8aejRhxBMBbl5kgWeZdC', 'USER');

INSERT IGNORE INTO article (title, content, user_id, created_at, view_count, version)
VALUES
('title1','content1',1, NOW() - INTERVAL 30 MINUTE, 0, 0),
('title2','content2',2, NOW() - INTERVAL 29 MINUTE, 0, 0),
('title3','content3',3, NOW() - INTERVAL 28 MINUTE, 0, 0),
('title4','content4',1, NOW() - INTERVAL 27 MINUTE, 0, 0),
('title5','content5',2, NOW() - INTERVAL 26 MINUTE, 0, 0),
('title6','content6',3, NOW() - INTERVAL 25 MINUTE, 0, 0),
('title7','content7',1, NOW() - INTERVAL 24 MINUTE, 0, 0),
('title8','content8',2, NOW() - INTERVAL 23 MINUTE, 0, 0),
('title9','content9',3, NOW() - INTERVAL 22 MINUTE, 0, 0),
('title10','content10',1, NOW() - INTERVAL 21 MINUTE, 0, 0),
('title11','content11',2, NOW() - INTERVAL 20 MINUTE, 0, 0),
('title12','content12',3, NOW() - INTERVAL 19 MINUTE, 0, 0),
('title13','content13',1, NOW() - INTERVAL 18 MINUTE, 0, 0),
('title14','content14',2, NOW() - INTERVAL 17 MINUTE, 0, 0),
('title15','content15',3, NOW() - INTERVAL 16 MINUTE, 0, 0),
('title16','content16',1, NOW() - INTERVAL 15 MINUTE, 0, 0),
('title17','content17',2, NOW() - INTERVAL 14 MINUTE, 0, 0),
('title18','content18',3, NOW() - INTERVAL 13 MINUTE, 0, 0),
('title19','content19',1, NOW() - INTERVAL 12 MINUTE, 0, 0),
('title20','content20',2, NOW() - INTERVAL 11 MINUTE, 0, 0),
('title21','content21',3, NOW() - INTERVAL 10 MINUTE, 0, 0),
('title22','content22',1, NOW() - INTERVAL 9 MINUTE, 0, 0),
('title23','content23',2, NOW() - INTERVAL 8 MINUTE, 0, 0),
('title24','content24',3, NOW() - INTERVAL 7 MINUTE, 0, 0),
('title25','content25',1, NOW() - INTERVAL 6 MINUTE, 0, 0),
('title26','content26',2, NOW() - INTERVAL 5 MINUTE, 0, 0),
('title27','content27',3, NOW() - INTERVAL 4 MINUTE, 0, 0),
('title28','content28',1, NOW() - INTERVAL 3 MINUTE, 0, 0),
('title29','content29',2, NOW() - INTERVAL 2 MINUTE, 0, 0),
('title30','content30',3, NOW() - INTERVAL 1 MINUTE, 0, 0);