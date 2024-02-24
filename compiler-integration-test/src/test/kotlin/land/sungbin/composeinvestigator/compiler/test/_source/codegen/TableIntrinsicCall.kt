/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("TestFunctionName")

package land.sungbin.composeinvestigator.compiler.test._source.codegen

import androidx.compose.runtime.Composable
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracker

@Composable
@Suppress("unused")
private fun Variable() {
  val table = currentComposableInvalidationTracker

  println("Current Composable name is \"${table.currentComposableName.name}\".")
  println("Current Composable keyName is '${table.currentComposableKeyName}'.")
}

@Composable
@Suppress("unused")
private fun Direct() {
  currentComposableInvalidationTracker

  println("Current Composable name is \"${currentComposableInvalidationTracker.currentComposableName.name}\".")
  println("Current Composable keyName is '${currentComposableInvalidationTracker.currentComposableKeyName}'.")
}
