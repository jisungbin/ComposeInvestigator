# ComposeInvestigator

Trace the recomposition[^1] of a Composable with its cause without any boilerplate code 😎.

[^1]: A recomposition can also be called an "invalidation".

This tool was initiated for personal learning and has not been proven necessary for production.
However, it can perform the following tasks:

- **Reports if a Composable is skipped during recomposition.**
- **Reports if the arguments of a Composable have changed and been recomposed.** It can also compare
  the values before and after the change.
- **Reports if the state values inside a Composable have been modified.** It can also compare the
  values before and after the change.
- **Retrieves the call stack leading up to the invocation of a Composable.** This helps identify the
  specific Composable being recomposed when the same Composable is reused in multiple places.

![preview](https://github.com/jisungbin/ComposeInvestigator/assets/40740128/98991bd9-97f2-47a7-9cc9-6f9cd1cda0e3)

---

## Getting Started  ![gradle-plugin-version](https://img.shields.io/maven-central/v/land.sungbin.composeinvestigator/composeinvestigator-gradle-plugin?style=flat-square)

Just add the plugin to your module-level Gradle like this:

```groovy
plugins {
  id 'land.sungbin.composeinvestigator' version '<version>'
}
```

Snapshots of the development version are available in Sonatype's snapshots repository.
Snapshot versions are released only occasionally when pre-validation is needed to resolve issues.

```groovy
repositories {
  // ...
  maven {
    url 'https://s01.oss.sonatype.org/content/repositories/snapshots/'
  }
}
```

You don't need to use any APIs to get started. But if you're
looking for a fancier experience, consider using the runtime API.

If you want to learn about the runtime API, check out our [advanced](advanced.md) page.

## Caveats

- Currently, only Restartable Groups with all stable arguments are supported. In simple terms, most
  composables are supported, but some special case composables are not. (#99, #133)

## License

ComposeInvestigator is available under
the [MIT license](https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE).
