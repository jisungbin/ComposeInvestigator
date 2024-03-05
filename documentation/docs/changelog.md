# Change Log

ComposeInvestigator is heavily dependent on the version
of [Compose Compiler](https://developer.android.com/jetpack/androidx/releases/compose-compiler),
so the version of ComposeInvestigator follows the
format `[Compose Compiler Version - ComposeInvestigator Version]`.

In other words, you need to adjust the Compose Compiler version and Kotlin version to use
ComposeInvestigator.

---

### Unreleased

- Fixed #123: Tracking `DerivedState` no longer crashes. Instead, due to technical limitations,
  `DerivedState` is not currently supported for tracking state changes.

### 1.5.10-0.1.0

- Initial release.
