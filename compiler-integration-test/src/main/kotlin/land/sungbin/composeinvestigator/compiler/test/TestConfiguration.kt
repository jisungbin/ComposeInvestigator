// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.test

import land.sungbin.composeinvestigator.runtime.ComposableInformation
import land.sungbin.composeinvestigator.runtime.ComposableInvalidationLogger
import land.sungbin.composeinvestigator.runtime.ComposeInvestigatorConfig
import land.sungbin.composeinvestigator.runtime.InvalidationResult

typealias Investigated = Pair<ComposableInformation, InvalidationResult>

object TestConfiguration {
  val logs: List<Investigated>
    field = mutableListOf()

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
