Page({
  data: {
    mode: 'request', // 'request' or 'listing'
    brands: [
      { id: 1, name: '苹果' },
      { id: 2, name: '华为' },
      { id: 3, name: '小米' },
      { id: 4, name: 'OPPO' },
      { id: 5, name: 'vivo' },
      { id: 6, name: '荣耀' },
      { id: 7, name: '三星' },
      { id: 8, name: '一加' }
    ],
    selectedBrandId: 1,
    selectedBrandName: '苹果',
    
    // Selection state
    selectedSeries: '',
    selectedModel: '',
    selectedConfig: '', // Replaced memory/color with generic config for simplicity as per mockup
    
    // UI state
    isSelecting: false,
    selectingType: '', // 'series', 'model', 'config'
    selectingTypeLabel: '',
    selectionOptions: []
  },

  onLoad(options) {
    if (options.mode) {
      this.setData({ mode: options.mode });
    }
  },

  onSelectBrand(e) {
    const id = e.currentTarget.dataset.id;
    const item = this.data.brands.find(b => b.id === id);
    this.setData({
      selectedBrandId: id,
      selectedBrandName: item ? item.name : '',
      // Reset selections when brand changes
      selectedSeries: '',
      selectedModel: '',
      selectedConfig: ''
    });
  },

  openSelector(e) {
    const type = e.currentTarget.dataset.type;
    
    // Dependencies check
    if (type === 'model' && !this.data.selectedSeries) {
      wx.showToast({ title: '请先选择系列', icon: 'none' });
      return;
    }
    if (type === 'config' && !this.data.selectedModel) {
      wx.showToast({ title: '请先选择机型', icon: 'none' });
      return;
    }

    let options = [];
    let label = '';

    switch(type) {
      case 'series':
        label = '系列';
        options = ['iPhone 15 系列', 'iPhone 14 系列', 'iPhone 13 系列'];
        break;
      case 'model':
        label = '机型';
        // Mock logic
        if (this.data.selectedSeries.includes('15')) {
          options = ['iPhone 15', 'iPhone 15 Pro', 'iPhone 15 Pro Max', 'iPhone 15 Plus'];
        } else {
          options = ['iPhone 14', 'iPhone 14 Pro', 'iPhone 14 Pro Max'];
        }
        break;
      case 'config':
        label = '配置';
        options = ['黑色 128G', '黑色 256G', '蓝色 256G', '蓝色 512G', '原色 256G', '原色 512G'];
        break;
    }

    this.setData({
      isSelecting: true,
      selectingType: type,
      selectingTypeLabel: label,
      selectionOptions: options
    });
  },

  closeSelector() {
    this.setData({
      isSelecting: false,
      selectingType: '',
      selectingTypeLabel: '',
      selectionOptions: []
    });
  },

  onSelectOption(e) {
    const value = e.currentTarget.dataset.value;
    const type = this.data.selectingType;
    
    const update = {
      isSelecting: false,
      selectingType: '',
      selectingTypeLabel: '',
      selectionOptions: []
    };

    if (type === 'series') {
      update.selectedSeries = value;
      update.selectedModel = ''; // Reset child
      update.selectedConfig = '';
    }
    if (type === 'model') {
      update.selectedModel = value;
      update.selectedConfig = ''; // Reset child
    }
    if (type === 'config') update.selectedConfig = value;

    this.setData(update);
    
    // Auto open next selector
    if (type === 'series') {
      setTimeout(() => {
        this.openSelector({ currentTarget: { dataset: { type: 'model' } } });
      }, 200);
    } else if (type === 'model') {
       setTimeout(() => {
        this.openSelector({ currentTarget: { dataset: { type: 'config' } } });
      }, 200);
    }
  },

  onNext() {
    const { selectedBrandName, selectedSeries, selectedModel, selectedConfig, mode } = this.data;
    
    if (!selectedSeries || !selectedModel || !selectedConfig) {
      wx.showToast({ title: '请完整选择信息', icon: 'none' });
      return;
    }

    // Navigate to Publish page
    const params = new URLSearchParams({
      mode: mode,
      brand: selectedBrandName,
      series: selectedSeries,
      model: selectedModel,
      config: selectedConfig
    }).toString();

    wx.navigateTo({
      url: `/pages/publish/index?${params}`
    });
  }
});
