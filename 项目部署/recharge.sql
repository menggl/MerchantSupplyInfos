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
  status INT NOT NULL DEFAULT 0 COMMENT '0未支付 1已完成',
  wechat_transaction_id VARCHAR(64) COMMENT '微信支付订单号',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
);

