Page({
  data: {
    photos: [
      "https://via.placeholder.com/640x360/d9d9d9/999999?text=Image1",
      "https://via.placeholder.com/640x360/e0e0e0/999999?text=Image2",
      "https://via.placeholder.com/640x360/ededed/999999?text=Image3"
    ],
    qty: '5台',
    minPrice: '¥5199',
    time: '01/05 13:55'
  },
  onLoad(options) {
    // Set Navigation Title dynamically based on options or defaults
    // Mockup: Brand · Series · Model · Config
    // If we have these passed from previous page, we use them.
    // Otherwise use default.
    // Since we don't have full data flow, I'll use a placeholder or partial data.
    
    const title = '苹果 · iPhone 17 系列 · iPhone 17 · 黑色 256G';
    wx.setNavigationBarTitle({
      title: title
    });

    if (options.qty) {
      this.setData({ qty: `${options.qty}台` });
    }
    if (options.minPrice) {
      this.setData({ minPrice: `¥${options.minPrice}` });
    }
    if (options.time) {
      this.setData({ time: options.time });
    }
  },
  onCallPhone(e) {
    const phone = e.currentTarget.dataset.phone;
    wx.makePhoneCall({
      phoneNumber: phone,
      fail: () => {
        // Fallback or copy
        wx.setClipboardData({
          data: phone,
          success: () => {
            wx.showToast({
              title: '号码已复制',
              icon: 'success'
            });
          }
        });
      }
    });
  }
});
