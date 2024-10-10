# Change Log

ComposeInvestigator is heavily dependent on the Kotlin version. So the version of 
ComposeInvestigator follows the format `[Kotlin Version - ComposeInvestigator Version]`.

---

## [Unreleased]

## [2.1.0-Beta1-1.0.0-beta01] (unreleased)

- #172: Supports K2, with the entire logic rewritten. Some runtime features have
  been removed, and no new features have been added. 

### Artifact Address Update

The artifact address has been changed as follows:
 
- Previous: `land.sungbin.composeinvestigator`
- Updated: `in.sungb.composeinvestigator`

The domain sungbin.land has expired, and I no longer own `land.sungbin`. For future
stability and maintenance, the artifact address has been changed to `in.sungb`, 
which is under my ownership. The package name remains unchanged.

## [1.5.11-0.2.1] - 2024-03-23

- No API changes.

### Dependency updates

- `Kotlin`: `1.9.22` -> `1.9.23`
- `Compose Compiler`: `1.5.10` -> `1.5.11` 
- `Compose Runtime`: `1.6.3` -> `1.6.4`

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

[Unreleased]: https://github.com/jisungbin/ComposeInvestigator/compare/2.1.0-Beta1-1.0.0-beta01...HEAD
[2.1.0-Beta1-1.0.0-beta01]: https://github.com/jisungbin/ComposeInvestigator/releases/tag/2.1.0-Beta1-1.0.0-beta01 
[1.5.11-0.2.1]: https://github.com/jisungbin/ComposeInvestigator/releases/tag/1.5.11-0.2.1 
[1.5.10-0.2.1]: https://github.com/jisungbin/ComposeInvestigator/releases/tag/1.5.10-0.2.1
[1.5.10-0.2.0]: https://github.com/jisungbin/ComposeInvestigator/releases/tag/1.5.10-0.2.0
[1.5.10-0.1.1]: https://github.com/jisungbin/ComposeInvestigator/releases/tag/1.5.10-0.1.1
[1.5.10-0.1.0]: https://github.com/jisungbin/ComposeInvestigator/releases/tag/1.5.10-0.1.0
