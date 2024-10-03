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

val basicLayoutTable by lazy { currentComposableInvalidationTracer }

@Composable fun BasicLayout() {
  Text("")
}

fun basicLayout() = ComposableInformation(
  name = "BasicLayout",
  packageName = "land.sungbin.composeinvestigator.compiler.test",
  fileName = "BasicLayout.kt"
)
