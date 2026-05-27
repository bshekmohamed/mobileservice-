# Mobile Service Centre - Android Application

A modern Android application for mobile repair services built with Kotlin and Material Design 3.

## Features

✨ **Modern UI**
- Material Design 3 theme
- Smooth navigation with bottom navigation bar
- Beautiful card-based layouts
- Responsive design for different screen sizes

🛠️ **Service Management**
- Browse available services
- View service details and pricing
- Book services easily

📅 **Booking Management**
- View all your bookings
- Track booking status
- View booking dates and prices

👤 **User Profile**
- View and manage profile information
- Display contact details
- User information management

## Architecture

The app follows MVVM (Model-View-ViewModel) architecture:

- **UI Layer**: Fragments with ViewBinding
- **Data Layer**: Models for Service, Booking
- **Navigation**: Android Navigation Component
- **Database**: Room (configured)
- **API**: Retrofit (configured)

## Project Structure

```
app/src/main/
├── java/com/secondlife/mobile/
│   ├── ui/
│   │   ├── MainActivity.kt
│   │   ├── fragments/
│   │   │   ├── HomeFragment.kt
│   │   │   ├── ServicesFragment.kt
│   │   │   ├── BookingsFragment.kt
│   │   │   └── ProfileFragment.kt
│   │   └── adapters/
│   │       ├── ServiceAdapter.kt
│   │       └── BookingAdapter.kt
│   └── data/
│       └── model/
│           ├── Service.kt
│           └── Booking.kt
├── res/
│   ├── layout/
│   ├── menu/
│   ├── drawable/
│   ├── values/
│   └── navigation/
└── AndroidManifest.xml
```

## Dependencies

- **AndroidX Core**: Core Android libraries
- **Material Design 3**: Modern Material UI components
- **Navigation Component**: Fragment navigation
- **Room Database**: Local data persistence
- **Retrofit**: API calls
- **Glide**: Image loading
- **Gson**: JSON parsing

## Getting Started

### Prerequisites
- Android Studio (Hedgehog or later)
- Kotlin 1.8+
- Android SDK 34
- Minimum SDK 26

### Installation

1. Clone the repository
```bash
git clone https://github.com/bshekmohamed/mobileservice-.git
```

2. Open in Android Studio

3. Build the project
```bash
./gradlew build
```

4. Run on emulator or device
```bash
./gradlew installDebug
```

## Customization

### Change App Name
Edit `app/src/main/res/values/strings.xml`:
```xml
<string name="app_name">Your App Name</string>
```

### Change Colors
Edit `app/src/main/res/values/colors.xml`

### Add Your Services
Modify `ServicesFragment.kt` to load from your backend API

## Building for Release

1. Generate signed APK:
```bash
./gradlew assembleRelease
```

2. The APK will be in `app/build/outputs/apk/release/`

## API Integration

The app is configured with Retrofit for API integration.

### Example Usage:

```kotlin
// Create API interface
interface ApiService {
    @GET("services")
    suspend fun getServices(): List<Service>
}

// Make API calls
val retrofit = Retrofit.Builder()
    .baseUrl("https://your-api.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val apiService = retrofit.create(ApiService::class.java)
```

## Version History

### v3.0.0 (Current)
- Complete UI overhaul with Material Design 3
- Modern fragment-based architecture
- Enhanced navigation and user experience
- Updated dependencies to latest versions

### v2.0.0
- Initial release

## Contributing

Contributions are welcome! Please follow these steps:

1. Create a new branch (`git checkout -b feature/amazing-feature`)
2. Commit your changes (`git commit -m 'Add amazing feature'`)
3. Push to the branch (`git push origin feature/amazing-feature`)
4. Open a Pull Request

## License

This project is licensed under the MIT License - see LICENSE file for details.

## Contact

For support or queries, contact: bshekmohamed@gmail.com

---

**Made with ❤️ by B Shek Mohamed**
