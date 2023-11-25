/*
 * Designed and developed by Ji Sungbin 2023.
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

@Composable
fun InvalidationProcessedParameterChangedRoot() {
  var count by remember { mutableIntStateOf(0) }
  Button(onClick = { count = 1 }) {}
  InvalidationProcessedParameterChangedChild(count)
}

@Composable
private fun InvalidationProcessedParameterChangedChild(count: Int) {
  Text(text = "$count")
}