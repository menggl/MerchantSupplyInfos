Page({
  data: {
    mode: 'request', // 'request' or 'listing'
    action: '', // 'edit' or empty
    displayModel: '未选择机型',
    photos: [],
    quantity: '',
    minPrice: '',
    maxPrice: '',
    sellPrice: '',
    deadlineDate: '',
    deadlineTime: ''
  },
  onLoad(options) {
    const { mode, action, brand, series, model, config, initStock, initMinPrice, initMaxPrice, initQty, initDeadline } = options;
    
    // Set display model
    const parts = [brand, series, model, config].filter(Boolean);
    const displayModel = parts.length > 0 ? parts.join(' · ') : '未选择机型';
    
    // Set Navigation Title
    let title = '填写求购信息';
    if (mode === 'listing') {
      title = action === 'edit' ? '编辑上架信息' : '填写上架信息';
    } else {
      title = action === 'edit' ? '编辑求购信息' : '填写求购信息';
    }
    wx.setNavigationBarTitle({ title });

    // Set initial date for deadline if request mode
    if (!initDeadline && mode !== 'listing') {
      const date = new Date();
      date.setDate(date.getDate() + 3);
      const y = date.getFullYear();
      const m = String(date.getMonth() + 1).padStart(2, '0');
      const d = String(date.getDate()).padStart(2, '0');
      this.setData({
        deadlineDate: `${y}-${m}-${d}`,
        deadlineTime: '12:00'
      });
    }

    this.setData({
      mode: mode || 'request',
      action: action || '',
      displayModel,
      quantity: initStock || initQty || '',
      minPrice: initMinPrice || '',
      maxPrice: initMaxPrice || '',
      sellPrice: initMinPrice || '' // For listing, initMinPrice is used as sell price in mockup logic
    });
  },
  
  onInputQuantity(e) { this.setData({ quantity: e.detail.value }); },
  onInputMinPrice(e) { this.setData({ minPrice: e.detail.value }); },
  onInputMaxPrice(e) { this.setData({ maxPrice: e.detail.value }); },
  onInputSellPrice(e) { this.setData({ sellPrice: e.detail.value }); },
  onDateChange(e) { this.setData({ deadlineDate: e.detail.value }); },
  onTimeChange(e) { this.setData({ deadlineTime: e.detail.value }); },

  onAddPhoto() {
    wx.chooseImage({
      count: 9 - this.data.photos.length,
      success: (res) => {
        this.setData({
          photos: [...this.data.photos, ...res.tempFilePaths]
        });
      }
    });
  },
  onRemovePhoto(e) {
    const index = e.currentTarget.dataset.index;
    const photos = this.data.photos;
    photos.splice(index, 1);
    this.setData({ photos });
  },

  onPublish() {
    const { mode, quantity, sellPrice, minPrice, maxPrice, photos } = this.data;
    
    if (mode === 'listing') {
      if (!quantity || !sellPrice) {
        wx.showToast({ title: '请填写完整信息', icon: 'none' });
        return;
      }
      // Mockup logic for listing publish
      wx.showToast({ title: '上架成功', icon: 'success' });
      setTimeout(() => {
        // Go back or to my listings
        wx.navigateBack();
      }, 1500);
    } else {
      if (!quantity || !minPrice || !maxPrice) {
        wx.showToast({ title: '请填写完整信息', icon: 'none' });
        return;
      }
       // Mockup logic for request publish
      wx.showToast({ title: '发布成功', icon: 'success' });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    }
  }
});
