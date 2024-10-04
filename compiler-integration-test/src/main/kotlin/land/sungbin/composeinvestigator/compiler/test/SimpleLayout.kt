/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mock.Linear
import androidx.compose.runtime.mock.Text
import land.sungbin.composeinvestigator.runtime.ComposableInformation
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracer

val simpleLayoutTable by lazy { currentComposableInvalidationTracer }

@Composable fun SimpleLayout(value: Int = 0) {
  Linear {
    LambdaText(value::toString)
  }
}

@Composable fun LambdaText(calucation: () -> String) {
  Text(calucation())
}

fun simpleLayout() = ComposableInformation(
  name = "SimpleLayout",
  packageName = "land.sungbin.composeinvestigator.compiler.test",
  fileName = "SimpleLayout.kt",
)

fun lambdaText() = ComposableInformation(
  name = "LambdaText",
  packageName = "land.sungbin.composeinvestigator.compiler.test",
  fileName = "SimpleLayout.kt",
)
