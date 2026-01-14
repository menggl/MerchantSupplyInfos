// pages/listings/index.js
Page({
  data: {
    requests: [
      {
        id: 1,
        title: 'iPhone 15 Pro Max',
        brand: '苹果 · iPhone 15',
        time: '2分钟前',
        config: '蓝色 256G 国行',
        quantity: '10台',
        priceRange: '¥8200 - ¥8400',
        deadline: '2026-01-08 18:00',
        phone: '138****1234',
        address: '深圳市福田区华强北路101号赛格广场'
      },
      {
        id: 2,
        title: 'Mate 60 Pro',
        brand: '华为 · Mate 60',
        time: '15分钟前',
        config: '雅川青 512G 全网通',
        quantity: '5台',
        priceRange: '¥6500 - ¥6800',
        deadline: '2026-01-08 20:00',
        phone: '139****5678',
        address: '深圳市福田区华强北路远望数码城'
      },
      {
        id: 3,
        title: '小米 14',
        brand: '小米 · 14',
        time: '30分钟前',
        config: '黑色 16+512G',
        quantity: '20台',
        priceRange: '¥3800 - ¥4000',
        deadline: '2026-01-09 12:00',
        phone: '136****9012',
        address: '深圳市南山区科技园'
      }
    ]
  },

  onTapPublish() {
    wx.navigateTo({
      url: '/pages/selector/index?mode=request',
      fail: () => {
        wx.showToast({
          title: '发布功能开发中',
          icon: 'none'
        });
      }
    });
  },

  onTapQuote(e) {
    const phone = e.currentTarget.dataset.phone;
    // Replace masked phone with real one for demo or just show modal
    const realPhone = phone.replace('****', '0000'); 
    wx.makePhoneCall({
      phoneNumber: realPhone,
      fail: (err) => {
        console.log('Call failed or cancelled', err);
      }
    });
  }
});
