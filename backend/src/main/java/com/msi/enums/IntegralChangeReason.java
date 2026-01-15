package com.msi.enums;

public enum IntegralChangeReason {
  RECHARGE("花钱充值积分"),
  SIGN_IN("签到送积分"),
  SPEND_BUY_REQUEST("花费积分求购");

  private final String description;

  IntegralChangeReason(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}

