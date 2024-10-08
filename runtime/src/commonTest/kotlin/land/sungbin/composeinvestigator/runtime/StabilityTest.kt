// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.runtime

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class StabilityTest {
  @Test fun printAsString() {
    assertThat(Stability.Certain(true).toString()).isEqualTo("Stable")
    assertThat(Stability.Certain(false).toString()).isEqualTo("Unstable")
    assertThat(Stability.Runtime("name").toString()).isEqualTo("Runtime(name)")
    assertThat(Stability.Unknown("name").toString()).isEqualTo("Uncertain(name)")
    assertThat(Stability.Parameter("name").toString()).isEqualTo("Parameter(name)")
    assertThat(
      Stability.Combined(
        Stability.Certain(true),
        Stability.Certain(false),
        Stability.Runtime("name"),
        Stability.Unknown("name2"),
        Stability.Parameter("name3"),
      ).toString(),
    )
      .isEqualTo("Stable,Unstable,Runtime(name),Uncertain(name2),Parameter(name3)")
  }
}
