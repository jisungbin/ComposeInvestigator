/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class DeclarationStabilityTest {
  @Test
  fun printAsString() {
    assertThat(DeclarationStability.Certain(true).toString()).isEqualTo("Stable")
    assertThat(DeclarationStability.Certain(false).toString()).isEqualTo("Unstable")
    assertThat(DeclarationStability.Runtime("name").toString()).isEqualTo("Runtime(name)")
    assertThat(DeclarationStability.Unknown("name").toString()).isEqualTo("Uncertain(name)")
    assertThat(DeclarationStability.Parameter("name").toString()).isEqualTo("Parameter(name)")
    assertThat(
      DeclarationStability.Combined(
        DeclarationStability.Certain(true),
        DeclarationStability.Certain(false),
        DeclarationStability.Runtime("name"),
        DeclarationStability.Unknown("name2"),
        DeclarationStability.Parameter("name3"),
      ).toString(),
    )
      .isEqualTo("Stable,Unstable,Runtime(name),Uncertain(name2),Parameter(name3)")
  }
}
