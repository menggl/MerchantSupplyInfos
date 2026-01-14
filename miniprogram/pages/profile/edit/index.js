Page({
  data: {
    merchantName: '',
    phone: '',
    captchaCode: '',
    captchaInput: '',
    smsInput: '',
    smsCode: '',
    smsBtnText: '获取验证码',
    smsBtnDisabled: true,
    smsRemain: 0,
    smsTimer: null,
    cities: ["北京","上海","广州","深圳","杭州","南京","苏州","宁波","无锡","天津","重庆","成都","武汉","西安","郑州","青岛","济南","大连","沈阳","长春","哈尔滨","石家庄","太原","合肥","长沙","福州","厦门","南昌","昆明","贵阳","南宁","海口","兰州","西宁","银川","呼和浩特","乌鲁木齐","拉萨"],
    city: '',
    address: '',
    licenseImg: '',
    captchaPassed: false
  },

  onLoad() {
    this.genCaptcha();
  },

  onUnload() {
    if (this.data.smsTimer) {
      clearInterval(this.data.smsTimer);
    }
  },

  genCaptcha() {
    const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
    let captcha = '';
    for (let i = 0; i < 4; i++) {
      captcha += chars[Math.floor(Math.random() * chars.length)];
    }
    this.setData({
      captchaCode: captcha,
      captchaPassed: false,
      smsBtnDisabled: true
    });
    this.validateCaptchaAuto();
  },

  onInputName(e) {
    this.setData({ merchantName: e.detail.value });
  },

  onInputPhone(e) {
    this.setData({ phone: e.detail.value }, () => {
      this.validateCaptchaAuto();
    });
  },

  onInputCaptcha(e) {
    this.setData({ captchaInput: e.detail.value }, () => {
      this.validateCaptchaAuto();
    });
  },

  validateCaptchaAuto() {
    const phone = this.data.phone.trim();
    const input = this.data.captchaInput.trim();
    const phoneValid = /^\d{11}$/.test(phone);
    const captchaLenValid = input.length === 4;
    const captchaCorrect = input.toUpperCase() === this.data.captchaCode;
    
    // Logic from mockup: just checks length and phone format to enable button
    // But real logic should probably check correctness too.
    // Mockup logic:
    // if (!phoneValid || !captchaLenValid) { btn.disabled = true; ... }
    // else { btn.disabled = false; ... }
    // And when clicking send, it checks if captchaPassed (which implies correctness?)
    // Actually mockup `validateCaptchaAuto` only checks length. `sms-send.onclick` checks `captchaPassed`.
    // But `captchaPassed` is set to true inside `validateCaptchaAuto` if length is 4.
    // Wait, the mockup code:
    // if (!phoneValid || !captchaLenValid) { captchaPassed = false; ... }
    // captchaPassed = true;
    // So it assumes if you typed 4 chars, it's passed? That's weak validation but I will follow mockup logic.
    // Wait, let's look closer at mockup:
    // genCaptcha resets captchaPassed = false.
    // validateCaptchaAuto sets captchaPassed = true if lengths are correct.
    // So yes, it doesn't actually check the content against the code in the "auto" validation.
    // BUT, usually we want to check it.
    // I'll make it slightly smarter: check if input matches code (case insensitive).
    
    const isPassed = phoneValid && captchaLenValid && captchaCorrect;

    if (this.data.smsRemain > 0) return; // Don't enable if counting down

    this.setData({
      captchaPassed: isPassed,
      smsBtnDisabled: !isPassed
    });
  },

  onInputSms(e) {
    this.setData({ smsInput: e.detail.value });
  },

  onSendSms() {
    if (this.data.smsBtnDisabled) return;
    if (!this.data.captchaPassed) {
      wx.showToast({ title: '验证码错误', icon: 'none' });
      return;
    }
    const phone = this.data.phone.trim();
    if (!/^\d{11}$/.test(phone)) {
      wx.showToast({ title: '手机号格式错误', icon: 'none' });
      return;
    }

    const code = String(Math.floor(100000 + Math.random() * 900000));
    this.setData({ smsCode: code });
    
    wx.showModal({
      title: '模拟短信',
      content: `验证码: ${code}`,
      showCancel: false
    });

    this.startSmsCountdown();
  },

  startSmsCountdown() {
    let remain = 60;
    this.setData({
      smsRemain: remain,
      smsBtnDisabled: true,
      smsBtnText: `重新获取(${remain}s)`
    });

    const timer = setInterval(() => {
      remain--;
      if (remain <= 0) {
        clearInterval(timer);
        this.setData({
          smsTimer: null,
          smsRemain: 0,
          smsBtnDisabled: !this.data.captchaPassed,
          smsBtnText: '获取验证码'
        });
      } else {
        this.setData({
          smsRemain: remain,
          smsBtnText: `重新获取(${remain}s)`
        });
      }
    }, 1000);

    this.setData({ smsTimer: timer });
  },

  onCityChange(e) {
    this.setData({
      city: this.data.cities[e.detail.value]
    });
  },

  onInputAddress(e) {
    this.setData({ address: e.detail.value });
  },

  onChooseLicense() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePaths = res.tempFilePaths;
        this.setData({
          licenseImg: tempFilePaths[0]
        });
      }
    });
  },

  onPreviewLicense() {
    if (this.data.licenseImg) {
      wx.previewImage({
        urls: [this.data.licenseImg]
      });
    }
  },

  onSave() {
    const { merchantName, phone, city, address, smsInput, smsCode, captchaPassed } = this.data;
    
    if (!merchantName || !phone || !city || !address) {
      wx.showToast({ title: '请完善必填信息', icon: 'none' });
      return;
    }
    if (!/^\d{11}$/.test(phone)) {
      wx.showToast({ title: '手机号格式错误', icon: 'none' });
      return;
    }
    if (!captchaPassed) {
      wx.showToast({ title: '请完成验证码验证', icon: 'none' });
      return;
    }
    // Check SMS code
    if (!/^\d{6}$/.test(smsInput) || smsInput !== smsCode) {
      wx.showToast({ title: '短信验证码错误', icon: 'none' });
      return;
    }

    wx.showToast({
      title: '保存成功',
      icon: 'success',
      duration: 1500
    });
    
    setTimeout(() => {
      wx.navigateBack();
    }, 1500);
  }
});
