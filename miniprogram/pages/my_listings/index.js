Page({
  data: {
    listings: [
      { 
        id: 1, 
        title: "iPhone 15 Pro Max", 
        brand: "苹果 · iPhone 15",
        config: "蓝色 256G 国行",
        stock: "10台",
        price: "¥8200",
        publishTime: "2026-01-05 10:30",
        views: 128,
        status: "active",
        statusText: "出售中",
        photos: [
          "https://via.placeholder.com/320x180/e0e0e0/999999?text=Image1",
          "https://via.placeholder.com/320x180/ededed/999999?text=Image2",
          "https://via.placeholder.com/320x180/d9d9d9/999999?text=Image3"
        ]
      },
      { 
        id: 2, 
        title: "Mate 60 Pro", 
        brand: "华为 · Mate 60",
        config: "雅川青 12+512",
        stock: "5台",
        price: "¥5500",
        publishTime: "2026-01-04 15:20",
        views: 85,
        status: "active",
        statusText: "出售中",
        photos: [
          "https://via.placeholder.com/320x180/e0e0e0/999999?text=Image1",
          "https://via.placeholder.com/320x180/ededed/999999?text=Image2"
        ]
      },
      { 
        id: 3, 
        title: "iPhone 13", 
        brand: "苹果 · iPhone 13",
        config: "白色 128G",
        stock: "0台",
        price: "¥3500",
        publishTime: "2025-12-28 09:15",
        views: 342,
        status: "sold",
        statusText: "已售出",
        photos: [
          "https://via.placeholder.com/320x180/e0e0e0/999999?text=Image1"
        ]
      }
    ]
  },
  onTapNew() {
    wx.navigateTo({
      url: '/pages/selector/index?mode=listing',
      fail: () => {
        wx.showToast({
          title: '发布页开发中',
          icon: 'none'
        });
      }
    });
  },
  onTapEdit(e) {
    const item = e.currentTarget.dataset.item;
    const parts = item.brand.split('·').map(s => s.trim());
    const brand = parts[0] || '';
    const series = parts[1] || '';
    const params = `mode=listing&action=edit&brand=${brand}&series=${series}&model=${item.title}&config=${item.config}&initStock=${item.stock.replace(/[^0-9]/g, '')}&initMinPrice=${item.price.replace(/[^0-9]/g, '')}`;
    wx.navigateTo({
      url: `/pages/publish/index?${params}`
    });
  },
  onTapToggleStatus(e) {
    const item = e.currentTarget.dataset.item;
    const newStatus = item.status === 'active' ? 'sold' : 'active';
    const newStatusText = newStatus === 'active' ? '出售中' : '已下架';
    
    const newList = this.data.listings.map(l => {
      if (l.id === item.id) {
        return { ...l, status: newStatus, statusText: newStatusText };
      }
      return l;
    });
    this.setData({ listings: newList });
    wx.showToast({
      title: newStatus === 'active' ? '已上架' : '已下架',
      icon: 'success'
    });
  },
  onTapDelete(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '提示',
      content: '确定删除该上架吗？',
      success: (res) => {
        if (res.confirm) {
          const newList = this.data.listings.filter(l => l.id !== id);
          this.setData({ listings: newList });
          wx.showToast({
            title: '删除成功',
            icon: 'success'
          });
        }
      }
    });
  }
});
