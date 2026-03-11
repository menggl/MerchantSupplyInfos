# 以后我提的所有的功能需求，都要在这个文档中记录保存，实现完之后，要及时更新这个文档（我提的要求-》实现细节），如果我提的要求太啰嗦和复杂，你可以简化描述（以你的理解去简化），但不允许修改我提的要求

滑动验证码的github开源代码地址，使用这个滑动验证码替换现有的图形验证码
https://github.com/raodv/captcha?tab=readme-ov-file



根据下面的要求实现支付接口功能，包括数据库表（[text](项目部署/recharge.sql)）设计和对应的接口功能（/Users/menggl/workspace/MerchantSupplyInfos/backend/src/main/java/com/msi/controller/WeChatPayController.java）

接口文档：https://pay.weixin.qq.com/doc/v3/partner/4012076732
结论 为“积分充值”小程序支付，后端需要完成两步：

1. 调用服务商模式 JSAPI/小程序下单接口生成 prepay_id。
2. 组装并签名返回给小程序 wx.requestPayment 所需字段。
    以下是必须生成/返回的字段清单与规则。
后端下单（服务商模式）必须准备的字段

- sp_appid：服务商小程序 appid（若在服务商小程序内支付）
- sp_mchid：服务商商户号
- sub_mchid：子商户号（收款方）
- sub_appid：子商户小程序 appid（若在子商户小程序内支付）
- out_trade_no：商户订单号
- description：商品描述（如“积分充值-xxx积分”）
- amount.total：订单金额（分）
- amount.currency：CNY
- payer：支付者信息
  - 若在服务商小程序内支付：payer.sp_openid
  - 若在子商户小程序内支付：payer.sub_openid
- notify_url：支付回调地址
- 可选但常用：time_expire、attach、scene_info、detail、goods_tag、support_fapiao、settle_info
   来源与规则说明：服务商开发指引明确了 sub_mchid/sub_appid/prepay_id 等关键参数与整体流程。 ( https://pay.weixin.qq.com/doc/v3/partner/4012076732 )
后端返回给小程序的字段（用于 wx.requestPayment） 这些字段必须由后端生成并返回给前端：

- appId：实际调起支付的小程序 appid（与下单时 sp_appid 或 sub_appid 一致）
- timeStamp：秒级时间戳字符串
- nonceStr：随机字符串
- package：固定格式 "prepay_id=xxx"
- signType：RSA
- paySign：使用 appId、timeStamp、nonceStr、package 生成的签名
   这些字段是小程序拉起支付的必需参数。 ( https://pay.weixin.qq.com/doc/v3/merchant/4012791898 )
建议的后端响应结构（示例字段）

- payment：{ appId, timeStamp, nonceStr, package, signType, paySign }
- order：{ out_trade_no, amount, rechargeId }
   这样前端只负责调用 wx.requestPayment，订单号用于查询与回调对账。
实现要点

- 生成 paySign 时使用“实际调起支付的小程序 appid”，并且与下单时 appid 保持一致，否则无法拉起支付。 ( https://pay.weixin.qq.com/doc/v3/partner/4012076732 )
- prepay_id 有效期 2 小时，超时需重新下单获取。 ( https://pay.weixin.qq.com/doc/v3/partner/4012076732 )
- 支付结果以服务端回调/查单为准，前端回调只做展示。 ( https://pay.weixin.qq.com/doc/v3/partner/4012076732 )
如需我进一步给出你们当前“积分充值”页面的具体接口设计（请求/响应字段、签名生成流程、回调验签处理），我可以直接按你们后端框架整理一份可落地的接口清单。


https://pay.weixin.qq.com/doc/v3/partner/4012085801
参考下这个回调接口文档，梳理下回调接口的实现逻辑是否正常，回调接口必须先打印日志，再将请求参数保存到数据库表（/Users/menggl/workspace/MerchantSupplyInfos/项目部署/recharge.sql增加一个创建表语句）中，再实现对应的业务逻辑

/Users/menggl/workspace/MerchantSupplyInfos/backend/src/main/java/com/msi/config/WeChatPayConfig.java
/Users/menggl/workspace/MerchantSupplyInfos/.env
商户号：1738438969
appID：wx0901e1be9b067e6d


/Users/menggl/workspace/MerchantSupplyInfos/项目部署/schema.sql
在这个sql文件里面增加一个用户反馈表，要求有商户ID，商户反馈内容（300个字），商户反馈时间（默认当前时间）

/Users/menggl/workspace/MerchantSupplyInfos/backend/src/main/java/com/msi/controller
在这个目录下面增加一个Controller类，类名叫做UserFeedbackController，这个类的作用是处理用户反馈的请求，严格校验用户反馈的内容（不能有sql注入内容，安全性要考虑清楚）

该用户反馈接口必须登录并且只有注册商户才能调用，拦截器中拦截一下

如果用户提交的内容涉及sql注入，也给用户返回反馈成功（但是不会保存到数据库，打印专属的日志），防止注入攻击


我想在backend项目中添加一个定时任务，每天晚上12点之后做前一天的数据统计，然后发送统计结果到企业微信群（另一个群）里面，统计信息包含下面几个数据
1.每天新增商户的数量（merchant_info表中create_time为当天的商户数量），总共的商户数量，每天真实注册商户的数量（merchant_info表中create_time为当天，并且merchant_phone不为空的商户数量），总共的有效注册的商户数量（merchant_info表中merchant_phone字段不为空的商户数量）
2.新机：每天上架产品的数量（merchant_phone_product表中product_type为0，is_valid为1，state为1，并且create_time为当天的数量，条数）

新用户（merchant_phone_product表中create_time为当天，并且merchant_id在merchant_info表中merchant_phone字段不为空的商户数量）上架产品的数量（merchant_phone_product表中product_type为0，is_valid为1，state为1，并且create_time为当天的数量，条数）

老用户（merchant_phone_product表中create_time不是当天，并且merchant_id在merchant_info表中merchant_phone字段不为空的商户数量）上架产品的数量（merchant_phone_product表中product_type为0，is_valid为1，state为1，并且create_time为当天的数量，条数）

每天更新的上架产品的数量（merchant_phone_product表中product_type为0，is_valid为1，state为1，并且update_time为当天的数量，条数）

新用户当天更新上架产品的数量（merchant_phone_product表中product_type为0，is_valid为1，state为1，并且update_time为当天的数量，条数）

老用户当天更新上架产品的数量（merchant_phone_product表中product_type为0，is_valid为1，state为1，并且update_time为当天的数量，条数）

总共的上架产品数量（merchant_phone_product表中product_type为0，is_valid为1，state为1，条数）
未上架的有效产品数量（merchant_phone_product表中product_type为0，is_valid为1，state为2，条数）
更新的求购新机数量（buy_request表中product_type为0，is_valid为1，state为1，update_time字段为当天的数量，条数）
3.二手机：每天上架产品的数量（merchant_phone_product表中product_type为1，is_valid为1，state为1，并且create_time为当天的数量，条数）
每天更新的上架产品的数量（merchant_phone_product表中product_type为1，is_valid为1，state为1，并且update_time为当天的数量，条数）

新用户（merchant_phone_product表中create_time为当天，并且merchant_id在merchant_info表中merchant_phone字段不为空的商户数量）上架产品的数量（merchant_phone_product表中product_type为1，is_valid为1，state为1，并且create_time为当天的数量，条数）

老用户（merchant_phone_product表中create_time不是当天，并且merchant_id在merchant_info表中merchant_phone字段不为空的商户数量）上架产品的数量（merchant_phone_product表中product_type为1，is_valid为1，state为1，并且create_time为当天的数量，条数）

每天更新的上架产品的数量（merchant_phone_product表中product_type为1，is_valid为1，state为1，并且update_time为当天的数量，条数）

新用户当天更新上架产品的数量（merchant_phone_product表中product_type为1，is_valid为1，state为1，并且update_time为当天的数量，条数）

老用户当天更新上架产品的数量（merchant_phone_product表中product_type为1，is_valid为1，state为1，并且update_time为当天的数量，条数）

总共的上架产品数量（merchant_phone_product表中product_type为1，is_valid为1，state为1，条数）
未上架的有效产品数量（merchant_phone_product表中product_type为1，is_valid为1，state为2，条数）
更新的求购二手机数量（buy_request表中product_type为1，is_valid为1，state为1，update_time字段为当天的数量，条数）
4.上架产品沟通电话数量（merchant_call_record表call_type为0，create_time为当天，条数）
求购产品沟通电话数量（merchant_call_record表call_type为1，create_time为当天，条数）
上架产品沟通的商户总数（merchant_call_record表call_type为0，create_time为当天，caller_merchant_id去重后的数量）
求购产品沟通的商户数量（merchant_call_record表call_type为1，create_time为当天，caller_merchant_id去重后的数量）
5.商家签到数量（merchant_member_integral_spend表change_reason为“签到送积分”，change_time为当天，条数）
商家充值次数（merchant_member_integral_spend表change_reason为“花钱充值积分”，change_time为当天，条数）
商家充值总数（merchant_member_integral_spend表change_reason为“花钱充值积分”，change_time为当天，充值金额总数change_amount累加和）
上面说的当天，是定时任务的前一天，定时任务执行时间在晚上12点半




backend项目中帮我添加一个Controller接口，调用后直接执行上面的统计任务（不会再定时执行了，为了测试用），该接口不需要登录直接可以调用，但不许传入一个固定的uuid值进行校验（校验不通过也不会执行统计任务）
https://www.saizanjibao.com/api/statistics/trigger?token=821fe142f4f94f10b3de32f074c5d1c7



我想在admin项目的前端项目（admin）中的左边大菜单列表中添加一个标题“行情资讯”，放到字典管理的菜单的下面，其它设置菜单的上面
行情资讯界面中，新增、编辑资讯中的内容使用富文本方式编辑，最好有直接的手机展示效果，保留展示效果，编辑的时候什么样子，在手机里面展示的时候就是什么样子

编辑行情资讯中的内容编辑框中，能设置成新闻特定的默认格式或者样式吗？或者替换一个比较好用的新闻编辑组件

点击“行情资讯”，进入行情资讯界面，界面中展示行情资讯表中的内容

backend项目中，增加三个接口，
一个是获取所有的行情资讯标题（前三条，根据排序字段sort获取sort最小的三条数据），
第二个是获取行情资讯列表，包含主标题、摘要、排序、时间，包含分页功能
第三个接口是根据id获取具体的行情资讯内容，包含主标题、内容、时间
这三个接口不必走拦截器（不需要用户登录）既可以查看调用


我想在后端添加一个接口，该接口需要用户登录后才能调用，而且必须完成商户信息填写（登录状态下可以查看到商户的手机号不为空），调用该接口后，会在redis中缓存该商户的ID，该缓存的key为当天日期格式为（yyyy-MM-dd，例子：2026-03-04），value为商户ID的一个set去重集合，每次调用该接口后，都会更新数据库表daily_statistics表中的daily_active_users字段值，该字段值保存缓存set的值的数量，注意判断daily_statistics表中是否有当天日期（statistics_date）的数据，如果没有，则添加一条数据





我的上架界面，点击卡片后下方出现的四个按钮的文字大小能调大一些吗？

admin后台管理的首页中的数据统计，改为全部从daily_statistics表中获取数据
admin后台管理的首页不是都从daily_statistics表中获取数据吗？为什么还会报下面的错误
java.sql.SQLSyntaxErrorException: Unknown column 'ds1_0.daily_new_member_count' in 'field list'
        at com.mysql.cj.jdbc.exceptions.SQLError.createSQLException(SQLError.java:112) ~[mysql-connector-j-9.1.0.jar!/:9.1.0]
        at com.mysql.cj.jdbc.exceptions.SQLExceptionsMapping.translateException(SQLExceptionsMapping.java:114) ~[mysql-connector-j-9.1.0.jar!/:9.1.0]
        at com.mysql.cj.jdbc.ClientPreparedStatement.executeInternal(ClientPreparedStatement.java:988) ~[mysql-connector-j-9.1.0.jar!/:9.1.0]
        at com.mysql.cj.jdbc.ClientPreparedStatement.executeQuery(ClientPreparedStatement.java:1056) ~[mysql-connector-j-9.1.0.jar!/:9.1.0]
        at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeQuery(ProxyPreparedStatement.java:52) ~[HikariCP-5.1.0.jar!/:na]
        at com.zaxxer.hikari.pool.HikariProxyPreparedStatement.executeQuery(Hikar