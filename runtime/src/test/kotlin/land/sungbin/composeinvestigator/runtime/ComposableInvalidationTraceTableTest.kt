/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import kotlin.test.Test
import land.sungbin.composeinvestigator.runtime.affect.AffectedField

class ComposableInvalidationTraceTableTest {
  @Test
  fun computeInvalidationReasonWhenNoPreviousInfo() {
    val table = ComposableInvalidationTraceTable()

    val param = AffectedField.ValueParameter(
      name = "name",
      typeName = "kotlin.String",
      stability = Stability.Stable,
      valueString = "value",
      valueHashCode = 0,
    )
    val reason = table.computeInvalidationReason(keyName = "keyName", fields = listOf(param))

    assertThat(table.affectFields).containsOnly("keyName" to listOf(param))
    assertThat(reason).isEqualTo(InvalidationReason.Initial)
  }

  @Test
  fun computeInvalidationReasonWhenPreviousInfoExists() {
    val table = ComposableInvalidationTraceTable()

    val oldParam = AffectedField.ValueParameter(
      name = "name",
      typeName = "kotlin.String",
      stability = Stability.Stable,
      valueString = "value",
      valueHashCode = 0,
    )
    val newParam = AffectedField.ValueParameter(
      name = "name",
      typeName = "kotlin.String",
      stability = Stability.Unstable,
      valueString = "new value",
      valueHashCode = 1,
    )

    table.computeInvalidationReason(keyName = "keyName", fields = listOf(oldParam))
    val reason = table.computeInvalidationReason(keyName = "keyName", fields = listOf(newParam))

    assertThat(table.affectFields).containsOnly("keyName" to listOf(newParam))
    assertThat(reason).isEqualTo(InvalidationReason.FieldChanged(listOf(oldParam changedTo newParam)))
  }

  @Test
  fun fieldChangedDisplayAsParamsOnlyString() {
    val changedParam = InvalidationReason.FieldChanged(
      listOf(
        FieldChanged(
          old = AffectedField.ValueParameter(
            name = "name",
            typeName = "kotlin.String",
            stability = Stability.Stable,
            valueString = "value",
            valueHashCode = 0,
          ),
          new = AffectedField.ValueParameter(
            name = "name",
            typeName = "kotlin.String",
            stability = Stability.Stable,
            valueString = "new value",
            valueHashCode = 1,
          ),
        ),
        FieldChanged(
          old = AffectedField.ValueParameter(
            name = "name2",
            typeName = "kotlin.String",
            stability = Stability.Unstable,
            valueString = "value2",
            valueHashCode = 10,
          ),
          new = AffectedField.ValueParameter(
            name = "name2",
            typeName = "kotlin.String",
            stability = Stability.Unstable,
            valueString = "new value2",
            valueHashCode = 11,
          ),
        ),
      ),
    )

    assertThat(changedParam.toString()).isEqualTo(
      """
          |[FieldChanged]
          |  1. name <Stable>
          |    Old: value (0)
          |    New: new value (1)
          |  2. name2 <Unstable>
          |    Old: value2 (10)
          |    New: new value2 (11)
          |
      """.trimMargin(),
    )
  }
}
