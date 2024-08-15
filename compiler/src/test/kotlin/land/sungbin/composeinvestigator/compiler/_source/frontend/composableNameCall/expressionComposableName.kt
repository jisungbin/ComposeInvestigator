package land.sungbin.composeinvestigator.compiler._source.frontend.composableNameCall

import kotlin.random.Random
import land.sungbin.composeinvestigator.runtime.ComposableName

fun expressionComposableName() {
  ComposableName(Random.nextBoolean().toString())
}
