/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import BaseButton from '@/components/BaseButton.vue'
</script>

<template>
  <div class="landing-page">
    <section class="hero">
      <div class="container">
        <h1>{{ $t('home.hero.title') }}</h1>
        <p>{{ $t('home.hero.subtitle') }}</p>
        <div class="actions">
          <BaseButton variant="primary">{{ $t('home.hero.getStarted') }}</BaseButton>
          <BaseButton variant="secondary">{{ $t('home.hero.learnMore') }}</BaseButton>
        </div>
      </div>
    </section>

    <section class="features">
      <div class="container">
        <div class="feature-grid">
          <div class="feature-card">
            <h3>{{ $t('home.features.fast.title') }}</h3>
            <p>{{ $t('home.features.fast.description') }}</p>
          </div>
          <div class="feature-card">
            <h3>{{ $t('home.features.reliable.title') }}</h3>
            <p>{{ $t('home.features.reliable.description') }}</p>
          </div>
          <div class="feature-card">
            <h3>{{ $t('home.features.secure.title') }}</h3>
            <p>{{ $t('home.features.secure.description') }}</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.landing-page {
  display: flex;
  flex-direction: column;
}

.hero {
  padding: 6rem 0;
  background-color: var(--color-background-soft);
  text-align: center;
}

.hero h1 {
  font-size: 3.5rem;
  font-weight: 800;
  margin-bottom: 1.5rem;
  color: var(--color-heading);
}

.hero p {
  font-size: 1.25rem;
  margin-bottom: 2.5rem;
  color: var(--color-text);
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

.actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
}


.features {
  padding: 6rem 0;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
}

.feature-card {
  padding: 2.5rem;
  border-radius: 12px;
  background-color: var(--color-background-soft);
  border: 1px solid var(--color-border);
  transition: transform 0.2s;
}

.feature-card:hover {
  transform: translateY(-5px);
}

.feature-card h3 {
  font-size: 1.5rem;
  font-weight: 700;
  margin-bottom: 1rem;
  color: var(--color-heading);
}

.feature-card p {
  color: var(--color-text);
  line-height: 1.6;
}
</style>
