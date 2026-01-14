SET NAMES utf8mb4;
INSERT INTO supply_item(brand,series,model,color,storage,spec,city,price,seller,updated_at) VALUES
('苹果','iPhone','iPhone 17','黑色','256G','17 6.3寸 国行','济南市',6299,'A商家',CURRENT_TIMESTAMP()),
('苹果','iPhone','iPhone 16','白色','128G','16 6.1寸 国行','济南市',4599,'B商家',CURRENT_TIMESTAMP()),
('华为','Mate','Mate 70','银色','256G','70 6.7寸 国行','济南市',5399,'C商家',CURRENT_TIMESTAMP());

INSERT INTO brand(name) VALUES
('苹果'),('华为'),('小米'),('OPPO'),('VIVO'),('三星'),('荣耀');

INSERT INTO phone_series(brand_id,series_name,sort,deleted)
SELECT b.id,'iPhone',100,0 FROM brand b WHERE b.name='苹果';
INSERT INTO phone_series(brand_id,series_name,sort,deleted)
SELECT b.id,'Mate',100,0 FROM brand b WHERE b.name='华为';
INSERT INTO phone_series(brand_id,series_name,sort,deleted)
SELECT b.id,'P',90,0 FROM brand b WHERE b.name='华为';
INSERT INTO phone_series(brand_id,series_name,sort,deleted)
SELECT b.id,'小米',100,0 FROM brand b WHERE b.name='小米';

INSERT INTO phone_model(brand_id,series_id,model_name,sort,deleted)
VALUES ((SELECT id FROM brand WHERE name='苹果'),(SELECT id FROM phone_series WHERE series_name='iPhone'),'iPhone 17',100,0);
INSERT INTO phone_model(brand_id,series_id,model_name,sort,deleted)
VALUES ((SELECT id FROM brand WHERE name='苹果'),(SELECT id FROM phone_series WHERE series_name='iPhone'),'iPhone 16',90,0);
INSERT INTO phone_model(brand_id,series_id,model_name,sort,deleted)
VALUES ((SELECT id FROM brand WHERE name='苹果'),(SELECT id FROM phone_series WHERE series_name='iPhone'),'iPhone 15',80,0);

INSERT INTO phone_model(brand_id,series_id,model_name,sort,deleted)
VALUES ((SELECT id FROM brand WHERE name='华为'),(SELECT id FROM phone_series WHERE series_name='Mate'),'Mate 70',100,0);
INSERT INTO phone_model(brand_id,series_id,model_name,sort,deleted)
VALUES ((SELECT id FROM brand WHERE name='华为'),(SELECT id FROM phone_series WHERE series_name='P'),'P70',95,0);

INSERT INTO phone_model(brand_id,series_id,model_name,sort,deleted)
VALUES ((SELECT id FROM brand WHERE name='小米'),(SELECT id FROM phone_series WHERE series_name='小米'),'小米14',90,0);

INSERT INTO phone_spec(brand_id,series_id,model_id,spec_name,sort,deleted)
SELECT (SELECT id FROM brand WHERE name='苹果'), (SELECT id FROM phone_series WHERE series_name='iPhone'), m.id,'17 6.3寸 国行',100,0 FROM phone_model m WHERE m.model_name='iPhone 17';
INSERT INTO phone_spec(brand_id,series_id,model_id,spec_name,sort,deleted)
SELECT (SELECT id FROM brand WHERE name='苹果'), (SELECT id FROM phone_series WHERE series_name='iPhone'), m.id,'16 6.1寸 国行',90,0 FROM phone_model m WHERE m.model_name='iPhone 16';
INSERT INTO phone_spec(brand_id,series_id,model_id,spec_name,sort,deleted)
SELECT (SELECT id FROM brand WHERE name='苹果'), (SELECT id FROM phone_series WHERE series_name='iPhone'), m.id,'15 6.1寸 国行',80,0 FROM phone_model m WHERE m.model_name='iPhone 15';

INSERT INTO phone_spec(brand_id,series_id,model_id,spec_name,sort,deleted)
SELECT (SELECT id FROM brand WHERE name='华为'), (SELECT id FROM phone_series WHERE series_name='Mate'), m.id,'70 6.7寸 国行',100,0 FROM phone_model m WHERE m.model_name='Mate 70';
INSERT INTO phone_spec(brand_id,series_id,model_id,spec_name,sort,deleted)
SELECT (SELECT id FROM brand WHERE name='华为'), (SELECT id FROM phone_series WHERE series_name='P'), m.id,'P70 6.5寸 国行',95,0 FROM phone_model m WHERE m.model_name='P70';

INSERT INTO phone_spec(brand_id,series_id,model_id,spec_name,sort,deleted)
SELECT (SELECT id FROM brand WHERE name='小米'), (SELECT id FROM phone_series WHERE series_name='小米'), m.id,'14 6.7寸 国行',90,0 FROM phone_model m WHERE m.model_name='小米14';

INSERT INTO merchant_info(
  id, wechat_id, wechat_name, merchant_name, merchant_phone, merchant_address,
  contact_name, is_member, member_expire_date, registration_date, is_valid
) VALUES (
  1, 'wx_123456', '张三', 'A商户', '13800000001', '济南市历城区',
  '张三', 1, DATE_ADD(CURRENT_TIMESTAMP(), INTERVAL 90 DAY), CURRENT_TIMESTAMP(), 1
);
