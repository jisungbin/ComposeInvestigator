/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.source.logger

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracer

val invalidationProcessedFileTable = currentComposableInvalidationTracer

@Composable
fun InvalidationProcessedRoot() {
  var state by remember { mutableIntStateOf(0) }
  Button(onClick = { state++ }) {}
  InvalidationProcessedChild(state)
}

@Composable
private fun InvalidationProcessedChild(count: Int) {
  Text(text = "$count")
}
