# SecondShelf – Campus Marketplace

SecondShelf is an Android-based campus marketplace application that allows students to buy and sell second-hand items, including textbooks, electronics, stationery, and laboratory equipment.

The application provides a convenient platform for students to browse listings, search for products, communicate with sellers, and manage their own listings.

**Built with Kotlin, Jetpack Compose, and Material 3.**

## Features

### Dashboard
- Personalized greeting and hero banner.
- Category filters.
- Recently added product listings.

### Marketplace
- Browse available second-hand products.
- Search and filter listings by category and condition.
- Sort products by price, newest, and relevance.

### Product Listings
- View detailed product information.
- Save and share listings.
- Contact sellers.
- Edit and delete personal listings.

### Search
- Search by product name, category, seller, and keywords.
- Keyword suggestions.

### User Profile
- View and edit profile information.
- Manage personal listings and saved items.
- Access messages and settings.

### Create Listing
- Validated product listing form.
- Product category, condition, price, and location fields.

### Messaging
- Conversations between buyers and sellers.
- Send and receive messages.

### Notifications
- Mark notifications as read.
- Dismiss notifications.
- Unread notification indicator.

### Authentication
- Supabase email and password authentication.
- Google OAuth authentication.

## Technologies Used

| Technology | Purpose |
|---|---|
| Kotlin | Main programming language |
| Jetpack Compose | Android user interface |
| Material 3 | UI components and design system |
| Supabase Auth | User authentication |
| Gradle Kotlin DSL | Build configuration |
| Android Studio | Development environment |

## Architecture

- **UI Layer:** Jetpack Compose and Material 3.
- **State Management:** `MarketplaceViewModel` using Compose `mutableStateOf`.
- **Authentication:** Supabase Auth managed through `AuthViewModel`.
- **Data Layer:** Mock data and repository logic in `MockData.kt`, structured for future Supabase/PostgREST integration.

## Project Structure

```text
app/src/main/java/com/example/secondshelf/
├── MainActivity.kt
├── SupabaseClient.kt
└── ui/
    ├── AuthViewModel.kt
    ├── viewmodel/
    │   └── MarketplaceViewModel.kt
    ├── model/
    │   ├── Models.kt
    │   └── MockData.kt
    ├── components/
    ├── screens/
    ├── navigation/
    │   └── AppNavigation.kt
    └── theme/
```

## Requirements

- Android Studio Ladybug or newer.
- JDK 17 or newer.
- Android SDK 36.
- Minimum Android SDK: 24.

## Setup Instructions

1. Clone the repository.
2. Open the project in Android Studio.
3. Create a `local.properties` file in the project root if it does not already exist.
4. Configure your Android SDK path and Supabase credentials:

```properties
sdk.dir=/path/to/Android/sdk
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-anon-key
```

5. Sync the Gradle project.
6. Run the application using an Android emulator or physical device.

## Build

To build the debug APK:

```bash
./gradlew :app:assembleDebug
```

The generated APK is located at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

To build the release APK:

```bash
./gradlew :app:assembleRelease
```

## Environment Configuration

| Variable | Location | Description |
|---|---|---|
| `SUPABASE_URL` | `local.properties` | Supabase project URL |
| `SUPABASE_ANON_KEY` | `local.properties` | Supabase anonymous key |

**Security:** Never commit `local.properties` or expose private credentials in the repository.

## Contributors

SecondShelf was developed collaboratively by four team members.

| Contributor | Contribution |
|---|---|
| Oh Chu Meng | Project development |
| Yeoh Ming Xuan | In-app chat functionality |
| Low Jia Horng | Marketplace functionality, product listing, and search |
| Choong Jun Zac | QR code integration and testing |

**Low Jia Horng:** [GitHub Profile](https://github.com/zIon6767)

## Project Status

Collaborative academic software development project.

## License

Private academic project. Not currently distributed under an open-source license.
