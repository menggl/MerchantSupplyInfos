USE msi;

DROP TABLE IF EXISTS merchant_recharge_order;
CREATE TABLE IF NOT EXISTS merchant_recharge_order (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_no VARCHAR(64) NOT NULL UNIQUE COMMENT '订单号',
  merchant_id BIGINT NOT NULL COMMENT '商户信息表ID',
  recharge_type INT NOT NULL COMMENT '1.积分充值 2.会员充值',
  integral_amount INT COMMENT '积分数量',
  member_months INT COMMENT '会员月数',
  total_amount INT NOT NULL COMMENT '支付金额(分)',
  pay_status INT NOT NULL DEFAULT 0 COMMENT '0未支付 1已完成',
  wechat_transaction_id VARCHAR(64) COMMENT '微信支付订单号',
  app_id VARCHAR(32) COMMENT '小程序AppID',
  prepay_id VARCHAR(64) COMMENT '微信预支付ID',
  nonce_str VARCHAR(64) COMMENT '随机字符串',
  time_stamp VARCHAR(32) COMMENT '时间戳',
  package_val VARCHAR(128) COMMENT '统一下单接口返回的 prepay_id 参数值，提交格式如：prepay_id=***',
  sign_type VARCHAR(32) DEFAULT 'RSA' COMMENT '签名类型',
  pay_sign VARCHAR(512) COMMENT '签名',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
);

DROP TABLE IF EXISTS wechat_pay_notify_log;
CREATE TABLE IF NOT EXISTS wechat_pay_notify_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  notify_id VARCHAR(64) COMMENT '通知ID',
  event_type VARCHAR(64) COMMENT '事件类型',
  resource_type VARCHAR(64) COMMENT '资源类型',
  summary VARCHAR(255) COMMENT '摘要',
  resource_ciphertext TEXT COMMENT '加密数据',
  request_body TEXT COMMENT '原始请求体',
  process_status VARCHAR(255) COMMENT '处理状态',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
);
