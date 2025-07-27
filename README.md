![ChatZ Banner](.github/assets/chatz_banner.webp)

## Overview

💬 **ChatZ** is a sleek, real-time chat application for Android, built using **Kotlin** and traditional **XML layouts**. Powered by **Firebase**, ChatZ delivers smooth, reliable messaging with a modern, minimal design.

## Key Features

- **Real-Time Chat**  
  Instantly send and receive messages with Firebase Realtime Database.

- **User Authentication**  
  Sign in or sign up securely using Firebase Auth.

- **Clean UI**  
  XML-based layouts with Material Design 3 support.

- **Timestamps**  
  Messages show accurate time sent and received.

- **User Profiles**  
  View and update profile details.

- **Custom Edits to User Profile**  
  Show your profile status and username with personalized profile picture.

## 🛠️ Built With

- Kotlin – Primary development language  
- XML Layouts – Classic UI building approach  
- Firebase – Backend for Auth and Firestore DB
- Cloudinary - For Image hosting 
- ViewModel + LiveData – MVVM architecture  
- Glide – Image loading  
- Material3 – UI theming and styling  
- Navigation Component – For screen transitions

## 🧰 Tech Stack & Tools

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white&style=for-the-badge"/>
  <img src="https://img.shields.io/badge/XML%20Layouts-2196F3?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Firebase-FFCA28?logo=firebase&logoColor=black&style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Material--3-6200EE?logo=material-design&logoColor=white&style=for-the-badge"/>
  <img src="https://img.shields.io/badge/MVVM-Architecture-26A69A?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Android%20Studio-3DDC84?logo=android-studio&logoColor=white&style=for-the-badge"/>
</p>

## 📸 Screenshots

<p align="center">
  <img src=".github/assets/login.webp" width="200"/>
  <img src=".github/assets/register.webp" width="200"/>
  <img src=".github/assets/home.webp" width="200"/>
  <img src=".github/assets/profile.webp" width="200"/>
  <img src=".github/assets/shimmer.webp" width="200"/>
  <img src=".github/assets/users.webp" width="200"/>
  <img src=".github/assets/message.webp" width="200"/>
</p>

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog or newer  
- Kotlin 1.9+  
- Android SDK 34+  
- Firebase Project with Authentication and Firestore Database enabled

### Steps

1. Clone this repo:
   ```bash
   git clone https://github.com/Krtonia/ChatZ.git

2. Open the project in Android Studio

4. Sync Gradle and resolve dependencies

5. Run the app on your emulator or real device 🚀

## 🧩 Architecture

- MVVM pattern with clean separation of UI, ViewModel, and Repository
- Uses StateFlow / LiveData for reactive updates
- Organized modular code (UI → ViewModels)

## 🤝 Contributing
- Contributions, issues, and feature requests are welcome!

- Fork the repo

- Create your feature branch (git checkout -b feature/new-feature)
- Commit your changes (git commit -m 'Add new feature')
- Push to the branch (git push origin feature/new-feature)
- Open a Pull Request