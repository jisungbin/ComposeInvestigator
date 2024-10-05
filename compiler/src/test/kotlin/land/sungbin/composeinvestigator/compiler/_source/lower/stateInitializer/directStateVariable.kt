// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("unused", "UnusedVariable")

package land.sungbin.composeinvestigator.compiler._source.lower.stateInitializer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

private object CustomState2 : State<Unit> {
  override val value get() = Unit
}

private fun delegateStateVariable() {
  val state = mutableStateOf(Unit)
  var state2 = run { CustomState2 }
}

private fun delegateNullableStateVariable() {
  val nullableState: State<Unit>? = CustomState2
  val state = nullableState
  var state2 = run { nullableState }
}

@Composable private fun delegateStateVariableComposable() {
  val state = remember { CustomState2 }
  var state2 = remember { run { mutableStateOf(Unit) } }
}

@Composable private fun delegateNullableStateVariableComposable() {
  val nullableState: State<Unit>? = remember { mutableStateOf(Unit) }
  val state = remember { nullableState }
  var state2 = remember { run { nullableState } }
}
