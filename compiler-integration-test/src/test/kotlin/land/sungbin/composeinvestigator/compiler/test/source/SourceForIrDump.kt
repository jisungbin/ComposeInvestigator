/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.source

import androidx.compose.runtime.Composable
import land.sungbin.composeinvestigator.runtime.AffectedComposable
import land.sungbin.composeinvestigator.runtime.ComposeInvestigateLogType
import land.sungbin.composeinvestigator.runtime.ComposeInvestigateLogger

@ComposeInvestigateLogger
fun composeInvestigateLogger(composable: AffectedComposable, logType: ComposeInvestigateLogType) {
  when (logType) {
    is ComposeInvestigateLogType.InvalidationProcessed -> {
      println("<${composable.name} in ${composable.pkg}> InvalidationProcessed. DiffParams: ${logType.diffParams}")
    }
    is ComposeInvestigateLogType.InvalidationSkipped -> {
      println("<${composable.name} in ${composable.pkg}> InvalidationSkipped")
    }
  }
}

@Composable
fun entry(one: Int, two: String) {
  println(one)
  println(two)
  println(ComposeInvestigateLogType.InvalidationSkipped)
  println(ComposeInvestigateLogType.InvalidationProcessed(null))
}
