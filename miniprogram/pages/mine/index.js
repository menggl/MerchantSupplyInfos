// pages/mine/index.js
Page({
  data: {
    
  },

  onLoad() {

  },

  onTapCheckIn() {
    wx.showToast({
      title: '签到成功',
      icon: 'success'
    });
  },

  navTo(e) {
    const url = e.currentTarget.dataset.url;
    wx.navigateTo({
      url: url,
      fail: () => {
        wx.showToast({
          title: '功能开发中',
          icon: 'none'
        });
      }
    });
  }
});
