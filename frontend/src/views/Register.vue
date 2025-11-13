<template>
  <div class="auth-container">
    <el-card class="auth-card">
      <template #header>
        <div class="auth-header">
          <h2>Personal Finance Manager</h2>
          <p>Create a new account</p>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleRegister"
      >
        <el-form-item label="Name" prop="name">
          <el-input
            v-model="form.name"
            placeholder="Enter your name"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="Email" prop="email">
          <el-input
            v-model="form.email"
            type="email"
            placeholder="Enter your email"
            :prefix-icon="Message"
          />
        </el-form-item>

        <el-form-item label="Password" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="Enter your password"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item label="Confirm Password" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="Confirm your password"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            native-type="submit"
            :loading="loading"
            style="width: 100%"
          >
            Register
          </el-button>
        </el-form-item>

        <div class="auth-footer">
          <span>Already have an account?</span>
          <router-link to="/login">Login here</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  name: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('Passwords do not match'))
  } else {
    callback()
  }
}

const rules = {
  name: [
    { required: true, message: 'Please enter your name', trigger: 'blur' }
  ],
  email: [
    { required: true, message: 'Please enter your email', trigger: 'blur' },
    { type: 'email', message: 'Please enter a valid email', trigger: 'blur' }
  ],
  password: [
    { required: true, message: 'Please enter your password', trigger: 'blur' },
    { min: 8, message: 'Password must be at least 8 characters', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: 'Please confirm your password', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const { confirmPassword, ...registerData } = form
        await authStore.register(registerData)
        ElMessage.success('Registration successful!')
        router.push('/')
      } catch (error) {
        ElMessage.error(error.response?.data?.message || 'Registration failed')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.auth-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.auth-card {
  width: 100%;
  max-width: 450px;
}

.auth-header {
  text-align: center;
}

.auth-header h2 {
  margin: 0 0 10px;
  color: #303133;
}

.auth-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.auth-footer {
  text-align: center;
  margin-top: 20px;
  color: #606266;
}

.auth-footer a {
  color: #409eff;
  text-decoration: none;
  margin-left: 5px;
}

.auth-footer a:hover {
  text-decoration: underline;
}
</style>
