/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.lower

import kotlin.test.Ignore
import kotlin.test.Test
import land.sungbin.composeinvestigator.compiler.FeatureFlag
import land.sungbin.composeinvestigator.compiler._compilation.AbstractCompilerTest
import org.jetbrains.kotlin.utils.addToStdlib.enumSetOf

@Ignore(
  "" +
    "- Composable properties should also be tested.\n" +
    "- Stable Expression Composable should also be tested.",
)
class InvalidationProcessTransformTest : AbstractCompilerTest(
  enumSetOf(FeatureFlag.InvalidationProcessTracing),
  sourceRoot = "lower/invalidationProcessAndSkip",
) {
  @Test fun movableComposable() = irTest(source("movableComposable.kt")) {
    """
val ComposableInvalidationTraceTableImpl%MovableComposableKt: ComposableInvalidationTraceTable = ComposableInvalidationTraceTable()
private fun use(any: Any): Int {
  return any.hashCode()
}
@Composable
private fun blockComposable(any: Any, any2: Any, %composer: Composer?, %changed: Int) {
  %composer = %composer.startRestartGroup(<>)
  val %dirty = %changed
  if (%changed and 0b0110 == 0) {
    %dirty = %dirty or if (%composer.changedInstance(any)) 0b0100 else 0b0010
  }
  if (%changed and 0b00110000 == 0) {
    %dirty = %dirty or if (%composer.changedInstance(any2)) 0b00100000 else 0b00010000
  }
  if (%dirty and 0b00010011 != 0b00010010 || !%composer.skipping) {
    if (isTraceInProgress()) {
      traceEventStart(<>, %dirty, -1, <>)
    }
    val tmp0_currentValueArguments = mutableListOf()
    val tmp1_any%valueArgu = ValueArgument("any", "kotlin.Any", any.toString(), any.hashCode(), Certain(false))
    tmp0_currentValueArguments.add(tmp1_any%valueArgu)
    val tmp2_any2%valueArgu = ValueArgument("any2", "kotlin.Any", any2.toString(), any2.hashCode(), Certain(false))
    tmp0_currentValueArguments.add(tmp2_any2%valueArgu)
    val tmp3_invalidationReason = ComposableInvalidationTraceTableImpl%MovableComposableKt.computeInvalidationReason("fun-blockComposable(Any,Any)Unit/pkg-land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip/file-movableComposable.kt", tmp0_currentValueArguments)
    ComposeInvestigatorConfig.logger.log(ComposableInformation("blockComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "movableComposable.kt"), Processed(tmp3_invalidationReason))
    %composer.startMovableGroup(<>, %composer.joinKey(any, any2))
    use(any)
    val tmp0 = use(any2)
    %composer.endMovableGroup()
    tmp0
    if (isTraceInProgress()) {
      traceEventEnd()
    }
  } else {
    %composer.skipToGroupEnd()
  }
  %composer.endRestartGroup()?.updateScope { %composer: Composer?, %force: Int ->
    blockComposable(any, any2, %composer, updateChangedFlags(%changed or 0b0001))
  }
}
@Composable
private fun expressionComposable(any: Any, any2: Any, %composer: Composer?, %changed: Int): Int {
  %composer.startReplaceGroup(<>)
  if (isTraceInProgress()) {
    traceEventStart(<>, %changed, -1, <>)
  }
  val tmp0_currentValueArguments = mutableListOf()
  val tmp1_any%valueArgu = ValueArgument("any", "kotlin.Any", any.toString(), any.hashCode(), Certain(false))
  tmp0_currentValueArguments.add(tmp1_any%valueArgu)
  val tmp2_any2%valueArgu = ValueArgument("any2", "kotlin.Any", any2.toString(), any2.hashCode(), Certain(false))
  tmp0_currentValueArguments.add(tmp2_any2%valueArgu)
  val tmp3_invalidationReason = ComposableInvalidationTraceTableImpl%MovableComposableKt.computeInvalidationReason("fun-expressionComposable(Any,Any)Int/pkg-land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip/file-movableComposable.kt", tmp0_currentValueArguments)
  ComposeInvestigatorConfig.logger.log(ComposableInformation("expressionComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "movableComposable.kt"), Processed(tmp3_invalidationReason))
  val tmp0 = <block>{
    %composer.startMovableGroup(<>, %composer.joinKey(any, any2))
    val tmp1 = use(any) + use(any2)
    %composer.endMovableGroup()
    tmp1
  }
  if (isTraceInProgress()) {
    traceEventEnd()
  }
  %composer.endReplaceGroup()
  return tmp0
}
    """
  }

  @Test fun noGroupComposable() = irTest(source("noGroupComposable.kt")) {
    """
val ComposableInvalidationTraceTableImpl%NoGroupComposableKt: ComposableInvalidationTraceTable = ComposableInvalidationTraceTable()
private fun use(any: Any): Int {
  return any.hashCode()
}
@Composable
@ExplicitGroupsComposable
private fun blockComposable(any: Any, any2: Any, %composer: Composer?, %changed: Int) {
  if (isTraceInProgress()) {
    traceEventStart(<>, %changed, -1, <>)
  }
  val tmp0_currentValueArguments = mutableListOf()
  val tmp1_any%valueArgu = ValueArgument("any", "kotlin.Any", any.toString(), any.hashCode(), Certain(false))
  tmp0_currentValueArguments.add(tmp1_any%valueArgu)
  val tmp2_any2%valueArgu = ValueArgument("any2", "kotlin.Any", any2.toString(), any2.hashCode(), Certain(false))
  tmp0_currentValueArguments.add(tmp2_any2%valueArgu)
  val tmp3_invalidationReason = ComposableInvalidationTraceTableImpl%NoGroupComposableKt.computeInvalidationReason("fun-blockComposable(Any,Any)Unit/pkg-land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip/file-noGroupComposable.kt", tmp0_currentValueArguments)
  ComposeInvestigatorConfig.logger.log(ComposableInformation("blockComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "noGroupComposable.kt"), Processed(tmp3_invalidationReason))
  use(any)
  use(any2)
  if (isTraceInProgress()) {
    traceEventEnd()
  }
}
@Composable
@ExplicitGroupsComposable
private fun expressionComposable(any: Any, any2: Any, %composer: Composer?, %changed: Int): Int {
  if (isTraceInProgress()) {
    traceEventStart(<>, %changed, -1, <>)
  }
  val tmp0_currentValueArguments = mutableListOf()
  val tmp1_any%valueArgu = ValueArgument("any", "kotlin.Any", any.toString(), any.hashCode(), Certain(false))
  tmp0_currentValueArguments.add(tmp1_any%valueArgu)
  val tmp2_any2%valueArgu = ValueArgument("any2", "kotlin.Any", any2.toString(), any2.hashCode(), Certain(false))
  tmp0_currentValueArguments.add(tmp2_any2%valueArgu)
  val tmp3_invalidationReason = ComposableInvalidationTraceTableImpl%NoGroupComposableKt.computeInvalidationReason("fun-expressionComposable(Any,Any)Int/pkg-land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip/file-noGroupComposable.kt", tmp0_currentValueArguments)
  ComposeInvestigatorConfig.logger.log(ComposableInformation("expressionComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "noGroupComposable.kt"), Processed(tmp3_invalidationReason))
  val tmp0 = use(any) + use(any2)
  if (isTraceInProgress()) {
    traceEventEnd()
  }
  return tmp0
}
    """
  }

  @Test fun noInvestigationComposable() = irTest(source("noInvestigationComposable.kt")) {
    """
val ComposableInvalidationTraceTableImpl%NoInvestigationComposableKt: ComposableInvalidationTraceTable = ComposableInvalidationTraceTable()
private fun use(any: Any): Int {
  return any.hashCode()
}
@Composable
@NoInvestigation
private fun noInvestigationBlockComposable(any: Any, any2: Any, %composer: Composer?, %changed: Int) {
  %composer = %composer.startRestartGroup(<>)
  val %dirty = %changed
  if (%changed and 0b0110 == 0) {
    %dirty = %dirty or if (%composer.changedInstance(any)) 0b0100 else 0b0010
  }
  if (%changed and 0b00110000 == 0) {
    %dirty = %dirty or if (%composer.changedInstance(any2)) 0b00100000 else 0b00010000
  }
  if (%dirty and 0b00010011 != 0b00010010 || !%composer.skipping) {
    if (isTraceInProgress()) {
      traceEventStart(<>, %dirty, -1, <>)
    }
    use(any)
    use(any2)
    if (isTraceInProgress()) {
      traceEventEnd()
    }
  } else {
    %composer.skipToGroupEnd()
  }
  %composer.endRestartGroup()?.updateScope { %composer: Composer?, %force: Int ->
    noInvestigationBlockComposable(any, any2, %composer, updateChangedFlags(%changed or 0b0001))
  }
}
@Composable
@NoInvestigation
private fun noInvestigationExpressionComposable(any: Any, any2: Any, %composer: Composer?, %changed: Int): Int {
  %composer.startReplaceGroup(<>)
  if (isTraceInProgress()) {
    traceEventStart(<>, %changed, -1, <>)
  }
  val tmp0 = use(any) + use(any2)
  if (isTraceInProgress()) {
    traceEventEnd()
  }
  %composer.endReplaceGroup()
  return tmp0
}
    """
  }

  // FIXME file-level annotations are not rendered by dumpSrc
  @Test fun noInvestigationFile() = irTest(source("noInvestigationFile.kt")) {
    """
private fun use(any: Any): Int {
  return any.hashCode()
}
@Composable
private fun blockComposable(any: Any, any2: Any, %composer: Composer?, %changed: Int) {
  %composer = %composer.startRestartGroup(<>)
  val %dirty = %changed
  if (%changed and 0b0110 == 0) {
    %dirty = %dirty or if (%composer.changedInstance(any)) 0b0100 else 0b0010
  }
  if (%changed and 0b00110000 == 0) {
    %dirty = %dirty or if (%composer.changedInstance(any2)) 0b00100000 else 0b00010000
  }
  if (%dirty and 0b00010011 != 0b00010010 || !%composer.skipping) {
    if (isTraceInProgress()) {
      traceEventStart(<>, %dirty, -1, <>)
    }
    use(any)
    use(any2)
    if (isTraceInProgress()) {
      traceEventEnd()
    }
  } else {
    %composer.skipToGroupEnd()
  }
  %composer.endRestartGroup()?.updateScope { %composer: Composer?, %force: Int ->
    blockComposable(any, any2, %composer, updateChangedFlags(%changed or 0b0001))
  }
}
@Composable
private fun expressionComposable(any: Any, any2: Any, %composer: Composer?, %changed: Int): Int {
  %composer.startReplaceGroup(<>)
  if (isTraceInProgress()) {
    traceEventStart(<>, %changed, -1, <>)
  }
  val tmp0 = use(any) + use(any2)
  if (isTraceInProgress()) {
    traceEventEnd()
  }
  %composer.endReplaceGroup()
  return tmp0
}
    """
  }

  @Test fun readonlyComposable() = irTest(source("readonlyComposable.kt")) {
    """
val ComposableInvalidationTraceTableImpl%ReadonlyComposableKt: ComposableInvalidationTraceTable = ComposableInvalidationTraceTable()
private fun use(any: Any): Int {
  return any.hashCode()
}
@Composable
@ReadOnlyComposable
private fun blockComposable(any: Any, any2: Any, %composer: Composer?, %changed: Int) {
  %composer = %composer.startRestartGroup(<>)
  val %dirty = %changed
  if (%changed and 0b0110 == 0) {
    %dirty = %dirty or if (%composer.changedInstance(any)) 0b0100 else 0b0010
  }
  if (%changed and 0b00110000 == 0) {
    %dirty = %dirty or if (%composer.changedInstance(any2)) 0b00100000 else 0b00010000
  }
  if (%dirty and 0b00010011 != 0b00010010 || !%composer.skipping) {
    if (isTraceInProgress()) {
      traceEventStart(<>, %dirty, -1, <>)
    }
    val tmp0_currentValueArguments = mutableListOf()
    val tmp1_any%valueArgu = ValueArgument("any", "kotlin.Any", any.toString(), any.hashCode(), Certain(false))
    tmp0_currentValueArguments.add(tmp1_any%valueArgu)
    val tmp2_any2%valueArgu = ValueArgument("any2", "kotlin.Any", any2.toString(), any2.hashCode(), Certain(false))
    tmp0_currentValueArguments.add(tmp2_any2%valueArgu)
    val tmp3_invalidationReason = ComposableInvalidationTraceTableImpl%ReadonlyComposableKt.computeInvalidationReason("fun-blockComposable(Any,Any)Unit/pkg-land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip/file-readonlyComposable.kt", tmp0_currentValueArguments)
    ComposeInvestigatorConfig.logger.log(ComposableInformation("blockComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "readonlyComposable.kt"), Processed(tmp3_invalidationReason))
    use(any)
    use(any2)
    if (isTraceInProgress()) {
      traceEventEnd()
    }
  } else {
    %composer.skipToGroupEnd()
  }
  %composer.endRestartGroup()?.updateScope { %composer: Composer?, %force: Int ->
    blockComposable(any, any2, %composer, updateChangedFlags(%changed or 0b0001))
  }
}
@Composable
@ReadOnlyComposable
private fun expressionComposable(any: Any, any2: Any, %composer: Composer?, %changed: Int): Int {
  if (isTraceInProgress()) {
    traceEventStart(<>, %changed, -1, <>)
  }
  val tmp0_currentValueArguments = mutableListOf()
  val tmp1_any%valueArgu = ValueArgument("any", "kotlin.Any", any.toString(), any.hashCode(), Certain(false))
  tmp0_currentValueArguments.add(tmp1_any%valueArgu)
  val tmp2_any2%valueArgu = ValueArgument("any2", "kotlin.Any", any2.toString(), any2.hashCode(), Certain(false))
  tmp0_currentValueArguments.add(tmp2_any2%valueArgu)
  val tmp3_invalidationReason = ComposableInvalidationTraceTableImpl%ReadonlyComposableKt.computeInvalidationReason("fun-expressionComposable(Any,Any)Int/pkg-land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip/file-readonlyComposable.kt", tmp0_currentValueArguments)
  ComposeInvestigatorConfig.logger.log(ComposableInformation("expressionComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "readonlyComposable.kt"), Processed(tmp3_invalidationReason))
  val tmp0 = use(any) + use(any2)
  if (isTraceInProgress()) {
    traceEventEnd()
  }
  return tmp0
}
    """
  }

  @Test fun replaceableComposable() = irTest(source("replaceableComposable.kt")) {
    """
val ComposableInvalidationTraceTableImpl%ReplaceableComposableKt: ComposableInvalidationTraceTable = ComposableInvalidationTraceTable()
private fun use(any: Any): Int {
  return any.hashCode()
}
@Composable
@NonRestartableComposable
private fun blockComposable(any: Any, any2: Any, %composer: Composer?, %changed: Int) {
  %composer.startReplaceGroup(<>)
  if (isTraceInProgress()) {
    traceEventStart(<>, %changed, -1, <>)
  }
  val tmp0_currentValueArguments = mutableListOf()
  val tmp1_any%valueArgu = ValueArgument("any", "kotlin.Any", any.toString(), any.hashCode(), Certain(false))
  tmp0_currentValueArguments.add(tmp1_any%valueArgu)
  val tmp2_any2%valueArgu = ValueArgument("any2", "kotlin.Any", any2.toString(), any2.hashCode(), Certain(false))
  tmp0_currentValueArguments.add(tmp2_any2%valueArgu)
  val tmp3_invalidationReason = ComposableInvalidationTraceTableImpl%ReplaceableComposableKt.computeInvalidationReason("fun-blockComposable(Any,Any)Unit/pkg-land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip/file-replaceableComposable.kt", tmp0_currentValueArguments)
  ComposeInvestigatorConfig.logger.log(ComposableInformation("blockComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "replaceableComposable.kt"), Processed(tmp3_invalidationReason))
  val tmp0_subject = Default.nextBoolean()
  when {
    tmp0_subject == true -> {
      use(any)
    }
    else -> {
      use(any2)
    }
  }
  if (isTraceInProgress()) {
    traceEventEnd()
  }
  %composer.endReplaceGroup()
}
@Composable
@NonRestartableComposable
private fun expressionComposable(any: Any, any2: Any, %composer: Composer?, %changed: Int): Int {
  %composer.startReplaceGroup(<>)
  if (isTraceInProgress()) {
    traceEventStart(<>, %changed, -1, <>)
  }
  val tmp0_currentValueArguments = mutableListOf()
  val tmp1_any%valueArgu = ValueArgument("any", "kotlin.Any", any.toString(), any.hashCode(), Certain(false))
  tmp0_currentValueArguments.add(tmp1_any%valueArgu)
  val tmp2_any2%valueArgu = ValueArgument("any2", "kotlin.Any", any2.toString(), any2.hashCode(), Certain(false))
  tmp0_currentValueArguments.add(tmp2_any2%valueArgu)
  val tmp3_invalidationReason = ComposableInvalidationTraceTableImpl%ReplaceableComposableKt.computeInvalidationReason("fun-expressionComposable(Any,Any)Int/pkg-land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip/file-replaceableComposable.kt", tmp0_currentValueArguments)
  ComposeInvestigatorConfig.logger.log(ComposableInformation("expressionComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "replaceableComposable.kt"), Processed(tmp3_invalidationReason))
  val tmp0 = <block>{
    val tmp0_subject = Default.nextBoolean()
    when {
      tmp0_subject == true -> {
        use(any)
      }
      else -> {
        use(any2)
      }
    }
  }
  if (isTraceInProgress()) {
    traceEventEnd()
  }
  %composer.endReplaceGroup()
  return tmp0
}
    """
  }

  @Test fun restartableComposable() = irTest(source("restartableComposable.kt")) {
    """
val ComposableInvalidationTraceTableImpl%RestartableComposableKt: ComposableInvalidationTraceTable = ComposableInvalidationTraceTable()
private fun use(any: Any): Int {
  return any.hashCode()
}
@Composable
private fun blockComposable(any: Any, any2: Any, %composer: Composer?, %changed: Int) {
  %composer = %composer.startRestartGroup(<>)
  val %dirty = %changed
  if (%changed and 0b0110 == 0) {
    %dirty = %dirty or if (%composer.changedInstance(any)) 0b0100 else 0b0010
  }
  if (%changed and 0b00110000 == 0) {
    %dirty = %dirty or if (%composer.changedInstance(any2)) 0b00100000 else 0b00010000
  }
  if (%dirty and 0b00010011 != 0b00010010 || !%composer.skipping) {
    if (isTraceInProgress()) {
      traceEventStart(<>, %dirty, -1, <>)
    }
    val tmp0_currentValueArguments = mutableListOf()
    val tmp1_any%valueArgu = ValueArgument("any", "kotlin.Any", any.toString(), any.hashCode(), Certain(false))
    tmp0_currentValueArguments.add(tmp1_any%valueArgu)
    val tmp2_any2%valueArgu = ValueArgument("any2", "kotlin.Any", any2.toString(), any2.hashCode(), Certain(false))
    tmp0_currentValueArguments.add(tmp2_any2%valueArgu)
    val tmp3_invalidationReason = ComposableInvalidationTraceTableImpl%RestartableComposableKt.computeInvalidationReason("fun-blockComposable(Any,Any)Unit/pkg-land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip/file-restartableComposable.kt", tmp0_currentValueArguments)
    ComposeInvestigatorConfig.logger.log(ComposableInformation("blockComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "restartableComposable.kt"), Processed(tmp3_invalidationReason))
    use(any)
    use(any2)
    if (isTraceInProgress()) {
      traceEventEnd()
    }
  } else {
    %composer.skipToGroupEnd()
  }
  %composer.endRestartGroup()?.updateScope { %composer: Composer?, %force: Int ->
    blockComposable(any, any2, %composer, updateChangedFlags(%changed or 0b0001))
  }
}
@Composable
private fun expressionComposable(any: Any, any2: Any, %composer: Composer?, %changed: Int): Int {
  %composer.startReplaceGroup(<>)
  if (isTraceInProgress()) {
    traceEventStart(<>, %changed, -1, <>)
  }
  val tmp0_currentValueArguments = mutableListOf()
  val tmp1_any%valueArgu = ValueArgument("any", "kotlin.Any", any.toString(), any.hashCode(), Certain(false))
  tmp0_currentValueArguments.add(tmp1_any%valueArgu)
  val tmp2_any2%valueArgu = ValueArgument("any2", "kotlin.Any", any2.toString(), any2.hashCode(), Certain(false))
  tmp0_currentValueArguments.add(tmp2_any2%valueArgu)
  val tmp3_invalidationReason = ComposableInvalidationTraceTableImpl%RestartableComposableKt.computeInvalidationReason("fun-expressionComposable(Any,Any)Int/pkg-land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip/file-restartableComposable.kt", tmp0_currentValueArguments)
  ComposeInvestigatorConfig.logger.log(ComposableInformation("expressionComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "restartableComposable.kt"), Processed(tmp3_invalidationReason))
  val tmp0 = use(any) + use(any2)
  if (isTraceInProgress()) {
    traceEventEnd()
  }
  %composer.endReplaceGroup()
  return tmp0
}
    """
  }
}
