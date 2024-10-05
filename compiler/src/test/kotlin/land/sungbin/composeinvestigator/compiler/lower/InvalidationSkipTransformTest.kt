// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.lower

import kotlin.test.Ignore
import kotlin.test.Test
import land.sungbin.composeinvestigator.compiler.FeatureFlag
import land.sungbin.composeinvestigator.compiler._compilation.AbstractCompilerTest
import org.jetbrains.kotlin.utils.addToStdlib.enumSetOf

class InvalidationSkipTransformTest : AbstractCompilerTest(
  enumSetOf(FeatureFlag.InvalidationSkipTracing),
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
@@ -22,10 +23,15 @@
     tmp0
     if (isTraceInProgress()) {
       traceEventEnd()
     }
   } else {
+    ComposeInvestigatorConfig.logger.log(ComposableInformation(
+      name = "blockComposable",
+      packageName = "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip",
+      fileName = "movableComposable.kt"
+    ).withCompoundKey(%composer.compoundKeyHash), Skipped)
     %composer.skipToGroupEnd()
   }
   %composer.endRestartGroup()?.updateScope { %composer: Composer?, %force: Int ->
     blockComposable(any, any2, %composer, updateChangedFlags(%changed or 0b0001))
   }
@@ -51,10 +57,15 @@
     tmp0
     if (isTraceInProgress()) {
       traceEventEnd()
     }
   } else {
+    ComposeInvestigatorConfig.logger.log(ComposableInformation(
+      name = "blockStableComposable",
+      packageName = "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip",
+      fileName = "movableComposable.kt"
+    ).withCompoundKey(%composer.compoundKeyHash), Skipped)
     %composer.skipToGroupEnd()
   }
   %composer.endRestartGroup()?.updateScope { %composer: Composer?, %force: Int ->
     blockStableComposable(any, any2, %composer, updateChangedFlags(%changed or 0b0001))
   }
    """
  }

  @Test fun noGroupComposable() = diff(source("noGroupComposable.kt")) {
    """
@@ -1,5 +1,6 @@
+val ComposableInvalidationTraceTableImpl%NoGroupComposableKt: ComposableInvalidationTraceTable = ComposableInvalidationTraceTable()
 private fun use(any: Any): Int {
   return any.hashCode()
 }
 @Composable
 @ExplicitGroupsComposable
    """.trimIndent()
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
@@ -20,10 +21,15 @@
     use(any2)
     if (isTraceInProgress()) {
       traceEventEnd()
     }
   } else {
+    ComposeInvestigatorConfig.logger.log(ComposableInformation(
+      name = "blockComposable",
+      packageName = "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip",
+      fileName = "readonlyComposable.kt"
+    ).withCompoundKey(%composer.compoundKeyHash), Skipped)
     %composer.skipToGroupEnd()
   }
   %composer.endRestartGroup()?.updateScope { %composer: Composer?, %force: Int ->
     blockComposable(any, any2, %composer, updateChangedFlags(%changed or 0b0001))
   }
@@ -47,10 +53,15 @@
     use(any2)
     if (isTraceInProgress()) {
       traceEventEnd()
     }
   } else {
+    ComposeInvestigatorConfig.logger.log(ComposableInformation(
+      name = "blockStableComposable",
+      packageName = "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip",
+      fileName = "readonlyComposable.kt"
+    ).withCompoundKey(%composer.compoundKeyHash), Skipped)
     %composer.skipToGroupEnd()
   }
   %composer.endRestartGroup()?.updateScope { %composer: Composer?, %force: Int ->
     blockStableComposable(any, any2, %composer, updateChangedFlags(%changed or 0b0001))
   }
    """
  }

  // TODO
  //  If the FeatureFlag is tuned so that the table is not needed, consider optimising to skip
  //  creating the table instance. But how often does this happen in the real world?
  @Test fun replaceableComposable() = diff(source("replaceableComposable.kt")) {
    """
@@ -1,5 +1,6 @@
+val ComposableInvalidationTraceTableImpl%ReplaceableComposableKt: ComposableInvalidationTraceTable = ComposableInvalidationTraceTable()
 private fun use(any: Any): Int {
   return any.hashCode()
 }
 @Composable
 @NonRestartableComposable
    """.trimIndent()
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
@@ -19,10 +20,15 @@
     use(any2)
     if (isTraceInProgress()) {
       traceEventEnd()
     }
   } else {
+    ComposeInvestigatorConfig.logger.log(ComposableInformation(
+      name = "blockComposable",
+      packageName = "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip",
+      fileName = "restartableComposable.kt"
+    ).withCompoundKey(%composer.compoundKeyHash), Skipped)
     %composer.skipToGroupEnd()
   }
   %composer.endRestartGroup()?.updateScope { %composer: Composer?, %force: Int ->
     blockComposable(any, any2, %composer, updateChangedFlags(%changed or 0b0001))
   }
@@ -45,10 +51,15 @@
     use(any2)
     if (isTraceInProgress()) {
       traceEventEnd()
     }
   } else {
+    ComposeInvestigatorConfig.logger.log(ComposableInformation(
+      name = "blockStableComposable",
+      packageName = "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip",
+      fileName = "restartableComposable.kt"
+    ).withCompoundKey(%composer.compoundKeyHash), Skipped)
     %composer.skipToGroupEnd()
   }
   %composer.endRestartGroup()?.updateScope { %composer: Composer?, %force: Int ->
     blockStableComposable(any, any2, %composer, updateChangedFlags(%changed or 0b0001))
   }
    """
  }
}
