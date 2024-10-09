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

## Getting Started  ![gradle-plugin-version](https://img.shields.io/maven-central/v/in.sungb.composeinvestigator/composeinvestigator-gradle-plugin?style=flat-square)

Just add the plugin to your module-level Gradle like this:

```groovy
plugins {
  id 'in.sungb.composeinvestigator' version '<version>'
}
```

You don't need to use any APIs to get started. But if you're looking for a fancier experience, 
consider using the runtime API.

If you want to learn about the runtime API, check out [advanced](advanced.md) page.

### License

```
Copyright 2024 Ji Sungbin

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```