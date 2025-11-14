<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card income">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="32"><TrendCharts /></el-icon>
            </div>
            <div class="stat-details">
              <div class="stat-label">Total Income</div>
              <div class="stat-value">${{ formatNumber(summary.totalIncome) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card expense">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="32"><Coin /></el-icon>
            </div>
            <div class="stat-details">
              <div class="stat-label">Total Expenses</div>
              <div class="stat-value">${{ formatNumber(summary.totalExpense) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card balance">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="32"><Wallet /></el-icon>
            </div>
            <div class="stat-details">
              <div class="stat-label">Net Balance</div>
              <div class="stat-value">${{ formatNumber(summary.balance) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card accounts">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="32"><CreditCard /></el-icon>
            </div>
            <div class="stat-details">
              <div class="stat-label">Total Accounts</div>
              <div class="stat-value">{{ accountStore.accounts.length }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>Monthly Trends</span>
              <el-select v-model="trendMonths" @change="fetchTrends" style="width: 120px">
                <el-option label="3 Months" :value="3" />
                <el-option label="6 Months" :value="6" />
                <el-option label="12 Months" :value="12" />
              </el-select>
            </div>
          </template>
          <div v-if="!loading" class="chart-container">
            <Line :data="trendChartData" :options="chartOptions" />
          </div>
          <el-skeleton v-else :rows="6" animated />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card>
          <template #header>
            <span>Accounts Overview</span>
          </template>
          <div class="accounts-list">
            <div
              v-for="account in accountStore.accounts"
              :key="account.id"
              class="account-item"
            >
              <div class="account-info">
                <div class="account-name">{{ account.name }}</div>
                <div class="account-type">{{ account.type }}</div>
              </div>
              <div class="account-balance">${{ formatNumber(account.balance) }}</div>
            </div>
            <el-empty v-if="accountStore.accounts.length === 0" description="No accounts yet" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>Recent Transactions</span>
          </template>
          <el-table :data="recentTransactions" style="width: 100%">
            <el-table-column prop="transactionDate" label="Date" width="120">
              <template #default="{ row }">
                {{ formatDate(row.transactionDate) }}
              </template>
            </el-table-column>
            <el-table-column prop="description" label="Description" />
            <el-table-column prop="categoryName" label="Category" width="150" />
            <el-table-column prop="accountName" label="Account" width="150" />
            <el-table-column prop="type" label="Type" width="100">
              <template #default="{ row }">
                <el-tag :type="row.type === 'INCOME' ? 'success' : 'danger'" size="small">
                  {{ row.type }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="Amount" width="120" align="right">
              <template #default="{ row }">
                <span :class="row.type === 'INCOME' ? 'income-amount' : 'expense-amount'">
                  {{ row.type === 'INCOME' ? '+' : '-' }}${{ formatNumber(row.amount) }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Line } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
} from 'chart.js'
import { TrendCharts, Coin, Wallet, CreditCard } from '@element-plus/icons-vue'
import { reportAPI } from '@/services/report'
import { transactionAPI } from '@/services/transaction'
import { useAccountStore } from '@/stores/account'

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
)

const accountStore = useAccountStore()
const loading = ref(true)
const trendMonths = ref(6)

const summary = reactive({
  totalIncome: 0,
  totalExpense: 0,
  balance: 0
})

const trendChartData = ref({
  labels: [],
  datasets: []
})

const recentTransactions = ref([])

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'top'
    }
  }
}

const formatNumber = (num) => {
  return Number(num || 0).toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

const fetchSummary = async () => {
  try {
    const now = new Date()
    const data = await reportAPI.getSummary({
      month: now.getMonth() + 1,
      year: now.getFullYear()
    })
    summary.totalIncome = data.totalIncome
    summary.totalExpense = data.totalExpense
    summary.balance = data.balance
  } catch (error) {
    console.error('Failed to fetch summary:', error)
  }
}

const fetchTrends = async () => {
  try {
    const data = await reportAPI.getTrends({ months: trendMonths.value })
    trendChartData.value = {
      labels: data.map(d => `${d.month}/${d.year}`),
      datasets: [
        {
          label: 'Income',
          data: data.map(d => d.income),
          borderColor: '#67c23a',
          backgroundColor: 'rgba(103, 194, 58, 0.1)',
          tension: 0.3
        },
        {
          label: 'Expenses',
          data: data.map(d => d.expense),
          borderColor: '#f56c6c',
          backgroundColor: 'rgba(245, 108, 108, 0.1)',
          tension: 0.3
        }
      ]
    }
  } catch (error) {
    console.error('Failed to fetch trends:', error)
  }
}

const fetchRecentTransactions = async () => {
  try {
    const data = await transactionAPI.getAll({ page: 0, size: 5 })
    recentTransactions.value = data.content || []
  } catch (error) {
    console.error('Failed to fetch transactions:', error)
  }
}

onMounted(async () => {
  loading.value = true
  await Promise.all([
    accountStore.fetchAccounts(),
    fetchSummary(),
    fetchTrends(),
    fetchRecentTransactions()
  ])
  loading.value = false
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stats-row,
.chart-row {
  margin-bottom: 0;
}

.stat-card {
  cursor: default;
}

.stat-card.income {
  border-left: 4px solid #67c23a;
}

.stat-card.expense {
  border-left: 4px solid #f56c6c;
}

.stat-card.balance {
  border-left: 4px solid #409eff;
}

.stat-card.accounts {
  border-left: 4px solid #e6a23c;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: #f5f7fa;
}

.income .stat-icon {
  color: #67c23a;
  background: rgba(103, 194, 58, 0.1);
}

.expense .stat-icon {
  color: #f56c6c;
  background: rgba(245, 108, 108, 0.1);
}

.balance .stat-icon {
  color: #409eff;
  background: rgba(64, 158, 255, 0.1);
}

.accounts .stat-icon {
  color: #e6a23c;
  background: rgba(230, 162, 60, 0.1);
}

.stat-details {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 300px;
}

.accounts-list {
  max-height: 300px;
  overflow-y: auto;
}

.account-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #ebeef5;
}

.account-item:last-child {
  border-bottom: none;
}

.account-info {
  flex: 1;
}

.account-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.account-type {
  font-size: 12px;
  color: #909399;
}

.account-balance {
  font-size: 16px;
  font-weight: bold;
  color: #409eff;
}

.income-amount {
  color: #67c23a;
  font-weight: 500;
}

.expense-amount {
  color: #f56c6c;
  font-weight: 500;
}
</style>
