/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import type { MemberPOJO } from '@/stores/auth'
import { useAuth } from '@/stores/auth'

defineProps<{
  user: MemberPOJO
}>()

const { logout } = useAuth()
const open = ref(false)
const container = ref<HTMLElement | null>(null)

function toggle() {
  open.value = !open.value
}

function handleOutsideClick(event: MouseEvent) {
  if (container.value && !container.value.contains(event.target as Node)) {
    open.value = false
  }
}

onMounted(() => document.addEventListener('click', handleOutsideClick))
onUnmounted(() => document.removeEventListener('click', handleOutsideClick))
</script>
<template>
  <div class="user-profile" ref="container">
    <button class="user-button" @click="toggle" :aria-expanded="open">
      <img :src="user.profilePictureUrl" :alt="user.displayName" class="avatar" />
      <span class="username">{{ user.displayName }}</span>
      <span class="chevron" :class="{ rotated: open }">▾</span>
    </button>
    <div v-if="open" class="dropdown">
      <button class="dropdown-item" @click="logout">
        {{ $t('nav.logout') }}
      </button>
    </div>
  </div>
</template>
<style scoped>
.user-profile {
  position: relative;
  display: inline-block;
}
.user-button {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.25rem 0.5rem;
  border-radius: 8px;
  color: var(--color-text);
  font-family: inherit;
  font-size: inherit;
  font-weight: bold;
  transition: background-color 0.2s;
}
.user-button:hover {
  background-color: var(--color-background-mute);
}
.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
}
.chevron {
  font-size: 0.75rem;
  transition: transform 0.2s;
}
.chevron.rotated {
  transform: rotate(180deg);
}
.dropdown {
  position: absolute;
  right: 0;
  top: calc(100% + 0.5rem);
  min-width: 10rem;
  background-color: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 100;
  overflow: hidden;
}
.dropdown-item {
  display: block;
  width: 100%;
  padding: 0.6rem 1rem;
  background: none;
  border: none;
  text-align: left;
  cursor: pointer;
  color: var(--color-text);
  font-family: inherit;
  font-size: inherit;
  transition: background-color 0.2s;
}
.dropdown-item:hover {
  background-color: var(--color-background-mute);
}
</style>
