<template>
  <el-container class="layout-container">
    <el-aside :width="sidebarWidth" class="sidebar">
      <div class="logo">
        <el-icon :size="28"><Wallet /></el-icon>
        <span v-if="!isCollapsed">Finance Manager</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapsed"
        :router="true"
        class="sidebar-menu"
      >
        <el-menu-item index="/" :route="{ name: 'Dashboard' }">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>Dashboard</template>
        </el-menu-item>

        <el-menu-item index="/accounts" :route="{ name: 'Accounts' }">
          <el-icon><CreditCard /></el-icon>
          <template #title>Accounts</template>
        </el-menu-item>

        <el-menu-item index="/transactions" :route="{ name: 'Transactions' }">
          <el-icon><DocumentCopy /></el-icon>
          <template #title>Transactions</template>
        </el-menu-item>

        <el-menu-item index="/categories" :route="{ name: 'Categories' }">
          <el-icon><FolderOpened /></el-icon>
          <template #title>Categories</template>
        </el-menu-item>

        <el-menu-item index="/budgets" :route="{ name: 'Budgets' }">
          <el-icon><PieChart /></el-icon>
          <template #title>Budgets</template>
        </el-menu-item>

        <el-menu-item index="/reports" :route="{ name: 'Reports' }">
          <el-icon><TrendCharts /></el-icon>
          <template #title>Reports</template>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer">
        <el-button
          :icon="isCollapsed ? Expand : Fold"
          circle
          @click="toggleSidebar"
        />
      </div>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <h3>{{ pageTitle }}</h3>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click">
            <div class="user-info">
              <el-avatar :size="32" :icon="UserFilled" />
              <span class="username">{{ authStore.user?.name }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/profile')">
                  <el-icon><User /></el-icon>
                  Profile
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  Logout
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  Wallet,
  DataAnalysis,
  CreditCard,
  DocumentCopy,
  FolderOpened,
  PieChart,
  TrendCharts,
  UserFilled,
  User,
  SwitchButton,
  Expand,
  Fold
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isCollapsed = ref(false)

const sidebarWidth = computed(() => isCollapsed.value ? '64px' : '240px')

const activeMenu = computed(() => route.path)

const pageTitle = computed(() => {
  const titleMap = {
    '/': 'Dashboard',
    '/accounts': 'Accounts',
    '/transactions': 'Transactions',
    '/categories': 'Categories',
    '/budgets': 'Budgets',
    '/reports': 'Reports',
    '/profile': 'Profile'
  }
  return titleMap[route.path] || 'Finance Manager'
})

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

const handleLogout = () => {
  ElMessageBox.confirm('Are you sure you want to logout?', 'Confirm Logout', {
    confirmButtonText: 'Logout',
    cancelButtonText: 'Cancel',
    type: 'warning'
  }).then(() => {
    authStore.logout()
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background: #304156;
  transition: width 0.3s;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid #3a5169;
}

.sidebar-menu {
  flex: 1;
  border: none;
  background: #304156;
}

.sidebar-menu :deep(.el-menu-item) {
  color: #bfcbd9;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background: #263445 !important;
  color: #fff;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: #409eff !important;
  color: #fff;
}

.sidebar-footer {
  padding: 20px;
  display: flex;
  justify-content: center;
  border-top: 1px solid #3a5169;
}

.header {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left h3 {
  margin: 0;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 4px;
  transition: background 0.3s;
}

.user-info:hover {
  background: #f5f7fa;
}

.username {
  color: #303133;
  font-size: 14px;
}

.main-content {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
