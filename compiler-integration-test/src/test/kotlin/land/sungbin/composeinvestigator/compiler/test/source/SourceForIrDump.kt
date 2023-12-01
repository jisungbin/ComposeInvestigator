@file:Suppress("TestFunctionName", "ComposableNaming", "unused")

package land.sungbin.composeinvestigator.compiler.test.source

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
  InvalidationProcessedChild(count)
}

@Composable
fun InvalidationProcessedRoot_StateDirectReference() {
  val count = remember { mutableIntStateOf(0) }
  Button(onClick = { count.intValue = 1 }) {}
  InvalidationProcessedChild(count.intValue)
}

@Composable
private fun InvalidationProcessedChild(count: Int) {
  Text(text = "$count")
}
