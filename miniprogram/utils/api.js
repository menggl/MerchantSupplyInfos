const BASE_URL = 'http://127.0.0.1:8083/api'

function get(url, params) {
  return new Promise((resolve, reject) => {
    console.log('API Request:', url, params)
    wx.request({
      url: BASE_URL + url,
      method: 'GET',
      data: params || {},
      success: res => {
        console.log('API Response:', url, res)
        if (res.statusCode === 200) {
          resolve(res.data)
        } else {
          console.error('API Error:', url, res.statusCode, res.data)
          reject(res)
        }
      },
      fail: err => {
        console.error('API Fail:', url, err)
        reject(err)
      }
    })
  })
}

function listCities() {
  return get('/cities')
}

function searchSupplies(params) {
  return get('/supplies', params)
}

function listBrands() {
  return get('/brands/detail')
}

function listSeries(brandId) {
  return get('/series/by-brand', { brandId })
}

function listModels(seriesId) {
  return get('/models/by-series', { seriesId })
}

function listSpecs(modelId) {
  return get('/specs/by-model', { modelId })
}

module.exports = {
  listCities,
  searchSupplies,
  listBrands,
  listSeries,
  listModels,
  listSpecs
}
