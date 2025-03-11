> [!WARNING]
>
> ### This tool has many issues and is not recommended for use.
>
> I'm currently redeveloping this tool from the ground up. It will take a long time
> to reach a stable stage.
>
> Please see [rework](https://github.com/jisungbin/ComposeInvestigator/tree/rework) branch.

## ComposeInvestigator

Trace the recomposition of a Composable with its cause without boilerplate code 😎.

This tool was started for personal learning and has not been proven necessary for production.
However, it can perform the following tasks:

- **Reports if a Composable is skipped during recomposition.**
- **Reports if the arguments of a Composable have changed and been recomposed.** It can also compare the
  values before and after the change.
- **Reports if the state values inside a Composable have been modified.** It can also compare the values
  before and after the change. *(via ComposeInvestigator's runtime plugin)*

---

### Getting Started ![gradle-plugin-version](https://img.shields.io/maven-central/v/in.sungb.composeinvestigator/composeinvestigator-gradle-plugin?style=flat-square)

Just add the plugin to your module-level Gradle like this:

```groovy
plugins {
  id 'in.sungb.composeinvestigator' version '<version>'
}
```

> [!IMPORTANT]
>
> ComposeInvestigator is heavily dependent on the Kotlin version. So the version of 
> ComposeInvestigator follows the format `[Kotlin Version - ComposeInvestigator Version]`.

You don't need to use any APIs to get started. But if you're looking for a fancier experience,
consider using the runtime API.

Comprehensive documentation for ComposeInvestigator is available
on [project website](https://jisungbin.github.io/ComposeInvestigator).

### Preview

![image](https://github.com/jisungbin/ComposeInvestigator/assets/40740128/98991bd9-97f2-47a7-9cc9-6f9cd1cda0e3)

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
