# COS30017 — Mobile Application Development

A collection of weekly practical exercises and assignment projects developed for the COS30017 Mobile Application Development course.

This repository organizes Android application development work by week and assignment, providing a centralized location for source code, project configuration, and ongoing development.

## Repository Structure

```text
Cos30017/
├── Week01/
│   └── First_application_Week1/
├── Week02/
├── Week03/
├── ASM1/
├── .gitignore
└── README.md
```

> **Note:** Each project folder may contain its own Android Studio project files and application source code.

## Projects and Practical Exercises

| Directory | Description |
|---|---|
| `Week01/` | First Android application and introductory development exercises. |
| `Week02/` | Android application development exercises for Week 2. |
| `Week03/` | Android application development exercises for Week 3. |
| `ASM1/` | Assignment 1 Android application project. |

## Technologies and Tools

- **Kotlin** — Application logic and Android development.
- **XML** — Android user interface layouts and resources.
- **Android Studio** — Integrated development environment.
- **Gradle** — Project configuration and build automation.
- **Git and GitHub** — Version control and source code management.

## Getting Started

### Prerequisites

Before opening a project, ensure that you have installed:

- Android Studio.
- A compatible Java Development Kit (JDK), as required by the project's Gradle configuration.
- The Android SDK components required by the project.

An Android emulator or a compatible Android device can be used to run the applications.

### Running a Project

1. Clone this repository:

   ```bash
   git clone https://github.com/locod1/Cos30017.git
   ```

2. Open the repository folder and navigate to the project you want to run.

3. In Android Studio, open the directory containing the project's `settings.gradle` or `settings.gradle.kts` file.

4. Allow Gradle to synchronize and download any required dependencies.

5. Select an available Android emulator or connected device.

6. Click **Run** to build and launch the application.

> Some project folders may contain nested directories. Always open the actual Android Studio project root rather than the outer organizational folder.

## Repository Organization Guidelines

To keep the repository maintainable and easy to navigate:

- Keep weekly exercises in their corresponding `WeekXX/` directories.
- Keep assignment projects separate from weekly practical exercises.
- Use consistent folder naming conventions.
- Include relevant source code, resources, and build configuration files.
- Exclude generated build outputs, local machine settings, and unnecessary IDE cache files.
- Update this README when new weekly exercises or assignments are added.

## Version Control

This repository uses Git for version control and GitHub for remote storage.

Typical commands for submitting changes:

```bash
git status
git add .
git commit -m "Describe your changes"
git push origin main
```

Before pushing, review the staged changes to ensure that generated files, local configuration files, and sensitive information are not included.

## Notes

- Each project may have its own dependencies and Android SDK requirements.
- Project setup may vary depending on the exercise or assignment.
- Refer to the relevant project directory for its implementation and configuration.

---

**Course:** COS30017 — Mobile Application Development  
**Repository:** [locod1/Cos30017](https://github.com/locod1/Cos30017)
