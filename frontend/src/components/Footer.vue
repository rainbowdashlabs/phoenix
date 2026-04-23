/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<template>
  <footer class="footer">
    <div class="container">
      <p>{{ $t('footer.copyright') }}</p>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  width: 100%;
  background-color: var(--color-background-soft);
  border-top: 1px solid var(--color-border);
  padding: 2rem 0;
  margin-top: auto;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
  padding: 0 2rem;
  color: var(--color-text);
  font-size: 0.9rem;
}
</style>
