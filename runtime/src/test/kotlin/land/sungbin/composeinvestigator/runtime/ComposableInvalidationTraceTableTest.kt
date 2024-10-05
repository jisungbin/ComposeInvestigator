// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.runtime

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import kotlin.test.Test
import land.sungbin.composeinvestigator.runtime.ComposableInvalidationTraceTable.AffectedName

class ComposableInvalidationTraceTableTest {
  @Test fun computeInvalidationReasonWhenNoPreviousInfo() {
    val table = ComposableInvalidationTraceTable()

    val argument = ValueArgument(
      name = "name",
      type = "kotlin.String",
      stability = Stability.Stable,
      valueString = "value",
      valueHashCode = 0,
    )
    val reason = table.computeInvalidationReason(keyName = "keyName", compoundKey = 0, arguments = listOf(argument))

    assertThat(table.affectedArguments).containsOnly(AffectedName("keyName", compoundKey = 0) to listOf(argument))
    assertThat(reason).isEqualTo(InvalidationResult.InitialComposition)
  }

  @Test fun computeInvalidationReasonWhenPreviousInfoExists() {
    val table = ComposableInvalidationTraceTable()

    val oldArgument = ValueArgument(
      name = "name",
      type = "kotlin.String",
      stability = Stability.Stable,
      valueString = "value",
      valueHashCode = 0,
    )
    val newArgument = ValueArgument(
      name = "name",
      type = "kotlin.String",
      stability = Stability.Stable,
      valueString = "new value",
      valueHashCode = 1,
    )

    table.computeInvalidationReason(keyName = "keyName", compoundKey = 0, arguments = listOf(oldArgument))
    val reason = table.computeInvalidationReason(keyName = "keyName", compoundKey = 0, arguments = listOf(newArgument))

    assertThat(table.affectedArguments).containsOnly(AffectedName("keyName", compoundKey = 0) to listOf(newArgument))
    assertThat(reason).isEqualTo(InvalidationResult.ArgumentChanged(listOf(oldArgument changedTo newArgument)))
  }

  @Test fun argumentChangedDisplayAsString() {
    val reason = InvalidationResult.ArgumentChanged(
      listOf(
        ChangedArgument(
          previous = ValueArgument(
            name = "name",
            type = "kotlin.String",
            stability = Stability.Stable,
            valueString = "value",
            valueHashCode = 0,
          ),
          new = ValueArgument(
            name = "name",
            type = "kotlin.String",
            stability = Stability.Stable,
            valueString = "new value",
            valueHashCode = 1,
          ),
        ),
        ChangedArgument(
          previous = ValueArgument(
            name = "name2",
            type = "kotlin.String",
            stability = Stability.Unstable,
            valueString = "value2",
            valueHashCode = 10,
          ),
          new = ValueArgument(
            name = "name2",
            type = "kotlin.String",
            stability = Stability.Unstable,
            valueString = "new value2",
            valueHashCode = 11,
          ),
        ),
      ),
    )

    assertThat(reason.toString()).isEqualTo(
      """
      |[ArgumentChanged]
      |1. name <Stable>
      |  Old: value (0)
      |  New: new value (1)
      |2. name2 <Unstable>
      |  Old: value2 (10)
      |  New: new value2 (11)
      |
      """.trimMargin(),
    )
  }
}
