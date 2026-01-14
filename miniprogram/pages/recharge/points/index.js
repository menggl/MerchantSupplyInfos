Page({
  data: {
    currentPoints: 1200,
    rate: 0.10,
    rechargeInput: '',
    needPay: '0.00',
    newTotal: 1200,
    btnDisabled: true
  },

  onInput(e) {
    const valStr = e.detail.value;
    const val = parseInt(valStr || '0', 10);
    const valid = Number.isFinite(val) && val > 0;
    
    this.setData({
      rechargeInput: valStr,
      needPay: valid ? (val * this.data.rate).toFixed(2) : '0.00',
      newTotal: valid ? this.data.currentPoints + val : this.data.currentPoints,
      btnDisabled: !valid
    });
  },

  onRecharge() {
    if (this.data.btnDisabled) return;
    
    const val = parseInt(this.data.rechargeInput, 10);
    const amount = (val * this.data.rate).toFixed(2);
    
    wx.showModal({
      title: '支付确认',
      content: `确认支付 ¥${amount} 充值 ${val} 积分？`,
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '充值中...' });
          setTimeout(() => {
            wx.hideLoading();
            const newPoints = this.data.currentPoints + val;
            this.setData({
              currentPoints: newPoints,
              rechargeInput: '',
              needPay: '0.00',
              newTotal: newPoints,
              btnDisabled: true
            });
            wx.showToast({
              title: '充值成功',
              icon: 'success'
            });
          }, 1000);
        }
      }
    });
  }
});
