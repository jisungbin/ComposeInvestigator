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
fun InvalidationProcessedRoot_StateDelegateReference() {
  var count by remember { mutableIntStateOf(0) }
  Button(onClick = { count = 1 }) {}
  InvalidationProcessedChild_StateDelegateReference(count)
}

@Composable
private fun InvalidationProcessedChild_StateDelegateReference(count: Int) {
  Text(text = "$count")
}

@Composable
fun InvalidationProcessedRoot_StateDirectReference() {
  val count = remember { mutableIntStateOf(0) }
  Button(onClick = { count.intValue = 1 }) {}
  InvalidationProcessedChild_StateDirectReference(count.intValue)
}

@Composable
private fun InvalidationProcessedChild_StateDirectReference(count: Int) {
  Text(text = "$count")
}
