# Change Log

ComposeInvestigator is heavily dependent on the version
of [Compose Compiler](https://developer.android.com/jetpack/androidx/releases/compose-compiler),
so the version of ComposeInvestigator follows the
format `[Compose Compiler Version - ComposeInvestigator Version]`.

In other words, you need to adjust the Compose Compiler version and Kotlin version to use
ComposeInvestigator.

It also depends slightly on the Compose Runtime and Compose Animation versions.
The last tested versions are both `1.6.3`.

---

## [Unreleased]

## [1.5.10-0.2.1] - 2024-03-12

- Fixed #136: Tracking `Animatable` no longer crashes.

## [1.5.10-0.2.0] - 2024-03-10

- Eliminated a potential memory leak.
- Increase Compose Runtime and Compose Animation versions to 1.6.3.
- Adds the `@NoInvestigation` API to suppress the workings of ComposeInvestigator.

### Breaking Changes

- Fixed #121: Now pass the callstack information to the `ComposableInvalidationListener`.

## [1.5.10-0.1.1] - 2024-03-06

- Fixed #123: Tracking `DerivedState` no longer crashes. Instead, due to technical limitations,
  `DerivedState` is not currently supported for tracking state changes.

## [1.5.10-0.1.0] - 2024-02-26

- Initial release.

[Unreleased]: https://github.com/jisungbin/ComposeInvestigator/compare/1.5.10-0.2.1...HEAD
[1.5.10-0.2.1]: https://github.com/jisungbin/ComposeInvestigator/releases/tag/1.5.10-0.2.1
[1.5.10-0.2.0]: https://github.com/jisungbin/ComposeInvestigator/releases/tag/1.5.10-0.2.0
[1.5.10-0.1.1]: https://github.com/jisungbin/ComposeInvestigator/releases/tag/1.5.10-0.1.1
[1.5.10-0.1.0]: https://github.com/jisungbin/ComposeInvestigator/releases/tag/1.5.10-0.1.0
