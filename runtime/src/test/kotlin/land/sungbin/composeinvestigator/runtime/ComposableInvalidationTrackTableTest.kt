/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe

class ComposableInvalidationTrackTableTest : FeatureSpec() {
  init {
    feature("computeDiffParamsIfPresent") {
      scenario("when no previous parameter info") {
        val paramInfo = ParameterInfo(
          name = "name",
          stability = DeclarationStability.Certain(true),
          value = "value",
          hashCode = 0,
        )
        val table = ComposableInvalidationTrackTable()
        val diffParams = table.computeDiffParamsIfPresent(
          composableKeyName = "composableKeyName",
          composableOriginalName = "composableOriginalName",
          parameterInfos = arrayOf(paramInfo),
        )

        table.parameterMap shouldBe mapOf("composableKeyName" to arrayOf(paramInfo))
        diffParams shouldBe null
      }
      scenario("when previous parameter info exists") {
        val prevParamInfo = ParameterInfo(
          name = "name",
          stability = DeclarationStability.Certain(true),
          value = "value",
          hashCode = 0,
        )
        val newParamInfo = ParameterInfo(
          name = "name",
          stability = DeclarationStability.Certain(true),
          value = "new value",
          hashCode = 1,
        )
        val table = ComposableInvalidationTrackTable().apply {
          computeDiffParamsIfPresent(
            composableKeyName = "composableKeyName",
            composableOriginalName = "composableOriginalName",
            parameterInfos = arrayOf(prevParamInfo),
          )
        }
        val diffParams = table.computeDiffParamsIfPresent(
          composableKeyName = "composableKeyName",
          composableOriginalName = "composableOriginalName",
          parameterInfos = arrayOf(newParamInfo),
        )

        table.parameterMap shouldBe mapOf("composableKeyName" to arrayOf(newParamInfo))
        diffParams shouldBe DiffParams(
          name = "composableOriginalName",
          params = listOf(prevParamInfo to newParamInfo),
        )
      }
    }
    feature("DiffParams") {
      scenario("display as String (single param)") {
        val diffParams = DiffParams(
          name = "composableOriginalName",
          params = listOf(
            ParameterInfo(
              name = "name",
              stability = DeclarationStability.Certain(true),
              value = "value",
              hashCode = 0,
            ) to ParameterInfo(
              name = "name",
              stability = DeclarationStability.Certain(true),
              value = "new value",
              hashCode = 1,
            ),
          ),
        )

        diffParams.toString() shouldBe """
          |<composableOriginalName> DiffParams(
          |  1. [name <Stable>] value (0) -> new value (1)
          |)
          |""".trimMargin()
      }
      scenario("display as String (multiple params)") {
        val diffParams = DiffParams(
          name = "composableOriginalName",
          params = listOf(
            ParameterInfo(
              name = "name",
              stability = DeclarationStability.Certain(true),
              value = "value",
              hashCode = 0,
            ) to ParameterInfo(
              name = "name",
              stability = DeclarationStability.Certain(true),
              value = "new value",
              hashCode = 1,
            ),
            ParameterInfo(
              name = "name2",
              stability = DeclarationStability.Runtime("unstable"),
              value = "value2",
              hashCode = 2,
            ) to ParameterInfo(
              name = "name2",
              stability = DeclarationStability.Runtime("unstable"),
              value = "new value2",
              hashCode = 3,
            ),
          ),
        )

        diffParams.toString() shouldBe """
          |<composableOriginalName> DiffParams(
          |  1. [name <Stable>] value (0) -> new value (1)
          |  2. [name2 <Runtime(unstable)>] value2 (2) -> new value2 (3)
          |)
          |""".trimMargin()
      }
    }
  }
}
