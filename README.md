# <p align="center">ShareRides</p>
<p align="center">A car-sharing (carpooling) Android app</p>

## School and Course
<img src="https://epg.ulisboa.pt/sites/ulisboa.pt/files/styles/logos_80px_vert/public/uo/logos/logo_ist.jpg?itok=2NCqbcIP" width="100" height="50">

[Instituto Superior Técnico](https://tecnico.ulisboa.pt/)

[Computer Science and Engineering](https://tecnico.ulisboa.pt/en/education/courses/masters-programmes/computer-science-and-engineering/)

## Class Subject and Goals
### Class: CMU - Mobile and Ubiquitous Computing (2025/26)
### Goals

- Android app development with Kotlin and Jetpack Compose
- Location awareness (maps, places search, nearby rides)
- Resource frugality (battery and metered data)
- Local caching and offline support (Room, WorkManager)
- Integration with an external service (IPMA weather API)
- Backend integration (Supabase: Auth, Postgres, Storage)

### Grade: 18/20 ![Grade](https://img.shields.io/badge/Grade-A-brightgreen)

## Problem Specification

SharIST gives mobile support to people offering shared car rides and to
passengers searching for them. Users can find rides near them, see who is
driving and in which car, request rides, and rate each other.

The assignment required:

- **Accounts and profiles**: name, photo, vehicles, ratings with a histogram, and comments from other users.
- **Map** with available rides, ride requests and each user's favourite locations.
- **Rides and ride requests**: a ride has a departure point, destination, date/time, cost, capacity and a cancellation window. A ride request has a radius around the origin and destination and a time tolerance. Both can recur daily or weekly.
- **Pictures** of users, cars and locations.
- **Ratings** from 1 to 5 stars. A new rating from the same user overwrites the previous one.
- **One external service**, chosen from weather or payments.
- **Resource frugality**: fetch data only when it is needed, and load photos automatically on Wi-Fi but only on tap on a metered connection.
- **Two advanced features** out of three: caching, anti-fraud mechanisms, disconnected operation.

## Implemented Features

| Area | What the app does |
| --- | --- |
| Accounts | Sign up, login, change email and password (Supabase Auth) |
| Profiles | Photo, average rating, rating histogram, reviews from other users, one review per pair of users |
| Vehicles and locations | Add vehicles (brand, model, plate, colour, capacity, photo) and favourite locations (with photo) |
| Rides | Create rides (daily/weekly recurrence, cost, seats, cancellation window), subscribe, unsubscribe |
| Ride requests | Create requests with a tolerance range. A driver can accept one, and the first driver to accept wins |
| Map | Google Maps with nearby rides, requests and favourite locations, plus place autocomplete |
| Weather (external service) | Rides can be subscribed with "only if it doesn't rain". The [IPMA API](https://api.ipma.pt/) is checked periodically in the background |
| Caching (advanced) | Room database as the local cache, with a TTL policy per data type and Wi-Fi/mobile-aware prefetch radius |
| Disconnected operation (advanced) | Rides, requests, reviews and other changes are stored locally with a sync state and pushed when the connection returns |

### Resource frugality

- Nearby rides are fetched on the server side, filtered by a bounding box around the user, so only relevant rows are transferred.
- The search radius is larger on Wi-Fi (100 km) than on mobile data, so the app prefetches more content when data is free (`LocationPolicy.kt`).
- Cache entries expire by type (`CachePolicy.kt`).
- Photos are compressed before upload (`ImageCompressor.kt`).
- Background sync, validation and weather checks run through WorkManager, and only when a network connection is available.

## Tech Stack

- Kotlin, Jetpack Compose (Material 3), Navigation Compose
- Room (with KSP), DataStore, WorkManager
- Supabase (`supabase-kt`): Auth, Postgrest and Storage
- Google Maps Compose, Places SDK, Fused Location
- Retrofit and Gson for the IPMA API, Coil for images
- Min SDK 24, target SDK 36

## Project Structure

```
app/src/main/java/com/example/sharist/
├── data/
│   ├── local/        Room DAOs, database, utilities (network type, image compression)
│   ├── model/        Entities and serializable models
│   ├── policies/     Cache TTL and location radius policies
│   ├── remote/       Supabase client, IPMA API, SQL functions (remote/rpc/*.sql)
│   ├── repository/   Repositories combining local and remote sources
│   └── session/      Session and cache managers
├── sync/             WorkManager workers (sync, validation, weather)
└── ui/               Compose screens and components
```

## Setup

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (a recent version that supports AGP 9.x)
- JDK 11 or newer (the one bundled with Android Studio works)
- An Android emulator or device with Google Play services (needed for Maps and location)
- A [Supabase](https://supabase.com/) account (free tier is enough)
- A [Google Cloud](https://console.cloud.google.com/) project with billing enabled (Maps has a free monthly credit)

### 1. Clone the repository

```bash
git clone https://github.com/iribeirocampos/ShareRides.git
```

Open the folder in Android Studio and let Gradle sync. It will fail to run properly until you finish the steps below.

### 2. Create the Supabase project

1. Create a new project in the [Supabase dashboard](https://supabase.com/dashboard).
2. Go to **Project Settings → API** and copy the **Project URL** and the **anon / publishable key**. Use the anon key only. Never put the `service_role` key in the app.
3. In **Authentication → Providers**, keep Email enabled. For quick testing you can turn off "Confirm email" so new accounts work immediately.

#### Database tables

The app expects these tables in the `public` schema. Column names are case-sensitive and use camelCase, so they are quoted in SQL.

| Table | Columns |
| --- | --- |
| `profiles` | `id` (uuid, same as the auth user id), `type` (`DRIVER` or `RIDER`), `username`, `photoUrl`, `averageRating` |
| `vehicle` | `id`, `brand`, `model`, `userId`, `licencePlate`, `color`, `capacity`, `remotePhotoUrl` |
| `location` | `id`, `name`, `address`, `latitude`, `longitude`, `userId`, `remotePhotoUrl` |
| `ride` | `id`, `departure` (jsonb), `destination` (jsonb), `departureDateTime`, `arrivalDateTime`, `cost`, `availableSeats`, `userId`, `vehicleId`, `cancelUpToDays`, `isWeekly`, `isDaily` |
| `rideRequest` | `id`, `userId`, `departure` (jsonb), `destination` (jsonb), `toleranceRange`, `date`, `isWeekly`, `isDaily`, `driverId`, `active` |
| `subscription` | `id`, `rideId`, `userRiderId`, `onlyInRain`, `reoccurrence` (unique on `rideId` + `userRiderId`) |
| `review` | `id`, `targetUserId`, `reviewerId`, `reviewerName`, `rating`, `comment` |

`departure` and `destination` are JSON objects with at least `latitude` and `longitude` (see `SimpleLocation.kt`). The local-only `syncState` field is not stored in Supabase.

You can create the tables in the **Table Editor** or with the **SQL Editor**. The [models](app/src/main/java/com/example/sharist/data/model) are the source of truth for the column types.

#### SQL functions (RPC)

Open the **SQL Editor** and run each file in [`app/src/main/java/com/example/sharist/data/remote/rpc/`](app/src/main/java/com/example/sharist/data/remote/rpc):

- `FindAvailableRides.sql` (`get_rides_near`)
- `FindAvailableRideRequests.sql` (`get_ride_requests_near`)
- `SubscribeUser.sql` (`subscribe_to_ride`)
- `DeleteSubscription.sql` (`delete_subscription`)
- `AcceptRideRequest.sql` (`accept_ride_request`)

#### Storage buckets

In **Storage**, create three **public** buckets. The names must match exactly:

- `userPhoto`
- `vehicle`
- `locations`

#### Row Level Security

Supabase enables Row Level Security by default on new tables, and without policies the app will get empty results or permission errors. Add policies so that authenticated users can read the tables and insert/update/delete only their own rows, and can upload to the three buckets. For a quick local test you can disable RLS on the tables, but don't do that for anything public.

### 3. Get a Google Maps API key

1. In the [Google Cloud Console](https://console.cloud.google.com/), create a project (or reuse one).
2. Enable **Maps SDK for Android** and **Places API**.
3. Create an API key under **APIs & Services → Credentials**.
4. Recommended: restrict the key to Android apps using the package name `com.example.sharist` and the SHA-1 of your debug keystore.

To print your debug SHA-1:

```bash
# Windows (PowerShell)
keytool -list -v -keystore $env:USERPROFILE\.android\debug.keystore -alias androiddebugkey -storepass android -keypass android

# macOS / Linux
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

### 4. Create `local.properties`

In the project root (next to `settings.gradle.kts`), create or edit `local.properties`. Android Studio creates it with `sdk.dir` already set, so just add the three keys below. The file is listed in `.gitignore`, so your keys are never committed.

```properties
sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk

SUPABASE_URL=https://<your-project-ref>.supabase.co
SUPABASE_KEY=<your-supabase-anon-key>
MAPS_API_KEY=<your-google-maps-api-key>
```

| Variable | Used for | Where to find it |
| --- | --- | --- |
| `SUPABASE_URL` | Backend URL | Supabase → Project Settings → API |
| `SUPABASE_KEY` | Anon/publishable key for Auth, Postgrest and Storage | Supabase → Project Settings → API |
| `MAPS_API_KEY` | Google Maps and Places autocomplete | Google Cloud → Credentials |

The values are read in `app/build.gradle.kts` and exposed as `BuildConfig` fields (and as a manifest placeholder for the Maps key). Without the Supabase values the app has no backend, and without `MAPS_API_KEY` the map and place search won't work. After editing the file, sync Gradle and rebuild.

The IPMA weather API is public and needs no key.

### 5. Build and run

From Android Studio, pick an emulator or a device and press **Run**. Or from the command line:

```bash
# Windows
gradlew.bat assembleDebug

# macOS / Linux
./gradlew assembleDebug
```

The APK is generated in `app/build/outputs/apk/debug/`.

The app asks for location permission on first use. On an emulator, set a location in **Extended controls → Location**, ideally in Portugal so the IPMA weather data matches.

## Troubleshooting

- **Blank map**: `MAPS_API_KEY` is missing or wrong, the Maps SDK for Android isn't enabled, or the key restrictions don't match your package name and SHA-1.
- **"Supabase is not configured"**: `SUPABASE_URL` or `SUPABASE_KEY` is empty in `local.properties`. Sync Gradle again after editing.
- **Empty lists or permission errors after login**: check the Row Level Security policies and that the tables and buckets have the exact names above.
- **Photo upload fails**: check that the `userPhoto`, `vehicle` and `locations` buckets exist and allow uploads for authenticated users.

<h2>Credits</h2>

- Author: <a href="https://github.com/iribeirocampos" target="_blank">Iuri Campos</a>
- Co-author: <a href="https://github.com/T-MSD" target="_blank">T-MSD</a>
- Co-author: <a href="https://github.com/maeve04" target="_blank">maeve04</a>

<h2>Copyright</h2>
This project is licensed under the terms of the MIT license and protected by IST Honor Code and Community Code of Conduct.

<img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white"> <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white"> <img src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white"> <img src="https://img.shields.io/badge/Supabase-3ECF8E?style=for-the-badge&logo=supabase&logoColor=white">
