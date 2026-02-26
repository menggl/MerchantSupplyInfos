SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS msi CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE msi;

-- 已统一到商户商品表，移除 supply_item

-- 手机品牌表
DROP TABLE IF EXISTS brand;
CREATE TABLE IF NOT EXISTS brand (
  id BIGINT PRIMARY KEY,
  name VARCHAR(64) UNIQUE,
  sort INT DEFAULT 0,
  deleted TINYINT DEFAULT 0
);

-- 手机系列表
DROP TABLE IF EXISTS phone_series;
CREATE TABLE IF NOT EXISTS phone_series (
  id BIGINT PRIMARY KEY,
  brand_id BIGINT,
  series_name VARCHAR(64),
  sort INT DEFAULT 0,
  deleted TINYINT DEFAULT 0
);

-- 手机型号表
DROP TABLE IF EXISTS phone_model;
CREATE TABLE IF NOT EXISTS phone_model (
  id BIGINT PRIMARY KEY,
  brand_id BIGINT,
  series_id BIGINT,
  model_name VARCHAR(128),
  sort INT DEFAULT 0,
  deleted TINYINT DEFAULT 0
);

-- 手机规格表
DROP TABLE IF EXISTS phone_spec;
CREATE TABLE IF NOT EXISTS phone_spec (
  id BIGINT PRIMARY KEY,
  brand_id BIGINT,
  series_id BIGINT,
  model_id BIGINT,
  spec_name VARCHAR(128),
  sort INT DEFAULT 0,
  deleted TINYINT DEFAULT 0
);


-- 商户信息表
DROP TABLE IF EXISTS merchant_info;
CREATE TABLE IF NOT EXISTS merchant_info (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  public_id VARCHAR(32) UNIQUE COMMENT '商户外部ID（UUID去掉-）',
  wechat_id VARCHAR(128) UNIQUE,
  token VARCHAR(128) UNIQUE COMMENT '登录凭证',
  wechat_name VARCHAR(128),
  merchant_name VARCHAR(128),
  merchant_phone VARCHAR(32) UNIQUE,
  passwd VARCHAR(32) COMMENT '登录密码(MD5)',
  registration_date DATETIME COMMENT '商户注册日期',
  cancellation_date DATETIME COMMENT '商户注销日期',
  city_code VARCHAR(64) COMMENT '城市编码',
  merchant_address VARCHAR(255),
  latitude DECIMAL(10, 6) COMMENT '纬度',
  longitude DECIMAL(10, 6) COMMENT '经度',
  business_license_url VARCHAR(150) COMMENT '营业执照图片URL',
  store_photo_url VARCHAR(150) COMMENT '商家门店照片URL',
  id_card_photo_url VARCHAR(150) COMMENT '商家身份证照片URL',
  contact_name VARCHAR(64) COMMENT '联系人姓名',
  is_valid INT DEFAULT 1 COMMENT '1有效0无效',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);




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


DROP TABLE IF EXISTS user_feedback;
CREATE TABLE IF NOT EXISTS user_feedback (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  merchant_id BIGINT NOT NULL COMMENT '商户ID',
  feedback_content VARCHAR(300) NOT NULL COMMENT '商户反馈内容',
  feedback_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '商户反馈时间'
);



DROP TABLE IF EXISTS city_dict;
CREATE TABLE IF NOT EXISTS city_dict (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  city_code VARCHAR(64) UNIQUE,
  city_name VARCHAR(128),
  sort INT DEFAULT 0,
  valid INT DEFAULT 1,
  is_online INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  modify_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO city_dict (city_code, city_name, sort, valid, is_online) VALUES
('000000', '全国', 1, 1, 1),
('410100', '郑州市', 2, 1, 0),
('440300', '深圳市', 3, 1, 0),
('370100', '济南市', 4, 1, 1),
('510100', '成都市', 5, 1, 0),
('110000', '北京市', 6, 1, 0),
('610100', '西安市', 7, 1, 0),
('120000', '天津市', 8, 1, 0),
('620100', '兰州市', 9, 1, 0),
('420100', '武汉市', 10, 1, 0),
('330100', '杭州市', 11, 1, 0),
('130100', '石家庄市', 12, 1, 0),
('430100', '长沙市', 13, 1, 0),
('320100', '南京市', 14, 1, 0),
('310000', '上海市', 15, 1, 0),
('140100', '太原市', 16, 1, 0),
('530100', '昆明市', 17, 1, 0),
('640100', '银川市', 18, 1, 0);

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
  description TEXT COMMENT '产品描述信息',
  region VARCHAR(50) COMMENT '新机售卖区域',
  price INT COMMENT '产品价格',
  stock INT COMMENT '产品库存',
  listing_time DATETIME COMMENT '上架时间',
  is_valid INT DEFAULT 1 COMMENT '1有效0无效',
  `state` INT DEFAULT 1 COMMENT '1商户上架 2商户下架',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  KEY idx_merchant_phone_product_merchant_id (merchant_id),
  -- 优化后的复合索引：product_type（必选）、brand_id（常用）、city_code（可选）放在后面
  -- 这样即使 city_code 为空，product_type 和 brand_id 依然可以使用索引
  KEY idx_merchant_phone_product_search (product_type, brand_id, city_code, series_id, model_id, spec_id),
  -- 针对价格范围查询的辅助索引 (当brand_id=-1或需要价格排序/筛选时使用)
  KEY idx_merchant_phone_product_price (product_type, price, city_code)
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

-- 商户搜索记录表
DROP TABLE IF EXISTS merchant_search_record;
CREATE TABLE IF NOT EXISTS merchant_search_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  merchant_id BIGINT COMMENT '商户信息表ID',
  product_type INT DEFAULT 0 COMMENT '0新机 1二手机',
  search_keyword VARCHAR(30) COMMENT '模糊搜索关键词',
  brand_id BIGINT COMMENT '品牌ID',
  series_id BIGINT COMMENT '系列ID',
  model_id BIGINT COMMENT '型号ID',
  spec_id BIGINT COMMENT '配置ID',
  city_code VARCHAR(6) COMMENT '城市编码',
  search_time DATETIME COMMENT '搜索时间'
);
