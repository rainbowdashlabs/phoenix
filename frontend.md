# Frontend Guidelines

## Stack
- Vue 3 with `<script setup>` and TypeScript
- Vue Router 5 for routing
- vue-i18n 11 for translations
- Axios via `@/api/client` for API calls
- FontAwesome (`@fortawesome/vue-fontawesome`) registered globally in `main.ts`

## FontAwesome
- Icons are registered in `main.ts` via `library.add(...)` from `@fortawesome/free-solid-svg-icons` or `@fortawesome/free-brands-svg-icons`
- Use `<font-awesome-icon icon="icon-name" />` in templates
- Always add new icons to the `library.add(...)` call in `main.ts` before using them

## Auth Store (`src/stores/auth.ts`)
- The session endpoint returns a `UserContext` (not just a `User`)
- `UserContext` contains: `userId`, `token`, `guilds` (map of `GuildContext`), `user` (MemberPOJO), `created`, `role`
- `MemberPOJO` fields: `name`, `id`, `color`, `avatarUrl`
- `GuildContext` contains: `roles`, `guild` (DiscordGuild with `id`, `name`, `icon`, `permissions`, `owner`)
- Access logged-in user via `state.userContext`, check login with `state.userContext !== null`
- Access guilds via `Object.values(state.userContext.guilds)`

## Theme / Dark Mode
- Theme is controlled via `data-theme` attribute on `<html>` (`'light'` | `'dark'` | removed for system)
- Persisted in `localStorage` under key `'theme'`
- CSS in `base.css`: `@media (prefers-color-scheme: dark)` uses `:root:not([data-theme='light'])`, and `:root[data-theme='dark']` for explicit dark
- Theme toggle logic lives in `Footer.vue`

## Routing
- Routes defined in `src/router/index.ts`
- Views live in `src/views/`
- FAQ: `/faq` → `FaqView.vue`
- TOS: `/tos` → `TosView.vue`
- Privacy: `/privacy` → `PrivacyView.vue`

## i18n
- Locale files in `src/i18n/locales/en-US.json`
- All user-visible strings must use `$t('key')` — no hardcoded strings in templates
- Add new keys to the locale file whenever adding new UI text

## Guild Store (`src/stores/guild.ts`)
- Selected guild ID persisted in `localStorage` under `'selected_guild_id'`
- Call `initFromGuilds(guilds)` after guilds are available (e.g. in a `watch` on the guilds computed); it sets the first guild if none is stored, or `'0'` if no guilds exist
- Use `setGuild(id)` to change the selected guild; it updates both reactive state and localStorage
- `GuildList.vue` is a dropdown switcher: shows the selected guild, opens a dropdown of all guilds on click, and calls `setGuild` on selection

## API
- Base client: `src/api/client.ts` (Axios, base URL `/`)
- In dev mode, backend runs on `http://localhost:8888`
- Auth token stored in `localStorage` under `'auth_token'` and sent as `Authorization` header
- Selected guild ID sent as `Guild-Id` header on every request, read from `localStorage` key `'selected_guild_id'` (defaults to `'0'`)

## Link Hover Styling
- Global `a:hover` in `main.css` only changes `color` to `var(--color-primary)` — no background highlight.
- Do not add `background-color` overrides in individual components for hover states; fix it globally.

## Component Conventions
- License header comment block at top of every `.vue` file
- Scoped `<style scoped>` in every component
- Use CSS variables from `base.css` for all colors (e.g. `var(--color-text)`, `var(--color-background)`)
- Guild icons: `https://cdn.discordapp.com/icons/{id}/{icon}.png`; fall back to FA `users` icon if `icon` is null
- **Text/layout styling must live in reusable components, not duplicated per-view.** E.g. `ContentPage.vue` provides the shared page layout (padding, max-width container, h1 styling) used by FaqView, TosView, PrivacyView — add new content pages by using `<ContentPage :title="...">` with a slot for body content.
- **Footer sub-components** (all in `src/components/footer/`): `ThemeToggle.vue` (theme button + logic), `FooterLinkList.vue` (vertical icon+link list), `FooterLinkRow.vue` (horizontal link row with spacers), `FooterInfo.vue` (centered muted info block), `GuildList.vue` (user guild list). `Footer.vue` itself only contains grid/layout styling. Single-use components that are exclusively owned by a parent component must be grouped in a subdirectory named after that parent.
- IDE may report false-positive semantic errors for `$t()` used as a prop value ("Type argument cannot be inferred from usage") — these are safe to ignore; `npm run build` is the source of truth.
