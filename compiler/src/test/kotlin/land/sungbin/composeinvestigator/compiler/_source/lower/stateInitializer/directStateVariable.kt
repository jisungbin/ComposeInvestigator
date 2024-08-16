@file:Suppress("unused", "UnusedVariable")

package land.sungbin.composeinvestigator.compiler._source.lower.stateInitializer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable fun directStateVariable() {
  val state = remember { mutableStateOf(Unit) }
  val state2 = remember { run { mutableStateOf(Unit) } }
}
