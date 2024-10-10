// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.test

import androidx.compose.runtime.Composable
import composemock.Container
import composemock.Text
import land.sungbin.composeinvestigator.runtime.ComposableInformation
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracer

val simpleLayoutTable by lazy { currentComposableInvalidationTracer }

@Composable fun SimpleLayout(value: Int = 0) {
  Container("SimpleLayout") {
    NumberText(value)
  }
}

@Composable fun NumberText(value: Int) {
  Text(value.toString())
}

fun simpleLayout() = ComposableInformation(
  name = "SimpleLayout",
  packageName = "land.sungbin.composeinvestigator.compiler.test",
  fileName = "SimpleLayout.kt",
)

fun numberText() = ComposableInformation(
  name = "NumberText",
  packageName = "land.sungbin.composeinvestigator.compiler.test",
  fileName = "SimpleLayout.kt",
)
