/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import apiClient from '@/api/client'
import ThemeToggle from '@/components/footer/ThemeToggle.vue'
import FooterLinkList from '@/components/footer/FooterLinkList.vue'
import FooterLinkRow from '@/components/footer/FooterLinkRow.vue'
import FooterInfo from '@/components/footer/FooterInfo.vue'
import GuildList from '@/components/footer/GuildList.vue'

interface Links {
  invite: string
  support: string
  kofi: string
}

const links = ref<Links | null>(null)

onMounted(async () => {
  try {
    const response = await apiClient.get<Links>('/v1/data/links')
    links.value = response.data
  } catch (e) {
    console.error('Failed to fetch links', e)
  }
})
</script>
<template>
  <footer class="footer">
    <div class="container">
      <!-- Left: Theme toggle + FAQ/ToS/Privacy -->
      <div class="column column-left">
        <ThemeToggle />
        <FooterLinkList style="margin-top: 0.75rem">
          <li>
            <font-awesome-icon icon="circle-question" class="link-icon" />
            <RouterLink to="/faq">{{ $t('footer.faq') }}</RouterLink>
          </li>
          <li>
            <font-awesome-icon icon="file-lines" class="link-icon" />
            <RouterLink to="/tos">{{ $t('footer.tos') }}</RouterLink>
          </li>
          <li>
            <font-awesome-icon icon="shield-halved" class="link-icon" />
            <RouterLink to="/privacy">{{ $t('footer.privacy') }}</RouterLink>
          </li>
        </FooterLinkList>
      </div>

      <!-- Middle: Links + info -->
      <div class="column column-middle">
        <FooterLinkRow>
          <template v-if="links?.invite">
            <span class="link-item"><font-awesome-icon icon="robot" class="link-icon" /><a :href="links.invite" target="_blank" rel="noopener">{{ $t('footer.invite') }}</a></span>
            <span class="spacer">|</span>
          </template>
          <template v-if="links?.support">
            <span class="link-item"><font-awesome-icon :icon="['fab', 'discord']" class="link-icon" /><a :href="links.support" target="_blank" rel="noopener">{{ $t('footer.support') }}</a></span>
            <span class="spacer">|</span>
          </template>
          <template v-if="links?.kofi">
            <span class="link-item"><font-awesome-icon icon="mug-hot" class="link-icon" /><a :href="links.kofi" target="_blank" rel="noopener">{{ $t('footer.kofi') }}</a></span>
            <span class="spacer">|</span>
          </template>
          <span class="link-item"><font-awesome-icon :icon="['fab', 'github']" class="link-icon" /><a href="https://github.com/RainbowDashLabs/phoenix" target="_blank" rel="noopener">{{ $t('footer.github') }}</a></span>
        </FooterLinkRow>
        <FooterInfo>
          <p>© 2026 RainbowDashLabs and Contributor</p>
          <p>Made with ❤️ and 🏳️‍🌈</p>
          <p>Licensed under AGPL-3.0</p>
        </FooterInfo>
      </div>

      <!-- Right: Guilds -->
      <div class="column column-right">
        <GuildList />
      </div>
    </div>
  </footer>
</template>
<style scoped>
.footer {
  width: 100%;
  background-color: var(--color-background-soft);
  border-top: 1px solid var(--color-border);
  padding: 2rem 0 1rem;
  margin-top: auto;
}
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 2rem;
}
@media (max-width: 768px) {
  .container {
    grid-template-columns: 1fr;
  }
}
</style>
