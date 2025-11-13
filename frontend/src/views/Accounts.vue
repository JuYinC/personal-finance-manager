<template>
  <div class="accounts-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>My Accounts</span>
          <el-button type="primary" :icon="Plus" @click="openDialog()">
            Add Account
          </el-button>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col
          v-for="account in accountStore.accounts"
          :key="account.id"
          :xs="24"
          :sm="12"
          :lg="8"
        >
          <el-card class="account-card" :class="`type-${account.type.toLowerCase()}`">
            <div class="account-header">
              <div class="account-icon">
                <el-icon :size="24">
                  <CreditCard v-if="account.type === 'CREDIT_CARD'" />
                  <Wallet v-else-if="account.type === 'CASH'" />
                  <Coin v-else />
                </el-icon>
              </div>
              <el-dropdown trigger="click">
                <el-button :icon="MoreFilled" circle text />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="openDialog(account)">
                      <el-icon><Edit /></el-icon>
                      Edit
                    </el-dropdown-item>
                    <el-dropdown-item @click="handleDelete(account)">
                      <el-icon><Delete /></el-icon>
                      Delete
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>

            <div class="account-details">
              <div class="account-name">{{ account.name }}</div>
              <div class="account-type">{{ formatAccountType(account.type) }}</div>
              <div class="account-balance">${{ formatNumber(account.balance) }}</div>
              <div class="account-currency">{{ account.currency }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-if="accountStore.accounts.length === 0" description="No accounts yet. Create your first account!" />
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? 'Edit Account' : 'Add Account'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
      >
        <el-form-item label="Account Name" prop="name">
          <el-input v-model="form.name" placeholder="e.g., Main Bank Account" />
        </el-form-item>

        <el-form-item label="Account Type" prop="type">
          <el-select v-model="form.type" placeholder="Select account type" style="width: 100%">
            <el-option label="Bank Account" value="BANK" />
            <el-option label="Credit Card" value="CREDIT_CARD" />
            <el-option label="Cash" value="CASH" />
          </el-select>
        </el-form-item>

        <el-form-item label="Initial Balance" prop="balance">
          <el-input-number
            v-model="form.balance"
            :min="0"
            :precision="2"
            :step="100"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="Currency" prop="currency">
          <el-select v-model="form.currency" placeholder="Select currency" style="width: 100%">
            <el-option label="TWD - Taiwan Dollar" value="TWD" />
            <el-option label="USD - US Dollar" value="USD" />
            <el-option label="EUR - Euro" value="EUR" />
            <el-option label="GBP - British Pound" value="GBP" />
            <el-option label="JPY - Japanese Yen" value="JPY" />
            <el-option label="CNY - Chinese Yuan" value="CNY" />
          </el-select>
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
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, CreditCard, Wallet, Coin, MoreFilled, Edit, Delete } from '@element-plus/icons-vue'
import { useAccountStore } from '@/stores/account'

const accountStore = useAccountStore()
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const submitting = ref(false)
const editingId = ref(null)

const form = reactive({
  name: '',
  type: 'BANK',
  balance: 0,
  currency: 'TWD'
})

const rules = {
  name: [{ required: true, message: 'Please enter account name', trigger: 'blur' }],
  type: [{ required: true, message: 'Please select account type', trigger: 'change' }],
  balance: [{ required: true, message: 'Please enter initial balance', trigger: 'blur' }],
  currency: [{ required: true, message: 'Please select currency', trigger: 'change' }]
}

const formatNumber = (num) => {
  return Number(num || 0).toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

const formatAccountType = (type) => {
  const typeMap = {
    'BANK': 'Bank Account',
    'CREDIT_CARD': 'Credit Card',
    'CASH': 'Cash'
  }
  return typeMap[type] || type
}

const openDialog = (account = null) => {
  if (account) {
    isEdit.value = true
    editingId.value = account.id
    form.name = account.name
    form.type = account.type
    form.balance = account.balance
    form.currency = account.currency
  } else {
    isEdit.value = false
    editingId.value = null
    form.name = ''
    form.type = 'BANK'
    form.balance = 0
    form.currency = 'TWD'
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (isEdit.value) {
          await accountStore.updateAccount(editingId.value, form)
          ElMessage.success('Account updated successfully')
        } else {
          await accountStore.createAccount(form)
          ElMessage.success('Account created successfully')
        }
        dialogVisible.value = false
      } catch (error) {
        ElMessage.error(error.response?.data?.message || 'Operation failed')
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleDelete = (account) => {
  ElMessageBox.confirm(
    `Are you sure you want to delete "${account.name}"?`,
    'Delete Account',
    {
      confirmButtonText: 'Delete',
      cancelButtonText: 'Cancel',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await accountStore.deleteAccount(account.id)
      ElMessage.success('Account deleted successfully')
    } catch (error) {
      ElMessage.error(error.response?.data?.message || 'Failed to delete account')
    }
  }).catch(() => {})
}

accountStore.fetchAccounts()
</script>

<style scoped>
.accounts-page {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.account-card {
  margin-bottom: 20px;
  transition: transform 0.3s, box-shadow 0.3s;
}

.account-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.account-card.type-bank {
  border-left: 4px solid #409eff;
}

.account-card.type-credit_card {
  border-left: 4px solid #e6a23c;
}

.account-card.type-cash {
  border-left: 4px solid #67c23a;
}

.account-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.account-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: #f5f7fa;
}

.type-bank .account-icon {
  color: #409eff;
  background: rgba(64, 158, 255, 0.1);
}

.type-credit_card .account-icon {
  color: #e6a23c;
  background: rgba(230, 162, 60, 0.1);
}

.type-cash .account-icon {
  color: #67c23a;
  background: rgba(103, 194, 58, 0.1);
}

.account-details {
  padding: 10px 0;
}

.account-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 5px;
}

.account-type {
  font-size: 14px;
  color: #909399;
  margin-bottom: 15px;
}

.account-balance {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.account-currency {
  font-size: 12px;
  color: #909399;
}
</style>
