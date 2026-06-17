# HiPenter — Build Task Tracker

## Phase 1: Theme & Branding
- [ ] Update Color.kt with HiPenter palette
- [ ] Update Type.kt with Inter + Outfit fonts
- [ ] Update Theme.kt with custom color scheme

## Phase 2: Data Models & Network Layer
- [x] Create Post.kt data model
- [x] Create HiPenterApi.kt (Vercel proxy + Sheets API client)
- [x] Create AuthInterceptor.kt (OkHttp interceptor)

## Phase 3: Auth Layer
- [x] Create UserSession.kt
- [x] Create GoogleAuthManager.kt
- [x] Create LoginScreen.kt

## Phase 4: User Sheet Manager
- [x] Create UserSheetManager.kt

## Phase 5: Data Repository
- [x] Rewrite DataRepository.kt
- [x] Create LocalCache.kt

## Phase 6: UI Components
- [x] Create PostCard.kt
- [x] Create WordCounter.kt

## Phase 7: Screens
- [x] Rewrite MainScreen.kt → Feed Screen
- [x] Rewrite MainScreenViewModel.kt
- [x] Create ComposePostScreen.kt
- [x] Create ProfileScreen.kt

## Phase 8: Navigation
- [ ] Update NavigationKeys.kt
- [ ] Update Navigation.kt

## Phase 9: Build Configuration
- [ ] Update libs.versions.toml (add dependencies)
- [ ] Update app/build.gradle.kts (add deps + buildConfig)
- [ ] Update AndroidManifest.xml (INTERNET permission)

## Phase 10: Backend
- [ ] Create Apps Script backend (Code.gs + setup instructions)
- [ ] Create Vercel proxy (feed.ts, post.ts, delete.ts, config)

## Phase 11: CI/CD
- [ ] Create GitHub Actions release workflow
- [ ] Create GitHub Actions build workflow

## Phase 12: Web Landing Page
- [ ] Create docs/index.html
- [ ] Create docs/style.css
