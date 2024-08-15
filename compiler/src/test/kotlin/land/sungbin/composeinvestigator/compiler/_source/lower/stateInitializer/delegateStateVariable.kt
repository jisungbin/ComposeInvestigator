package land.sungbin.composeinvestigator.compiler._source.lower.stateInitializer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

fun delegateStateVariable() {
  val state by mutableStateOf(Unit)
  var state2 by run { mutableStateOf(Unit) }
}