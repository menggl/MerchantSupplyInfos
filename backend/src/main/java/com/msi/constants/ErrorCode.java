package com.msi.constants;

public class ErrorCode {
    // 权限类 (Permission) - A开头
    /** Token缺失或无效 */
    public static final String UNAUTHORIZED = "A0001";

    // 提醒类 (Reminder) - R开头
    /** 商户信息未完善（未绑定手机号） */
    public static final String PROFILE_INCOMPLETE = "R0001";
    /** 商户会员已过期 */
    public static final String MEMBER_EXPIRED = "R0002";
    /** 积分不足 */
    public static final String INTEGRAL_INSUFFICIENT = "R0003";

    // 验证类 (Validation) - V开头
    /** 图形验证码错误（无效或过期） */
    public static final String CAPTCHA_ERROR = "V0001";
    /** 短信发送次数超限 */
    public static final String SMS_LIMIT_EXCEEDED = "V0002";
}
