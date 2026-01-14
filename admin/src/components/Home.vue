<template>
  <div style="height: 100%; display: flex; flex-direction: column; gap: 16px;">
    <el-row :gutter="16" style="margin-bottom: 4px;">
      <el-col :span="6">
        <el-card>
          <div style="font-size: 14px; color: #909399;">商家注册总数</div>
          <div style="font-size: 28px; font-weight: 600; margin-top: 8px;">{{ stats.merchant.total }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="font-size: 14px; color: #909399;">商家上架产品总数</div>
          <div style="font-size: 28px; font-weight: 600; margin-top: 8px;">{{ stats.product.total }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="font-size: 14px; color: #909399;">办理会员商家总数</div>
          <div style="font-size: 28px; font-weight: 600; margin-top: 8px;">{{ stats.member.total }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="font-size: 14px; color: #909399;">充值积分总金额</div>
          <div style="font-size: 28px; font-weight: 600; margin-top: 8px;">¥{{ formatMoney(stats.recharge.total) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="flex: 1; min-height: 0;">
      <el-col :span="12" style="height: 100%;">
        <el-card
          style="height: 100%;"
          :body-style="{ height: '100%', display: 'flex', flexDirection: 'column', padding: '20px' }"
        >
          <div style="margin-bottom: 8px; font-weight: 600;">商家注册趋势</div>
          <div ref="merchantChartRef" style="flex: 1; min-height: 260px; width: 100%;"></div>
        </el-card>
      </el-col>
      <el-col :span="12" style="height: 100%;">
        <el-card
          style="height: 100%;"
          :body-style="{ height: '100%', display: 'flex', flexDirection: 'column', padding: '20px' }"
        >
          <div style="margin-bottom: 8px; font-weight: 600;">商家上架产品趋势</div>
          <div ref="productChartRef" style="flex: 1; min-height: 260px; width: 100%;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="flex: 1; min-height: 0;">
      <el-col :span="12" style="height: 100%;">
        <el-card
          style="height: 100%;"
          :body-style="{ height: '100%', display: 'flex', flexDirection: 'column', padding: '20px' }"
        >
          <div style="margin-bottom: 8px; font-weight: 600;">办理会员趋势</div>
          <div ref="memberChartRef" style="flex: 1; min-height: 260px; width: 100%;"></div>
        </el-card>
      </el-col>
      <el-col :span="12" style="height: 100%;">
        <el-card
          style="height: 100%;"
          :body-style="{ height: '100%', display: 'flex', flexDirection: 'column', padding: '20px' }"
        >
          <div style="margin-bottom: 8px; font-weight: 600;">积分充值金额趋势</div>
          <div ref="rechargeChartRef" style="flex: 1; min-height: 260px; width: 100%;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount, reactive, ref, nextTick } from 'vue'
import axios from 'axios'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'

const merchantChartRef = ref(null)
const productChartRef = ref(null)
const memberChartRef = ref(null)
const rechargeChartRef = ref(null)

let merchantChart
let productChart
let memberChart
let rechargeChart

const stats = reactive({
  merchant: { total: 0, dates: [], daily: [], cumulative: [] },
  product: { total: 0, dates: [], daily: [], cumulative: [] },
  member: { total: 0, dates: [], daily: [], cumulative: [] },
  recharge: { total: 0, dates: [], daily: [], cumulative: [] }
})

const formatMoney = (val) => {
  if (val == null) return '0.00'
  const num = typeof val === 'number' ? val : parseFloat(val)
  if (Number.isNaN(num)) return '0.00'
  return num.toFixed(2)
}

const buildCountOption = (title, data) => {
  return {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['每日新增', '累计总数']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '8%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.dates
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [
      {
        name: '每日新增',
        type: 'line',
        data: data.daily
      },
      {
        name: '累计总数',
        type: 'line',
        data: data.cumulative
      }
    ]
  }
}

const buildAmountOption = (title, data) => {
  return {
    tooltip: {
      trigger: 'axis',
      valueFormatter: (value) => `¥${formatMoney(value)}`
    },
    legend: {
      data: ['每日金额', '累计金额']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '8%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.dates
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '每日金额',
        type: 'line',
        data: data.daily
      },
      {
        name: '累计金额',
        type: 'line',
        data: data.cumulative
      }
    ]
  }
}

const initCharts = () => {
  if (merchantChartRef.value && !merchantChart) {
    merchantChart = echarts.init(merchantChartRef.value)
  }
  if (productChartRef.value && !productChart) {
    productChart = echarts.init(productChartRef.value)
  }
  if (memberChartRef.value && !memberChart) {
    memberChart = echarts.init(memberChartRef.value)
  }
  if (rechargeChartRef.value && !rechargeChart) {
    rechargeChart = echarts.init(rechargeChartRef.value)
  }

  if (merchantChart) {
    merchantChart.setOption(buildCountOption('商家注册趋势', stats.merchant), true)
    merchantChart.resize()
  }
  if (productChart) {
    productChart.setOption(buildCountOption('商家上架产品趋势', stats.product), true)
    productChart.resize()
  }
  if (memberChart) {
    memberChart.setOption(buildCountOption('办理会员趋势', stats.member), true)
    memberChart.resize()
  }
  if (rechargeChart) {
    rechargeChart.setOption(buildAmountOption('积分充值金额趋势', stats.recharge), true)
    rechargeChart.resize()
  }
}

const fetchStats = async () => {
  try {
    const resp = await axios.get('/admin/dashboard/stats')
    const data = resp.data || {}

    const merchant = data.merchant || {}
    const product = data.product || {}
    const member = data.member || {}
    const recharge = data.recharge || {}

    stats.merchant.total = merchant.total || 0
    stats.merchant.dates = merchant.dates || []
    stats.merchant.daily = merchant.daily || []
    stats.merchant.cumulative = merchant.cumulative || []

    stats.product.total = product.total || 0
    stats.product.dates = product.dates || []
    stats.product.daily = product.daily || []
    stats.product.cumulative = product.cumulative || []

    stats.member.total = member.total || 0
    stats.member.dates = member.dates || []
    stats.member.daily = member.daily || []
    stats.member.cumulative = member.cumulative || []

    stats.recharge.total = recharge.total || 0
    stats.recharge.dates = recharge.dates || []
    stats.recharge.daily = recharge.daily || []
    stats.recharge.cumulative = recharge.cumulative || []

    await nextTick()
    requestAnimationFrame(() => {
      initCharts()
      requestAnimationFrame(() => {
        handleResize()
      })
    })
  } catch (e) {
    console.error(e)
    ElMessage.error('获取首页统计数据失败')
  }
}

const handleResize = () => {
  if (merchantChart) merchantChart.resize()
  if (productChart) productChart.resize()
  if (memberChart) memberChart.resize()
  if (rechargeChart) rechargeChart.resize()
}

onMounted(() => {
  fetchStats()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (merchantChart) merchantChart.dispose()
  if (productChart) productChart.dispose()
  if (memberChart) memberChart.dispose()
  if (rechargeChart) rechargeChart.dispose()
})
</script>
