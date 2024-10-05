// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.test

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mock.Text
import land.sungbin.composeinvestigator.runtime.ComposableInformation
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracer

val simpleTextTable by lazy { currentComposableInvalidationTracer }

@Composable fun SimpleText(value: String = "") {
  Text(value)
}

fun simpleText() = ComposableInformation(
  name = "SimpleText",
  packageName = "land.sungbin.composeinvestigator.compiler.test",
  fileName = "SimpleText.kt",
)
