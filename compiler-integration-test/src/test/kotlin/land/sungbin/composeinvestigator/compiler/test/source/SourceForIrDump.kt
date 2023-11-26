@file:Suppress("TestFunctionName", "ComposableNaming", "unused")

package land.sungbin.composeinvestigator.compiler.test.source

import androidx.compose.runtime.Composable
import land.sungbin.composeinvestigator.runtime.ComposableInvalidationEffect
import land.sungbin.composeinvestigator.runtime.ComposableInvalidationListener

@Composable
fun Main() {
  ComposableInvalidationEffect {
    ComposableInvalidationListener { _, _ -> }
  }
}