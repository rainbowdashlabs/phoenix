/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
</script>
<template>
  <div class="link-row">
    <slot />
  </div>
</template>
<style scoped>
.link-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  font-size: 0.9rem;
}
.link-row :deep(.link-item) {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  white-space: nowrap;
}
.link-row :deep(.link-icon) {
  font-size: 0.75rem;
  color: var(--color-primary);
  flex-shrink: 0;
}
.link-row :deep(a) {
  color: var(--color-text);
  text-decoration: none;
  transition: color 0.2s;
}
.link-row :deep(a:hover) {
  color: var(--color-primary);
}
.link-row :deep(.spacer) {
  color: var(--color-border);
  user-select: none;
}
</style>
