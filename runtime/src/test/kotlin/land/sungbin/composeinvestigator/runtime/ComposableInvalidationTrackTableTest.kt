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
        val reason = table.computeInvalidationReason(keyName = "keyName", fields = listOf(param))

        table.affectFields shouldBe mapOf("keyName" to listOf(param))
        reason shouldBe InvalidationReason.Initial
      }
      scenario("when previous info exists") {
        val table = ComposableInvalidationTrackTable()

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

        table.computeInvalidationReason(keyName = "keyName", fields = listOf(oldParam))
        val reason = table.computeInvalidationReason(keyName = "keyName", fields = listOf(newParam))

        table.affectFields shouldBe mapOf("keyName" to listOf(newParam))
        reason shouldBe InvalidationReason.FieldChanged(listOf(oldParam changedTo newParam))
      }
    }
    feature("AffectedField.FieldChanged") {
      scenario("display as String (params only)") {
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
            AffectedField.ValueParameter(
              name = "name2",
              stability = DeclarationStability.Unstable,
              valueString = "value2",
              valueHashCode = 10,
            ) changedTo AffectedField.ValueParameter(
              name = "name2",
              stability = DeclarationStability.Unstable,
              valueString = "new value2",
              valueHashCode = 11,
            ),
          ),
        )

        changedParam.toString() shouldBe """
          |[FieldChanged]
          |  1. name <Stable>
          |    Old: value (0)
          |    New: new value (1)
          |  2. name2 <Unstable>
          |    Old: value2 (10)
          |    New: new value2 (11)
          |""".trimMargin()
      }
    }
  }
}
