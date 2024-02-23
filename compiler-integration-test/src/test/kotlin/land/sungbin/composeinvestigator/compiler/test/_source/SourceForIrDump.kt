@file:Suppress("unused", "TestFunctionName")

/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test._source

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable

fun main() {
  println("Hello, world!")
}

@Composable
fun TestComposable() {
  BasicText(text = "Hello, world!")
}
