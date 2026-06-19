/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
</script>
<template>
  <div class="footer-info">
    <slot />
  </div>
</template>
<style scoped>
.footer-info {
  margin-top: 0.75rem;
  font-size: 0.85rem;
  color: var(--color-text);
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  align-items: center;
  text-align: center;
}
.footer-info :deep(p) {
  margin: 0;
}
</style>
