/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class DeclarationStabilityTest : ShouldSpec() {
  init {
    should("Meaningful toString") {
      DeclarationStability.Certain(true).toString() shouldBe "Stable"
      DeclarationStability.Certain(false).toString() shouldBe "Unstable"
      DeclarationStability.Runtime("name").toString() shouldBe "Runtime(name)"
      DeclarationStability.Unknown("name").toString() shouldBe "Uncertain(name)"
      DeclarationStability.Parameter("name").toString() shouldBe "Parameter(name)"
      DeclarationStability.Combined(
        DeclarationStability.Certain(true),
        DeclarationStability.Certain(false),
        DeclarationStability.Runtime("name"),
        DeclarationStability.Unknown("name"),
        DeclarationStability.Parameter("name"),
      ).toString() shouldBe "Stable,Unstable,Runtime(name),Uncertain(name),Parameter(name)"
    }
  }
}
