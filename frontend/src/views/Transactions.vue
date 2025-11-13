<template>
  <div class="transactions-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>Transactions</span>
          <el-button type="primary" :icon="Plus" @click="openDialog()">
            Add Transaction
          </el-button>
        </div>
      </template>

      <div class="filters">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="6">
            <el-select
              v-model="filters.accountId"
              placeholder="All Accounts"
              clearable
              @change="fetchTransactions"
              style="width: 100%"
            >
              <el-option
                v-for="account in accountStore.accounts"
                :key="account.id"
                :label="account.name"
                :value="account.id"
              />
            </el-select>
          </el-col>

          <el-col :xs="24" :sm="12" :md="6">
            <el-select
              v-model="filters.categoryId"
              placeholder="All Categories"
              clearable
              @change="fetchTransactions"
              style="width: 100%"
            >
              <el-option
                v-for="category in categoryStore.categories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              />
            </el-select>
          </el-col>

          <el-col :xs="24" :sm="12" :md="6">
            <el-select
              v-model="filters.type"
              placeholder="All Types"
              clearable
              @change="fetchTransactions"
              style="width: 100%"
            >
              <el-option label="Income" value="INCOME" />
              <el-option label="Expense" value="EXPENSE" />
            </el-select>
          </el-col>

          <el-col :xs="24" :sm="12" :md="6">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="To"
              start-placeholder="Start date"
              end-placeholder="End date"
              @change="handleDateChange"
              style="width: 100%"
            />
          </el-col>
        </el-row>
      </div>

      <el-table
        :data="transactions"
        v-loading="loading"
        style="width: 100%"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="transactionDate" label="Date" width="120" sortable="custom">
          <template #default="{ row }">
            {{ formatDate(row.transactionDate) }}
          </template>
        </el-table-column>

        <el-table-column prop="description" label="Description" min-width="200" />

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

        <el-table-column label="Actions" width="120" fixed="right">
          <template #default="{ row }">
            <el-button :icon="Edit" size="small" text @click="openDialog(row)" />
            <el-button :icon="Delete" size="small" text type="danger" @click="handleDelete(row)" />
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchTransactions"
          @current-change="fetchTransactions"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? 'Edit Transaction' : 'Add Transaction'"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
      >
        <el-form-item label="Type" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio-button value="INCOME">Income</el-radio-button>
            <el-radio-button value="EXPENSE">Expense</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="Account" prop="accountId">
              <el-select v-model="form.accountId" placeholder="Select account" style="width: 100%">
                <el-option
                  v-for="account in accountStore.accounts"
                  :key="account.id"
                  :label="account.name"
                  :value="account.id"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="Category" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="Select category" style="width: 100%">
                <el-option
                  v-for="category in categoryStore.categories"
                  :key="category.id"
                  :label="category.name"
                  :value="category.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="Amount" prop="amount">
          <el-input-number
            v-model="form.amount"
            :min="0.01"
            :precision="2"
            :step="10"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="Date" prop="transactionDate">
          <el-date-picker
            v-model="form.transactionDate"
            type="date"
            placeholder="Select date"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="Description" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="Enter transaction description"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? 'Update' : 'Create' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { transactionAPI } from '@/services/transaction'
import { useAccountStore } from '@/stores/account'
import { useCategoryStore } from '@/stores/category'

const accountStore = useAccountStore()
const categoryStore = useCategoryStore()

const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const submitting = ref(false)
const editingId = ref(null)

const transactions = ref([])
const dateRange = ref([])

const filters = reactive({
  accountId: null,
  categoryId: null,
  type: null,
  startDate: null,
  endDate: null
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const form = reactive({
  type: 'EXPENSE',
  accountId: null,
  categoryId: null,
  amount: 0,
  transactionDate: new Date(),
  description: ''
})

const rules = {
  type: [{ required: true, message: 'Please select type', trigger: 'change' }],
  accountId: [{ required: true, message: 'Please select account', trigger: 'change' }],
  categoryId: [{ required: true, message: 'Please select category', trigger: 'change' }],
  amount: [{ required: true, message: 'Please enter amount', trigger: 'blur' }],
  transactionDate: [{ required: true, message: 'Please select date', trigger: 'change' }],
  description: [{ required: true, message: 'Please enter description', trigger: 'blur' }]
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

const handleDateChange = (dates) => {
  if (dates && dates.length === 2) {
    filters.startDate = dates[0].toISOString().split('T')[0]
    filters.endDate = dates[1].toISOString().split('T')[0]
  } else {
    filters.startDate = null
    filters.endDate = null
  }
  fetchTransactions()
}

const handleSortChange = ({ prop, order }) => {
  // Handle sorting if needed
  fetchTransactions()
}

const fetchTransactions = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      ...filters
    }
    // Remove null values
    Object.keys(params).forEach(key => {
      if (params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })

    const data = await transactionAPI.getAll(params)
    transactions.value = data.content || []
    pagination.total = data.totalElements || 0
  } catch (error) {
    console.error('Failed to fetch transactions:', error)
  } finally {
    loading.value = false
  }
}

const openDialog = (transaction = null) => {
  if (transaction) {
    isEdit.value = true
    editingId.value = transaction.id
    form.type = transaction.type
    form.accountId = transaction.accountId
    form.categoryId = transaction.categoryId
    form.amount = transaction.amount
    form.transactionDate = new Date(transaction.transactionDate)
    form.description = transaction.description
  } else {
    isEdit.value = false
    editingId.value = null
    form.type = 'EXPENSE'
    form.accountId = null
    form.categoryId = null
    form.amount = 0
    form.transactionDate = new Date()
    form.description = ''
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const submitData = {
          ...form,
          transactionDate: form.transactionDate.toISOString().split('T')[0]
        }

        if (isEdit.value) {
          await transactionAPI.update(editingId.value, submitData)
          ElMessage.success('Transaction updated successfully')
        } else {
          await transactionAPI.create(submitData)
          ElMessage.success('Transaction created successfully')
        }

        dialogVisible.value = false
        fetchTransactions()
      } catch (error) {
        ElMessage.error(error.response?.data?.message || 'Operation failed')
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleDelete = (transaction) => {
  ElMessageBox.confirm(
    'Are you sure you want to delete this transaction?',
    'Delete Transaction',
    {
      confirmButtonText: 'Delete',
      cancelButtonText: 'Cancel',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await transactionAPI.delete(transaction.id)
      ElMessage.success('Transaction deleted successfully')
      fetchTransactions()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || 'Failed to delete transaction')
    }
  }).catch(() => {})
}

onMounted(async () => {
  await Promise.all([
    accountStore.fetchAccounts(),
    categoryStore.fetchCategories(),
    fetchTransactions()
  ])
})
</script>

<style scoped>
.transactions-page {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filters {
  margin-bottom: 20px;
}

.income-amount {
  color: #67c23a;
  font-weight: 500;
}

.expense-amount {
  color: #f56c6c;
  font-weight: 500;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
