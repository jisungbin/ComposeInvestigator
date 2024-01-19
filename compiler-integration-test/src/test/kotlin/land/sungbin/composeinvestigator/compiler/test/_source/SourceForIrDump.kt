/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("TestFunctionName", "ComposableNaming", "unused")

package land.sungbin.composeinvestigator.compiler.test._source

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun One() {
  Button(onClick = {}) {
    Text(text = "Hi!")
  }
  Two(
    { Text(text = "One") },
    { Text(text = "Two") },
    { Text(text = "Three") },
    { Text(text = "Four") },
  )
}

@Composable
fun Two(vararg contents: @Composable () -> Unit) {
  contents.forEach { content ->
    Content(content = content)
  }
}

@Composable
fun Content(content: @Composable () -> Unit) {
  content()
}
