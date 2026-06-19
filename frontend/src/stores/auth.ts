/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import { reactive, readonly } from 'vue'
import apiClient from '@/api/client'

export interface MemberPOJO {
  displayName: string
  id: string
  color: string
  profilePictureUrl: string
}

export interface DiscordGuild {
  id: string
  name: string
  icon: string | null
  permissions: string
  permissions_new: string
  owner: boolean
}

export interface GuildContext {
  roles: string[]
  guild: DiscordGuild
}

export interface UserContext {
  userId: number
  token: string
  guilds: Record<string, GuildContext>
  user: MemberPOJO
  created: string
  role: string[]
}

interface AuthState {
  userContext: UserContext | null
  loading: boolean
}

const state = reactive<AuthState>({
  userContext: null,
  loading: true
})

export function useAuth() {
  const fetchUser = async () => {
    state.loading = true
    try {
      const response = await apiClient.get<UserContext>('/v1/session/user')
      state.userContext = response.data
    } catch (error: any) {
      if (error.response?.status !== 401) {
        console.error('Failed to fetch user:', error)
      }
      state.userContext = null
    } finally {
      state.loading = false
    }
  }

  const login = () => {
    const baseURL = import.meta.env.DEV ? 'http://localhost:8888' : ''
    window.location.href = `${baseURL}/v1/auth/login`
  }

  const logout = async () => {
    try {
      await apiClient.post('/v1/auth/logout')
    } catch (error) {
      console.error('Failed to logout:', error)
    } finally {
      localStorage.removeItem('auth_token')
      state.userContext = null
    }
  }

  return {
    state: readonly(state),
    fetchUser,
    login,
    logout
  }
}
