/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe
import land.sungbin.composeinvestigator.runtime.affect.AffectedField

class ComposableInvalidationTrackTableTest : FeatureSpec() {
  init {
    feature("computeInvalidationReason") {
      scenario("when no previous info") {
        val table = ComposableInvalidationTrackTable()
        val param = AffectedField.ValueParameter(
          name = "name",
          stability = DeclarationStability.Stable,
          valueString = "value",
          valueHashCode = 0,
        )
        val invalidationReason = table.computeInvalidationReason(
          keyName = "keyName",
          fields = listOf(param),
        )

        table.affectFields shouldBe mapOf("keyName" to listOf(param))
        invalidationReason shouldBe InvalidationReason.Initial
      }
      scenario("when previous info exists") {
        val oldParam = AffectedField.ValueParameter(
          name = "name",
          stability = DeclarationStability.Stable,
          valueString = "value",
          valueHashCode = 0,
        )
        val newParam = AffectedField.ValueParameter(
          name = "name",
          stability = DeclarationStability.Unstable,
          valueString = "new value",
          valueHashCode = 1,
        )
        val table = ComposableInvalidationTrackTable().apply {
          computeInvalidationReason(
            keyName = "keyName",
            fields = listOf(oldParam),
          )
        }
        val invalidationReason = table.computeInvalidationReason(
          keyName = "keyName",
          fields = listOf(newParam),
        )

        table.affectFields shouldBe mapOf("keyName" to listOf(newParam))
        invalidationReason shouldBe InvalidationReason.FieldChanged(listOf(oldParam changedTo newParam))
      }
    }
    feature("AffectedField.FieldChanged") {
      scenario("display as String (param only)") {
        val changedParam = InvalidationReason.FieldChanged(
          listOf(
            AffectedField.ValueParameter(
              name = "name",
              stability = DeclarationStability.Stable,
              valueString = "value",
              valueHashCode = 0,
            ) changedTo AffectedField.ValueParameter(
              name = "name",
              stability = DeclarationStability.Stable,
              valueString = "new value",
              valueHashCode = 1,
            ),
          ),
        )

        changedParam.toString() shouldBe """
          |FieldChanged(
          |  [Parameters]
          |    1. name <Stable>
          |      Old: value (0)
          |      New: new value (1)
          |)
          |""".trimMargin()
      }
      scenario("display as String (state only)") {
        val changedState = InvalidationReason.FieldChanged(
          listOf(
            AffectedField.StateProperty(
              name = "name",
              valueString = "value",
              valueHashCode = 0,
            ) changedTo AffectedField.StateProperty(
              name = "name",
              valueString = "new value",
              valueHashCode = 1,
            ),
          ),
        )

        changedState.toString() shouldBe """
          |FieldChanged(
          |  [Parameters]
          |    (no changed parameter)
          |  [States]
          |    1. name
          |      Old: value (0)
          |      New: new value (1)
          |)
          |""".trimMargin()
      }
      scenario("display as String (params and states)") {
        val changes = InvalidationReason.FieldChanged(
          listOf(
            AffectedField.ValueParameter(
              name = "name",
              valueString = "value",
              valueHashCode = 0,
              stability = DeclarationStability.Stable,
            ) changedTo AffectedField.ValueParameter(
              name = "name",
              valueString = "new value",
              valueHashCode = 1,
              stability = DeclarationStability.Stable,
            ),
            AffectedField.ValueParameter(
              name = "name2",
              valueString = "value2",
              valueHashCode = 2,
              stability = DeclarationStability.Stable,
            ) changedTo AffectedField.ValueParameter(
              name = "name2",
              valueString = "new value2",
              valueHashCode = 3,
              stability = DeclarationStability.Stable,
            ),
            AffectedField.StateProperty(
              name = "name3",
              valueString = "value3",
              valueHashCode = 4,
            ) changedTo AffectedField.StateProperty(
              name = "name3",
              valueString = "new value3",
              valueHashCode = 5,
            ),
            AffectedField.StateProperty(
              name = "name4",
              valueString = "value4",
              valueHashCode = 6,
            ) changedTo AffectedField.StateProperty(
              name = "name4",
              valueString = "new value4",
              valueHashCode = 7,
            ),
          ),
        )

        changes.toString() shouldBe """
          |FieldChanged(
          |  [Parameters]
          |    1. name <Stable>
          |      Old: value (0)
          |      New: new value (1)
          |    2. name2 <Stable>
          |      Old: value2 (2)
          |      New: new value2 (3)
          |  [States]
          |    1. name3
          |      Old: value3 (4)
          |      New: new value3 (5)
          |    2. name4
          |      Old: value4 (6)
          |      New: new value4 (7)
          |)
          |""".trimMargin()
      }
    }
  }
}
