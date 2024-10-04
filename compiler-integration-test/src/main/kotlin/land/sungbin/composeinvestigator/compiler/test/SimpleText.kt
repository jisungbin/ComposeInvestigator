/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mock.Text
import land.sungbin.composeinvestigator.runtime.ComposableInformation
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracer

val simpleTextTable by lazy { currentComposableInvalidationTracer }

@Composable fun SimpleText(value: String = "") {
  Text(value)
}

fun simpleText() = ComposableInformation(
  name = "SimpleText",
  packageName = "land.sungbin.composeinvestigator.compiler.test",
  fileName = "SimpleText.kt",
)
