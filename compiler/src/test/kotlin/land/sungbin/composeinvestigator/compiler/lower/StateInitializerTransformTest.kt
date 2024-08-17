/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.lower

import kotlin.test.Test
import land.sungbin.composeinvestigator.compiler._extension.AbstractGoldenTest
import land.sungbin.composeinvestigator.compiler._source.source

class StateInitializerTransformTest : AbstractGoldenTest() {
  @Test fun directStateVariable() =
    verifyIrGolden(source("lower/stateInitializer/directStateVariable.kt"))

  @Test fun delegateStateVariable() =
    verifyIrGolden(source("lower/stateInitializer/delegateStateVariable.kt"))
}
