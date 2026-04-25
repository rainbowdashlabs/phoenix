/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<template>
  <ContentPage :title="$t('faq.title')">
    <p>{{ $t('faq.content') }}</p>
  </ContentPage>
</template>
<script setup lang="ts">
import ContentPage from '@/components/ContentPage.vue'
</script>
