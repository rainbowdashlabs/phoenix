/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
interface Props {
  type?: 'button' | 'submit' | 'reset'
  variant?: 'primary' | 'secondary' | 'discord'
  disabled?: boolean
}

withDefaults(defineProps<Props>(), {
  type: 'button',
  variant: 'primary',
  disabled: false
})
</script>

<template>
  <button
    :type="type"
    :class="['base-button', `variant-${variant}`]"
    :disabled="disabled"
  >
    <slot />
  </button>
</template>

<style scoped>
.base-button {
  padding: 0.5rem 1rem;
  border-radius: 8px;
  border: none;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.2s;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: inherit;
  font-size: inherit;
}

.base-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.variant-primary {
  background-color: var(--color-primary);
  color: #181818;
}

.variant-primary:hover:not(:disabled) {
  background-color: var(--color-primary-accent);
}

.variant-secondary {
  background-color: var(--color-secondary);
  color: white;
}

.variant-secondary:hover:not(:disabled) {
  background-color: var(--color-secondary-accent);
}

.variant-discord {
  background-color: #5865f2;
  color: white;
}

.variant-discord:hover:not(:disabled) {
  background-color: #4752c4;
}
</style>
