# How to contribute to BrushKit

## Pull requests are always welcome

We always appreciate your ideas and feedback. Please feel free to submit any pull requests.

* [Create a new issue](https://github.com/line/BrushKit/issues) to ask questions,
  report bugs or propose new features and improvements.
* [Send a pull request](https://github.com/line/BrushKit/pulls) to contribute
  your work.

## Coding conventions

We generally follow 2 major Kotlin Coding Style Guide:

- [Official Kotlin Style Guide](https://kotlinlang.org/docs/reference/coding-conventions.html)
- [Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)

## Coding Style checking tools

[ktlint](https://ktlint.github.io/) is Kotlin linter which checks if code is following to official
Kotlin Style Guide. We can run ktlint from Gradle.

### How to run style checking/formating

Run the following command to check code style by ktlint.

```
./gradlew ktlintCheck
```

Run the following command to run formatter by ktlint.

```
./gradlew ktlintFormat
```
