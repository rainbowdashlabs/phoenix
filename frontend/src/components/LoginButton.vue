/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import { useAuth } from '@/stores/auth'
import BaseButton from './BaseButton.vue'

const { login } = useAuth()
</script>

<template>
  <BaseButton variant="discord" @click="login">
    {{ $t('nav.login') }}
  </BaseButton>
</template>

<style scoped>
</style>
