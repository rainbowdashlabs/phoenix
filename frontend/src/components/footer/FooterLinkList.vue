/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
</script>
<template>
  <ul class="link-list">
    <slot />
  </ul>
</template>
<style scoped>
.link-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}
.link-list :deep(li) {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.9rem;
}
.link-list :deep(.link-icon) {
  font-size: 0.75rem;
  color: var(--color-primary);
  flex-shrink: 0;
}
.link-list :deep(a) {
  color: var(--color-text);
  text-decoration: none;
  transition: color 0.2s;
}
.link-list :deep(a:hover) {
  color: var(--color-primary);
}
</style>
