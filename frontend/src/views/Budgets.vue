<template>
  <div class="budgets-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>Monthly Budgets</span>
          <div class="header-actions">
            <el-date-picker
              v-model="selectedDate"
              type="month"
              placeholder="Select month"
              @change="fetchBudgets"
              style="margin-right: 10px"
            />
            <el-button type="primary" :icon="Plus" @click="openDialog()">
              Set Budget
            </el-button>
          </div>
        </div>
      </template>

      <div v-if="budgets.length > 0" class="budgets-grid">
        <el-card
          v-for="budget in budgets"
          :key="budget.id"
          class="budget-card"
        >
          <div class="budget-header">
            <div class="category-info">
              <span class="category-icon" :style="{ background: budget.categoryColor + '20', color: budget.categoryColor }">
                {{ budget.categoryIcon || '📁' }}
              </span>
              <span class="category-name">{{ budget.categoryName }}</span>
            </div>
            <el-dropdown trigger="click">
              <el-button :icon="MoreFilled" circle text />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="openDialog(budget)">
                    <el-icon><Edit /></el-icon>
                    Edit
                  </el-dropdown-item>
                  <el-dropdown-item @click="handleDelete(budget)">
                    <el-icon><Delete /></el-icon>
                    Delete
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <div class="budget-amount">
            <div class="budget-label">Budget</div>
            <div class="budget-value">${{ formatNumber(budget.amount) }}</div>
          </div>

          <div class="budget-spent">
            <div class="spent-info">
              <span class="spent-label">Spent</span>
              <span class="spent-value">${{ formatNumber(budget.spent) }}</span>
            </div>
            <div class="remaining-info">
              <span class="remaining-label">Remaining</span>
              <span class="remaining-value" :class="{ 'over-budget': budget.remaining < 0 }">
                ${{ formatNumber(Math.abs(budget.remaining)) }}
              </span>
            </div>
          </div>

          <el-progress
            :percentage="getPercentage(budget)"
            :color="getProgressColor(budget)"
            :status="budget.remaining < 0 ? 'exception' : undefined"
          />
        </el-card>
      </div>

      <el-empty v-else description="No budgets set for this month" />
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? 'Update Budget' : 'Set Budget'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
      >
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

        <el-form-item label="Budget Amount" prop="amount">
          <el-input-number
            v-model="form.amount"
            :min="0.01"
            :precision="2"
            :step="100"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="Month" prop="month">
          <el-date-picker
            v-model="form.date"
            type="month"
            placeholder="Select month"
            style="width: 100%"
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, MoreFilled } from '@element-plus/icons-vue'
import { budgetAPI } from '@/services/budget'
import { useCategoryStore } from '@/stores/category'

const categoryStore = useCategoryStore()
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const submitting = ref(false)
const editingId = ref(null)
const selectedDate = ref(new Date())
const budgets = ref([])

const form = reactive({
  categoryId: null,
  amount: 0,
  date: new Date()
})

const rules = {
  categoryId: [{ required: true, message: 'Please select a category', trigger: 'change' }],
  amount: [{ required: true, message: 'Please enter budget amount', trigger: 'blur' }],
  date: [{ required: true, message: 'Please select a month', trigger: 'change' }]
}

const formatNumber = (num) => {
  return Number(num || 0).toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

const getPercentage = (budget) => {
  const percentage = (budget.spent / budget.amount) * 100
  return Math.min(percentage, 100)
}

const getProgressColor = (budget) => {
  const percentage = getPercentage(budget)
  if (percentage >= 100) return '#f56c6c'
  if (percentage >= 80) return '#e6a23c'
  return '#67c23a'
}

const fetchBudgets = async () => {
  loading.value = true
  try {
    const date = new Date(selectedDate.value)
    const params = {
      month: date.getMonth() + 1,
      year: date.getFullYear()
    }
    budgets.value = await budgetAPI.getAll(params)
  } catch (error) {
    console.error('Failed to fetch budgets:', error)
  } finally {
    loading.value = false
  }
}

const openDialog = (budget = null) => {
  if (budget) {
    isEdit.value = true
    editingId.value = budget.id
    form.categoryId = budget.categoryId
    form.amount = budget.amount
    form.date = new Date(budget.year, budget.month - 1)
  } else {
    isEdit.value = false
    editingId.value = null
    form.categoryId = null
    form.amount = 0
    form.date = selectedDate.value
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const date = new Date(form.date)
        const data = {
          categoryId: form.categoryId,
          amount: form.amount,
          month: date.getMonth() + 1,
          year: date.getFullYear()
        }

        await budgetAPI.createOrUpdate(data)
        ElMessage.success(isEdit.value ? 'Budget updated successfully' : 'Budget created successfully')
        dialogVisible.value = false
        fetchBudgets()
      } catch (error) {
        ElMessage.error(error.response?.data?.message || 'Operation failed')
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleDelete = (budget) => {
  ElMessageBox.confirm(
    `Are you sure you want to delete the budget for "${budget.categoryName}"?`,
    'Delete Budget',
    {
      confirmButtonText: 'Delete',
      cancelButtonText: 'Cancel',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await budgetAPI.delete(budget.id)
      ElMessage.success('Budget deleted successfully')
      fetchBudgets()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || 'Failed to delete budget')
    }
  }).catch(() => {})
}

onMounted(async () => {
  await categoryStore.fetchCategories()
  fetchBudgets()
})
</script>

<style scoped>
.budgets-page {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

.budgets-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.budget-card {
  border-left: 4px solid #409eff;
}

.budget-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.category-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.category-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  font-size: 18px;
}

.category-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.budget-amount {
  margin-bottom: 15px;
}

.budget-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.budget-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.budget-spent {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.spent-label,
.remaining-label {
  font-size: 12px;
  color: #909399;
}

.spent-value {
  font-size: 14px;
  font-weight: 600;
  color: #f56c6c;
}

.remaining-value {
  font-size: 14px;
  font-weight: 600;
  color: #67c23a;
}

.remaining-value.over-budget {
  color: #f56c6c;
}
</style>
