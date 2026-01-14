// pages/query/index.js
Page({
  data: {
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
    queryType: 'secondhand', // 'secondhand' or 'new'
    
    // Selection state
    selectedSeries: '',
    selectedModel: '',
    selectedMemory: '',
    selectedColor: '',
    
    // UI state
    isSelecting: false,
    selectingType: '', // 'series', 'model', 'memory', 'color'
    selectingTypeLabel: '',
    selectionOptions: []
  },

  onLoad(options) {

  },

  onTapSearchInput() {
    wx.navigateTo({
      url: '/pages/results/index?focus=true',
    });
  },

  onSelectBrand(e) {
    const id = e.currentTarget.dataset.id;
    this.setData({
      selectedBrandId: id,
      // Reset selections when brand changes
      selectedSeries: '',
      selectedModel: '',
      selectedMemory: '',
      selectedColor: ''
    });
  },

  setQueryType(e) {
    const type = e.currentTarget.dataset.type;
    this.setData({
      queryType: type
    });
  },

  openSelector(e) {
    const type = e.currentTarget.dataset.type;
    let options = [];
    let label = '';

    switch(type) {
      case 'series':
        label = '系列';
        options = ['iPhone 15', 'iPhone 14', 'iPhone 13', 'iPhone 12', 'iPhone 11'];
        break;
      case 'model':
        label = '机型';
        options = ['iPhone 14 Pro Max', 'iPhone 14 Pro', 'iPhone 14 Plus', 'iPhone 14'];
        break;
      case 'memory':
        label = '内存';
        options = ['128G', '256G', '512G', '1T'];
        break;
      case 'color':
        label = '颜色';
        options = ['暗紫色', '金色', '银色', '深空黑'];
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

    if (type === 'series') update.selectedSeries = value;
    if (type === 'model') update.selectedModel = value;
    if (type === 'memory') update.selectedMemory = value;
    if (type === 'color') update.selectedColor = value;

    this.setData(update);
  },

  onSearch() {
    const { queryType, selectedSeries, selectedModel, selectedMemory, selectedColor } = this.data;
    const params = new URLSearchParams({
      type: queryType,
      series: selectedSeries,
      model: selectedModel,
      memory: selectedMemory,
      color: selectedColor
    }).toString();
    
    wx.navigateTo({
      url: `/pages/results/index?type=${queryType}&series=${selectedSeries}&model=${selectedModel}&config=${selectedMemory} ${selectedColor}`,
    });
  }
});
