/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("unused", "UnusedVariable")

package land.sungbin.composeinvestigator.compiler._source.lower.stateInitializer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.reflect.KProperty

private operator fun <T> State<T>?.getValue(thisObj: Any?, property: KProperty<*>) = Unit
private operator fun <T> State<T>?.setValue(thisObj: Any?, property: KProperty<*>, value: T) {}

private object CustomState : State<Unit> {
  override val value get() = Unit
}

private fun delegateStateVariable() {
  val state by mutableStateOf(Unit)
  var state2 by run { CustomState }
}

private fun delegateNullableStateVariable() {
  val nullableState: State<Unit>? = CustomState
  val state by nullableState
  var state2 by run { nullableState }
}

@Composable private fun delegateStateVariableComposable() {
  val state by remember { CustomState }
  var state2 by remember { run { mutableStateOf(Unit) } }
}

@Composable private fun delegateNullableStateVariableComposable() {
  val nullableState: State<Unit>? = remember { mutableStateOf(Unit) }
  val state by remember { nullableState }
  var state2 by remember { run { nullableState } }
}
