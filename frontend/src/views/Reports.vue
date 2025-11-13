<template>
  <div class="reports-page">
    <el-row :gutter="20">
      <el-col :xs="24" :lg="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>Category Spending</span>
              <div class="header-controls">
                <el-radio-group v-model="categoryType" @change="fetchCategoryReport" size="small">
                  <el-radio-button value="EXPENSE">Expenses</el-radio-button>
                  <el-radio-button value="INCOME">Income</el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>
          <div v-if="!loading" class="chart-container">
            <Doughnut :data="categoryChartData" :options="doughnutOptions" />
          </div>
          <el-skeleton v-else :rows="6" animated />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <el-card>
          <template #header>
            <span>Category Breakdown</span>
          </template>
          <div class="category-list">
            <div
              v-for="item in categoryBreakdown"
              :key="item.categoryName"
              class="category-item"
            >
              <div class="category-info">
                <div
                  class="category-color"
                  :style="{ background: item.categoryColor }"
                ></div>
                <span class="category-name">{{ item.categoryName }}</span>
              </div>
              <div class="category-stats">
                <span class="category-amount">${{ formatNumber(item.totalAmount) }}</span>
                <span class="category-percentage">{{ item.percentage.toFixed(1) }}%</span>
              </div>
            </div>
            <el-empty v-if="categoryBreakdown.length === 0" description="No data available" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>Income vs Expenses</span>
              <el-date-picker
                v-model="summaryDate"
                type="month"
                placeholder="Select month"
                @change="fetchSummary"
              />
            </div>
          </template>
          <el-row :gutter="40" class="summary-stats">
            <el-col :xs="24" :sm="8">
              <div class="summary-card income">
                <div class="summary-icon">
                  <el-icon :size="40"><TrendCharts /></el-icon>
                </div>
                <div class="summary-details">
                  <div class="summary-label">Total Income</div>
                  <div class="summary-value">${{ formatNumber(summary.totalIncome) }}</div>
                </div>
              </div>
            </el-col>

            <el-col :xs="24" :sm="8">
              <div class="summary-card expense">
                <div class="summary-icon">
                  <el-icon :size="40"><Coin /></el-icon>
                </div>
                <div class="summary-details">
                  <div class="summary-label">Total Expenses</div>
                  <div class="summary-value">${{ formatNumber(summary.totalExpense) }}</div>
                </div>
              </div>
            </el-col>

            <el-col :xs="24" :sm="8">
              <div class="summary-card balance">
                <div class="summary-icon">
                  <el-icon :size="40"><Wallet /></el-icon>
                </div>
                <div class="summary-details">
                  <div class="summary-label">Net Balance</div>
                  <div class="summary-value" :class="{ negative: summary.balance < 0 }">
                    ${{ formatNumber(summary.balance) }}
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Doughnut } from 'vue-chartjs'
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js'
import { TrendCharts, Coin, Wallet } from '@element-plus/icons-vue'
import { reportAPI } from '@/services/report'

ChartJS.register(ArcElement, Tooltip, Legend)

const loading = ref(false)
const categoryType = ref('EXPENSE')
const summaryDate = ref(new Date())
const categoryBreakdown = ref([])

const summary = reactive({
  totalIncome: 0,
  totalExpense: 0,
  balance: 0
})

const categoryChartData = ref({
  labels: [],
  datasets: []
})

const doughnutOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'bottom'
    }
  }
}

const formatNumber = (num) => {
  return Number(num || 0).toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

const generateColors = (count) => {
  const colors = [
    '#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399',
    '#c71585', '#00ced1', '#ff8c00', '#9370db', '#20b2aa'
  ]
  return Array.from({ length: count }, (_, i) => colors[i % colors.length])
}

const fetchCategoryReport = async () => {
  loading.value = true
  try {
    const endDate = new Date()
    const startDate = new Date()
    startDate.setMonth(startDate.getMonth() - 1)

    const params = {
      startDate: startDate.toISOString().split('T')[0],
      endDate: endDate.toISOString().split('T')[0],
      type: categoryType.value
    }

    const data = await reportAPI.getByCategory(params)
    categoryBreakdown.value = data

    const colors = generateColors(data.length)
    categoryChartData.value = {
      labels: data.map(item => item.categoryName),
      datasets: [
        {
          data: data.map(item => item.totalAmount),
          backgroundColor: colors,
          borderWidth: 2,
          borderColor: '#fff'
        }
      ]
    }
  } catch (error) {
    console.error('Failed to fetch category report:', error)
  } finally {
    loading.value = false
  }
}

const fetchSummary = async () => {
  try {
    const date = new Date(summaryDate.value)
    const params = {
      month: date.getMonth() + 1,
      year: date.getFullYear()
    }
    const data = await reportAPI.getSummary(params)
    summary.totalIncome = data.totalIncome
    summary.totalExpense = data.totalExpense
    summary.balance = data.balance
  } catch (error) {
    console.error('Failed to fetch summary:', error)
  }
}

onMounted(async () => {
  await Promise.all([
    fetchCategoryReport(),
    fetchSummary()
  ])
})
</script>

<style scoped>
.reports-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-controls {
  display: flex;
  gap: 10px;
  align-items: center;
}

.chart-container {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.category-list {
  max-height: 400px;
  overflow-y: auto;
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #ebeef5;
}

.category-item:last-child {
  border-bottom: none;
}

.category-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.category-color {
  width: 16px;
  height: 16px;
  border-radius: 4px;
}

.category-name {
  font-weight: 500;
  color: #303133;
}

.category-stats {
  display: flex;
  align-items: center;
  gap: 15px;
}

.category-amount {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.category-percentage {
  font-size: 14px;
  color: #909399;
  min-width: 50px;
  text-align: right;
}

.summary-stats {
  padding: 20px 0;
}

.summary-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  border-radius: 8px;
  background: #f5f7fa;
  margin-bottom: 20px;
}

.summary-card.income {
  border-left: 4px solid #67c23a;
}

.summary-card.expense {
  border-left: 4px solid #f56c6c;
}

.summary-card.balance {
  border-left: 4px solid #409eff;
}

.summary-icon {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}

.income .summary-icon {
  color: #67c23a;
  background: rgba(103, 194, 58, 0.1);
}

.expense .summary-icon {
  color: #f56c6c;
  background: rgba(245, 108, 108, 0.1);
}

.balance .summary-icon {
  color: #409eff;
  background: rgba(64, 158, 255, 0.1);
}

.summary-details {
  flex: 1;
}

.summary-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.summary-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.summary-value.negative {
  color: #f56c6c;
}
</style>
