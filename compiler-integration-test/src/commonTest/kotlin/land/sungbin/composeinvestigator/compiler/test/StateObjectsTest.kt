// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.test

import composemock.runCompose
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test

@Ignore("This feature is not yet ready for public use.")
class StateObjectsTest {
  @BeforeTest fun prepare() {
    TestConfiguration.reset()
    stateObjectsTable.reset()
  }

  @Test fun directStateObjects() {
    runCompose {
      DirectStateObjects()
    }
  }

  @Test fun delegateStateObjects() {
    runCompose {
      DelegateStateObjects()
    }
  }
}
