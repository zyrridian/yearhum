# Yearhum

Yearhum is an offline-first Android application that lets users travel to any year between 1990 and 2025 to explore the era's music, video games, movies, television, and cultural milestones.

The application operates without a dedicated backend or user accounts. Browsing is instantaneous and fully functional offline using a pre-compiled SQLite database bundled in application assets, supplemented with lazy network enrichment for high-resolution artwork and release metadata.

---

## Architecture & Design Decisions

Unidirectional data flow with Room as the single source of truth:

```
Compose UI  <--- UiState ---  ViewModel  <---  Repository  <--- Room (SSOT)
                                                     |
                                            Lazy Enrichment Cache
                                                     |
                                          MusicBrainz / Cover Art API
```

* **Offline-First by Design:** All primary capsule data (songs, number-one albums, curated games, films, television series, and events) resides in `app/src/main/assets/databases/capsules.db`. No network request is triggered during year-to-year paging.
* **Separation of Curation and Enrichment:** MusicBrainz does not track chart popularity or cultural significance. Popularity rankings are curated ahead of time through Wikipedia and Wikidata; MusicBrainz and Cover Art Archive are queried lazily solely to resolve cover artwork and first-release dates.
* **Network Throttling:** Remote queries are routed through a token-bucket rate limiter enforcing MusicBrainz API guidelines (1 request per second with an explicit user agent).
* **Navigation:** Built with AndroidX Navigation 3, maintaining state across backstack transitions without XML navigation graphs.
* **Per-Decade Theming:** Material 3 color schemes dynamically adapt based on the decade being viewed (e.g., 1980s neon, 1990s grunge palettes) while preserving system dark mode contrast.

---

## Features

* **Year-by-Year Capsules:** Browse any year between 1990 and 2025 with instant horizontal pagination. Each capsule aggregates top Billboard singles, number-one albums, notable video game releases, box office films, television premieres, and historical moments.
* **Your Life in Music:** Generates a personalized milestone timeline based on a birth year, matching key life stages (childhood, adolescence, early adulthood) to the cultural context of those years.
* **Per-Decade Visual Theming:** Dynamic color schemes adapted to the aesthetics of each era (1970s warm tones, 1980s neon, 1990s grunge, 2000s chrome) while respecting system dark mode and contrast standards.
* **External Media Integration:** Outbound search intents to YouTube, YouTube Music, and Spotify for music, with trailer and Wikipedia lookups for films, television, and games. No audio or media is hosted or streamed internally.
* **Favorites & Local Preferences:** Bookmark individual items to a dedicated local collection; adjust birth year, preferred country lens, theme modes, and manage cached metadata via DataStore.

---

## Project Structure

The repository is organized into a single multi-module Gradle project:

```
Yearhum/
├── app/                        # Android application module
│   ├── schemas/                # Exported Room schemas (version-tracked)
│   └── src/main/
│       ├── assets/databases/   # Pre-populated capsules.db
│       └── java/.../yearhum/
│           ├── core/           # Common models, DataStore, design system, network
│           ├── data/           # Room database, DAOs, entities, remote clients, repositories
│           ├── domain/         # Domain models, repository contracts, use cases
│           ├── feature/        # Feature screens (timeline, detail, life timeline, favorites, settings)
│           ├── navigation/     # Navigation 3 route definitions and displays
│           └── ui/theme/       # Base Material 3 and decade themes
└── tools/
    └── dataset-builder/        # Kotlin/JVM CLI utility to generate capsules.db
        └── src/main/
            ├── kotlin/builder/ # Wikipedia scrapers, MusicBrainz resolver, SQLite asset writer
            └── resources/      # Hand-curated non-music dataset (curated.tsv)
```

---

## Getting Started

### Prerequisites

* Android Studio Ladybug (2024.2.1) or newer
* JDK 17
* Android SDK 37 (compileSdk 37, minSdk 26)

### 1. Generating or Updating the Database

The application requires `app/src/main/assets/databases/capsules.db` to launch. To build the asset database from source:

```bash
# Build capsules for 1990 through 2025 with MusicBrainz resolution
./gradlew :tools:dataset-builder:run --args="--from 1990 --to 2025"

# Fast dry-run without network calls (generates placeholder artwork entries)
./gradlew :tools:dataset-builder:run --args="--from 1990 --to 2025 --skip-mb"
```

The output SQLite file is validated against `app/schemas/.../1.json` and written directly to the application asset directory.

### 2. Running Unit Tests

```bash
# Dataset builder tests (validates categories, rank uniqueness, TSV integrity)
./gradlew :tools:dataset-builder:test

# Android application unit tests (use cases, repositories, viewmodels, theme logic)
./gradlew :app:testDebugUnitTest
```

### 3. Assembling the Application

```bash
./gradlew :app:assembleDebug
```

> **Note:** When updating `capsules.db` during development, uninstall any existing debug build from your test device (`adb uninstall com.example.yearhum`) or clear application storage so Room re-initializes from the updated asset database.

---

## Data Sources & Attributions

* **Chart Rankings:** Wikipedia year-end lists of Billboard rankings, licensed under [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/).
* **Release Metadata:** [MusicBrainz](https://musicbrainz.org/), providing core metadata dedicated to the public domain under [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
* **Cover Artwork:** [Cover Art Archive](https://coverartarchive.org/), a joint project by the Internet Archive and MusicBrainz. Artwork remains the property of respective copyright holders.
* **Games, Film, TV & Events:** Hand-curated editorial dataset compiled from public release records and Wikimedia projects.
* **Streaming & Media Links:** Yearhum does not host, scrape, or stream audio. All playback links dispatch external intents to YouTube, YouTube Music, or Spotify.
