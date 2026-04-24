/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import type { User } from '@/stores/auth'

defineProps<{
  user: User
}>()
</script>

<template>
  <div class="user-info">
    <img :src="user.avatar" :alt="user.name" class="avatar" />
    <span class="username">{{ user.name }}</span>
  </div>
</template>

<style scoped>
.user-info {
  display: flex;
  align-items: center;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  margin-right: 0.5rem;
}

.username {
  font-weight: bold;
  color: var(--color-text);
}
</style>
