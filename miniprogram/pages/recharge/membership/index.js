Page({
  data: {
    plans: [
      { id: 'day', name: '一天会员', desc: '有效期 1 天', price: 5 },
      { id: 'month', name: '月会员', desc: '有效期 30 天', price: 39 },
      { id: 'year', name: '年会员', desc: '有效期 365 天', price: 399 },
      { id: 'lifetime', name: '终身会员', desc: '永久有效', price: 1299 },
    ],
    selectedId: null,
    selectedPlan: null,
    payAmount: 0
  },

  onSelectPlan(e) {
    const id = e.currentTarget.dataset.id;
    const plan = this.data.plans.find(p => p.id === id);
    if (plan) {
      this.setData({
        selectedId: id,
        selectedPlan: plan,
        payAmount: plan.price
      });
    }
  },

  onPay() {
    if (!this.data.selectedId) {
      wx.showToast({
        title: '请先选择会员类型',
        icon: 'none'
      });
      return;
    }

    wx.showModal({
      title: '支付确认',
      content: `确认支付 ¥${this.data.payAmount} 开通 ${this.data.selectedPlan.name}？`,
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '支付中...' });
          setTimeout(() => {
            wx.hideLoading();
            wx.showToast({
              title: '支付成功',
              icon: 'success'
            });
            setTimeout(() => {
              wx.navigateBack();
            }, 1500);
          }, 1000);
        }
      }
    });
  }
});
