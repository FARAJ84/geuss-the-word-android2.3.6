# Guess the Word - Android Game

A Wordle-style guessing game for Android 2.3.6 (API Level 10) - compatible with Samsung GT-S5360 Galaxy Y.

## Features

- **Category Selection**: Choose from 13 categories (All, Name, Game, Artist, Music, Item, Animal, Food, Place, Brand, Movie, Sport, Color)
- **Word Length**: Configurable 3-7 letters, supports multi-part words (e.g., "VAN GOGH", "NEW YORK")
- **5 Attempts**: Classic Wordle gameplay with 5 chances to guess
- **Color Coding**:
  - 🟢 Green = Correct letter, correct position
  - 🟡 Yellow = Correct letter, wrong position
  - 🔴 Red = Letter not in word
- **On-Screen Keyboard**: QWERTY layout with color-coded keys showing letter status
- **Physical Keyboard Support**: Works with phone keyboard input
- **Large Database**: 200+ built-in words across all categories
- **Export/Import**: Save/load words and categories to/from JSON files on SD card
- **Custom Words**: Add your own words and categories
- **Settings**: Configure word length, multi-part words, sound, vibration

## Project Structure

```
GuessTheWord/
├── AndroidManifest.xml
├── project.properties
├── ant.properties
├── proguard-project.txt
├── README.md
├── assets/
├── res/
│   ├── drawable/
│   │   ├── ic_launcher.xml
│   │   ├── key_background_*.xml
│   │   └── tile_background_*.xml
│   ├── layout/
│   │   ├── activity_main_menu.xml
│   │   ├── activity_category_select.xml
│   │   ├── activity_game.xml
│   │   ├── activity_settings.xml
│   │   ├── activity_import_export.xml
│   │   ├── list_item_category.xml
│   │   └── tile_letter.xml
│   ├── values/
│   │   ├── strings.xml
│   │   └── colors.xml
│   ├── menu/
│   └── xml/
└── src/com/guesstheword/
    ├── MainMenuActivity.java
    ├── CategorySelectActivity.java
    ├── GameActivity.java
    ├── SettingsActivity.java
    ├── ImportExportActivity.java
    ├── CategoryAdapter.java
    ├── DatabaseHelper.java
    ├── Word.java
    ├── Category.java
    └── LetterState.java
```

## Building the Project

### Using Eclipse (ADT) - Recommended for Android 2.3.6

1. Open Eclipse with ADT plugin
2. File → Import → Existing Android Code Into Workspace
3. Select the `GuessTheWord` folder
4. Build and run on device/emulator

### Using Ant (Command Line)

```bash
cd GuessTheWord
ant debug
```

The APK will be in `bin/GuessTheWord-debug.apk`

### Using Android Studio (Not recommended for API 10)

Android Studio dropped support for API 10. If needed:
1. Create new project with minSdkVersion 10
2. Copy src/res/assets to new project
3. Update build.gradle for legacy support

## Installation on GT-S5360 Galaxy Y

1. Enable "Unknown sources" in Settings → Security
2. Transfer APK to phone (Bluetooth, USB, SD card)
3. Install via file manager
4. Grant storage permission when prompted for import/export

## Gameplay

1. **Main Menu**: Start new game, select categories, import/export, settings, help
2. **Category Select**: Check multiple categories, add custom ones, start game
3. **Game Screen**: 
   - 5 rows of letter tiles (one per attempt)
   - On-screen QWERTY keyboard
   - Enter guess using screen keyboard or phone keyboard
   - Press Enter to submit, Del to backspace
   - Tiles color after each guess
   - Keyboard keys update to show letter status
4. **Win/Lose**: Dialog shows result with option to play again or return to menu

## Import/Export Format

### Words Export (JSON)
```json
{
  "category_id": 2,
  "export_date": 1699999999999,
  "words": [
    {"text": "CHESS", "multipart": false},
    {"text": "VAN GOGH", "multipart": true}
  ]
}
```

### Categories Export (JSON)
```json
{
  "export_date": 1699999999999,
  "categories": [
    {"name": "Custom Category", "builtin": false}
  ]
}
```

### Full Export (JSON)
```json
{
  "export_date": 1699999999999,
  "categories": [...],
  "words": [
    {"text": "CHESS", "category": "Game", "multipart": false}
  ]
}
```

## Default Categories & Words

- **All**: All words combined
- **Name**: JOHN, MARY, ALICE, CHRIS, PETER, etc.
- **Game**: CHESS, POKER, MARIO, ZELDA, TETRIS, etc.
- **Artist**: PICASSO, MONET, VAN GOGH, DA VINCI, etc.
- **Music**: ROCK, JAZZ, BLUES, POP, REGGAE, CLASSICAL, etc.
- **Item**: BOOK, PEN, PHONE, TABLE, CHAIR, LAMP, etc.
- **Animal**: CAT, DOG, LION, ELEPHANT, GIRAFFE, etc.
- **Food**: PIE, CAKE, APPLE, BANANA, ORANGE, etc.
- **Place**: HOME, PARK, PARIS, LONDON, TOKYO, etc.
- **Brand**: NIKE, APPLE, GOOGLE, TESLA, BMW, SONY, etc.
- **Movie**: STAR WARS, AVATAR, TITANIC, MATRIX, etc.
- **Sport**: SOCCER, TENNIS, GOLF, FOOTBALL, etc.
- **Color**: RED, BLUE, GREEN, YELLOW, PURPLE, etc.

## Requirements

- Android 2.3.3+ (API 10)
- SD card for import/export
- Screen: 240x320 or higher (QVGA supported)

## Permissions

- `WRITE_EXTERNAL_STORAGE` - Export files to SD card
- `READ_EXTERNAL_STORAGE` - Import files from SD card

## License

Free to use and modify.