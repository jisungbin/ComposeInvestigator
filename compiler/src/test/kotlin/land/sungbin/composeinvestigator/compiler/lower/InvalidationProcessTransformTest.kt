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

class InvalidationProcessTransformTest : AbstractCompilerTest(
  enumSetOf(FeatureFlag.InvalidationProcessTracing),
  sourceRoot = "lower/invalidationProcessAndSkip",
) {
  @Test fun movableComposable() = diff(source("movableComposable.kt")) {
    """
@@ -1,5 +1,6 @@
+val ComposableInvalidationTraceTableImpl%MovableComposableKt: ComposableInvalidationTraceTable = ComposableInvalidationTraceTable()
 private fun use(any: Any): Int {
   return any.hashCode()
 }
 @Composable
 private fun blockComposable(any: Any, any2: Any, %composer: Composer?, %changed: Int) {
@@ -13,10 +14,17 @@
   }
   if (%dirty and 0b00010011 != 0b00010010 || !%composer.skipping) {
     if (isTraceInProgress()) {
       traceEventStart(<>, %dirty, -1, <>)
     }
+    val tmp0_currentValueArguments = mutableListOf()
+    val tmp1_any%valueArgu = ValueArgument("any", "kotlin.Any", any.toString(), any.hashCode(), Certain(false))
+    tmp0_currentValueArguments.add(tmp1_any%valueArgu)
+    val tmp2_any2%valueArgu = ValueArgument("any2", "kotlin.Any", any2.toString(), any2.hashCode(), Certain(false))
+    tmp0_currentValueArguments.add(tmp2_any2%valueArgu)
+    val tmp3_invalidationReason = ComposableInvalidationTraceTableImpl%MovableComposableKt.computeInvalidationReason("fun-blockComposable(Any,Any)Unit/pkg-land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip/file-movableComposable.kt", tmp0_currentValueArguments)
+    ComposeInvestigatorConfig.logger.log(ComposableInformation("blockComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "movableComposable.kt"), Processed(tmp3_invalidationReason))
     %composer.startMovableGroup(<>, %composer.joinKey(any, any2))
     use(any)
     val tmp0 = use(any2)
     %composer.endMovableGroup()
     tmp0
@@ -42,10 +50,17 @@
   }
   if (%dirty and 0b00010011 != 0b00010010 || !%composer.skipping) {
     if (isTraceInProgress()) {
       traceEventStart(<>, %dirty, -1, <>)
     }
+    val tmp0_currentValueArguments = mutableListOf()
+    val tmp1_any%valueArgu = ValueArgument("any", "kotlin.Int", any.toString(), any.hashCode(), Certain(true))
+    tmp0_currentValueArguments.add(tmp1_any%valueArgu)
+    val tmp2_any2%valueArgu = ValueArgument("any2", "kotlin.Int", any2.toString(), any2.hashCode(), Certain(true))
+    tmp0_currentValueArguments.add(tmp2_any2%valueArgu)
+    val tmp3_invalidationReason = ComposableInvalidationTraceTableImpl%MovableComposableKt.computeInvalidationReason("fun-blockStableComposable(Int,Int)Unit/pkg-land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip/file-movableComposable.kt", tmp0_currentValueArguments)
+    ComposeInvestigatorConfig.logger.log(ComposableInformation("blockStableComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "movableComposable.kt"), Processed(tmp3_invalidationReason))
     %composer.startMovableGroup(<>, %composer.joinKey(any, any2))
     use(any)
     val tmp0 = use(any2)
     %composer.endMovableGroup()
     tmp0
@@ -63,10 +78,17 @@
 private fun expressionComposable(any: Any, any2: Any, %composer: Composer?, %changed: Int): Int {
   %composer.startReplaceGroup(<>)
   if (isTraceInProgress()) {
     traceEventStart(<>, %changed, -1, <>)
   }
+  val tmp0_currentValueArguments = mutableListOf()
+  val tmp1_any%valueArgu = ValueArgument("any", "kotlin.Any", any.toString(), any.hashCode(), Certain(false))
+  tmp0_currentValueArguments.add(tmp1_any%valueArgu)
+  val tmp2_any2%valueArgu = ValueArgument("any2", "kotlin.Any", any2.toString(), any2.hashCode(), Certain(false))
+  tmp0_currentValueArguments.add(tmp2_any2%valueArgu)
+  val tmp3_invalidationReason = ComposableInvalidationTraceTableImpl%MovableComposableKt.computeInvalidationReason("fun-expressionComposable(Any,Any)Int/pkg-land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip/file-movableComposable.kt", tmp0_currentValueArguments)
+  ComposeInvestigatorConfig.logger.log(ComposableInformation("expressionComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "movableComposable.kt"), Processed(tmp3_invalidationReason))
   val tmp0 = <block>{
     %composer.startMovableGroup(<>, %composer.joinKey(any, any2))
     val tmp1 = use(any) + use(any2)
     %composer.endMovableGroup()
     tmp1
    """
  }

  @Test fun noGroupComposable() = diff(source("noGroupComposable.kt")) {
    """
@@ -1,14 +1,22 @@
+val ComposableInvalidationTraceTableImpl%NoGroupComposableKt: ComposableInvalidationTraceTable = ComposableInvalidationTraceTable()
 private fun use(any: Any): Int {
   return any.hashCode()
 }
 @Composable
 @ExplicitGroupsComposable
 private fun blockComposable(any: Any, any2: Any, %composer: Composer?, %changed: Int) {
   if (isTraceInProgress()) {
     traceEventStart(<>, %changed, -1, <>)
   }
+  val tmp0_currentValueArguments = mutableListOf()
+  val tmp1_any%valueArgu = ValueArgument("any", "kotlin.Any", any.toString(), any.hashCode(), Certain(false))
+  tmp0_currentValueArguments.add(tmp1_any%valueArgu)
+  val tmp2_any2%valueArgu = ValueArgument("any2", "kotlin.Any", any2.toString(), any2.hashCode(), Certain(false))
+  tmp0_currentValueArguments.add(tmp2_any2%valueArgu)
+  val tmp3_invalidationReason = ComposableInvalidationTraceTableImpl%NoGroupComposableKt.computeInvalidationReason("fun-blockComposable(Any,Any)Unit/pkg-land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip/file-noGroupComposable.kt", tmp0_currentValueArguments)
+  ComposeInvestigatorConfig.logger.log(ComposableInformation("blockComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "noGroupComposable.kt"), Processed(tmp3_invalidationReason))
   use(any)
   use(any2)
   if (isTraceInProgress()) {
     traceEventEnd()
   }
@@ -17,10 +25,17 @@
 @ExplicitGroupsComposable
 private fun blockStableComposable(any: Int, any2: Int, %composer: Composer?, %changed: Int) {
   if (isTraceInProgress()) {
     traceEventStart(<>, %changed, -1, <>)
   }
+  val tmp0_currentValueArguments = mutableListOf()
+  val tmp1_any%valueArgu = ValueArgument("any", "kotlin.Int", any.toString(), any.hashCode(), Certain(true))
+  tmp0_currentValueArguments.add(tmp1_any%valueArgu)
+  val tmp2_any2%valueArgu = ValueArgument("any2", "kotlin.Int", any2.toString(), any2.hashCode(), Certain(true))
+  tmp0_currentValueArguments.add(tmp2_any2%valueArgu)
+  val tmp3_invalidationReason = ComposableInvalidationTraceTableImpl%NoGroupComposableKt.computeInvalidationReason("fun-blockStableComposable(Int,Int)Unit/pkg-land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip/file-noGroupComposable.kt", tmp0_currentValueArguments)
+  ComposeInvestigatorConfig.logger.log(ComposableInformation("blockStableComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "noGroupComposable.kt"), Processed(tmp3_invalidationReason))
   use(any)
   use(any2)
   if (isTraceInProgress()) {
     traceEventEnd()
   }
@@ -29,10 +44,17 @@
 @ExplicitGroupsComposable
 private fun expressionComposable(any: Any, any2: Any, %composer: Composer?, %changed: Int): Int {
   if (isTraceInProgress()) {
     traceEventStart(<>, %changed, -1, <>)
   }
+  val tmp0_currentValueArguments = mutableListOf()
+  val tmp1_any%valueArgu = ValueArgument("any", "kotlin.Any", any.toString(), any.hashCode(), Certain(false))
+  tmp0_currentValueArguments.add(tmp1_any%valueArgu)
+  val tmp2_any2%valueArgu = ValueArgument("any2", "kotlin.Any", any2.toString(), any2.hashCode(), Certain(false))
+  tmp0_currentValueArguments.add(tmp2_any2%valueArgu)
+  val tmp3_invalidationReason = ComposableInvalidationTraceTableImpl%NoGroupComposableKt.computeInvalidationReason("fun-expressionComposable(Any,Any)Int/pkg-land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip/file-noGroupComposable.kt", tmp0_currentValueArguments)
+  ComposeInvestigatorConfig.logger.log(ComposableInformation("expressionComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "noGroupComposable.kt"), Processed(tmp3_invalidationReason))
   val tmp0 = use(any) + use(any2)
   if (isTraceInProgress()) {
     traceEventEnd()
   }
   return tmp0
    """
  }

  @Ignore("FIXME `fun c(l: @Composable () -> Unit)` ==> NO TABLE GENERATED")
  @Test fun noInvestigationComposable() = clean(source("noInvestigationComposable.kt"))

  @Test fun noInvestigationFile() = clean(source("noInvestigationFile.kt"))

  @Test fun readonlyComposable() = diff(source("readonlyComposable.kt")) {
    """
@@ -1,5 +1,6 @@
+val ComposableInvalidationTraceTableImpl%ReadonlyComposableKt: ComposableInvalidationTraceTable = ComposableInvalidationTraceTable()
 private fun use(any: Any): Int {
   return any.hashCode()
 }
 @Composable
 @ReadOnlyComposable
@@ -14,10 +15,17 @@
   }
   if (%dirty and 0b00010011 != 0b00010010 || !%composer.skipping) {
     if (isTraceInProgress()) {
       traceEventStart(<>, %dirty, -1, <>)
     }
+    val tmp0_currentValueArguments = mutableListOf()
+    val tmp1_any%valueArgu = ValueArgument("any", "kotlin.Any", any.toString(), any.hashCode(), Certain(false))
+    tmp0_currentValueArguments.add(tmp1_any%valueArgu)
+    val tmp2_any2%valueArgu = ValueArgument("any2", "kotlin.Any", any2.toString(), any2.hashCode(), Certain(false))
+    tmp0_currentValueArguments.add(tmp2_any2%valueArgu)
+    val tmp3_invalidationReason = ComposableInvalidationTraceTableImpl%ReadonlyComposableKt.computeInvalidationReason("fun-blockComposable(Any,Any)Unit/pkg-land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip/file-readonlyComposable.kt", tmp0_currentValueArguments)
+    ComposeInvestigatorConfig.logger.log(ComposableInformation("blockComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "readonlyComposable.kt"), Processed(tmp3_invalidationReason))
     use(any)
     use(any2)
     if (isTraceInProgress()) {
       traceEventEnd()
     }
@@ -41,10 +49,17 @@
   }
   if (%dirty and 0b00010011 != 0b00010010 || !%composer.skipping) {
     if (isTraceInProgress()) {
       traceEventStart(<>, %dirty, -1, <>)
     }
+    val tmp0_currentValueArguments = mutableListOf()
+    val tmp1_any%valueArgu = ValueArgument("any", "kotlin.Int", any.toString(), any.hashCode(), Certain(true))
+    tmp0_currentValueArguments.add(tmp1_any%valueArgu)
+    val tmp2_any2%valueArgu = ValueArgument("any2", "kotlin.Int", any2.toString(), any2.hashCode(), Certain(true))
+    tmp0_currentValueArguments.add(tmp2_any2%valueArgu)
+    val tmp3_invalidationReason = ComposableInvalidationTraceTableImpl%ReadonlyComposableKt.computeInvalidationReason("fun-blockStableComposable(Int,Int)Unit/pkg-land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip/file-readonlyComposable.kt", tmp0_currentValueArguments)
+    ComposeInvestigatorConfig.logger.log(ComposableInformation("blockStableComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "readonlyComposable.kt"), Processed(tmp3_invalidationReason))
     use(any)
     use(any2)
     if (isTraceInProgress()) {
       traceEventEnd()
     }
@@ -59,10 +74,17 @@
 @ReadOnlyComposable
 private fun expressionComposable(any: Any, any2: Any, %composer: Composer?, %changed: Int): Int {
   if (isTraceInProgress()) {
     traceEventStart(<>, %changed, -1, <>)
   }
+  val tmp0_currentValueArguments = mutableListOf()
+  val tmp1_any%valueArgu = ValueArgument("any", "kotlin.Any", any.toString(), any.hashCode(), Certain(false))
+  tmp0_currentValueArguments.add(tmp1_any%valueArgu)
+  val tmp2_any2%valueArgu = ValueArgument("any2", "kotlin.Any", any2.toString(), any2.hashCode(), Certain(false))
+  tmp0_currentValueArguments.add(tmp2_any2%valueArgu)
+  val tmp3_invalidationReason = ComposableInvalidationTraceTableImpl%ReadonlyComposableKt.computeInvalidationReason("fun-expressionComposable(Any,Any)Int/pkg-land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip/file-readonlyComposable.kt", tmp0_currentValueArguments)
+  ComposeInvestigatorConfig.logger.log(ComposableInformation("expressionComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "readonlyComposable.kt"), Processed(tmp3_invalidationReason))
   val tmp0 = use(any) + use(any2)
   if (isTraceInProgress()) {
     traceEventEnd()
   }
   return tmp0
    """
  }

  @Test fun replaceableComposable() = diff(source("replaceableComposable.kt")) {
    """
@@ -1,15 +1,23 @@
+val ComposableInvalidationTraceTableImpl%ReplaceableComposableKt: ComposableInvalidationTraceTable = ComposableInvalidationTraceTable()
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
+  val tmp0_currentValueArguments = mutableListOf()
+  val tmp1_any%valueArgu = ValueArgument("any", "kotlin.Any", any.toString(), any.hashCode(), Certain(false))
+  tmp0_currentValueArguments.add(tmp1_any%valueArgu)
+  val tmp2_any2%valueArgu = ValueArgument("any2", "kotlin.Any", any2.toString(), any2.hashCode(), Certain(false))
+  tmp0_currentValueArguments.add(tmp2_any2%valueArgu)
+  val tmp3_invalidationReason = ComposableInvalidationTraceTableImpl%ReplaceableComposableKt.computeInvalidationReason("fun-blockComposable(Any,Any)Unit/pkg-land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip/file-replaceableComposable.kt", tmp0_currentValueArguments)
+  ComposeInvestigatorConfig.logger.log(ComposableInformation("blockComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "replaceableComposable.kt"), Processed(tmp3_invalidationReason))
   val tmp0_subject = Default.nextBoolean()
   when {
     tmp0_subject == true -> {
       use(any)
     }
@@ -27,10 +35,17 @@
 private fun blockStableComposable(any: Int, any2: Int, %composer: Composer?, %changed: Int) {
   %composer.startReplaceGroup(<>)
   if (isTraceInProgress()) {
     traceEventStart(<>, %changed, -1, <>)
   }
+  val tmp0_currentValueArguments = mutableListOf()
+  val tmp1_any%valueArgu = ValueArgument("any", "kotlin.Int", any.toString(), any.hashCode(), Certain(true))
+  tmp0_currentValueArguments.add(tmp1_any%valueArgu)
+  val tmp2_any2%valueArgu = ValueArgument("any2", "kotlin.Int", any2.toString(), any2.hashCode(), Certain(true))
+  tmp0_currentValueArguments.add(tmp2_any2%valueArgu)
+  val tmp3_invalidationReason = ComposableInvalidationTraceTableImpl%ReplaceableComposableKt.computeInvalidationReason("fun-blockStableComposable(Int,Int)Unit/pkg-land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip/file-replaceableComposable.kt", tmp0_currentValueArguments)
+  ComposeInvestigatorConfig.logger.log(ComposableInformation("blockStableComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "replaceableComposable.kt"), Processed(tmp3_invalidationReason))
   val tmp0_subject = Default.nextBoolean()
   when {
     tmp0_subject == true -> {
       use(any)
     }
@@ -48,10 +63,17 @@
 private fun expressionComposable(any: Any, any2: Any, %composer: Composer?, %changed: Int): Int {
   %composer.startReplaceGroup(<>)
   if (isTraceInProgress()) {
     traceEventStart(<>, %changed, -1, <>)
   }
+  val tmp0_currentValueArguments = mutableListOf()
+  val tmp1_any%valueArgu = ValueArgument("any", "kotlin.Any", any.toString(), any.hashCode(), Certain(false))
+  tmp0_currentValueArguments.add(tmp1_any%valueArgu)
+  val tmp2_any2%valueArgu = ValueArgument("any2", "kotlin.Any", any2.toString(), any2.hashCode(), Certain(false))
+  tmp0_currentValueArguments.add(tmp2_any2%valueArgu)
+  val tmp3_invalidationReason = ComposableInvalidationTraceTableImpl%ReplaceableComposableKt.computeInvalidationReason("fun-expressionComposable(Any,Any)Int/pkg-land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip/file-replaceableComposable.kt", tmp0_currentValueArguments)
+  ComposeInvestigatorConfig.logger.log(ComposableInformation("expressionComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "replaceableComposable.kt"), Processed(tmp3_invalidationReason))
   val tmp0 = <block>{
     val tmp0_subject = Default.nextBoolean()
     when {
       tmp0_subject == true -> {
         use(any)
    """
  }

  @Test fun restartableComposable() = diff(source("restartableComposable.kt")) {
    """
@@ -1,5 +1,6 @@
+val ComposableInvalidationTraceTableImpl%RestartableComposableKt: ComposableInvalidationTraceTable = ComposableInvalidationTraceTable()
 private fun use(any: Any): Int {
   return any.hashCode()
 }
 @Composable
 private fun blockComposable(any: Any, any2: Any, %composer: Composer?, %changed: Int) {
@@ -13,10 +14,17 @@
   }
   if (%dirty and 0b00010011 != 0b00010010 || !%composer.skipping) {
     if (isTraceInProgress()) {
       traceEventStart(<>, %dirty, -1, <>)
     }
+    val tmp0_currentValueArguments = mutableListOf()
+    val tmp1_any%valueArgu = ValueArgument("any", "kotlin.Any", any.toString(), any.hashCode(), Certain(false))
+    tmp0_currentValueArguments.add(tmp1_any%valueArgu)
+    val tmp2_any2%valueArgu = ValueArgument("any2", "kotlin.Any", any2.toString(), any2.hashCode(), Certain(false))
+    tmp0_currentValueArguments.add(tmp2_any2%valueArgu)
+    val tmp3_invalidationReason = ComposableInvalidationTraceTableImpl%RestartableComposableKt.computeInvalidationReason("fun-blockComposable(Any,Any)Unit/pkg-land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip/file-restartableComposable.kt", tmp0_currentValueArguments)
+    ComposeInvestigatorConfig.logger.log(ComposableInformation("blockComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "restartableComposable.kt"), Processed(tmp3_invalidationReason))
     use(any)
     use(any2)
     if (isTraceInProgress()) {
       traceEventEnd()
     }
@@ -39,10 +47,17 @@
   }
   if (%dirty and 0b00010011 != 0b00010010 || !%composer.skipping) {
     if (isTraceInProgress()) {
       traceEventStart(<>, %dirty, -1, <>)
     }
+    val tmp0_currentValueArguments = mutableListOf()
+    val tmp1_any%valueArgu = ValueArgument("any", "kotlin.Int", any.toString(), any.hashCode(), Certain(true))
+    tmp0_currentValueArguments.add(tmp1_any%valueArgu)
+    val tmp2_any2%valueArgu = ValueArgument("any2", "kotlin.Int", any2.toString(), any2.hashCode(), Certain(true))
+    tmp0_currentValueArguments.add(tmp2_any2%valueArgu)
+    val tmp3_invalidationReason = ComposableInvalidationTraceTableImpl%RestartableComposableKt.computeInvalidationReason("fun-blockStableComposable(Int,Int)Unit/pkg-land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip/file-restartableComposable.kt", tmp0_currentValueArguments)
+    ComposeInvestigatorConfig.logger.log(ComposableInformation("blockStableComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "restartableComposable.kt"), Processed(tmp3_invalidationReason))
     use(any)
     use(any2)
     if (isTraceInProgress()) {
       traceEventEnd()
     }
@@ -57,10 +72,17 @@
 private fun expressionComposable(any: Any, any2: Any, %composer: Composer?, %changed: Int): Int {
   %composer.startReplaceGroup(<>)
   if (isTraceInProgress()) {
     traceEventStart(<>, %changed, -1, <>)
   }
+  val tmp0_currentValueArguments = mutableListOf()
+  val tmp1_any%valueArgu = ValueArgument("any", "kotlin.Any", any.toString(), any.hashCode(), Certain(false))
+  tmp0_currentValueArguments.add(tmp1_any%valueArgu)
+  val tmp2_any2%valueArgu = ValueArgument("any2", "kotlin.Any", any2.toString(), any2.hashCode(), Certain(false))
+  tmp0_currentValueArguments.add(tmp2_any2%valueArgu)
+  val tmp3_invalidationReason = ComposableInvalidationTraceTableImpl%RestartableComposableKt.computeInvalidationReason("fun-expressionComposable(Any,Any)Int/pkg-land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip/file-restartableComposable.kt", tmp0_currentValueArguments)
+  ComposeInvestigatorConfig.logger.log(ComposableInformation("expressionComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "restartableComposable.kt"), Processed(tmp3_invalidationReason))
   val tmp0 = use(any) + use(any2)
   if (isTraceInProgress()) {
     traceEventEnd()
   }
   %composer.endReplaceGroup()
    """
  }
}
