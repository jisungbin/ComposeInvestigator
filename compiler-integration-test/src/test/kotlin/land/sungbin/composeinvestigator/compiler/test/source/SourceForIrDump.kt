/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.source

import androidx.compose.runtime.Composable
import land.sungbin.composeinvestigator.runtime.ComposeInvestigateLogType
import land.sungbin.composeinvestigator.runtime.ComposeInvestigateLogger

@ComposeInvestigateLogger
fun composeInvestigateLogger(composableName: String, logType: ComposeInvestigateLogType) {
  when (logType) {
    is ComposeInvestigateLogType.InvalidationProcessed -> {
      println("<$composableName> InvalidationProcessed. DiffParams: ${logType.diffParams}")
    }
    is ComposeInvestigateLogType.InvalidationSkipped -> {
      println("<$composableName> InvalidationSkipped")
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
