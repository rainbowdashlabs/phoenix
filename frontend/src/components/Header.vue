/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import { RouterLink } from 'vue-router'
import { useAuth } from '@/stores/auth'
import { useApplication } from '@/stores/application'
import LoginButton from './LoginButton.vue'
import UserProfile from './UserProfile.vue'

const { state } = useAuth()
const { state: appState } = useApplication()
</script>

<template>
  <header class="sticky-header">
    <div class="container">
      <div class="logo">
        <RouterLink to="/">
          <img v-if="appState.botInfo" :src="appState.botInfo.profilePictureUrl" :alt="appState.botInfo.displayName" class="logo-avatar" />
          <span>{{ appState.botInfo ? appState.botInfo.displayName : 'Phoenix' }}</span>
        </RouterLink>
      </div>
      <nav>
        <RouterLink to="/">{{ $t('nav.home') }}</RouterLink>
        <RouterLink to="/about">{{ $t('nav.about') }}</RouterLink>
        <div v-if="!state.loading" class="auth-section">
          <LoginButton v-if="!state.userContext" />
          <UserProfile v-else :user="state.userContext.user" />
        </div>
      </nav>
    </div>
  </header>
</template>

<style scoped>
.sticky-header {
  position: sticky;
  top: 0;
  width: 100%;
  background-color: var(--color-background);
  border-bottom: 1px solid var(--color-border);
  z-index: 1000;
  padding: 1rem 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 2rem;
}

.logo a {
  font-size: 1.5rem;
  font-weight: bold;
  color: var(--color-heading);
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.logo-avatar {
  width: 2rem;
  height: 2rem;
  border-radius: 50%;
}

nav {
  display: flex;
  align-items: center;
}

nav a {
  margin-left: 1.5rem;
  color: var(--color-text);
  text-decoration: none;
}

nav a:hover {
  color: var(--color-primary);
}

nav a.router-link-exact-active {
  color: var(--color-primary);
  font-weight: bold;
}

.auth-section {
  margin-left: 1.5rem;
}
</style>
