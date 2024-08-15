package land.sungbin.composeinvestigator.compiler.lower

import kotlin.test.Test
import land.sungbin.composeinvestigator.compiler._compilation.AbstractIrGoldenTest
import land.sungbin.composeinvestigator.compiler._source.source

class StateInitializerTransformTest : AbstractIrGoldenTest() {
  @Test fun directStateVariable() =
    verifyIrGolden(source("lower/stateInitializer/directStateVariable.kt"))

  @Test fun delegateStateVariable() =
    verifyIrGolden(source("lower/stateInitializer/delegateStateVariable.kt"))
}