SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS msi CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE msi;


-- 商户信息表
DROP TABLE IF EXISTS merchant_info;
CREATE TABLE IF NOT EXISTS merchant_info (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  public_id VARCHAR(32) UNIQUE COMMENT '商户外部ID（UUID去掉-）',
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
  stock_id BIGINT COMMENT '库存ID',-- 关联库存表的主键ID，merchant_new_phone_stock表、merchant_second_phone_stock表
  remark VARCHAR(255) COMMENT '新机备注',
  other_remark VARCHAR(255) COMMENT '新机其它备注信息',
  second_hand_version VARCHAR(128) COMMENT '二手机版本',
  second_hand_condition VARCHAR(128) COMMENT '二手机成色',
  second_hand_function VARCHAR(128) COMMENT '二手机拆修和功能',
  battery_status INT COMMENT '电池状态',
  description TEXT COMMENT '产品描述信息',
  price INT COMMENT '产品价格',
  stock INT COMMENT '产品库存',
  listing_time DATETIME COMMENT '上架时间',
  is_valid INT DEFAULT 1 COMMENT '1有效0无效',
  `state` INT DEFAULT 1 COMMENT '1商户上架 2商户下架',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  KEY idx_merchant_phone_product_merchant_id (merchant_id),
  KEY idx_merchant_phone_product_search (city_code, product_type, brand_id, series_id, model_id, spec_id),
  UNIQUE KEY uk_product_type_stock_id (product_type, stock_id)
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





-- 新增一个商户自己维护的一套品牌、系列、型号、配置
-- 品牌、系列、型号的ID可以是已有品牌、系列、型号的ID，如果ID值为空，那么表示新增一个品牌、系列、型号
-- 除了ID，还有一个name字段，用于存储品牌、系列、型号、配置的名称
-- 每一个商户最多只能同时存在5条商户自己维护的品牌、系列、型号、配置数据
DROP TABLE IF EXISTS merchant_custom_brand;
CREATE TABLE IF NOT EXISTS merchant_custom_brand (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  merchant_id BIGINT COMMENT '商户信息表ID',
  brand_id BIGINT COMMENT '品牌ID',
  brand_name VARCHAR(64) COMMENT '品牌名称',
  series_id BIGINT COMMENT '系列ID',
  series_name VARCHAR(64) COMMENT '系列名称',
  model_id BIGINT COMMENT '型号ID',
  model_name VARCHAR(64) COMMENT '型号名称',
  spec_id BIGINT COMMENT '配置ID', -- 这个字段应该不可能有值，如果有值，说明该品牌、系列、型号、配置已经存在字典表中
  spec_name VARCHAR(64) COMMENT '配置名称',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
);

-- 商户和配置表的id字段，用于关联商户自己维护的一套品牌、系列、型号、配置
-- 有一个spec_type字段，为0表示是共有配置（phone_spec表），1表示是商户自己维护的配置（merchant_custom_dict表）
-- 有表自增id，商户id，spec_type、phone_spec表ID，merchant_custom_dict表ID
-- 有一个create_time字段，记录创建时间
-- 有一个update_time字段，记录修改时间
DROP TABLE IF EXISTS merchant_brand_map;
CREATE TABLE IF NOT EXISTS merchant_brand_map (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  merchant_id BIGINT COMMENT '商户信息表ID',
  spec_type INT DEFAULT 0 COMMENT '0共有配置 1商户自己维护的配置',
  phone_spec_id BIGINT COMMENT '共有配置ID',
  custom_brand_id BIGINT COMMENT '商户自己维护的配置ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
);

-- 商户的新机库存信息，有自增id，商户ID，merchant_brand_map ID，备注ID、其它备注Id、售卖价，上下架状态
-- 上下架状态为0表示下架，1表示上架
-- 有一个create_time字段，记录创建时间
-- 有一个update_time字段，记录修改时间
DROP TABLE IF EXISTS merchant_new_phone_stock;
CREATE TABLE IF NOT EXISTS merchant_new_phone_stock (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  merchant_id BIGINT COMMENT '商户信息表ID',
  brand_map_id BIGINT COMMENT '商户自己维护的配置ID(merchant_brand_map)',
  spec_type INT DEFAULT 0 COMMENT '0共有配置 1商户自己维护的配置',
  remark_id BIGINT COMMENT '备注ID',
  other_remark_id BIGINT COMMENT '其它备注ID',
  price INT COMMENT '售卖价（分）',
  stock_count INT COMMENT '库存数量',
  stock_status INT DEFAULT 0 COMMENT '上下架状态 0下架 1上架', -- 默认值是下架
  valid INT DEFAULT 1 COMMENT '1有效0无效', -- 默认值是有效
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
);

-- 商户的二手机库存信息，有自增id，商户ID，merchant_brand_map ID，版本ID、成色Id，拆修和功能ID，电池健康度、售卖价，上下架状态
-- 上下架状态为0表示下架，1表示上架
-- 有一个create_time字段，记录创建时间
-- 有一个update_time字段，记录修改时间
DROP TABLE IF EXISTS merchant_second_phone_stock;
CREATE TABLE IF NOT EXISTS merchant_second_phone_stock (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  merchant_id BIGINT COMMENT '商户信息表ID',
  brand_map_id BIGINT COMMENT '商户自己维护的配置ID(merchant_brand_map)',
  spec_type INT DEFAULT 0 COMMENT '0共有配置 1商户自己维护的配置',
  version_id BIGINT COMMENT '版本ID',
  condition_id BIGINT COMMENT '成色ID',
  repair_function_id BIGINT COMMENT '拆修和功能ID',
  battery_health_id INT COMMENT '电池健康度ID',
  price INT COMMENT '售卖价（分）',
  stock_count INT COMMENT '库存数量',
  stock_status INT DEFAULT 0 COMMENT '上下架状态 0下架 1上架', -- 默认值是下架
  valid INT DEFAULT 1 COMMENT '1有效0无效', -- 默认值是有效
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
);

-- 新机、二手机实付款、实收款、款项备注
-- 表的自增ID、商户ID、库存ID、实付款（分）、实收款（分）、款项备注
DROP TABLE IF EXISTS merchant_payment_record;
CREATE TABLE IF NOT EXISTS merchant_payment_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  merchant_id BIGINT COMMENT '商户信息表ID',
  stock_id BIGINT COMMENT '库存ID',
  actual_payment INT COMMENT '实付款（分）',
  actual_receipt INT COMMENT '实收款（分）',
  remark VARCHAR(256) COMMENT '款项备注',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
);

-- 新机、二手机的入库价、数量（二手机固定为1）、出库价、数量（二手机固定为1）
-- 商户ID、stock_type（0新机 1二手机）、stock ID、入库价（分）、数量、出库价（分）、数量
DROP TABLE IF EXISTS merchant_stock_price_count;
CREATE TABLE IF NOT EXISTS merchant_stock_price_count (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  merchant_id BIGINT COMMENT '商户信息表ID',
  stock_type INT DEFAULT 0 COMMENT '0新机 1二手机',
  stock_id BIGINT COMMENT '库存ID',
  in_price INT COMMENT '入库价（分）',
  in_count INT COMMENT '数量',
  out_price INT COMMENT '出库价（分）',
  out_count INT COMMENT '数量',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
);
