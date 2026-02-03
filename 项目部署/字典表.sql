SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS msi CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE msi;

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