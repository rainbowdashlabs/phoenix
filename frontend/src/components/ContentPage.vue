/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<template>
  <div class="content-page">
    <div class="container">
      <h1>{{ title }}</h1>
      <slot />
    </div>
  </div>
</template>
<script setup lang="ts">
defineProps<{ title: string }>()
</script>
<style scoped>
.content-page {
  padding: 4rem 2rem;
}
.container {
  max-width: 800px;
  margin: 0 auto;
}
h1 {
  font-size: 2rem;
  margin-bottom: 1rem;
  color: var(--color-heading);
}
</style>
