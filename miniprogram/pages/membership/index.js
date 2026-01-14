Page({
  data: {
    membership: {
      merchantName: "济南慧慧通讯",
      type: "黄金会员",
      startTime: "2026-01-01 00:00",
      endTime: "2026-12-31 23:59",
    },
    remainDays: 0,
    rightsVisible: false,
    rightsList: []
  },

  onLoad() {
    this.calcRemainDays();
  },

  calcRemainDays() {
    const end = this.data.membership.endTime;
    // We replace '-' with '/' for compatibility, though standard Date parsing often works.
    const endDate = new Date(end.replace(/-/g, '/'));
    const now = new Date();
    const diff = Math.ceil((endDate - now) / (1000 * 60 * 60 * 24));
    this.setData({
      remainDays: diff > 0 ? diff : 0
    });
  },

  onRenew() {
    wx.navigateTo({
      url: '/pages/recharge/membership/index'
    });
  },

  onShowRights() {
    const map = {
      "黄金会员": [
        "优先展示商家信息与商品列表",
        "专属客服通道与问题优先响应",
        "更高的发布数量与更快审核速度",
        "会员专属活动与折扣权益",
        "数据统计报表基础版"
      ],
      "白银会员": [
        "提升展示优先级",
        "客服响应优先级提升",
        "发布数量适度提升",
        "参与部分会员活动"
      ],
      "钻石会员": [
        "全站顶级展示位与曝光",
        "一对一客户成功支持",
        "发布无限量与极速审核",
        "专属市场活动与推广资源",
        "高级数据报表与分析"
      ]
    };
    const list = map[this.data.membership.type] || ["会员基础展示优化", "客服优先级提升", "适度提升发布额度"];
    
    this.setData({
      rightsList: list,
      rightsVisible: true
    });
  },

  onCloseRights() {
    this.setData({
      rightsVisible: false
    });
  }
});
