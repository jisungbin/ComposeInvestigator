/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test

import land.sungbin.composeinvestigator.runtime.ComposableInformation
import land.sungbin.composeinvestigator.runtime.ComposableInvalidationLogger
import land.sungbin.composeinvestigator.runtime.ComposeInvestigatorConfig
import land.sungbin.composeinvestigator.runtime.InvalidationType

object TestConfiguration {
  val logs = mutableListOf<Pair<ComposableInformation, InvalidationType>>()

  init {
    ComposeInvestigatorConfig.logger = ComposableInvalidationLogger { composable, type ->
      logs += composable to type
      println("[${composable.name}] $type")
    }
  }

  fun reset() {
    logs.clear()
  }
}