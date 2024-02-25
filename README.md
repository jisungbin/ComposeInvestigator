## ComposeInvestigator ![gradle-plugin-version](https://img.shields.io/maven-central/v/land.sungbin.composeinvestigator/composeinvestigator-gradle-plugin?style=flat-square)

Trace the recomposition of a Composable with its cause without any boilerplate code 😎.

This tool was initiated for personal learning and has not been proven necessary for production.
However, it can perform the following tasks:

- **Reports if a Composable is skipped during recomposition.**
- **Reports if the arguments of a Composable have changed and been recomposed.** It can also compare the
  values before and after the change.
- **Reports if the state values inside a Composable have been modified.** It can also compare the values
  before and after the change.
- **Retrieves the call stack leading up to the invocation of a Composable.** This helps identify the
  specific Composable being recomposed when the same Composable is reused in multiple places.

---

### Getting Started 

Just add a Gradle plugin like this one:

```kotlin
plugins {
  id("land.sungbin.composeinvestigator") version "<version>"
}
```

You don't need to use any APIs to get started. But if you're looking for a fancier experience,
consider using the runtime API.

Comprehensive documentation for ComposeInvestigator is available
on [project website](https://jisungbin.github.io/ComposeInvestigator).

### Preview

![image](https://github.com/jisungbin/ComposeInvestigator/assets/40740128/98991bd9-97f2-47a7-9cc9-6f9cd1cda0e3)
