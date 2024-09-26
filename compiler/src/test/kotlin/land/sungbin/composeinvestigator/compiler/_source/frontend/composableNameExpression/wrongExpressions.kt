/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

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
