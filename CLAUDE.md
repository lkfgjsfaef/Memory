# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A couple's memory-sharing app — track daily records, important dates (with countdown), wish lists, memory timelines, photo albums, visited locations on a map, and calendar mood tracking with custom emojis.

- **Backend**: Spring Boot 3.5.14 with raw JDBC (port 8081)
- **Web frontend**: Vue 3 + Vite (`frontend/`)
- **Android app**: Native Android with Java 17 (`memory/`)

## Build & Run

### Backend (Spring Boot + Maven)

```bash
./mvnw compile                  # Compile
./mvnw spring-boot:run          # Run (port 8081)
./mvnw test                     # Run tests
./mvnw test -Dtest=MemoryApplicationTests   # Single test class
./mvnw package -DskipTests      # Package JAR
```

Requires MySQL with database `memory_db`. See `application-sample.yml` for configuration template.
Run `src/main/resources/memory_db.sql` (full Navicat dump: DDL + seed data) to initialize tables and seed data.
Tests are minimal (single `contextLoads` smoke test).

### Web Frontend (Vue 3 + Vite)

```bash
cd frontend
npm install
npm run dev        # Dev server on port 5174, proxies /api to localhost:8081
npm run build      # Production build
```

### Android App

Open `memory/` in Android Studio (or build with Gradle):

```bash
cd memory
./gradlew assembleDebug      # Build debug APK
./gradlew installDebug        # Install on connected device/emulator
```

- minSdk 26, targetSdk 35, Java 17
- Uses ViewBinding (no data binding)
- Emulator connects to host at `http://10.0.2.2:8081/` (see `ApiClient.java`)

## Configuration

### Backend (`src/main/resources/application.yml`)

- `spring.datasource` — MySQL connection (`memory_db` on `localhost:3306`)
- `server.port` — 8081
- `app.jwt.secret` — HMAC signing key for JWT tokens
- `app.jwt.expiration` — token TTL in ms (default 7 days)
- `app.qiniu.*` — Qiniu cloud storage credentials (access key, secret key, bucket, domain, upload URL)
- `app.upload.path` — local file upload directory (default `./uploads`, served at `/uploads/**` by `WebConfig`)
- `logging.level.com.niit` — DEBUG by default

### Web Frontend (`frontend/vite.config.js`)

- Dev server on port **5174**, bound to `0.0.0.0`
- Proxies `/api` to `http://localhost:8081`
- Allowed hosts: `.cpolar.top` (for tunnel testing)

## Architecture

### Backend Layers

Standard three-tier: **Controller → Service → Repository (raw JDBC)**

- **Controllers** (`controller/`) — REST endpoints returning `Result` (uniform `{code, message, data}` envelope). Each method wraps logic in try/catch.
- **Services** (`service/`) — business logic. Errors propagate as RuntimeExceptions caught by controllers.
- **Repositories** (`repository/`) — raw `JdbcTemplate` with hand-written SQL and `BeanPropertyRowMapper`. No JPA, no Spring Data, no ORM. SQL injection safe via parameterized queries.
- **Entities** (`entity/`) — plain POJOs mapping to DB columns via `BeanPropertyRowMapper` underscore-to-camelCase conversion.
- **Config** (`config/`) — JWT interceptor/util, CORS, password encoder, Qiniu, web config.

### Authentication (Backend)

JWT-based auth via `JwtInterceptor` (registered in `WebConfig`). All `/api/**` requests pass through except:
- `OPTIONS` (CORS preflight)
- `POST /api/auth/login`

The interceptor extracts `Bearer` token from `Authorization` header, validates via `JwtUtil`, stores `userId`/`username` in `UserContext` (ThreadLocal). Passwords are BCrypt-encoded.

### Backend API Endpoints

| Controller | Base Path | Resources |
|---|---|---|
| `AuthController` | `/api/auth` | login, me, user/{id}, avatar |
| `HomeController` | `/api` | couple (GET/PUT), important-dates (CRUD) |
| `DailyController` | `/api/daily-records` | CRUD + stats |
| `CalendarController` | `/api/calendar` | notes (CRUD by year/month), moods (upsert by date) |
| `WishController` | `/api/wishes` | CRUD + stats (filterable by status/category) |
| `MemoryController` | `/api` | albums, moments, locations (CRUD) |
| `CustomEmojiController` | `/api/custom-emojis` | CRUD (shared emoji pool, no auth required) |
| `QiniuController` | `/api/qiniu` | upload-token |

### Android Architecture

**MVVM pattern**: Fragment → ViewModel → Repository → Retrofit Service → Backend API

#### Package Structure

```
com.niit.memory/
├── data/
│   ├── api/          # Retrofit services + ApiClient + AuthInterceptor
│   ├── model/        # POJOs matching backend entities
│   └── repository/   # Data access layer wrapping Retrofit calls
├── ui/
│   ├── screens/      # One package per screen (Fragment + ViewModel)
│   │   ├── home/     # HomeFragment, HomeViewModel
│   │   ├── daily/    # DailyFragment, DailyPublishActivity, DailyDetailActivity, DailyViewModel
│   │   ├── calendar/ # CalendarFragment, CalendarViewModel
│   │   ├── wishlist/ # WishlistFragment, WishlistViewModel
│   │   ├── memories/ # MemoriesFragment, MemoriesViewModel
│   │   ├── login/    # LoginActivity, LoginViewModel
│   │   ├── albumdetail/  # AlbumDetailActivity, AlbumDetailViewModel
│   │   └── momentdetail/ # MomentDetailActivity, MomentDetailViewModel
│   ├── adapters/     # RecyclerView adapters
│   └── widget/       # NonScrollGridView (calendar grid)
├── util/             # SessionManager, ImageViewer, QiniuHelper, CoilHelper, TaskExecutor, etc.
├── MainActivity.java     # Container: bottom nav + logout
└── MemoryApplication.java
```

#### Key Libraries

| Library | Purpose |
|---|---|
| Retrofit + OkHttp + Gson | HTTP client, connects to backend at `10.0.2.2:8081` |
| Coil 2.7 | Image loading (thumbnail, placeholder, error fallback) |
| OSMDroid 6.1 | Map with Amap tile source (visited locations) |
| Qiniu Android SDK 8.2 | Direct-to-cloud image upload |
| Material 1.12 | Material Design 3 components |
| Navigation 2.8 | Bottom nav + nav_graph.xml |
| ViewModel + LiveData | MVVM reactive state |

#### Navigation

**Bottom nav (5 tabs)** via `nav_graph.xml`:

| Tab | Fragment | Features |
|---|---|---|
| Home (`nav_home`) | HomeFragment | Couple info, avatar upload, important dates with countdown |
| Daily (`nav_daily`) | DailyFragment | Daily records with year/month filter, stats, multi-image upload |
| Calendar (`nav_calendar`) | CalendarFragment | Custom calendar grid, notes (CRUD), mood (today only) |
| Wishlist (`nav_wishlist`) | WishlistFragment | Wishes with status/category/owner filters, multi-image upload |
| Memories (`nav_memories`) | MemoriesFragment | 3-tab: map (visited locations), timeline (moments), albums |

**Detail activities** (navigated via Intent, not nav graph):
- `DailyDetailActivity` — full daily record view
- `DailyPublishActivity` — create/edit daily record with image upload
- `AlbumDetailActivity` — album photos + cover management
- `MomentDetailActivity` — single moment detail

**Entry flow**: `LoginActivity` (JWT login) → `MainActivity` (bottom nav container).

#### Authentication (Android)

- `LoginActivity` sends credentials → receives JWT token → `SessionManager` persists token, userId, nickname to DataStore
- `AuthInterceptor` attaches `Bearer` token to all Retrofit requests
- Logout: clears session → redirects to `LoginActivity`
- Two preset demo accounts (configurable in LoginActivity / LoginView)

#### Image Upload Flow

1. User picks images via `GetMultipleContents()` (multi-select)
2. Images are sequentially uploaded on background thread
3. Each image: cached to temp file → ViewModel gets Qiniu token from backend → Multipart upload directly to Qiniu → receive CDN URL
4. Uploaded URLs are appended to form data (comma-separated)
5. Save button is blocked (`isUploading` flag) until all uploads complete
6. Progress shown as "正在上传 2/5..."

Each ViewModel has its own `uploadImage(File)` method (duplicated logic — the same Qiniu upload pattern).

### Web Frontend (Vue 3 + Vite)

- Vue 3 with Vue Router (8 routes: `/login`, `/`, `/daily`, `/calendar`, `/wishlist`, `/memories`, `/memories/album/:id`, `/memories/moment/:id`)
- Route guards: `requiresAuth` → `/login`; `guest` → `/`
- `src/data/api.js` — Axios with Bearer token interceptor, auto-redirect to `/login` on 401
- `src/stores/userStore.js` — auth state in localStorage
- `src/data/mock.js` — mock data fallback
- `src/composables/` — `useToast.js`, `useApi.js`, `useUpload.js` (Qiniu SDK), `useImageCompress.js`
- Shared components: `NavBar.vue`, `StatsCard.vue`, `Modal.vue`, `Toast.vue`
- Images uploaded via Qiniu SDK: get token from backend → upload directly → store URL
- Local file serving: `WebConfig` maps `/uploads/**` to `app.upload.path` (default `./uploads`)

### Database

11 tables in `memory_db` with `utf8mb4` encoding:
`users`, `couple`, `important_date`, `daily_record`, `wish`, `calendar_note`, `calendar_mood`, `memory_album`, `memory_moment`, `visited_location`, `custom_emoji`

- `memory_db.sql` is a full Navicat dump (DDL + seed data for all tables)
- Incremental migration in `src/main/resources/update_20260618.sql` (removed music_playlist, added custom_emoji)

### Package Naming

The Maven artifactId is `menmory` (typo), but the Java package is `com.niit.memory`. Both live under `src/main/java/com/niit/`.

## Key Design Decisions

### Backend
- No JPA/Hibernate — raw JDBC via `JdbcTemplate`, SQL hand-written in repositories
- No input validation — entities have no validation annotations
- JWT authentication — stateless, ThreadLocal `UserContext`
- Custom emoji pool is shared across both users (no per-user ownership)
- Constructor injection throughout (no `@Autowired` fields)
- CORS wide open — all origins, methods, headers
- `CustomEmojiController` skips the service layer (controller → repository directly)

### Android
- No dependency injection framework — manual singleton (`ApiClient`) and constructor injection
- Background threads via `new Thread()` or `TaskExecutor` (no coroutines, no RxJava)
- Each ViewModel duplicates `uploadImage()` (Qiniu token + upload logic)
- Multi-select images via `GetMultipleContents()`, sequential upload in background thread
- `SessionManager` wraps DataStore for token persistence
- OSMDroid uses Amap tile source (not default OSM)
- UI uses programmatic layout creation for dialogs (AlertDialog.Builder + LinearLayout)

### Shared
- Both frontends (Vue and Android) use the same backend API
- Image upload: frontend gets Qiniu token from backend → uploads directly to Qiniu → stores CDN URL
- Consistent `{code, message, data}` API response format
