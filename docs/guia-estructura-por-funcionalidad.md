# Guía: Estructura del proyecto por funcionalidad

Esta guía organiza las clases del monorepo **ExpenseTracker** agrupadas por **función o capacidad** en lugar de por servicio. El objetivo es entender qué hace cada pieza del sistema, dónde vive cada responsabilidad y cómo fluye una petición de extremo a extremo.

---

## Objetivo

El proyecto es una aplicación web de finanzas personales construida con una arquitectura de microservicios (Java 25, Spring Boot 4.1, Maven). Solo dos servicios tienen código implementado hoy:

| Servicio               | Puerto | Estado       |
| ---------------------- | ------ | ------------ |
| `identity-service`     | 8081   | Implementado |
| `gateway`              | 8080   | Implementado |
| `expense-service`      | 8082   | Implementado |
| `notification-service` | 8083   | Planificado  |
| `frontend`             | 4200   | Implementado |

El `notification-service` está **pendiente** (roadmap: Fase 6 del README). El `frontend` está **implementado** (Fases F1–F6 del `docs/guia-frontend.md` cerradas: scaffolding y tema, autenticación, dashboard, y CRUD de transacciones/categorías/presupuestos).

---

## Mapa de clases por funcionalidad

### 1. Validación JWT

Responsabilidad: emitir, firmar y verificar los tokens JWT.

| Clase           | Ubicación                        | Descripción                                                                                         |
| --------------- | -------------------------------- | --------------------------------------------------------------------------------------------------- |
| `JwtService`    | `identity-service/.../security/` | Genera tokens access/refresh (HMAC, issuer, TTL, claims `type`/`email`/`roles`/`jti`) y los parsea. |
| `JwtService`    | `gateway/.../security/`          | Versión de solo lectura: únicamente valida y parsea tokens. No genera.                              |
| `JwtAuthFilter` | `identity-service/.../security/` | Filtro que extrae el `Bearer` token, lo valida y autentica en el `SecurityContext`.                 |
| `JwtAuthFilter` | `gateway/.../security/`          | Igual que el anterior, pero además propaga la identidad hacia los servicios descendentes.           |

**Puntos a tener en cuenta:**

- El secreto (`app.jwt.secret`) y el issuer (`app.jwt.issuer`) deben ser **los mismos** en identity-service y gateway; de lo contrario el gateway rechazará tokens válidos.
- Solo los tokens de tipo `access` autentican en los filtros; los `refresh` se descartan.
- El secreto se lee de `.env` y se resuelve por `spring.config.import`.

### 2. Propagación de identidad (service-to-service)

Responsabilidad: comunicar quién es el usuario autenticado a los servicios internos sin volver a preguntarle.

| Clase                       | Ubicación               | Descripción                                                                                 |
| --------------------------- | ----------------------- | ------------------------------------------------------------------------------------------- |
| `UserHeadersRequestWrapper` | `gateway/.../security/` | Re-escribe el request inyectando las cabeceras `X-User-Id`, `X-User-Email`, `X-User-Roles`. |

**Puntos a tener en cuenta:**

- Esta es la base de la "comunicación servicio-a-servicio" del proyecto: el gateway autentica una vez y los servicios internos confían en las cabeceras.
- El wrapper elimina cualquier cabecera entrante con esos nombres para evitar suplantación (ver `getHeaderNames()`).

### 3. Configuración de seguridad HTTP

Responsabilidad: definir qué endpoints son públicos, cuáles requieren autenticación y cómo se gestionan las sesiones.

| Clase                          | Ubicación                      | Descripción                                                                                                    |
| ------------------------------ | ------------------------------ | -------------------------------------------------------------------------------------------------------------- |
| `SecurityConfig`               | `identity-service/.../config/` | Cadena stateless, BCrypt, `UserDetailsService`, `DaoAuthenticationProvider`, endpoints `/api/auth/*` públicos. |
| `SecurityConfig`               | `gateway/.../config/`          | Cadena stateless del gateway con la misma lista de endpoints públicos.                                         |
| `RestAuthenticationEntryPoint` | ambos `security/`              | Devuelve 401 en JSON cuando hay acceso no autenticado.                                                         |

**Puntos a tener en cuenta:**

- Ambos `SecurityConfig` desactivan CSRF y usan `SessionCreationPolicy.STATELESS` (sin sesiones gestionadas por Spring Security).
- `RestAuthenticationEntryPoint` está **duplicado** en ambos servicios: si se modifica, hay que cambiarlo en los dos (posible mejora: extraerlo a un módulo compartido).

### 4. Sesión y refresh tokens

Responsabilidad: emitir, almacenar, validar y revocar refresh tokens de forma segura.

| Clase                 | Ubicación                        | Descripción                                                            |
| --------------------- | -------------------------------- | ---------------------------------------------------------------------- |
| `CookieService`       | `identity-service/.../security/` | Crea/limpia la cookie HttpOnly `refresh_token`.                        |
| `RefreshTokenService` | `identity-service/.../service/`  | Almacena (hash SHA-256), valida expiración/revocación y revoca tokens. |
| `RefreshToken`        | `identity-service/.../domain/`   | Entidad JPA que persiste el hash del token en `refresh_tokens`.        |

**Puntos a tener en cuenta:**

- Nunca se guarda el token en claro: solo su hash SHA-256, como good practice de seguridad.
- El refresh es **rotativo**: cada renovación revoca el token anterior y emite uno nuevo.
- La cookie es `HttpOnly` + `SameSite=Lax` pero `secure(false)` en local (ajustar a `true` en producción tras HTTPS).

### 5. Autenticación (core)

Responsabilidad: registro, login, logout y renovación de sesión.

| Clase            | Ubicación                       | Descripción                                                                        |
| ---------------- | ------------------------------- | ---------------------------------------------------------------------------------- |
| `AuthController` | `identity-service/.../web/`     | Endpoints REST `/api/auth/register`, `/login`, `/logout`, `/refresh`.              |
| `AuthService`    | `identity-service/.../service/` | Lógica de registro (valida duplicados), login (BCrypt), logout y refresh rotativo. |

**Puntos a tener en cuenta:**

- El login acepta **email o username** como credencial (`findByEmailOrUsername`).
- La validación de los DTOs usa Bean Validation (`@NotBlank`, `@Email`, `@Size`).

### 6. Gestión de usuarios

Responsabilidad: representación y persistencia del usuario.

| Clase            | Ubicación                          | Descripción                                                   |
| ---------------- | ---------------------------------- | ------------------------------------------------------------- |
| `User`           | `identity-service/.../domain/`     | Entidad JPA `users` con roles, timestamps y flag `enabled`.   |
| `UserRepository` | `identity-service/.../transactionRepo/` | `findByEmailOrUsername`, `existsByEmail`, `existsByUsername`. |
| `Role`           | `identity-service/.../domain/`     | Enum `USER`, `ADMIN`.                                         |

**Puntos a tener en cuenta:**

- Los roles se guardan como `@ElementCollection` con `FetchType.EAGER`.
- `password_hash` está marcado como `nullable = true` por si en el futuro se implementa OAuth2.

### 7. Manejo de errores y DTOs de API

Responsabilidad: respuestas HTTP consistentes y contrato de entrada/salida.

| Clase                    | Ubicación                          | Descripción                                                                                                               |
| ------------------------ | ---------------------------------- | ------------------------------------------------------------------------------------------------------------------------- |
| `GlobalExceptionHandler` | `identity-service/.../web/`        | `@RestControllerAdvice` que traduce excepciones a `ErrorResponse`.                                                        |
| Excepciones              | `identity-service/.../exceptions/` | `EmailAlreadyExistsException`, `UsernameAlreadyExistsException`, `UserNotFoundException`, `InvalidRefreshTokenException`. |
| DTOs                     | `identity-service/.../dto/`        | `RegisterRequest`, `LoginRequest`, `TokenResponse`, `ErrorResponse`.                                                      |

**Puntos a tener en cuenta:**

- El formato de error es uniforme: `timestamp`, `status`, `error`, `message`, `path`.
- `BadCredentialsException` y `MethodArgumentNotValidException` también se centralizan aquí.

### 8. Puntos de entrada de aplicación

| Clase                        | Ubicación               | Descripción                                     |
| ---------------------------- | ----------------------- | ----------------------------------------------- |
| `IdentityServiceApplication` | `identity-service/.../` | Arranque Spring Boot del servicio de identidad. |
| `GatewayApplication`         | `gateway/.../`          | Arranque Spring Boot del gateway.               |

---

## Flujo de una petición (passo a passo)

1. El cliente envía la petición al **gateway** (`:8080`) con `Authorization: Bearer <accessToken>`.
2. `JwtAuthFilter` (gateway) valida el token y, si es válido, `UserHeadersRequestWrapper` añade las cabeceras `X-User-Id`/`X-User-Email`/`X-User-Roles`.
3. Si el usuario no está autenticado, `RestAuthenticationEntryPoint` devuelve `401` JSON.
4. Para login/registro, el gateway enruta a `identity-service` (`:8081`), donde `AuthController` delega en `AuthService`.
5. `AuthService` emite access + refresh; `CookieService` coloca el refresh token en cookie y devuelve el access token en el body JSON.

---

## Pasos a seguir para extender el sistema

1. **Implementar `expense-service`** (Fase 1 y 2 del roadmap): entidades `Transaction`, `Category`, `Budget`, CRUD, filtros, paginación y resumen mensual.
2. **Enrutar en el gateway**: definir rutas de Spring Cloud Gateway para `/api/transactions`, `/api/categories`, `/api/budgets`, `/api/summary` hacia `:8082`.
3. **Consumir las cabeceras de identidad** en el servicio de gastos: usar `X-User-Id` como referencia del propietario de cada transacción (no volver a pedir credenciales).
4. **Mensajería (Fase 6)**: RabbitMQ para eventos `TransactionCreated`, `TransactionDeleted`, `BudgetExceeded` y el `notification-service`.
5. **Frontend (Fase 5)**: aplicación Angular consumiendo el gateway.
6. **Calidad (Fase 7)**: ampliar tests unitarios e integración (Testcontainers ya integrado).

---

## Cosas a tener en cuenta (checklist general)

- **Secreto compartido**: identity y gateway deben compartir `APP_JWT_SECRET` y el mismo issuer.
- **Duplicación intencionada**: `JwtService`, `RestAuthenticationEntryPoint` y `JwtAuthFilter` existen en ambos servicios; revisar ambos al tocar lógica de seguridad.
- **.env**: los servicios resuelven su configuración desde `.env` de la raíz; lanzarlos desde la raíz del repo (`./mvnw -pl <servicio> spring-boot:run`).
- **Módulos desactivados**: `expense-service` está comentado en el `pom.xml` raíz; activarlo cuando se implemente.
- **Passwords/tokens**: nunca registrar el token en claro; usar el hash SHA-256 ya implementado en `RefreshTokenService`.
- **Unit tests**: este resumen no incluye `IdentityServiceApplicationTests` ni `GatewayApplicationTests` por ser pruebas de contexto vacías.
