/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.source

import androidx.compose.runtime.Composable
import land.sungbin.composeinvestigator.runtime.ComposeInvestigateLogger
import land.sungbin.composeinvestigator.runtime.LogType

@ComposeInvestigateLogger
fun composeInvestigateLogger(composableName: String, logType: LogType) {
  when (logType) {
    is LogType.InvalidationProcessed -> {
      println("<$composableName> InvalidationProcessed. DiffParams: ${logType.diffParams}")
    }
    is LogType.InvalidationSkipped -> {
      println("<$composableName> InvalidationSkipped")
    }
  }
}

@Composable
fun entry(one: Int, two: String) {
  println(one)
  println(two)
  println(LogType.InvalidationSkipped)
  println(LogType.InvalidationProcessed(null))
}
