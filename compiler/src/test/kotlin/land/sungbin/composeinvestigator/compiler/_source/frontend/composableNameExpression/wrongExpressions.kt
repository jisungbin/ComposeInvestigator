// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("unused")

package land.sungbin.composeinvestigator.compiler._source.frontend.composableNameExpression

import androidx.compose.runtime.Composable
import land.sungbin.composeinvestigator.runtime.ComposableName

@Composable private fun magicNumberToStringExpression() {
  ComposableName(42.toString())
}

@Composable private fun stringConcatenationExpression() {
  ComposableName("My" + "Composable")
}

@Composable private fun stringTemplateExpression() {
  ComposableName("My${"Composable"}")
}

@Composable private fun stringInstanceExpression() {
  ComposableName(String())
}
