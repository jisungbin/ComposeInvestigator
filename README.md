## ComposeInvestigator

Trace the recomposition of a Composable with its cause without any boilerplate code 😎.

This tool was initiated for personal learning and has not been proven necessary for production. However, it can perform the following tasks:

- Reports if a Composable is skipped during recomposition.
- Reports if the arguments of a Composable have changed and been recomposed. It can also compare the values before and after the change.
- Reports if the state values inside a Composable have been modified. It can also compare the values before and after the change.
- Retrieves the call stack leading up to the invocation of a Composable. This helps identify the specific Composable being recomposed when the same Composable is reused in multiple places.

---

### Usage

ComposeInvestigator has various runtime APIs, but they are not yet documented. If you want to try it out in advance, you can understand the APIs by checking the runtime test code.

The overall usage will be documented before the stable version is released. In general, it works fine without using any APIs. Just run the app and check the logcat.

### Under the Hood

ComposeInvestigator is developed using a Kotlin compiler plugin. Detailed information will be documented before the stable version is released.

---

### Download

ComposeInvestigator is currently in the technical preview stage, and many tests are missing. Therefore, all usage should not take place in production, and unexpected issues may arise.

If you encounter bugs during use or have new feature suggestions, please report them as issues.

```kotlin
plugins {
  id("land.sungbin.composeinvestigator") version "0.1.0-dev"
}
```

### Preview

![image](https://github.com/jisungbin/ComposeInvestigator/assets/40740128/98991bd9-97f2-47a7-9cc9-6f9cd1cda0e3)
