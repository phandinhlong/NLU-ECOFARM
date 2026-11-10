# EcoFarm Frontend Conventions

Reference sheet for contributing to the frontend. Keep it short; prefer existing patterns over new ones.

## Tech Stack

- Vue 3 + TypeScript (`<script setup>`, Composition API)
- Vue Router
- Pinia (client state) + @tanstack/vue-query (server state)
- Axios
- Bootstrap 5 + bootstrap-vue-next
- Vite
- `vue-tsc` for type-checking

## Project Structure

```
src/
├── api/           # Backend integration
│   ├── clients/   # Axios instances + interceptors
│   ├── services/  # API call functions
│   ├── types/     # Request/response types
│   └── constants.ts
├── assets/        # Static assets
├── components/    # Reusable components
├── composables/   # Shared logic, vue-query hooks
├── layouts/       # PublicLayout, DefaultLayout
├── plugins/       # vue-query config, etc.
├── router/        # Route definitions
├── stores/        # Pinia stores (client state)
├── styles/        # Global CSS + Bootstrap overrides
└── views/         # Page components (route targets)
```

## State Management

| State | Tool | Examples |
|---|---|---|
| Client / UI | Pinia | current user, theme, sidebar, preferences |
| Server / API | Vue Query | fetched data, caching, loading, errors, mutations |

Never store API data in Pinia just to mirror it. Never cache UI state in Vue Query.

## API Layer

Flow: **constants → types → services → composables → components**

1. `api/constants.ts` — base paths (`API_V1 = '/api/v1'`).
2. `api/types/` — request/response interfaces (`ApiResponse<T>`, `LoginRequest`).
3. `api/services/` — thin functions using the shared axios client; build paths as `${API_V1}/<resource>`.
4. `composables/` — `useQuery` / `useMutation` wrappers; side effects (store updates, navigation) go in callbacks.
5. Components — call composables, render loading/error/data.

```ts
export const authService = {
  login: (data: LoginRequest) =>
    axios.post<ApiResponse<void>>(`${API_V1}/auth/login`, data),
}
```

Responses follow the backend contract: `{ isSuccess, message, data, timestamp }`. Base URL `/api/v1`, proxied by Vite in dev.

## Auth Flow

- **Cookies only.** Auth is `withCredentials: true`; no tokens in `localStorage` or app memory.
- **Enforcement lives in the axios interceptor**, not in routing. On `401` (except the login request) it redirects to `/login?redirect=<currentPath>`.
- **No router auth guards.** Do not add `beforeEach` checks or gate routes on a token. A `401` from the API is the single source of enforcement.
- Router only uses `afterEach` to set `document.title` from `meta.title`.
- `meta.requiresAuth` / `meta.guestOnly` are descriptive only.
- Login flow: `authService.login` → `getProfile` → store user in Pinia → redirect to `?redirect` or `/dashboard`.
- Logout: call service, clear user store, clear query cache, go to `/login`.

## Styling

- **Bootstrap utilities first**: `d-flex`, `p-4`, `vh-100`, `gap-3` over hand-written CSS.
- **bootstrap-vue-next components** (`BContainer`, `BCard`, `BButton`) over raw markup.
- **Design tokens**: CSS vars only, `--ef-*` (app) and `--bs-*` (Bootstrap). No hard-coded colors/spacing.
- **Custom CSS**: last resort, `<style scoped>`, when no utility/component fits.
- **Dark mode**: automatic via `prefers-color-scheme`; use tokens, never fixed palette values.

## Naming Conventions

| Kind | Convention | Example |
|---|---|---|
| Components / views (files) | PascalCase | `LoginView.vue`, `BrandHeader.vue` |
| Utilities / services (files) | kebab-case | `auth.service.ts` |
| Components | PascalCase | `LoginForm` |
| Functions / composables | camelCase, `use*` prefix | `useLoginMutation`, `setUser` |
| Types / interfaces | PascalCase | `LoginRequest`, `ApiResponse` |
| Constants | UPPER_SNAKE_CASE | `API_V1`, `MAX_PAGE_SIZE` |
| CSS variables | kebab-case | `--ef-primary`, `--bs-primary-rgb` |

## Code Style

- Always `<script setup lang="ts">`.
- Composition API only.
- Pinia: setup-style stores; keep them minimal.
- TypeScript strict; no `any` — use generics (`ApiResponse<T>`) or precise types.
- `import type` for type-only imports.
- Import alias `@/` → `src/`.
- Named exports for composables/utilities; no default exports.
- Destructure composables in `<script setup>`.
- Keep code self-documenting; minimal comments.
- Bootstrap-first: utilities and components before custom CSS.

## Router

- Two layouts: `PublicLayout` (guest pages, centered card) and `DefaultLayout` (authenticated, sidebar + content).
- Views are route targets in `src/views/`, lazy-loaded: `component: () => import('@/views/X.vue')`.
- Auth is not enforced here. See Auth Flow.

## Vue Query Defaults

Configured in `src/plugins/vue-query.ts`:
- `staleTime`: 5 min
- `gcTime`: 10 min
- `retry`: 1 (queries), 0 (mutations)
- `refetchOnWindowFocus`: false

## Scripts

```bash
npm run dev        # Dev server
npm run build      # Type-check + build
npm run type-check # vue-tsc
npm run preview    # Preview build
```

## Key Principles

1. Concise: no speculative abstractions.
2. Typed: TypeScript everywhere.
3. Layered: types → services → composables → components.
4. Separated: server state (Vue Query) vs client state (Pinia).
5. Bootstrap-first: utilities and components before custom CSS.
