// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.test

import androidx.compose.runtime.mock.compositionTest
import kotlin.test.BeforeTest
import org.junit.jupiter.api.Test

class StateObjectsTest {
  @BeforeTest fun prepare() {
    TestConfiguration.reset()
    stateObjectsTable.reset()
  }

  @Test fun directStateObjects() = compositionTest {
    compose { DirectStateObjects() }
  }

  @Test fun delegateStateObjects() = compositionTest {
    compose { DelegateStateObjects() }
  }
}
