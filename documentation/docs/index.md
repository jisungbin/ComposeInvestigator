# ComposeInvestigator ![gradle-plugin-version](https://img.shields.io/maven-central/v/land.sungbin.composeinvestigator/composeinvestigator-gradle-plugin?style=flat-square)

Trace the recomposition of a Composable with its cause without any boilerplate code 😎.

This tool was initiated for personal learning and has not been proven necessary for production.
However, it can perform the following tasks:

- **Reports if a Composable is skipped during recomposition.**
- **Reports if the arguments of a Composable have changed and been recomposed.** It can also compare
  the
  values before and after the change.
- **Reports if the state values inside a Composable have been modified.** It can also compare the
  values
  before and after the change.
- **Retrieves the call stack leading up to the invocation of a Composable.** This helps identify the
  specific Composable being recomposed when the same Composable is reused in multiple places.

![image](https://github.com/jisungbin/ComposeInvestigator/assets/40740128/98991bd9-97f2-47a7-9cc9-6f9cd1cda0e3)

---

### Getting Started

Just add a Gradle plugin like this one:

```kotlin
plugins {
  id("land.sungbin.composeinvestigator") version "<version>"
}
```

You don't need to use any APIs to get started. But if you're
looking for a fancier experience, consider using the runtime API.

If you want to learn about the runtime API, check out our [advanced](advanced.md) page.

### License

```
MIT License

Copyright (c) 2024 Ji Sungbin

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
