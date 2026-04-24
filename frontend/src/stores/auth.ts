/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import { reactive, readonly } from 'vue'
import apiClient from '@/api/client'

export interface User {
  name: string
  id: string
  color: string
  avatar: string
}

interface AuthState {
  user: User | null
  loading: boolean
}

const state = reactive<AuthState>({
  user: null,
  loading: true
})

export function useAuth() {
  const fetchUser = async () => {
    state.loading = true
    try {
      const response = await apiClient.get<User>('/v1/session/user')
      state.user = response.data
    } catch (error: any) {
      if (error.response?.status !== 401) {
        console.error('Failed to fetch user:', error)
      }
      state.user = null
    } finally {
      state.loading = false
    }
  }

  const login = () => {
    const baseURL = import.meta.env.DEV ? 'http://localhost:8888' : ''
    window.location.href = `${baseURL}/v1/auth/login`
  }

  return {
    state: readonly(state),
    fetchUser,
    login
  }
}
