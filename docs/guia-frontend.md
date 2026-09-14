# Guía: Frontend — Estado y plan por fases

Guía de desarrollo del frontend Angular del Expense Tracker. Documenta qué se ha hecho, las decisiones de arquitectura y el plan de las siguientes fases. Es una guía viva: se actualiza al cerrar cada fase.

---

## Estado actual

| Fase | Contenido | Estado |
| ---- | --------- | ------ |
| F1   | Scaffolding, tema, routing, infraestructura base | ✅ Cerrada |
| F2   | Autenticación: core (service, interceptor, guards), login/register | ✅ Cerrada |
| F3   | Shell de la app + Dashboard | 📋 Planificada (sin empezar) |
| F4   | CRUD Transacciones | 📋 Planificada |
| F5   | CRUD Categorías | 📋 Planificada |
| F6   | CRUD Presupuestos | 📋 Planificada |

---

## Stack y decisiones (registro de decisiones)

- **Angular 21** standalone, componentes señal (Signals) para estado local, lazy loading por ruta.
- **Tailwind CSS v4 + daisyUI 5.7.37**, tema **corporate** activado con `data-theme` en `index.html` + lista de themes en `styles.css`.
- **ng2-charts 10 + chart.js 4.5.1 + @angular/cdk 21.2.14** para los gráficos del dashboard (opción A: "ng2-charts + CDK").
- **Vitest** como runner de tests (`ng test`), un solo spec base (`app.spec.ts`).
- **Access token**: solo en memoria (signal privada en `AuthService`), nunca en localStorage.
- **Refresh token**: cookie HttpOnly `refresh_token`; se envía automáticamente (same-origin vía proxy). Nunca usar `withCredentials`.
- **URL base**: `environment.apiUrl = '/api'` → proxy `/api` → `http://localhost:8080` (gateway).
- **Formularios**: Reactive Forms clásicos (no signals-based forms).
- **Moneda configurable**: `environment.locale='es-ES'`, `environment.currency='EUR'` → `Intl.NumberFormat` para formatear importes.
- Aliases tsconfig: `@core/*` → `src/app/core/*`, `@features/*` → `src/app/layouts/*`, `@shared/*` → `src/app/shared/*`, `@env/*` → `src/environments/*`.
  - ⚠️ `@shared/*` apunta a `src/app/shared/` que **aún no existe**; se crea en F3.

---

## Estructura de archivos (actual)

```text
frontend/
├── proxy.conf.json                     # /api → localhost:8080
├── src/
│   ├── index.html                      # data-theme="corporate"
│   ├── styles.css                      # @import tailwindcss + daisyUI
│   ├── app/
│   │   ├── app.config.ts               # routing, http+interceptor, charts, appInitializer
│   │   ├── app.routes.ts               # redirect ''→dashboard, auth+main routes, wildcard
│   │   ├── core/
│   │   │   ├── models/                 # contratos API (verificados contra DTOs Java)
│   │   │   │   ├── auth.models.ts      # LoginRequest, RegisterRequest, TokenResponse
│   │   │   │   ├── common.models.ts    # ErrorResponse, Page<T>, TransactionType, BudgetStatus
│   │   │   │   ├── transaction.models.ts
│   │   │   │   ├── category.models.ts
│   │   │   │   ├── budget.models.ts
│   │   │   │   └── summary.models.ts   # MonthlySummaryResponse, CategorySummary
│   │   │   ├── services/
│   │   │   │   └── auth.service.ts     # signals, login/register/logout/refresh/initialize
│   │   │   ├── interceptors/
│   │   │   │   └── auth-interceptor.ts # Bearer + 401→refresh (single-flight) + retry 1×
│   │   │   ├── guards/
│   │   │   │   ├── auth.guard.ts       # asíncrono: initialize→dashboard, UrlTree /login
│   │   │   │   └── guest.guard.ts      # asíncrono: invertido
│   │   │   └── utils/
│   │   │       └── jwt.ts              # decodeJwt, isTokenExpired
│   │   └── layouts/
│   │       ├── auth/
│   │       │   ├── auth.routes.ts      # /login, /register (guestGuard)
│   │       │   ├── login/              # Reactive Form + daisyUI ✅
│   │       │   └── register/           # Reactive Form + daisyUI ✅
│   │       └── main/
│   │           ├── main.routes.ts      # authGuard + 4 rutas hijas
│   │           ├── dashboard/          # placeholder + botón "Salir" TEMPORAL (F2)
│   │           ├── transactions/       # placeholder "<p>transactions works!</p>"
│   │           ├── categories/         # placeholder
│   │           └── budgets/            # placeholder
│   └── environments/
│       ├── environment.ts              # apiUrl '/api', locale 'es-ES', currency 'EUR'
│       └── environment.development.ts
```

---

## Contratos de API consumidos (verificados contra el backend)

### Auth (`identity-service`, públicas)
- `POST /api/auth/register` → `TokenResponse` (201, 409 duplicado)
- `POST /api/auth/login` → `TokenResponse` (401 credenciales inválidas)
- `POST /api/auth/refresh` (cookie) → `TokenResponse` (debe ser **single-flight**)
- `POST /api/auth/logout` (cookie) → 204

### Expense (`expense-service`, protegidas — el interceptor inyecta `Bearer`)
- `GET  /api/summary/monthly?year=YYYY&month=M` → `MonthlySummaryResponse`
  - ⚠️ El README del repo la documenta como `GET /api/summary?month=2026-09`; **la ruta real es `/monthly` con `year` y `month` numéricos**.
- `GET  /api/transactions?type=&categoryId=&fromDate=&toDate=&page=&size=&sort=` → `Page<TransactionResponse>`
  - ⚠️ `TransactionResponse` **no incluye nombre de categoría** → el frontend debe unir con `categories` para mostrar nombres.
- `POST|PUT  /api/transactions[/{id}]` con `TransactionRequest` (type, description?, amount>0, categoryId, date)
- `DELETE /api/transactions/{id}` → 204
- `GET  /api/categories?type=` → `List<CategoryResponse>`
- `POST|PUT /api/categories[/{id}]` con `CategoryRequest` (name, type)
- `DELETE /api/categories/{id}` → 204 (409 si está en uso)
- `GET  /api/budgets?categoryId=&year=&month=&page=&size=&sort=` → `Page<BudgetResponse>`
- `POST|PUT /api/budgets[/{id}]` con `BudgetRequest` (categoryId, year[2000-2100], month[1-12], amount>0) — 409 viola unicidad categoría+mes
- `DELETE /api/budgets/{id}` → 204

### Formato de error
- `ErrorResponse` estándar: `{ timestamp, status, error, message, path }`.
- ⚠️ Excepción documentada: el gateway responde 401 con `{"error":"Unauthorized","message":"Authentication required"}` (sin `path`). Los componentes deben leer `message` defensivamente (ya es el patrón de login/register).

---

## Comportamiento de sesión documentado

- Access token JWT stateless 15 min (`sub`, `email`, `roles`, `jti`, tipo `access`); el gateway valida firma + tipo.
- Refresh JWT 7 días **+ hash SHA-256 en BD con rotación**: cada refresh revoca el anterior y emite uno nuevo ⇒ pivote **single-flight** (2 refreshes concurrentes ⇒ el 2º revienta con 401).
- Tras `refresh()` fallido → `clearSession()` y el error original se re-lanza.
- Multi-pestaña: la 2ª pestaña que refresque simultáneamente recibe 401 → cierra sesión en esa pestaña. **Comportamiento aceptado** (no usar `localStorage`/`BroadcastChannel` por ahora; documentar en README si se observa como defecto).
- `AuthService.initialize()` se ejecuta al arrancar (`provideAppInitializer`) y desde los guards; tras la primera resolución (`sessionResolved`) no repite el refresh.
- Login/register/logout **no pueden pasar por el interceptor de refresh**: están en `AUTH_ENDPOINTS` (se saltan).

---

## Fase 3 (siguiente) — Shell de la app + Dashboard

> ⚠️ **Pendiente de implementación.** Este es el plan, no el trabajo hecho.

### 3.1 Crear `src/app/shared/` (primera vez)
- `pipes/currency.pipe.ts`: `Intl.NumberFormat(locale, { style:'currency', currency })` con valores de `@env`.
- `utils/month.ts`: helpers fecha → `{ year, month }`, formatea "septiembre 2026", prev/next.
- `utils/http-error.ts`: extraer `HttpErrorResponse → message` (deduplica lógica de login/register).
- `components/alert-error.ts`: aero de error reutilizable (daisyUI `alert alert-error`).
- `components/loading-spinner.ts` o uso directo de `loading loading-spinner`.

### 3.2 Shell (`layouts/main/main-shell/`)
- Componente `MainShell` (layout) con:
  - Navbar daisyUI: título "Expense Tracker", enlaces Dashboard/Transacciones/Categorías/Presupuestos con `routerLinkActive` para el activo, dropdown de usuario en `navbar-end` con `currentUser().email` y botón "Cerrar sesión".
  - `<router-outlet/>` dentro de `<main class="container mx-auto p-4">`.
  - Logout → `auth.logout()` → navigate `/login` (mover lógica del botón temporal del dashboard).
- Confirmar clase activa de daisyUI: `link link-primary` con `routerLinkActive` (o combinación navbar/tab).
- `main.routes.ts`: cargar `MainShell` como componente del nodo raíz y dejar los 4 hijos tal cual (authGuard ya está en el padre).

### 3.3 Dashboard (reemplaza el placeholder)
- `core/services/summary.service.ts`: `getMonthlySummary(year, month)` → `MonthlySummaryResponse`.
- Estado del componente con signals: `displayMonth` (`{year, month}` inicial = mes actual), `summary`, `loading`, `error`.
- Navegación de mes: botones ◀ ▶ + etiqueta con nombre del mes (descartar `<input type="month">` para no pelear con el estilo daisyUI).
- Tarjetas `stat`: Extra (balance), Ingresos, Gastos (color acorde al signo).
- Dónut ng2-charts: `baseChart` con gastos por categoría (`summary.byCategory` filtrando `type='EXPENSE'`); usar colores daisyUI del tema (tailwind palette).
- Barras presupuesto vs real: por cada `CategorySummary` con `budgetAmount != null`, `progress` con clase según `budgetStatus` (WITHIN_LIMIT → primary, EXCEEDED → error).
- Estados: `loading` → skeletons/spinner; `error` → `alert-error` con botón reintentar.
- Eliminar el botón "Salir" temporal del dashboard (pasa al shell).

### 3.4 Limpieza F2
- Revisar que `dashboard.ts` ya no inyecte `AuthService` (lo hace el shell).
- Añadir tests Vitest mínimos: pipe moneda y `MainShell` (render + logout con mock de auth).

### 3.5 Verificación F3
1. Login → `/dashboard` con navbar; refresco de sesión en F5.
2. Navbar: 4 enlaces navegables, activo resaltado; dropdown muestra email.
3. Dashboard: selección de mes, 3 números, dónut y barras según datos; estados loading/error controlados (probar con el backend parado).
4. Salir desde el navbar limpia sesión y redirige a `/login`.
5. Generar datos de prueba (transacciones vía API) para ver el dashboard completo.

---

## Fases posteriores (plan a alto nivel)

### F4 — Transacciones
- `core/services/transaction.service.ts` + `category.service.ts` (para resolver nombres y selectores).
- Tabla paginada (`Page<T>`), filtros (tipo, categoría, rango fechas), ordenación por columnas.
- Modal daisyUI crear/editar con Reactive Form que cambia el select de categorías según tipo; delete con confirmación.

### F5 — Categorías
- Lista/fichas, modal crear/editar, delete con manejo de 409 ("categoría en uso").

### F6 — Presupuestos
- Vista por mes, formulario con categoría+mes+importe, manejo de 409 (duplicado categoría+mes), columna "gastado" cruzando con summary.

---

## Checklist general frontend

- Antes de cada fase: `npm run build` (AOT) + `npm test` en verde.
- Todos los importes de moneda pasan por el pipe `currency` (nunca `toFixed` a mano).
- Los errores HTTP se presentan con `ErrorResponse.message` vía `http-error.ts`.
- El access token se mantiene en memoria; nunca persistir.
- Respetar las rutas con prefijo `/api` (proxy) — sin él la petición cae al dev server y devuelve HTML.
- `@shared/*` exige crear por primera vez `src/app/shared/` en F3.