// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
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
