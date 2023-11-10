# WIP 👻👻👻

## ComposeInvalidator

Composable's forced invalidation[^1] request and invalidation tree building tools.

[^1]: Commonly known as "recomposition".

This tool was started for personal learning and is not proven to be necessary in production, but it can do the following things:

- **Request to force invalidate a Composable:** Forces the Composable to be invalidated without handling `State`.
- **Request to force dispose a Composable:** Disposing of a Composable will initialize any items cached by `key`, such as `remember`, `LaunchedEffect`, and `DisposableEffect`. That is, it initializes the Composable and requests an initial-composition.
- **Record "real" re-composed Composable:** Simply adding logs to a Composable function and seeing them printed doesn't mean that recomposition has occurred; this feature only logs when it's actually recomposed. ("fake" and "real" recompositions will be discussed in more detail later.)

- **When the root Composable is recomposed, record the child Composables that are recomposed with it:** Help easily identify tangled Composables.

The first and second features can be useful for people who want to control the Composable lifecycle without being tied to a `State` or Compose Runtime.

---

### Usage

```kotlin
@[Composable Invalidable(tag = [String?, default = null], disposable = [Boolean, default = true]) RecompositionRecord(child = [Boolean, default = true])]
fun HelloWorld() = Unit

// without Invalidable.tag

ComposeInvalidator.invalidHelloWorld() // auto-generated function
ComposeInvalidator.disposeHelloWorld() // auto-generated function

// with Invalidable.tag

ComposeInvalidator.invalidHelloWorld{Tag}() // auto-generated function
ComposeInvalidator.disposeHelloWorld{Tag}() // auto-generated function

// with @RecompositionRecord, each time a Composable is recomposed, its name is printed.
```

### Under the hood

The core principle behind this tool is the Kotlin Compiler Plugin (K2).

The tool works in two steps.

1. IR modulation of the Composable function. However, this step is performed before the Compose Compiler.
2. Re-modulating the IR that the Compose Compiler has modulated. It intercepts the IRs that Compose Compiler changes and re-morphs them for this tool.

... TBD

#### "fake" and "real" recompositions

TBD

---

### Download

This tool is currently in the early stages of development and **is not yet released**.

```kotlin
plugins {
  id("land.sungbin.composeinvalidator") version "0.1.0-SNAPSHOT"
}
```
