# 3D Hover Animation Card

This Android project demonstrates how to create an interactive card with a 3D hover animation effect. The design is inspired by a student ID card and implements smooth 3D rotation effects when a user interacts with it.

## Features

- 3D tilt animation responding to touch position
- Realistic lighting and shadow effects
- Smooth transitions with custom interpolators
- Card design based on a student ID/bus pass template

## Requirements

- Android Studio 4.2 or higher
- Minimum SDK: API 21 (Android 5.0 Lollipop)
- Target SDK: API 33 (Android 13)
- Java 8 or higher

## Dependencies

```gradle
implementation 'androidx.cardview:cardview:1.0.0'
implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
```

## Implementation Details

The project uses ObjectAnimator to create smooth 3D rotation effects. Custom touch listeners track finger position relative to the card center, calculating appropriate rotation angles and applying perspective transformations.