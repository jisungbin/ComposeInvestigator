## ComposeInvestigator

Trace the recomposition of a Composable with its cause without boilerplate code 😎.

This tool was initiated for personal learning and has not been proven necessary for production.
However, it can perform the following tasks:

- **Reports if a Composable is skipped during recomposition.**
- **Reports if the arguments of a Composable have changed and been recomposed.** It can also compare the
  values before and after the change.
- **Reports if the state values inside a Composable have been modified.** It can also compare the values
  before and after the change.
- **Retrieves the call stack leading up to the invocation of a Composable.** This helps identify the
  specific Composable being recomposed when the same Composable is reused in multiple places.

[*(Read the Medium's introductory post)*](https://jisungbin.medium.com/tracing-recompositions-without-boilerplate-code-e9800db1419e)

*Work is currently underway to adapt K2 and rewrite core logic: [#172](https://github.com/jisungbin/ComposeInvestigator/pull/172)*

---

### Getting Started ![gradle-plugin-version](https://img.shields.io/maven-central/v/land.sungbin.composeinvestigator/composeinvestigator-gradle-plugin?style=flat-square)

Just add the plugin to your module-level Gradle like this:

```groovy
plugins {
  id 'land.sungbin.composeinvestigator' version '<version>'
}
```

> [!IMPORTANT]
>
> ComposeInvestigator is heavily dependent on the version
> of [Compose Compiler](https://developer.android.com/jetpack/androidx/releases/compose-compiler),
> so the version of ComposeInvestigator follows the format `[Compose Compiler Version - ComposeInvestigator Version]`.
>
> In other words, you need to adjust the Compose Compiler version and Kotlin version to use ComposeInvestigator.
>
> It also depends slightly on the Compose Runtime and Compose Animation versions. 
> The last tested versions are both `1.6.3`.

Snapshots of the development version are available in Sonatype's snapshots repository.

```groovy
repositories {
  // ...
  maven {
    url 'https://s01.oss.sonatype.org/content/repositories/snapshots/'
  }
}
```

You don't need to use any APIs to get started. But if you're looking for a fancier experience,
consider using the runtime API.

Comprehensive documentation for ComposeInvestigator is available
on [project website](https://jisungbin.github.io/ComposeInvestigator).

### Known issues

- [#155](https://github.com/jisungbin/ComposeInvestigator/issues/155): Using the R8 build causes a crash.

### Preview

![image](https://github.com/jisungbin/ComposeInvestigator/assets/40740128/98991bd9-97f2-47a7-9cc9-6f9cd1cda0e3)
