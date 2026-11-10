# EcoFarm Frontend Conventions

## Tech Stack

- **Framework**: Vue 3 (Composition API, `<script setup>`, TypeScript)
- **Routing**: Vue Router 5.4
- **State**: Pinia (client state), @tanstack/vue-query (server state)
- **HTTP**: Axios 1.21
- **UI**: Bootstrap 5.3 + bootstrap-vue-next 1.2
- **Build**: Vite 8.2

## Project Structure

```
src/
├── api/                    # Backend integration
│   ├── clients/            # Axios instances + interceptors
│   ├── services/           # API call functions (use clients)
│   └── types/              # Request/response TypeScript types
├── assets/                 # Static assets (images, fonts)
├── components/             # Reusable Vue components
├── composables/            # Vue composables (vue-query hooks, shared logic)
├── layouts/                # Layout components (PublicLayout, DefaultLayout)
├── plugins/                # Plugin configs (vue-query, etc.)
├── router/                 # Vue Router setup
├── stores/                 # Pinia stores (client state only)
├── styles/                 # Global styles (index.css with Bootstrap overrides)
└── views/                  # Page components (route targets)
```

## State Management Split

- **Pinia**: client state (UI preferences, theme, sidebar state)
- **Vue Query**: server state (API data, caching, loading, errors)

Auth tokens stored in **cookies** (browser handles automatically).

## API Layer Pattern

### 1. Types (`src/api/types/`)

```ts
// types/common.ts
export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
  timestamp: string
}

// types/auth.ts
export interface LoginRequest {
  username: string
  password: string
}
```

### 2. Services (`src/api/services/`)

```ts
// services/auth.service.ts
import axios from '@/api/clients/axios'
import type { LoginRequest, LoginResponse } from '@/api/types/auth'

export const authService = {
  login: (data: LoginRequest) =>
    axios.post<ApiResponse<LoginResponse>>('/auth/login', data),
}
```

### 3. Composables (`src/composables/`)

```ts
// composables/useAuth.ts
import { useMutation } from '@tanstack/vue-query'
import { authService } from '@/api/services'
import { useUserStore } from '@/stores'

export const useLoginMutation = () => {
  const userStore = useUserStore()

  return useMutation({
    mutationFn: authService.login,
    onSuccess: (response) => {
      userStore.setUser(response.data.data.user)
    },
  })
}
```

### 4. Usage in Components

```vue
<script setup lang="ts">
import { useLoginMutation } from '@/composables/useAuth'

const { mutate: login, isPending, error } = useLoginMutation()

const handleSubmit = (data: LoginRequest) => {
  login(data)
}
</script>
```

## Axios Interceptors

**Request**: Sets `withCredentials: true` for cookies

**Response (success)**: Checks `response.data.success`, throws if false

**Response (error)**:
- `401` → redirect to `/login?redirect=<currentPath>`
- Extracts error message from `response.data.message`

## Router Guards

**Location**: `src/router/index.ts`

**Auth guard** (`beforeEach`):
- Reads `localStorage.accessToken` (fallback, cookies are primary)
- Protected routes (`meta.requiresAuth: true`) → redirect to login if no token
- Guest routes (`meta.guestOnly: true`) → redirect to dashboard if has token

**Title guard** (`afterEach`): Sets `document.title` from `meta.title`

## Styling Conventions

**CSS Variables**: Use `--ef-*` and `--bs-*` tokens from `styles/index.css`

**Bootstrap Utilities**: Prefer utilities over custom CSS (`d-flex`, `p-4`, `vh-100`)

**bootstrap-vue-next**: Use components (`BContainer`, `BCard`, `BButton`) over raw Bootstrap

**Custom CSS**: Only when Bootstrap can't provide it, use `<style scoped>`

**Dark Mode**: Auto via `@media (prefers-color-scheme: dark)` in `styles/index.css`

## Component Patterns

### Layouts

Two layouts:
- `PublicLayout` — guest pages (login, register), centered card
- `DefaultLayout` — authenticated pages, sidebar + main content

Wrap child routes with `<RouterView />`

### Views

Page components loaded by router, located in `src/views/`

Lazy-loaded: `component: () => import('@/views/LoginView.vue')`

### Composables

Reusable logic, vue-query hooks, shared functions

Export named functions/composables, not default exports

## Naming Conventions

**Files**: PascalCase for components/views (`LoginView.vue`), kebab-case for utilities (`auth.service.ts`)

**Components**: PascalCase (`BButton`, `LoginForm`)

**Functions**: camelCase (`useLoginMutation`, `setUser`)

**Types**: PascalCase interfaces (`LoginRequest`, `ApiResponse`)

**Constants**: UPPER_SNAKE_CASE (`MAX_PAGE_SIZE`)

**CSS vars**: kebab-case (`--ef-primary`, `--bs-primary-rgb`)

## TypeScript

**Strict mode**: enabled

**Import aliases**: `@/` maps to `src/`

**Type imports**: Use `import type` for type-only imports

**Any usage**: Avoid, use generics (`ApiResponse<T>`) or specific types

## Code Style

**Script setup**: Always use `<script setup lang="ts">`

**Composition API**: Preferred over Options API

**Reactivity**: `ref()` for primitives, `reactive()` for objects (or `ref()` everywhere)

**Comments**: Minimal, code should be self-documenting

**Destructuring**: Use in `<script setup>` for composables (`const { data, isLoading } = useQuery(...)`)

## Vue Query Defaults

Set in `src/plugins/vue-query.ts`:
- `staleTime`: 5 minutes
- `gcTime`: 10 minutes
- `retry`: 1 for queries, 0 for mutations
- `refetchOnWindowFocus`: false

## Backend API Contract

**Base URL**: `/api/v1` (proxied via Vite in dev)

**Response format**:
```json
{
  "success": true,
  "message": "Request successful",
  "data": {},
  "timestamp": "2026-11-10T02:36:12.821Z"
}
```

**Auth**: Cookies (HttpOnly, Secure)

**Endpoints**: RESTful (`/auth/login`, `/users`, `/farms`)

## Build & Dev

**Dev**: `npm run dev` (Vite dev server)

**Build**: `npm run build` (type-check + vite build)

**Type-check**: `npm run type-check` (vue-tsc)

**Preview**: `npm run preview` (preview production build)

## Key Principles

1. **Concise**: Minimal boilerplate, no speculative abstractions
2. **Typed**: TypeScript everywhere, no `any`
3. **Layered**: Types → Services → Composables → Components
4. **Separated**: Server state (vue-query) vs client state (Pinia)
5. **Bootstrap-first**: Use utilities + components before custom CSS
