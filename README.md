# jobaaya

**Professional Networking & Business Growth for the Multi-Trade Workforce**

jobaaya is a specialized professional directory and networking platform designed for the diverse workforce in India. It enables professionals across various trades—electricians, doctors, mechanics, nutritionists, and more—to showcase their skills, manage their availability, and connect with clients in their local proximity.

## Key Features

- **Multi-Trade Discovery**: Search and discover professionals by skill, profession, or location.
- **Near Me Maps**: Real-time Google Maps integration with GPS-based proximity search and routing directions.
- **Secure Peer-to-Peer Chat**: Encrypted one-to-one messaging with support for photos, videos, voice messages, and documents.
- **Professional Cards**: High-impact digital cards with Verified Badges, Ratings, and Reviews.
- **Integrated Tools**: Service calculator with tax/rate estimators and a persistent sticky notepad.
- **Admin & Moderation**: Advanced dashboard for verification management, report auditing, and growth analytics.
- **Multilingual Support**: Available in English, Hindi, Malayalam, and Arabic.

## Production Audit & Optimization

- **Architecture**: Clean MVVM architecture with Room database and Repository pattern.
- **Security**: EncryptedSharedPreferences for session management and secure API key handling via .env.
- **Performance**: Optimized for low-end devices with R8/Proguard minification and efficient image loading (Coil).
- **Multilingual**: Comprehensive localization system supporting localized strings and device language detection.
- **UI/UX**: Material 3 design with full Dark Mode support and dynamic color theming.

## Getting Started

1. **Clone & Open**: Open the project in Android Studio.
2. **Environment**: Create a `.env` file in the root directory (refer to `.env.example`).
   - Add your `GEMINI_API_KEY`.
   - Add your `MAPS_API_KEY` for Google Maps functionality.
3. **Build & Run**: Build the project and deploy it to an emulator or physical device.

---
*Built with ❤️ for the Indian Professional Workforce.*
