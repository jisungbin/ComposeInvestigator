/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("unused")

package land.sungbin.composeinvestigator.compiler.test.source

import androidx.compose.runtime.Composable
import land.sungbin.composeinvestigator.runtime.AffectedComposable
import land.sungbin.composeinvestigator.runtime.ComposableInvalidateType
import land.sungbin.composeinvestigator.runtime.ComposeInvestigateLogger

@ComposeInvestigateLogger
fun composeInvestigateLogger(composable: AffectedComposable, type: ComposableInvalidateType) {
  when (type) {
    is ComposableInvalidateType.Processed -> {
      println("<$composable> InvalidationProcessed. DiffParams: ${type.diffParams}")
    }
    is ComposableInvalidateType.Skipped -> {
      println("<${composable}> InvalidationSkipped")
    }
  }
}

@Composable
fun entry(one: Int, two: String) {
  println(one)
  println(two)
}
