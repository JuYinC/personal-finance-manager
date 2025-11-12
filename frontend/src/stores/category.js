import { defineStore } from 'pinia'
import { ref } from 'vue'
import { categoryAPI } from '@/services/category'

export const useCategoryStore = defineStore('category', () => {
  const categories = ref([])
  const loading = ref(false)

  async function fetchCategories() {
    loading.value = true
    try {
      categories.value = await categoryAPI.getAll()
    } catch (error) {
      console.error('Fetch categories error:', error)
    } finally {
      loading.value = false
    }
  }

  async function createCategory(data) {
    try {
      const newCategory = await categoryAPI.create(data)
      categories.value.push(newCategory)
      return newCategory
    } catch (error) {
      console.error('Create category error:', error)
      throw error
    }
  }

  async function updateCategory(id, data) {
    try {
      const updatedCategory = await categoryAPI.update(id, data)
      const index = categories.value.findIndex(c => c.id === id)
      if (index !== -1) {
        categories.value[index] = updatedCategory
      }
      return updatedCategory
    } catch (error) {
      console.error('Update category error:', error)
      throw error
    }
  }

  async function deleteCategory(id) {
    try {
      await categoryAPI.delete(id)
      categories.value = categories.value.filter(c => c.id !== id)
    } catch (error) {
      console.error('Delete category error:', error)
      throw error
    }
  }

  return {
    categories,
    loading,
    fetchCategories,
    createCategory,
    updateCategory,
    deleteCategory
  }
})
