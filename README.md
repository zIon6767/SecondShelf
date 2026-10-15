# Second Shelf

A campus marketplace Android app where students can buy and sell used items like textbooks, electronics, stationery, lab equipment, etc. safely.

Built with **Kotlin**, **Jetpack Compose**, and **Material 3**.

## Features

- **Dashboard** : greeting, hero banner, category filters, recent listings
- **Marketplace** : browse, search, filter by category/condition, sort by price/newest/relevance
- **Listing details** : save, share, contact seller, edit/delete own listings
- **Search** : product names, categories, sellers, keyword suggestions
- **Profile** : user info, my listings, saved items, messages, settings, edit profile
- **Create listing** : validated form with category, condition, price, location
- **Messaging** : conversations with sellers, send/receive messages
- **Notifications** : mark read, dismiss, unread badge on bell icon
- **Auth** : Supabase email/password and Google OAuth

## Requirements

- Android Studio Ladybug or newer
- JDK 17+
- Android SDK 36
- Min SDK 24

## Setup

1. Clone the repository and open it in Android Studio.
2. Create `local.properties` in the project root (if not present):

```properties
sdk.dir=/path/to/Android/sdk
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-anon-key
```

3. Sync Gradle and run on an emulator or device.

## Build

```bash
./gradlew :app:assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## Release build

```bash
./gradlew :app:assembleRelease
```

## Project structure

```
app/src/main/java/com/example/secondshelf/
├── MainActivity.kt
├── SupabaseClient.kt
├── ui/
│   ├── AuthViewModel.kt
│   ├── viewmodel/MarketplaceViewModel.kt   # App state (listings, favorites, messages)
│   ├── model/Models.kt, MockData.kt        # Data models + mock layer
│   ├── components/                         # Reusable UI (ListingCard, HeroBanner, etc.)
│   ├── screens/                            # All screens
│   ├── navigation/AppNavigation.kt
│   └── theme/
```

## Architecture

- **UI**: Jetpack Compose + Material 3
- **State**: `MarketplaceViewModel` with Compose `mutableStateOf`
- **Auth**: Supabase Auth via `AuthViewModel`
- **Data**: Mock repository in `MockData.kt` — structured for future Supabase/PostgREST integration

## Environment variables

| Key | Location | Description |
|-----|----------|-------------|
| `SUPABASE_URL` | `local.properties` | Supabase project URL |
| `SUPABASE_ANON_KEY` | `local.properties` | Supabase anonymous key |

Never commit `local.properties` — it is git-ignored.

# Contributors
This project was a collaborative effort by the following members:
* **(fill in): Oh Chu Meng [GitHub](https://github.com/MengsChill)**
* **In-app Chat: Yeoh Ming Xuan [GitHub](https://github.com/)**
* **(fill in): Low Jia Horng [GitHub](https://github.com/)**
* **QR code integration & Testing: Choong Jun Zac [GitHub](https://github.com/ishtardsama), [Email](choongjunzac@gmail.com)**

## License

Private project.
