/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useAuth } from '@/stores/auth'
import { useGuild } from '@/stores/guild'

const { state: authState } = useAuth()
const { state: guildState, setGuild, initFromGuilds } = useGuild()

const guilds = computed(() =>
  authState.userContext ? Object.values(authState.userContext.guilds) : []
)

const selectedGuild = computed(() =>
  guilds.value.find(gc => gc.guild.id === guildState.selectedGuildId) ?? null
)

const open = ref(false)

watch(guilds, (val) => {
  initFromGuilds(val)
}, { immediate: true })

function guildIconUrl(guildId: string, icon: string | null): string | null {
  if (!icon) return null
  return `https://cdn.discordapp.com/icons/${guildId}/${icon}.png`
}

function selectGuild(id: string) {
  setGuild(id)
  open.value = false
}

function toggleDropdown() {
  if (guilds.value.length > 0) {
    open.value = !open.value
  }
}
</script>
<template>
  <div class="guild-switcher" v-if="guilds.length > 0">
    <button class="guild-trigger" @click="toggleDropdown" :aria-expanded="open">
      <div class="guild-avatar-wrap">
        <img
          v-if="selectedGuild && guildIconUrl(selectedGuild.guild.id, selectedGuild.guild.icon)"
          :src="guildIconUrl(selectedGuild.guild.id, selectedGuild.guild.icon)!"
          :alt="selectedGuild?.guild.name"
          class="guild-avatar"
        />
        <div v-else class="guild-avatar guild-avatar--fallback">
          <font-awesome-icon icon="users" />
        </div>
      </div>
      <div class="guild-trigger-info">
        <span class="guild-trigger-label">{{ $t('footer.yourServers') }}</span>
        <span class="guild-trigger-name">{{ selectedGuild?.guild.name ?? '—' }}</span>
      </div>
      <font-awesome-icon icon="chevron-down" class="chevron" :class="{ rotated: open }" />
    </button>

    <Transition name="dropdown">
      <ul v-if="open" class="guild-dropdown">
        <li
          v-for="gc in guilds"
          :key="gc.guild.id"
          class="guild-option"
          :class="{ active: gc.guild.id === guildState.selectedGuildId }"
          @click="selectGuild(gc.guild.id)"
        >
          <div class="option-avatar-wrap">
            <img
              v-if="guildIconUrl(gc.guild.id, gc.guild.icon)"
              :src="guildIconUrl(gc.guild.id, gc.guild.icon)!"
              :alt="gc.guild.name"
              class="option-avatar"
            />
            <div v-else class="option-avatar option-avatar--fallback">
              <font-awesome-icon icon="users" />
            </div>
          </div>
          <span class="option-name">{{ gc.guild.name }}</span>
          <font-awesome-icon v-if="gc.guild.id === guildState.selectedGuildId" icon="check" class="option-check" />
        </li>
      </ul>
    </Transition>
  </div>
  <p v-else class="muted">{{ $t('footer.notLoggedIn') }}</p>
</template>
<style scoped>
.guild-switcher {
  position: relative;
  width: 100%;
}

/* ── Trigger button ── */
.guild-trigger {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  width: 100%;
  background: var(--color-background-mute);
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 0.6rem 0.75rem;
  cursor: pointer;
  color: var(--color-text);
  text-align: left;
  transition: border-color 0.15s, background 0.15s, box-shadow 0.15s;
}

.guild-trigger:hover {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--color-primary) 18%, transparent);
}

/* ── Avatar (trigger) ── */
.guild-avatar-wrap {
  flex-shrink: 0;
}

.guild-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: block;
  object-fit: cover;
}

.guild-avatar--fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  font-size: 1rem;
  color: var(--color-text);
}

/* ── Text block ── */
.guild-trigger-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 0.1rem;
}

.guild-trigger-label {
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  opacity: 0.55;
  line-height: 1;
}

.guild-trigger-name {
  font-size: 0.9rem;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--color-heading);
}

/* ── Chevron ── */
.chevron {
  flex-shrink: 0;
  opacity: 0.6;
  transition: transform 0.2s ease;
}

.chevron.rotated {
  transform: rotate(180deg);
}

/* ── Dropdown ── */
.guild-dropdown {
  position: absolute;
  bottom: calc(100% + 0.4rem);
  left: 0;
  right: 0;
  list-style: none;
  padding: 0.35rem;
  margin: 0;
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: 10px;
  max-height: 220px;
  overflow-y: auto;
  z-index: 100;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.18);
}

/* ── Dropdown item ── */
.guild-option {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  padding: 0.45rem 0.6rem;
  border-radius: 7px;
  cursor: pointer;
  transition: background 0.12s;
}

.guild-option:hover {
  background: var(--color-background-mute);
}

.guild-option.active {
  background: color-mix(in srgb, var(--color-primary) 12%, transparent);
}

/* ── Avatar (option) ── */
.option-avatar-wrap {
  flex-shrink: 0;
}

.option-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: block;
  object-fit: cover;
}

.option-avatar--fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-background-mute);
  border: 1px solid var(--color-border);
  font-size: 0.85rem;
  color: var(--color-text);
}

.option-name {
  flex: 1;
  font-size: 0.88rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.option-check {
  flex-shrink: 0;
  color: var(--color-primary);
  font-size: 0.8rem;
}

/* ── Transition ── */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(4px);
}

/* ── Not logged in ── */
.muted {
  font-size: 0.85rem;
  color: var(--color-text);
  opacity: 0.6;
}
</style>
