// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.test

import androidx.compose.runtime.Composable
import composemock.Container
import composemock.Text
import land.sungbin.composeinvestigator.runtime.ComposableInformation
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracer

val loopLayoutTable by lazy { currentComposableInvalidationTracer }

@Composable fun LoopLayout(value: Int = 0) {
  Container {
    repeat(5) { index ->
      if (index == 2)
        LoopText { value.plus(1).toString() }
      else if (index == 4)
        LoopText { value.plus(2).toString() }
      else
        LoopText { "0" }
    }
  }
}

@Composable fun LoopText(calucation: () -> String) {
  Text(calucation())
}

fun loopLayout() = ComposableInformation(
  name = "LoopLayout",
  packageName = "land.sungbin.composeinvestigator.compiler.test",
  fileName = "LoopLayout.kt",
)

fun loopText() = ComposableInformation(
  name = "LoopText",
  packageName = "land.sungbin.composeinvestigator.compiler.test",
  fileName = "LoopLayout.kt",
)
