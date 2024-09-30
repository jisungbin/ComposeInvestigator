/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("unused")

package land.sungbin.composeinvestigator.compiler._source.frontend.traceTableApiUsage

import androidx.compose.runtime.Composable
import land.sungbin.composeinvestigator.runtime.NoInvestigation
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracer

private val t = currentComposableInvalidationTracer
private val a = Unit.run {
  t.currentComposableName
  t.currentComposableKeyName
}

@Composable @NoInvestigation private fun C() {}
@Composable @NoInvestigation private fun C2() {}
@Composable @NoInvestigation private fun C3() {}
