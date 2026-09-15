# Guía: Frontend — Estado y plan por fases

Guía de desarrollo del frontend Angular del Expense Tracker. Documenta qué se ha hecho, las decisiones de arquitectura y el plan de las siguientes fases. Es una guía viva: se actualiza al cerrar cada fase.

---

## Estado actual

| Fase | Contenido | Estado |
| ---- | --------- | ------ |
| F1   | Scaffolding, tema, routing, infraestructura base | ✅ Cerrada |
| F2   | Autenticación: core (service, interceptor, guards), login/register | ✅ Cerrada |
| F3   | Shell de la app + Dashboard | ✅ Cerrada |
| F4   | CRUD Transacciones | ✅ Cerrada |
| F5   | CRUD Categorías | ✅ Cerrada |
| F6   | CRUD Presupuestos | ✅ Cerrada |

---

## Stack y decisiones (registro de decisiones)

- **Angular 21** standalone, componentes señal (Signals) para estado local, lazy loading por ruta.
- **Tailwind CSS v4 + daisyUI 5.7.37**, tema **corporate** activado con `data-theme` en `index.html` + lista de themes en `styles.css`.
- **ng2-charts 10 + chart.js 4.5.1 + @angular/cdk 21.2.14** para los gráficos del dashboard (opción A: "ng2-charts + CDK").
- **Vitest** como runner de tests (`ng test`); specs actuales: `app`, `money.pipe`, `http-error`, `main-shell` (8 tests).
- **Access token**: solo en memoria (signal privada en `AuthService`), nunca en localStorage.
- **Refresh token**: cookie HttpOnly `refresh_token`; se envía automáticamente (same-origin vía proxy). Nunca usar `withCredentials`.
- **URL base**: `environment.apiUrl = '/api'` → proxy `/api` → `http://localhost:8080` (gateway).
- **Formularios**: Reactive Forms clásicos (no signals-based forms).
- **Moneda configurable**: `environment.locale='es-ES'`, `environment.currency='EUR'` → `Intl.NumberFormat` para formatear importes.
- Aliases tsconfig: `@core/*` → `src/app/core/*`, `@features/*` → `src/app/layouts/*`, `@shared/*` → `src/app/shared/*`, `@env/*` → `src/environments/*`.
  - `@shared/*` se crea en F3 (pipes, utils, componentes reutilizables).

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
│   │   │   │   ├── auth.service.ts     # signals, login/register/logout/refresh/initialize
│   │   │   │   ├── summary.service.ts  # GET /api/summary/monthly
│   │   │   │   ├── transaction.service.ts  # CRUD + listado paginado (sort date,desc)
│   │   │   │   ├── category.service.ts     # CRUD + list (sin paginar)
│   │   │   │   └── budget.service.ts       # CRUD + listado paginado (sort year+month desc)
│   │   │   ├── interceptors/
│   │   │   │   └── auth-interceptor.ts # Bearer + 401→refresh (single-flight) + retry 1×
│   │   │   ├── guards/
│   │   │   │   ├── auth.guard.ts       # asíncrono: initialize→dashboard, UrlTree /login
│   │   │   │   └── guest.guard.ts      # asíncrono: invertido
│   │   │   └── utils/
│   │   │       └── jwt.ts              # decodeJwt, isTokenExpired
│   │   ├── shared/                     # creado en F3
│   │   │   ├── pipes/
│   │   │   │   ├── money.pipe.ts       # Intl es-ES/EUR (selector `money`)
│   │   │   │   └── category-name.pipe.ts  # categoryId → nombre (selector `categoryName`)
│   │   │   ├── utils/
│   │   │   │   ├── month.ts            # Month, currentMonth, shiftMonth, formatMonthLabel, monthName
│   │   │   │   └── http-error.ts       # HttpErrorResponse → ErrorResponse.message
│   │   │   └── components/
│   │   │       └── error-alert/        # alert alert-error + retry opcional
│   │   └── layouts/
│   │       ├── auth/
│   │       │   ├── auth.routes.ts      # /login, /register (guestGuard)
│   │       │   ├── login/              # Reactive Form + daisyUI ✅
│   │       │   └── register/           # Reactive Form + daisyUI ✅
│   │       └── main/
│   │           ├── main.routes.ts      # authGuard + MainShell (layout) + 4 hijos
│   │           ├── main-shell/         # navbar, dropdown usuario/logout, <router-outlet/> ✅
│   │           ├── dashboard/          # resumen mensual + dónut + barras presupuesto ✅
│   │           ├── transactions/       # tabla paginada + filtros + modal CRUD ✅
│   │           ├── categories/         # lista + modal CRUD (409 "en uso") ✅
│   │           └── budgets/            # vista mensual + paginación + modal CRUD ✅
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

## Fase 3 — Shell de la app + Dashboard ✅ Cerrada

> **Qué incluye** (implementado y verificado): carpeta `shared/` creada, layout `MainShell` con navbar daisyUI + dropdown de usuario, `SummaryService` y dashboard con resumen mensual, dónut y barras de presupuesto.

- **`shared/`**: `MoneyPipe` (selector `money`, `Intl.NumberFormat` con values de `@env`), `month.ts` (meses 1-based, `currentMonth`/`shiftMonth`/`formatMonthLabel`), `http-error.ts` (deduplica el `message` de error), `ErrorAlert` (`alert alert-error` + retry).
- **`MainShell`** (`layouts/main/main-shell/`): navbar con 4 enlaces + `routerLinkActive="link-primary"`, `<router-outlet/>` en `main`; `onLogout()` → `auth.logout()` → navigate `/login`. Es el `component` del nodo raíz de `main.routes.ts` (el `authGuard` sigue en el padre).
- **Dashboard**: signals `month`/`summary`/`loading`/`error`; navegación de mes ◀▶ con `shiftMonth`; 3 tarjetas (balance/ingresos/gastos), dónut ng2-charts (`canvas baseChart`), barras `progress` según `budgetStatus`. `load()` se deja **public** para usarlo como handler en el template.
- **Decisión de formato**: `Intl` es-ES/EUR separa el importe del símbolo con espacio de no ruptura (`\u00A0`) y no agrupa miles hasta 10.000 (`minimumGroupingDigits=2`, p. ej. `1234,50 €`). Los specs deben esperar `\u00A0` en la aserción.

---

## Fases 4–6 — CRUD de transacciones, categorías y presupuestos ✅ Cerradas

> Con F4–F6 el frontend queda **completo** (todas sus pantallas implementadas). Verificado con `npm run build` (AOT) y `npm test` (8 tests) en verde. Warnings benignos: bundle inicial >500 kB (549 kB) y `& -> Empty sub-selector` (cosmético de daisyUI).

### F4 — Transacciones
- `transaction.service.ts`: `list(filter?, page=0, size=10)` → `Page<TransactionResponse>` con `sort=date,desc`; `get`, `create`, `update`, `delete`.
- `category.service.ts` (compartida con F5): `list(type?)` → `CategoryResponse[]` (lista completa, no paginada) + CRUD.
- **`CategoryNamePipe`** (selector `categoryName`): resuelve `categoryId → nombre` contra el array enlazado (`{{ t.categoryId | categoryName: categories() }}`); si no encuentra muestra `—`. Motivo: `TransactionResponse` no incluye el nombre de categoría.
- Tabla `table-zebra` paginada con pager `join` (label `page.number + 1`); filtros por tipo (recorta el select de categorías), categoría y rango de fechas (`fromDate`/`toDate`); botón Limpiar.
- Modal crear/editar: signal `typeFilter` sincroniza el select de categorías con el tipo (`onFormTypeChange` resetea `categoryId`); al editar se ajusta al tipo de la fila. Delete con `confirm()`.

### F5 — Categorías
- Lista completa sin paginación; modal crear/editar (nombre + tipo), delete con `confirm()` y **manejo de 409** ("categoría en uso") vía `httpErrorMessage`.

### F6 — Presupuestos
- `budget.service.ts`: `list(filter?, page, size)` con dos sorts (`year,desc` + `month,desc` vía `HttpParams.append`) + CRUD.
- Vista mensual: navegación ◀▶ con `shiftMonth` + `monthLabel`; tabla con categoría (pipe), año, mes y importe.
- Formulario: select **solo categorías EXPENSE**, año [2000–2100], mes 1–12 con `monthName`, importe > 0; **409 duplicado categoría+mes** → mensaje inline en el formulario.
- ⚠️ **Desviación del plan**: se descarta la columna "gastado" cruzando con summary (el dashboard ya cruza presupuesto vs. gastos; la vista de presupuestos se mantiene simple por mes).

### Detalles técnicos relevantes
- **Queries signal en plantillas**: `viewChild<stringRef>` devuelve un **`ElementRef`** en runtime (no el nodo DOM). Llamar `showModal()`/`close()` (o la auto-desusenvuelta `modal?.close()` en plantilla) lanza "not a function". **Patrón correcto**: tipar `viewChild<ElementRef<HTMLDialogElement>>('modal')` y usar `this.modal()?.nativeElement.showModal()` en TS; para cerrar desde la plantilla exponer un método `closeModal()` → `this.modal()?.nativeElement.close()`.
- **Controles de importe**: se tipan `amount: [null as number | null, ...]` para poder `patchValue({ amount: b.amount })` desde respuestas (si no, el tipo queda `null` literal y el build falla).
- `monthName` (función de `shared/utils/month.ts`) se expone como propiedad `readonly monthName = monthName;` en `Budgets` para poder llamarla desde la plantilla.

---

## Checklist general frontend

- Antes de cada fase: `npm run build` (AOT) + `npm test` en verde.
- Todos los importes de moneda pasan por el pipe `money` (nunca `toFixed` a mano).
- Los errores HTTP se presentan con `ErrorResponse.message` vía `http-error.ts`.
- El access token se mantiene en memoria; nunca persistir.
- Respetar las rutas con prefijo `/api` (proxy) — sin él la petición cae al dev server y devuelve HTML.