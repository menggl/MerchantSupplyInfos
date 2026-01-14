Page({
  data: {
    requests: [
      { 
        id: 1, 
        title: "iPhone 15 Pro Max", 
        brand: "苹果 · iPhone 15",
        config: "蓝色 256G 国行",
        qty: "10台",
        priceRange: "¥8200 - ¥8400",
        deadline: "2026-01-08 18:00",
        phone: "138****1234",
        address: "深圳市福田区华强北路101号赛格广场",
        publishTime: "2分钟前",
        status: "active"
      },
      { 
        id: 2, 
        title: "Mate 60 Pro", 
        brand: "华为 · Mate 60",
        config: "雅川青 12+512",
        qty: "5台",
        priceRange: "¥5500 - ¥5800",
        deadline: "2026-01-07 20:00",
        phone: "139****5678",
        address: "济南市历下区山大路157号华强电子世界",
        publishTime: "15分钟前",
        status: "active"
      },
      { 
        id: 3, 
        title: "小米 14", 
        brand: "小米 · 小米 14",
        config: "黑色 16+512",
        qty: "3台",
        priceRange: "¥3800 - ¥4000",
        deadline: "2026-01-09 12:00",
        phone: "136****9999",
        address: "北京市中关村科贸电子城",
        publishTime: "45分钟前",
        status: "completed"
      }
    ]
  },

  onLoad(options) {
    // In a real app, fetch data here
  },

  onTapBack() {
    wx.navigateBack();
  },

  onTapAdd() {
    // Navigate to selector page
    wx.navigateTo({
      url: '/pages/selector/index?mode=request',
      fail: () => {
        wx.showToast({
          title: '选择器页面开发中',
          icon: 'none'
        });
      }
    });
  },

  onTapEdit(e) {
    const item = e.currentTarget.dataset.item;
    // Extract price range numbers
    const nums = item.priceRange.match(/\d+/g) || [];
    const initMin = nums[0] || '';
    const initMax = nums[1] || '';
    const initQty = (item.qty || '').replace(/\D/g, '') || '';
    
    // Construct params
    // Assuming item.brand format is "Brand · Series"
    const parts = item.brand.split('·').map(s => s.trim());
    const brand = parts[0] || '';
    const series = parts[1] || ''; // This might be "iPhone 15", which is series?
    // In mockup: brand="苹果 · iPhone 15", title="iPhone 15 Pro Max" (Model)
    
    const params = new URLSearchParams({
      mode: 'request',
      action: 'edit',
      brand: brand,
      series: series,
      model: item.title,
      config: item.config,
      initQty: initQty,
      initMinPrice: initMin,
      initMaxPrice: initMax,
      initDeadline: item.deadline
    }).toString();

    wx.navigateTo({
      url: `/pages/publish/index?${params}`
    });
  },

  onTapToggleStatus(e) {
    const item = e.currentTarget.dataset.item;
    const isStop = item.status === 'active';
    const actionText = isStop ? '停止求购' : '重新发布';
    
    wx.showModal({
      title: '提示',
      content: `确定${actionText}吗？`,
      success: (res) => {
        if (res.confirm) {
          const newStatus = isStop ? 'completed' : 'active';
          const newPublishTime = isStop ? item.publishTime : '刚刚';
          
          const newList = this.data.requests.map(req => {
            if (req.id === item.id) {
              return { ...req, status: newStatus, publishTime: newPublishTime };
            }
            return req;
          });
          
          this.setData({ requests: newList });
          wx.showToast({
            title: isStop ? '已停止' : '已发布',
            icon: 'success'
          });
        }
      }
    });
  },

  onTapDelete(e) {
    const item = e.currentTarget.dataset.item;
    wx.showModal({
      title: '提示',
      content: '确定删除该求购信息吗？',
      success: (res) => {
        if (res.confirm) {
          const newList = this.data.requests.filter(req => req.id !== item.id);
          this.setData({ requests: newList });
          wx.showToast({
            title: '已删除',
            icon: 'success'
          });
        }
      }
    });
  }
});
