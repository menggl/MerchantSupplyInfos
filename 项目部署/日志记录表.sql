SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS msi CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE msi;

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