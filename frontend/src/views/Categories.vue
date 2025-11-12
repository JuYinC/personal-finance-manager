<template>
  <div class="categories-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>Categories</span>
          <el-button type="primary" :icon="Plus" @click="openDialog()">
            Add Category
          </el-button>
        </div>
      </template>

      <el-table :data="categoryStore.categories" v-loading="categoryStore.loading" style="width: 100%">
        <el-table-column label="Icon" width="80">
          <template #default="{ row }">
            <div class="category-icon" :style="{ background: row.color + '20', color: row.color }">
              {{ row.icon || '📁' }}
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="name" label="Name" />

        <el-table-column label="Type" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.isSystem" type="info" size="small">System</el-tag>
            <el-tag v-else type="success" size="small">Custom</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Actions" width="120" align="center">
          <template #default="{ row }">
            <template v-if="!row.isSystem">
              <el-button :icon="Edit" size="small" text @click="openDialog(row)" />
              <el-button :icon="Delete" size="small" text type="danger" @click="handleDelete(row)" />
            </template>
            <span v-else style="color: #909399; font-size: 12px;">System</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? 'Edit Category' : 'Add Category'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
      >
        <el-form-item label="Name" prop="name">
          <el-input v-model="form.name" placeholder="e.g., Groceries" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="Icon (Emoji)" prop="icon">
              <el-input v-model="form.icon" placeholder="e.g., 🛒" maxlength="2" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="Color" prop="color">
              <el-color-picker v-model="form.color" />
            </el-form-item>
          </el-col>
        </el-row>
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
import { useCategoryStore } from '@/stores/category'

const categoryStore = useCategoryStore()
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const submitting = ref(false)
const editingId = ref(null)

const form = reactive({
  name: '',
  icon: '',
  color: '#409eff'
})

const rules = {
  name: [{ required: true, message: 'Please enter category name', trigger: 'blur' }],
  icon: [{ required: true, message: 'Please enter an icon', trigger: 'blur' }],
  color: [{ required: true, message: 'Please select a color', trigger: 'change' }]
}

const openDialog = (category = null) => {
  if (category) {
    isEdit.value = true
    editingId.value = category.id
    form.name = category.name
    form.icon = category.icon
    form.color = category.color
  } else {
    isEdit.value = false
    editingId.value = null
    form.name = ''
    form.icon = ''
    form.color = '#409eff'
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
          await categoryStore.updateCategory(editingId.value, form)
          ElMessage.success('Category updated successfully')
        } else {
          await categoryStore.createCategory(form)
          ElMessage.success('Category created successfully')
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

const handleDelete = (category) => {
  ElMessageBox.confirm(
    `Are you sure you want to delete "${category.name}"?`,
    'Delete Category',
    {
      confirmButtonText: 'Delete',
      cancelButtonText: 'Cancel',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await categoryStore.deleteCategory(category.id)
      ElMessage.success('Category deleted successfully')
    } catch (error) {
      ElMessage.error(error.response?.data?.message || 'Failed to delete category')
    }
  }).catch(() => {})
}

onMounted(() => {
  categoryStore.fetchCategories()
})
</script>

<style scoped>
.categories-page {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.category-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  font-size: 20px;
}
</style>
