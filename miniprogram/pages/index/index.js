// index.js
Page({
  data: {
    currentDate: ''
  },

  onLoad() {
    this.updateDate();
  },

  updateDate() {
    const now = new Date();
    const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' };
    this.setData({
      currentDate: now.toLocaleDateString('zh-CN', options)
    });
  },

  onTapBuy() {
    wx.switchTab({
      url: '/pages/query/index'
    });
  },

  onTapSell() {
    // Navigate to My Listings page
    // Ensure this page is defined in app.json
    wx.navigateTo({
      url: '/pages/my_listings/index',
      fail: (err) => {
        console.error('Navigation failed', err);
        wx.showToast({
          title: '功能开发中',
          icon: 'none'
        });
      }
    });
  }
});
