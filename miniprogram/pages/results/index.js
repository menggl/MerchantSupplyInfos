Page({
  data: {
    type: 'secondhand',
    subtitle: '',
    results: []
  },
  onLoad(options) {
    const type = options.type || 'secondhand';
    const parts = [];
    if (type === 'secondhand') parts.push('二手手机');
    else if (type === 'new') parts.push('新机');

    if (options.series && options.series !== 'undefined') parts.push(options.series);
    if (options.model && options.model !== 'undefined') parts.push(options.model);
    if (options.config && options.config.trim() !== 'undefined') parts.push(options.config);

    this.setData({
      type: type,
      subtitle: parts.length > 0 ? parts.join(' · ') : 'iPhone 17 黑色 256G',
      results: [
        { id: 1, shop: "济南海龙", city: "济南市", doorplate: "JN-华强C-605", minPrice: 5650, date: "01/05 11:03", img: "", status: "有货", brand: "苹果 · iPhone 15", config: "蓝色 256G 国行", phone: "138****1234" },
        { id: 2, shop: "济南慧慧通讯", city: "济南市", doorplate: "JN-淳和数码广场 1楼K1", minPrice: 5630, date: "01/05 13:55", img: "", status: "有货", brand: "苹果 · iPhone 15", config: "黑色 256G 国行", phone: "139****5678" },
        { id: 3, shop: "济南鑫鑫通讯", city: "济南市", doorplate: "JN-银座数码港 2楼A12", minPrice: 5650, date: "01/05 09:49", img: "", status: "少量", brand: "苹果 · iPhone 15", config: "白色 256G 国行", phone: "137****9012" },
        { id: 4, shop: "济南新世纪通讯", city: "济南市", doorplate: "JN-新世纪数码城 3楼C23", minPrice: 5650, date: "01/05 11:03", img: "", status: "有货", brand: "苹果 · iPhone 15", config: "原色 256G 国行", phone: "136****3456" },
        { id: 5, shop: "济南兰兰电子", city: "济南市", doorplate: "JN-高新万达 1楼M08", minPrice: 5650, date: "01/05 12:15", img: "", status: "有货", brand: "苹果 · iPhone 15", config: "蓝色 512G 国行", phone: "135****7890" },
        { id: 6, shop: "新华电讯", city: "济南市", doorplate: "JN-泉城广场 商务区H07", minPrice: 5700, date: "01/05 12:45", img: "", status: "有货", brand: "苹果 · iPhone 15", config: "黑色 512G 国行", phone: "133****1122" }
      ]
    });
  },
  onTapCard(e) {
    const item = e.currentTarget.dataset.item;
    wx.navigateTo({
      url: `/pages/detail/index?id=${item.id}&minPrice=${item.minPrice}&time=${item.date}&qty=5`,
      fail: () => {
        wx.showToast({
          title: '详情页开发中',
          icon: 'none'
        });
      }
    });
  }
});
