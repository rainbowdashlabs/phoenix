/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

type Theme = 'light' | 'dark' | 'system'
const theme = ref<Theme>((localStorage.getItem('theme') as Theme) ?? 'system')

function applyTheme(t: Theme) {
  theme.value = t
  localStorage.setItem('theme', t)
  if (t === 'dark') {
    document.documentElement.setAttribute('data-theme', 'dark')
  } else if (t === 'light') {
    document.documentElement.setAttribute('data-theme', 'light')
  } else {
    document.documentElement.removeAttribute('data-theme')
  }
}

onMounted(() => {
  applyTheme(theme.value)
})

function toggleTheme() {
  if (theme.value === 'dark') {
    applyTheme('light')
  } else {
    applyTheme('dark')
  }
}

const isDark = computed(() => {
  if (theme.value === 'dark') return true
  if (theme.value === 'light') return false
  return window.matchMedia('(prefers-color-scheme: dark)').matches
})
</script>
<template>
  <button class="theme-toggle" @click="toggleTheme" :aria-label="$t('footer.toggleTheme')">
    <font-awesome-icon :icon="isDark ? 'sun' : 'moon'" class="theme-icon" />
    <span>{{ isDark ? $t('footer.lightMode') : $t('footer.darkMode') }}</span>
  </button>
</template>
<style scoped>
.theme-toggle {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: none;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 0.4rem 0.75rem;
  cursor: pointer;
  color: var(--color-text);
  font-family: inherit;
  font-size: 0.9rem;
  transition: background-color 0.2s;
}
.theme-toggle:hover {
  background-color: var(--color-background-mute);
}
.theme-icon {
  font-size: 1rem;
}
</style>
