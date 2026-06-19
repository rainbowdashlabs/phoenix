/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import { reactive, readonly } from 'vue'

type AnyGuildContext = { guild: { id: string } }

const STORAGE_KEY = 'selected_guild_id'

interface GuildState {
  selectedGuildId: string
}

const queryGuildId = new URLSearchParams(window.location.search).get('guild_id')

const state = reactive<GuildState>({
  selectedGuildId: queryGuildId ?? localStorage.getItem(STORAGE_KEY) ?? '0'
})

export function useGuild() {
  const setGuild = (id: string) => {
    state.selectedGuildId = id
    localStorage.setItem(STORAGE_KEY, id)
  }

  const initFromGuilds = (guilds: AnyGuildContext[]) => {
    if (guilds.length === 0) {
      state.selectedGuildId = '0'
      return
    }
    const preferred = queryGuildId ?? localStorage.getItem(STORAGE_KEY)
    if (preferred && preferred !== '0' && guilds.some(gc => gc.guild.id === preferred)) {
      setGuild(preferred)
    } else {
      setGuild(guilds[0]!.guild.id)
    }
  }

  return {
    state: readonly(state),
    setGuild,
    initFromGuilds
  }
}
