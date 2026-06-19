/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import { reactive, readonly } from 'vue'
import apiClient from '@/api/client'
import type { MemberPOJO } from '@/stores/auth'

interface ApplicationState {
  botInfo: MemberPOJO | null
  loading: boolean
}

const state = reactive<ApplicationState>({
  botInfo: null,
  loading: true
})

export function useApplication() {
  const fetchApplication = async () => {
    state.loading = true
    try {
      const response = await apiClient.get<MemberPOJO>('/v1/data/application')
      state.botInfo = response.data
    } catch (error: any) {
      if (error.response?.status === 503) {
        setTimeout(fetchApplication, 3000)
        return
      }
      console.error('Failed to fetch application info:', error)
      state.botInfo = null
    } finally {
      state.loading = false
    }
  }

  return {
    state: readonly(state),
    fetchApplication
  }
}
