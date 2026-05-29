# UniMarket - Student Marketplace App

UniMarket is a professional, feature-rich Android application designed as a dedicated marketplace for university students. It enables students to buy, sell, and trade items within their campus community securely and efficiently.

## 🚀 Features

- **Secure Authentication**: Built-in signup and login system using Firebase Authentication, including university registration code validation.
- **Product Management**: Users can easily list new products for sale, browse categories, and view detailed product information.
- **Real-time Chat**: Integrated messaging system allowing buyers and sellers to communicate directly within the app.
- **Categorized Browsing**: Organizes listings into logical categories for better discoverability.
- **Student Profiles**: Personal profiles to manage active listings and account settings.
- **Modern UI/UX**: Built entirely with Jetpack Compose following Material 3 Design guidelines for a smooth and responsive experience.

## 🛠 Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Navigation**: [Compose Navigation](https://developer.android.com/jetpack/compose/navigation)
- **Backend**:
  - [Firebase Auth](https://firebase.google.com/docs/auth) (Authentication)
  - [Cloud Firestore](https://firebase.google.com/docs/firestore) (Database)
  - [Firebase Storage](https://firebase.google.com/docs/storage) (Media hosting)
- **Image Loading**: [Coil](https://coil-kt.github.io/coil/)
- **Dependency Management**: Gradle (Kotlin DSL)

## 📦 Installation & Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/UsmanTariq-542/UniMarket-Android-App.git
   ```

2. **Open in Android Studio**:
   - Open Android Studio and select "Open".
   - Navigate to the cloned directory and select it.

3. **Firebase Configuration**:
   - Create a new project in the [Firebase Console](https://console.firebase.google.com/).
   - Add an Android app with the package name `com.example.unimarketapp`.
   - Download the `google-services.json` file and place it in the `app/` directory.
   - Enable **Authentication** (Email/Password), **Firestore**, and **Storage** in the Firebase console.

4. **Build and Run**:
   - Let Gradle sync finish.
   - Connect an Android device or start an emulator (API level 24 or higher).
   - Click the "Run" button.

## 📸 Screenshots

| Splash Screen | Login / Signup | Home Screen |
| :---: | :---: | :---: |
| ![Splash](https://via.placeholder.com/200x400?text=Splash+Screen) | ![Login](https://via.placeholder.com/200x400?text=Login+Screen) | ![Home](https://via.placeholder.com/200x400?text=Home+Screen) |

## 🤝 Contributing

Contributions are welcome! Feel free to open issues or submit pull requests to improve the app.

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.
