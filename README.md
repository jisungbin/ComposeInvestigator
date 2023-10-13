# ComposeInvalidator 🚀

**Composable's Forced Invalidation Request and Invalidation Tree Building Tools.**

ComposeInvalidator is a tool designed to help you control the Composable lifecycle without relying on State or the Compose Runtime. It offers a range of features, including:

1. Request to Force Invalidate a Composable: This forces the Composable to be invalidated without handling State.

2. Request to Force Dispose a Composable: Disposing of a Composable will initialize any items cached by key, such as remember, LaunchedEffect, and DisposableEffect. Essentially, it reinitializes the Composable and requests an initial composition.

3. Record "Real" Recomposed Composables: ComposeInvalidator logs when a Composable function is actually recomposed, helping you distinguish between "fake" and "real" recompositions.

4. When the root Composable is recomposed, it records the child Composables that are recomposed with it, making it easier to identify tangled Composables.

While this tool was initially created for personal learning, it can be a valuable addition to your toolkit.

## Usage 🧰

```kotlin
@[Composable Invalidable(tag = [String?, default = null], disposable = [Boolean, default = true]) RecompositionRecord(child = [Boolean, default = true])]
fun HelloWorld() = Unit
Without Invalidable.tag:

ComposeInvalidator.invalidHelloWorld() (auto-generated function)
ComposeInvalidator.disposeHelloWorld() (auto-generated function)
With Invalidable.tag:

ComposeInvalidator.invalidHelloWorld{Tag}() (auto-generated function)
ComposeInvalidator.disposeHelloWorld{Tag}() (auto-generated function)
With @RecompositionRecord, each time a Composable is recomposed, its name is printed.

Under the Hood 🛠️
ComposeInvalidator relies on the Kotlin Compiler Plugin (K2) and works in two main steps:

IR modulation of the Composable function (before the Compose Compiler).
Re-modulating the IR modulated by the Compose Compiler to intercept changes and make them compatible with this tool.
"Fake" and "Real" Recompositions 🔄
(To Be Discussed)

Download ⬇️
This tool is currently in the early stages of development and is not yet officially released.
plugins {
  id("land.sungbin.composeinvalidator") version "0.1.0-SNAPSHOT"
}
Currently, only androidx compose is supported.

Footnotes 📝
Commonly known as "recomposition." ↩

Explore the power of ComposeInvalidator and gain better control over your Composables! 🎉

This updated README incorporates emojis to add visual appeal and uses concise language to make the information more accessible and engaging for readers.

