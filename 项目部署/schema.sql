SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS msi CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE msi;

-- 已统一到商户商品表，移除 supply_item

-- 手机品牌表
DROP TABLE IF EXISTS brand;
CREATE TABLE IF NOT EXISTS brand (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(64) UNIQUE,
  sort INT DEFAULT 0,
  deleted TINYINT DEFAULT 0
);

-- 手机系列表
DROP TABLE IF EXISTS phone_series;
CREATE TABLE IF NOT EXISTS phone_series (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  brand_id BIGINT,
  series_name VARCHAR(64),
  sort INT DEFAULT 0,
  deleted TINYINT DEFAULT 0
);

-- 手机型号表
DROP TABLE IF EXISTS phone_model;
CREATE TABLE IF NOT EXISTS phone_model (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  brand_id BIGINT,
  series_id BIGINT,
  model_name VARCHAR(128),
  sort INT DEFAULT 0,
  deleted TINYINT DEFAULT 0
);

-- 手机规格表
DROP TABLE IF EXISTS phone_spec;
CREATE TABLE IF NOT EXISTS phone_spec (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  brand_id BIGINT,
  series_id BIGINT,
  model_id BIGINT,
  spec_name VARCHAR(128),
  sort INT DEFAULT 0,
  deleted TINYINT DEFAULT 0
);

INSERT INTO brand (name, sort) VALUES ('苹果', 100), ('华为', 90), ('小米', 80), ('OPPO', 70), ('VIVO', 60), ('荣耀', 50);

INSERT INTO phone_series (brand_id, series_name, sort) SELECT id, 'iPhone', 100 FROM brand WHERE name = '苹果';
INSERT INTO phone_series (brand_id, series_name, sort) SELECT id, 'Mate', 100 FROM brand WHERE name = '华为';
INSERT INTO phone_series (brand_id, series_name, sort) SELECT id, 'P', 90 FROM brand WHERE name = '华为';
INSERT INTO phone_series (brand_id, series_name, sort) SELECT id, '小米', 100 FROM brand WHERE name = '小米';

INSERT INTO phone_model (brand_id, series_id, model_name, sort) SELECT b.id, s.id, 'iPhone 16 Pro Max', 100 FROM brand b, phone_series s WHERE b.name = '苹果' AND s.series_name = 'iPhone';
INSERT INTO phone_model (brand_id, series_id, model_name, sort) SELECT b.id, s.id, 'iPhone 15', 90 FROM brand b, phone_series s WHERE b.name = '苹果' AND s.series_name = 'iPhone';
INSERT INTO phone_model (brand_id, series_id, model_name, sort) SELECT b.id, s.id, 'Mate 60 Pro', 100 FROM brand b, phone_series s WHERE b.name = '华为' AND s.series_name = 'Mate';

INSERT INTO phone_spec (brand_id, series_id, model_id, spec_name, sort) SELECT b.id, s.id, m.id, '1TB 黑色', 100 FROM brand b, phone_series s, phone_model m WHERE b.name = '苹果' AND s.series_name = 'iPhone' AND m.model_name = 'iPhone 16 Pro Max';
INSERT INTO phone_spec (brand_id, series_id, model_id, spec_name, sort) SELECT b.id, s.id, m.id, '128G 白色', 90 FROM brand b, phone_series s, phone_model m WHERE b.name = '苹果' AND s.series_name = 'iPhone' AND m.model_name = 'iPhone 15';
INSERT INTO phone_spec (brand_id, series_id, model_id, spec_name, sort) SELECT b.id, s.id, m.id, '512G 雅川青', 100 FROM brand b, phone_series s, phone_model m WHERE b.name = '华为' AND s.series_name = 'Mate' AND m.model_name = 'Mate 60 Pro';

-- 商户信息表
DROP TABLE IF EXISTS merchant_info;
CREATE TABLE IF NOT EXISTS merchant_info (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  wechat_id VARCHAR(128) UNIQUE,
  token VARCHAR(128) UNIQUE COMMENT '登录凭证',
  wechat_name VARCHAR(128),
  merchant_name VARCHAR(128),
  merchant_phone VARCHAR(32),
  registration_date DATETIME COMMENT '商户注册日期',
  cancellation_date DATETIME COMMENT '商户注销日期',
  city_code VARCHAR(64) COMMENT '城市编码',
  merchant_address VARCHAR(255),
  latitude DECIMAL(10, 6) COMMENT '纬度',
  longitude DECIMAL(10, 6) COMMENT '经度',
  business_license_url VARCHAR(512) COMMENT '营业执照图片URL',
  contact_name VARCHAR(64) COMMENT '联系人姓名',
  is_valid INT DEFAULT 1 COMMENT '1有效0无效',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO merchant_info (
  wechat_id,
  token,
  wechat_name,
  merchant_name,
  merchant_phone,
  registration_date,
  cancellation_date,
  city_code,
  merchant_address,
  latitude,
  longitude,
  business_license_url,
  contact_name,
  is_valid
) VALUES
('wx_test_0001', 'token_0001', '张三', '张三通讯', '13800000001', '2026-01-10 09:00:00', NULL, ' 370100', '济南市历下区泉城路1号', 36.651200, 117.120100, 'https://example.com/licenses/merchant_0001.jpg', '张三', 1),
('wx_test_0002', 'token_0002', '李四', '李四数码', '13800000002', '2026-01-10 09:00:00', NULL, '440300', '深圳市南山区科技园科苑路88号', 22.542900, 113.959000, 'https://example.com/licenses/merchant_0002.jpg', '李四', 1),
('wx_test_0003', 'token_0003', '王五', '王五手机城', '13800000003', '2026-01-10 09:00:00', NULL, '410100', '郑州市金水区花园路66号', 34.765700, 113.753200, 'https://example.com/licenses/merchant_0003.jpg', '王五', 1),
('wx_test_0004', 'token_0004', '赵六', '赵六通讯广场', '13800000004', '2026-01-10 09:00:00', NULL, '110000', '北京市朝阳区建国路99号', 39.908700, 116.397500, 'https://example.com/licenses/merchant_0004.jpg', '赵六', 1),
('wx_test_0005', 'token_0005', '钱七', '钱七二手数码', '13800000005', '2026-01-10 09:00:00', NULL, '510100', '成都市武侯区人民南路四段3号', 30.657000, 104.066500, 'https://example.com/licenses/merchant_0005.jpg', '钱七', 1),
('wx_test_0006', 'token_0006', '孙八', '孙八通讯', '13800000006', '2026-01-10 09:00:00', NULL, '610100', '西安市雁塔区高新路12号', 34.220200, 108.910700, 'https://example.com/licenses/merchant_0006.jpg', '孙八', 1);

-- 会员信息表
DROP TABLE IF EXISTS merchant_member_info;
CREATE TABLE IF NOT EXISTS merchant_member_info (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  merchant_id BIGINT COMMENT '商户信息表ID',
  UNIQUE KEY uk_member_merchant (merchant_id),
  registration_date DATETIME COMMENT '商户注册日期',
  cancellation_date DATETIME COMMENT '商户注销日期',
  start_date DATETIME COMMENT '开通会员日期',
  end_date DATETIME COMMENT '会员截止日期',
  member_type INT COMMENT '1.月会员/2.年会员/3.终身会员',
  payment_amount DECIMAL(10,2) DEFAULT 0 COMMENT '支付金额',
  original_price DECIMAL(10,2) DEFAULT 0 COMMENT '原价',
  discount_price DECIMAL(10,2) DEFAULT 0 COMMENT '折扣价',
  commission DECIMAL(10,2) DEFAULT 0 COMMENT '佣金',
  is_valid INT DEFAULT 1 COMMENT '1有效0无效',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
);

INSERT INTO merchant_member_info (
  merchant_id,
  registration_date,
  cancellation_date,
  start_date,
  end_date,
  member_type,
  payment_amount,
  original_price,
  discount_price,
  commission,
  is_valid
) VALUES
(1, '2026-01-01 10:00:00', NULL, '2026-01-01 10:00:00', '2026-12-31 23:59:59', 2, 0.00, 0.00, 0.00, 0.00, 1),
(2, '2026-01-02 11:00:00', NULL, NULL, NULL, NULL, 0.00, 0.00, 0.00, 0.00, 1),
(3, '2026-01-03 09:30:00', NULL, '2026-01-03 09:30:00', '2026-02-02 23:59:59', 1, 100.00, 120.00, 20.00, 0.00, 1),
(4, '2026-01-04 14:20:00', NULL, NULL, NULL, NULL, 0.00, 0.00, 0.00, 0.00, 1),
(5, '2026-01-05 16:45:00', NULL, '2026-01-05 16:45:00', '2027-01-04 23:59:59', 2, 1000.00, 1200.00, 200.00, 0.00, 1),
(6, '2026-01-06 08:15:00', NULL, '2026-01-06 08:15:00', '2026-02-05 23:59:59', 1, 100.00, 120.00, 20.00, 0.00, 1);

-- 会员充值记录
DROP TABLE IF EXISTS merchant_member_recharge;
CREATE TABLE IF NOT EXISTS merchant_member_recharge (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  merchant_id BIGINT COMMENT '商户信息表ID',
  recharge_amount DECIMAL(10, 2) COMMENT '充值金额',
  original_price DECIMAL(10, 2) COMMENT '原价',
  discount_amount DECIMAL(10, 2) COMMENT '优惠金额',
  recharge_type INT COMMENT '1.月会员/2.年会员/3.终身会员',
  recharge_time DATETIME COMMENT '充值时间',
  is_valid INT DEFAULT 1 COMMENT '1有效0无效'
);

INSERT INTO merchant_member_recharge (
  merchant_id,
  recharge_amount,
  original_price,
  discount_amount,
  recharge_type,
  recharge_time,
  is_valid
) VALUES
(1, 1000.00, 1200.00, 200.00, 2, '2026-01-01 10:00:00', 1),
(3, 100.00, 120.00, 20.00, 1, '2026-01-03 09:30:00', 1),
(5, 1000.00, 1200.00, 200.00, 2, '2026-01-05 16:45:00', 1),
(6, 100.00, 120.00, 20.00, 1, '2026-01-06 08:15:00', 1);

-- 会员积分表
DROP TABLE IF EXISTS merchant_member_integral;
CREATE TABLE IF NOT EXISTS merchant_member_integral (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  merchant_id BIGINT COMMENT '商户信息表ID',
  integral INT DEFAULT 0 COMMENT '会员积分',
  is_valid INT DEFAULT 1 COMMENT '1有效0无效',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
);

-- 积分变更记录
DROP TABLE IF EXISTS merchant_member_integral_spend;
CREATE TABLE IF NOT EXISTS merchant_member_integral_spend (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  merchant_id BIGINT COMMENT '商户信息表ID',
  integral_before_spend INT COMMENT '变更前积分',
  integral_after_spend INT COMMENT '变更后积分',
  change_amount INT COMMENT '变更积分，正为加负为扣',
  change_reason VARCHAR(255) COMMENT '变更原因，签到送积分，花钱充值积分，花费积分求购',
  order_id BIGINT COMMENT '如果是求购，保存求购订单ID',
  change_time DATETIME COMMENT '变更时间'
);

-- 积分充值记录
DROP TABLE IF EXISTS merchant_member_integral_recharge;
CREATE TABLE IF NOT EXISTS merchant_member_integral_recharge (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  merchant_id BIGINT COMMENT '商户信息表ID',
  integral_before_recharge INT COMMENT '充值前积分',
  integral_after_recharge INT COMMENT '充值后积分',
  recharge_amount INT COMMENT '充值积分',
  recharge_money DECIMAL(10, 2) COMMENT '充值金额',
  recharge_time DATETIME COMMENT '充值时间'
);

INSERT INTO merchant_member_integral (merchant_id, integral, is_valid) VALUES
(1, 960, 1),
(3, 10, 1),
(5, 400, 1),
(6, 200, 1);

INSERT INTO merchant_member_integral_recharge (merchant_id, integral_before_recharge, integral_after_recharge, recharge_amount, recharge_money, recharge_time) VALUES
(1, 0, 1000, 1000, 100.00, '2026-01-02 10:00:00'),
(5, 0, 500, 500, 50.00, '2026-01-05 17:00:00'),
(6, 0, 200, 200, 20.00, '2026-01-06 09:00:00');

INSERT INTO merchant_member_integral_spend (merchant_id, integral_before_spend, integral_after_spend, change_amount, change_reason, order_id, change_time) VALUES
(1, 0, 1000, 1000, '花钱充值积分', NULL, '2026-01-02 10:00:00'),
(1, 1000, 1010, 10, '签到送积分', NULL, '2026-01-03 09:00:00'),
(1, 1010, 960, -50, '花费积分求购', 1001, '2026-01-04 14:30:00'),
(3, 0, 10, 10, '签到送积分', NULL, '2026-01-03 10:00:00'),
(5, 0, 500, 500, '花钱充值积分', NULL, '2026-01-05 17:00:00'),
(5, 500, 400, -100, '花费积分求购', 1002, '2026-01-06 11:20:00'),
(6, 0, 200, 200, '花钱充值积分', NULL, '2026-01-06 09:00:00');


DROP TABLE IF EXISTS city_dict;
CREATE TABLE IF NOT EXISTS city_dict (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  city_code VARCHAR(64) UNIQUE,
  city_name VARCHAR(128),
  sort INT DEFAULT 0,
  valid INT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  modify_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO city_dict (city_code, city_name, sort, valid) VALUES
('000000', '全国', 1, 1),
('410100', '郑州市', 2, 1),
('440300', '深圳市', 3, 1),
('370100', '济南市', 4, 1),
('510100', '成都市', 5, 1),
('110000', '北京市', 6, 1),
('610100', '西安市', 7, 1),
('120000', '天津市', 8, 1),
('620100', '兰州市', 9, 1),
('420100', '武汉市', 10, 1),
('330100', '杭州市', 11, 1),
('130100', '石家庄市', 12, 1),
('430100', '长沙市', 13, 1),
('320100', '南京市', 14, 1),
('310000', '上海市', 15, 1),
('140100', '太原市', 16, 1),
('530100', '昆明市', 17, 1),
('640100', '银川市', 18, 1);

DROP TABLE IF EXISTS phone_remark_dict;
CREATE TABLE IF NOT EXISTS phone_remark_dict (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  remark_name VARCHAR(128) UNIQUE,
  sort INT DEFAULT 0,
  valid INT DEFAULT 1,
  type TINYINT DEFAULT 0 COMMENT '0新机备注 1新机其它备注 2二手机版本 3二手机成色 4二手机拆修和功能',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  modify_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO phone_remark_dict (remark_name, sort, valid, type) VALUES
('保证省内纯原', 1, 1, 0),
('保证全国纯原', 2, 1, 0),
('省内拆封激活', 3, 1, 0),
('全国拆封激活', 4, 1, 0),
('省内拆封未激活', 5, 1, 0),
('全国拆封未激活', 6, 1, 0),
('全国纯原怕查', 7, 1, 0),
('全国纯原不怕串', 8, 1, 0),
('官网纯原封预激活', 9, 1, 0),
('公司纯原封', 10, 1, 0),
('公司纯原不怕串', 11, 1, 0),
('纯原带AC+预激活', 12, 1, 0),
('公司纯原带活动', 13, 1, 0),
('代发', 14, 1, 0),
('全国纯原', 15, 1, 0),
('省内纯原', 16, 1, 0);

INSERT INTO phone_remark_dict (remark_name, sort, valid, type) VALUES
('默认', 1, 1, 1),
('禁止出省', 2, 1, 1),
('可出全国', 3, 1, 1),
('禁出线上', 4, 1, 1),
('包邮', 5, 1, 1),
('怕串', 6, 1, 1),
('不怕串', 7, 1, 1),
('包装瑕疵', 8, 1, 1),
('机器瑕疵', 9, 1, 1),
('带AC+', 10, 1, 1),
('权益版', 11, 1, 1),
('联通定制', 12, 1, 1),
('电信定制', 13, 1, 1),
('移动定制', 14, 1, 1),
('教育机', 15, 1, 1),
('政企定制', 16, 1, 1),
('官换机', 17, 1, 1),
('演示机', 18, 1, 1),
('含税', 19, 1, 1),
('TD鼎桥版', 20, 1, 1),
('现货当面激活', 21, 1, 1),
('带碎屏险', 22, 1, 1),
('官翻全国联保', 23, 1, 1),
('提供激活照片', 24, 1, 1),
('当天激活发出', 25, 1, 1),
('特定区域销售', 26, 1, 1),
('现货', 27, 1, 1),
('盒子刮码', 28, 1, 1),
('牛皮外箱无或开封', 29, 1, 1),
('可过串', 30, 1, 1);

INSERT INTO phone_remark_dict (remark_name, sort, valid, type) VALUES
('大陆国行', 1, 1, 2),
('海外无锁', 2, 1, 2),
('海外有锁', 3, 1, 2),
('其他版本', 4, 1, 2),
('香港行货', 5, 1, 2),
('国行官换/官修机', 6, 1, 2);

INSERT INTO phone_remark_dict (remark_name, sort, valid, type) VALUES
('全新未拆封', 1, 1, 3),
('几乎全新', 2, 1, 3),
('细微磕碰划痕', 3, 1, 3),
('少量磕碰划痕', 4, 1, 3),
('轻度磕碰划痕', 5, 1, 3),
('严重磕碰划痕', 6, 1, 3),
('屏幕破损或外壳破碎', 7, 1, 3),
('屏幕深度划伤或色差', 8, 1, 3),
('屏幕发黄/透图/色斑', 9, 1, 3);

INSERT INTO phone_remark_dict (remark_name, sort, valid, type) VALUES
('无任何维修', 1, 1, 4),
('屏幕有维修', 2, 1, 4),
('更换电池', 3, 1, 4),
('外壳/摄像头有维修', 4, 1, 4),
('功能明显异常', 5, 1, 4),
('零件有维修', 6, 1, 4),
('主板有维修', 7, 1, 4),
('功能有轻度异常', 8, 1, 4);







-- 商户上架二手手机产品表
DROP TABLE IF EXISTS merchant_phone_product;
CREATE TABLE IF NOT EXISTS merchant_phone_product (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  merchant_id BIGINT COMMENT '商户信息表ID',
  brand_id BIGINT COMMENT '品牌ID',
  series_id BIGINT COMMENT '系列ID',
  model_id BIGINT COMMENT '型号ID',
  spec_id BIGINT COMMENT '配置ID',
  city_code VARCHAR(64) COMMENT '城市编码',
  product_type INT DEFAULT 0 COMMENT '0新机 1二手机',
  remark VARCHAR(255) COMMENT '新机备注',
  other_remark VARCHAR(255) COMMENT '新机其它备注信息',
  second_hand_version VARCHAR(128) COMMENT '二手机版本',
  second_hand_condition VARCHAR(128) COMMENT '二手机成色',
  second_hand_function VARCHAR(128) COMMENT '二手机拆修和功能',
  battery_status INT COMMENT '电池状态',
  `desc` TEXT COMMENT '产品描述信息',
  price INT COMMENT '产品价格',
  stock INT COMMENT '产品库存',
  listing_time DATETIME COMMENT '上架时间',
  is_valid INT DEFAULT 1 COMMENT '1有效0无效',
  `state` INT DEFAULT 1 COMMENT '1商户上架 2商户下架',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  KEY idx_merchant_phone_product_merchant_id (merchant_id),
  KEY idx_merchant_phone_product_search (city_code, product_type, brand_id, series_id, model_id, spec_id)
);

INSERT INTO merchant_phone_product (
  merchant_id, brand_id, series_id, model_id, spec_id, city_code,
  product_type, remark, other_remark,
  second_hand_version, second_hand_condition, second_hand_function, battery_status,
  `desc`, price, stock, listing_time
) VALUES
(
  1,
  (SELECT id FROM brand WHERE name = '苹果'),
  (SELECT id FROM phone_series WHERE series_name = 'iPhone'),
  (SELECT id FROM phone_model WHERE model_name = 'iPhone 16 Pro Max'),
  (SELECT id FROM phone_spec WHERE spec_name = '1TB 黑色'),
  '370100', -- Jinan
  0, -- New
  '保证全国纯原', '当天发货',
  NULL, NULL, NULL, NULL,
  '全新未拆封，正品保证', 9999, 10, NOW()
),
(
  5,
  (SELECT id FROM brand WHERE name = '苹果'),
  (SELECT id FROM phone_series WHERE series_name = 'iPhone'),
  (SELECT id FROM phone_model WHERE model_name = 'iPhone 15'),
  (SELECT id FROM phone_spec WHERE spec_name = '128G 白色'),
  '510100', -- Chengdu
  1, -- Second-hand
  NULL, NULL,
  '大陆国行', '细微磕碰划痕', '无任何维修', 95,
  '个人自用一手，成色很好', 3500, 1, NOW()
),
(
  6,
  (SELECT id FROM brand WHERE name = '华为'),
  (SELECT id FROM phone_series WHERE series_name = 'Mate'),
  (SELECT id FROM phone_model WHERE model_name = 'Mate 60 Pro'),
  (SELECT id FROM phone_spec WHERE spec_name = '512G 雅川青'),
  '610100', -- Xi'an
  0, -- New
  '公司纯原封', '送手机壳',
  NULL, NULL, NULL, NULL,
  '遥遥领先', 6999, 5, NOW()
);

DROP TABLE IF EXISTS merchant_product_image;
CREATE TABLE IF NOT EXISTS merchant_product_image (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  product_id BIGINT COMMENT '商户上架二手手机产品表ID',
  image_url VARCHAR(512) COMMENT '图片URL',
  valid INT DEFAULT 1 COMMENT '1有效0无效',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
);

INSERT INTO merchant_product_image (product_id, image_url) VALUES
(
  (SELECT id FROM merchant_phone_product WHERE merchant_id = 1 AND `desc` = '全新未拆封，正品保证'),
  'https://example.com/products/iphone16promax_1.jpg'
),
(
  (SELECT id FROM merchant_phone_product WHERE merchant_id = 1 AND `desc` = '全新未拆封，正品保证'),
  'https://example.com/products/iphone16promax_2.jpg'
),
(
  (SELECT id FROM merchant_phone_product WHERE merchant_id = 5 AND `desc` = '个人自用一手，成色很好'),
  'https://example.com/products/iphone15_1.jpg'
),
(
  (SELECT id FROM merchant_phone_product WHERE merchant_id = 6 AND `desc` = '遥遥领先'),
  'https://example.com/products/mate60pro_1.jpg'
),
(
  (SELECT id FROM merchant_phone_product WHERE merchant_id = 6 AND `desc` = '遥遥领先'),
  'https://example.com/products/mate60pro_2.jpg'
);

-- 求购信息表
DROP TABLE IF EXISTS buy_request;
CREATE TABLE IF NOT EXISTS buy_request (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  merchant_id BIGINT COMMENT '商户信息表ID',
  brand_id BIGINT COMMENT '品牌ID',
  series_id BIGINT COMMENT '系列ID',
  model_id BIGINT COMMENT '型号ID',
  spec_id BIGINT COMMENT '配置ID',
  city_code VARCHAR(64) COMMENT '城市编码',
  product_type INT DEFAULT 0 COMMENT '0新机 1二手机',
  buy_count INT COMMENT '求购数量',
  min_price INT COMMENT '求购最低价格',
  max_price INT COMMENT '求购最高价格',
  deadline DATETIME COMMENT '求购截止时间',
  cost_integral INT COMMENT '求购花费积分',
  is_valid INT DEFAULT 1 COMMENT '1有效0无效',
  `state` INT DEFAULT 1 COMMENT '1求购中 2已解决',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
);


insert into buy_request (merchant_id, brand_id, series_id, model_id, spec_id, city_code, product_type, buy_count, min_price, max_price, deadline, cost_integral)
values
(
  (SELECT id FROM merchant_info WHERE merchant_name = '张三通讯'),
  (SELECT id FROM brand WHERE name = '苹果'),
  (SELECT id FROM phone_series WHERE series_name = 'iPhone'),
  (SELECT id FROM phone_model WHERE model_name = 'iPhone 16 Pro Max'),
  (SELECT id FROM phone_spec WHERE spec_name = '1TB 黑色'),
  '370100', -- Jinan
  0, -- New
  1, -- 1 piece
  9000, 10000, -- price range
  '2024-08-01 23:59:59', -- deadline
  100 -- cost integral
),
(
  (SELECT id FROM merchant_info WHERE merchant_name = '李四数码'),
  (SELECT id FROM brand WHERE name = '苹果'),
  (SELECT id FROM phone_series WHERE series_name = 'iPhone'),
  (SELECT id FROM phone_model WHERE model_name = 'iPhone 15'),
  (SELECT id FROM phone_spec WHERE spec_name = '128G 白色'),
  '440300', -- Shenzhen
  1, -- Second hand
  2, -- 2 pieces
  4000, 5000, -- price range
  '2024-09-01 23:59:59', -- deadline
  50 -- cost integral
),
(
  (SELECT id FROM merchant_info WHERE merchant_name = '王五手机城'),
  (SELECT id FROM brand WHERE name = '华为'),
  (SELECT id FROM phone_series WHERE series_name = 'Mate'),
  (SELECT id FROM phone_model WHERE model_name = 'Mate 60 Pro'),
  (SELECT id FROM phone_spec WHERE spec_name = '512G 雅川青'),
  '410100', -- Zhengzhou
  0, -- New
  5, -- 5 pieces
  6000, 7000, -- price range
  '2024-10-01 23:59:59', -- deadline
  200 -- cost integral
);

-- 后台管理登录用户表
DROP TABLE IF EXISTS admin_user;
CREATE TABLE IF NOT EXISTS admin_user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(64) COMMENT '用户名',
  password VARCHAR(64) COMMENT '密码',
  is_valid INT DEFAULT 1 COMMENT '1有效0无效',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
);
-- 初始化管理员用户，密码是被md5 32位加密过的 
INSERT INTO admin_user (username, `password`, is_valid) VALUES
('admin', 'c82636a0061634041a9bb577c6f3b1e6', 1); -- saizan12345  

-- 短信发送日志表
DROP TABLE IF EXISTS sms_log;
CREATE TABLE IF NOT EXISTS sms_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  wechat_id VARCHAR(128) COMMENT '微信ID',
  phone VARCHAR(32) COMMENT '手机号',
  code VARCHAR(16) COMMENT '短信验证码',
  send_time DATETIME COMMENT '发送时间',
  KEY idx_sms_log_wechat_id (wechat_id)
);

-- 商户通话记录表
DROP TABLE IF EXISTS merchant_call_record;
CREATE TABLE IF NOT EXISTS merchant_call_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  call_type INT DEFAULT 0 COMMENT '0产品电话 1求购电话',
  caller_merchant_id BIGINT COMMENT '拨打电话的商户ID',
  callee_merchant_id BIGINT COMMENT '被拨打电话的商户ID',
  product_id BIGINT COMMENT '因哪个产品拨打电话',
  call_time DATETIME COMMENT '通话时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
);
