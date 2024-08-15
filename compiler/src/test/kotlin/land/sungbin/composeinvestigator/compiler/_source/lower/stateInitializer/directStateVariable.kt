package land.sungbin.composeinvestigator.compiler._source.lower.stateInitializer

import androidx.compose.runtime.mutableStateOf

fun directStateVariable() {
  val state = mutableStateOf(Unit)
  val state2 = run { mutableStateOf(Unit) }
}
