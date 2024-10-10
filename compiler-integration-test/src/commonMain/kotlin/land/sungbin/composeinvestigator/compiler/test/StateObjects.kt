// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.test

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracer

val stateObjectsTable by lazy { currentComposableInvalidationTracer }

@Composable fun DirectStateObjects() {
  val myUnitState = mutableStateOf(Unit)
  val myIntState = mutableIntStateOf(0)
  val myFloatState = mutableFloatStateOf(0f)
  val myLongState = mutableLongStateOf(0L)
  val myDoubleState = mutableDoubleStateOf(0.0)
  val myMapState = mutableStateMapOf(0 to 0)
  val myListState = mutableStateListOf(0)

  assertAll {
    assertThat(stateObjectsTable.findStateObjectName(myUnitState)).isEqualTo("myUnitState")
    assertThat(stateObjectsTable.findStateObjectName(myIntState)).isEqualTo("myIntState")
    assertThat(stateObjectsTable.findStateObjectName(myFloatState)).isEqualTo("myFloatState")
    assertThat(stateObjectsTable.findStateObjectName(myLongState)).isEqualTo("myLongState")
    assertThat(stateObjectsTable.findStateObjectName(myDoubleState)).isEqualTo("myDoubleState")
    assertThat(stateObjectsTable.findStateObjectName(myMapState)).isEqualTo("myMapState")
    assertThat(stateObjectsTable.findStateObjectName(myListState)).isEqualTo("myListState")
  }
}

@Suppress("UnusedVariable", "unused")
@Composable fun DelegateStateObjects() {
  val objects = mutableListOf<Any>()

  val myUnitState by mutableStateOf(Unit).also(objects::add)
  val myIntState by mutableIntStateOf(0).also(objects::add)
  val myFloatState by mutableFloatStateOf(0f).also(objects::add)
  val myLongState by mutableLongStateOf(0L).also(objects::add)
  val myDoubleState by mutableDoubleStateOf(0.0).also(objects::add)

  assertAll {
    assertThat(stateObjectsTable.findStateObjectName(objects[0])).isEqualTo("myUnitState")
    assertThat(stateObjectsTable.findStateObjectName(objects[1])).isEqualTo("myIntState")
    assertThat(stateObjectsTable.findStateObjectName(objects[2])).isEqualTo("myFloatState")
    assertThat(stateObjectsTable.findStateObjectName(objects[3])).isEqualTo("myLongState")
    assertThat(stateObjectsTable.findStateObjectName(objects[4])).isEqualTo("myDoubleState")
  }
}
