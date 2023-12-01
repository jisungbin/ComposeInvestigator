/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

import io.kotest.core.annotation.Ignored
import io.kotest.core.spec.style.FeatureSpec

@Ignored("WIP")
class ComposableInvalidationTrackTableTest : FeatureSpec() {
//  init {
//    feature("computeDiffParamsIfPresent") {
//      scenario("when no previous parameter info") {
//        val paramInfo = ParameterInfo(
//          name = "name",
//          stability = DeclarationStability.Certain(true),
//          valueString = "value",
//          valueHashCode = 0,
//        )
//        val table = ComposableInvalidationTrackTable()
//        val diffParams = table.computeInvalidationReason(
//          keyName = "composableKeyName",
//          parameterValues = arrayOf(paramInfo),
//        )
//
//        table.parameterMap shouldBe mapOf("composableKeyName" to arrayOf(paramInfo))
//        diffParams shouldBe null
//      }
//      scenario("when previous parameter info exists") {
//        val prevParamInfo = ParameterInfo(
//          name = "name",
//          stability = DeclarationStability.Certain(true),
//          valueString = "value",
//          valueHashCode = 0,
//        )
//        val newParamInfo = ParameterInfo(
//          name = "name",
//          stability = DeclarationStability.Certain(true),
//          valueString = "new value",
//          valueHashCode = 1,
//        )
//        val table = ComposableInvalidationTrackTable().apply {
//          computeInvalidationReason(
//            keyName = "composableKeyName",
//            parameterValues = arrayOf(prevParamInfo),
//          )
//        }
//        val diffParams = table.computeInvalidationReason(
//          keyName = "composableKeyName",
//          parameterValues = arrayOf(newParamInfo),
//        )
//
//        table.parameterMap shouldBe mapOf("composableKeyName" to arrayOf(newParamInfo))
//        diffParams shouldBe FieldChanged(listOf(prevParamInfo to newParamInfo))
//      }
//    }
//    feature("DiffParams") {
//      scenario("display as String (single param)") {
//        val diffParams = FieldChanged(
//          listOf(
//            ParameterInfo(
//              name = "name",
//              stability = DeclarationStability.Certain(true),
//              valueString = "value",
//              valueHashCode = 0,
//            ) to ParameterInfo(
//              name = "name",
//              stability = DeclarationStability.Certain(true),
//              valueString = "new value",
//              valueHashCode = 1,
//            ),
//          ),
//        )
//
//        diffParams.toString() shouldBe """
//          |<composableOriginalName> DiffParams(
//          |  1. [name <Stable>] value (0) -> new value (1)
//          |)
//          |""".trimMargin()
//      }
//      scenario("display as String (multiple params)") {
//        val diffParams = FieldChanged(
//          listOf(
//            ParameterInfo(
//              name = "name",
//              stability = DeclarationStability.Certain(true),
//              valueString = "value",
//              valueHashCode = 0,
//            ) to ParameterInfo(
//              name = "name",
//              stability = DeclarationStability.Certain(true),
//              valueString = "new value",
//              valueHashCode = 1,
//            ),
//            ParameterInfo(
//              name = "name2",
//              stability = DeclarationStability.Runtime("unstable"),
//              valueString = "value2",
//              valueHashCode = 2,
//            ) to ParameterInfo(
//              name = "name2",
//              stability = DeclarationStability.Runtime("unstable"),
//              valueString = "new value2",
//              valueHashCode = 3,
//            ),
//          ),
//        )
//
//        diffParams.toString() shouldBe """
//          |<composableOriginalName> DiffParams(
//          |  1. [name <Stable>] value (0) -> new value (1)
//          |  2. [name2 <Unstable>] value2 (2) -> new value2 (3)
//          |)
//          |""".trimMargin()
//      }
//    }
//  }
}
