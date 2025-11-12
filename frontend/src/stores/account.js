import { defineStore } from 'pinia'
import { ref } from 'vue'
import { accountAPI } from '@/services/account'

export const useAccountStore = defineStore('account', () => {
  const accounts = ref([])
  const loading = ref(false)

  async function fetchAccounts() {
    loading.value = true
    try {
      accounts.value = await accountAPI.getAll()
    } catch (error) {
      console.error('Fetch accounts error:', error)
    } finally {
      loading.value = false
    }
  }

  async function createAccount(data) {
    try {
      const newAccount = await accountAPI.create(data)
      accounts.value.push(newAccount)
      return newAccount
    } catch (error) {
      console.error('Create account error:', error)
      throw error
    }
  }

  async function updateAccount(id, data) {
    try {
      const updatedAccount = await accountAPI.update(id, data)
      const index = accounts.value.findIndex(a => a.id === id)
      if (index !== -1) {
        accounts.value[index] = updatedAccount
      }
      return updatedAccount
    } catch (error) {
      console.error('Update account error:', error)
      throw error
    }
  }

  async function deleteAccount(id) {
    try {
      await accountAPI.delete(id)
      accounts.value = accounts.value.filter(a => a.id !== id)
    } catch (error) {
      console.error('Delete account error:', error)
      throw error
    }
  }

  return {
    accounts,
    loading,
    fetchAccounts,
    createAccount,
    updateAccount,
    deleteAccount
  }
})
