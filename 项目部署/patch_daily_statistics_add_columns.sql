-- 补全 daily_statistics 表缺失的字段
ALTER TABLE daily_statistics ADD COLUMN daily_recharge_amount BIGINT DEFAULT 0 COMMENT '当日充值金额';
ALTER TABLE daily_statistics ADD COLUMN daily_new_member_count INT DEFAULT 0 COMMENT '当日新增会员数';
ALTER TABLE daily_statistics ADD COLUMN total_member_count INT DEFAULT 0 COMMENT '截止当天总会员数';
