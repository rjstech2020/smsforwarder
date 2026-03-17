# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SMS Forwarder is a Kotlin Android application that forwards incoming SMS messages to other phone numbers based on configurable rules. Simplified to only support SMS-to-SMS forwarding.

## Build Commands

- `./gradlew assembleDebug` - Build debug APK
- `./gradlew assembleRelease` - Build release APK
- `./gradlew clean` - Clean build artifacts

## Architecture

### Core Components (all in Kotlin)

- **SmsReceiver** (`network/SmsReceiver.kt`) - BroadcastReceiver that listens for incoming SMS and applies forwarding rules
- **SMSRuleData** (`SMSRuleData.kt`) - Data model for forwarding rules (keyword, negative keyword, phone number)
- **SharedPrefStorage** (`support/SharedPrefStorage.kt`) - Persistent storage using SharedPreferences

### Forwarding

Only SMS-to-SMS forwarding is supported. Rules define:
- `keyword` - SMS must contain this to match
- `negKeyword` - If SMS contains this, rule is skipped (negative matching)
- `phoneNumber` - Destination phone number to forward to
- `stopProcessRule` - If true, stop processing remaining rules after match

### UI Structure (all in Kotlin)

- **MainActivity** - Main screen showing rules list with FAB to add new rules
- **RuleListFragment** - Displays list of forwarding rules
- **RuleAddEditDialog** - Dialog for adding/editing rules (bottom sheet)
- **SettingsFragment** - App settings (minimal)

### Permissions Required

- `RECEIVE_SMS` - Receive incoming SMS
- `SEND_SMS` - Forward SMS to another number
- `INTERNET` - Firebase analytics/crashlytics

## Key Files

- `app/build.gradle` - Dependencies and build configuration (Kotlin, compileSdk 34)
- `app/src/main/AndroidManifest.xml` - App components and permissions
- `app/src/main/java/com/rjs/smsforward/support/Constants.kt` - App constants
- `gradle/wrapper/gradle-wrapper.properties` - Gradle 8.4

## Build Configuration

- compileSdk: 34
- targetSdk: 34
- minSdk: 21
- Kotlin JVM target: 17
