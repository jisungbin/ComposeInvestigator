/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.lower

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
@@ -22,10 +23,11 @@
     tmp0
     if (isTraceInProgress()) {
       traceEventEnd()
     }
   } else {
+    ComposeInvestigatorConfig.logger.log(ComposableInformation("blockComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "movableComposable.kt"), Skipped)
     %composer.skipToGroupEnd()
   }
   %composer.endRestartGroup()?.updateScope { %composer: Composer?, %force: Int ->
     blockComposable(any, any2, %composer, updateChangedFlags(%changed or 0b0001))
   }
@@ -51,10 +53,11 @@
     tmp0
     if (isTraceInProgress()) {
       traceEventEnd()
     }
   } else {
+    ComposeInvestigatorConfig.logger.log(ComposableInformation("blockStableComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "movableComposable.kt"), Skipped)
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
@@ -20,10 +21,11 @@
     use(any2)
     if (isTraceInProgress()) {
       traceEventEnd()
     }
   } else {
+    ComposeInvestigatorConfig.logger.log(ComposableInformation("blockComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "readonlyComposable.kt"), Skipped)
     %composer.skipToGroupEnd()
   }
   %composer.endRestartGroup()?.updateScope { %composer: Composer?, %force: Int ->
     blockComposable(any, any2, %composer, updateChangedFlags(%changed or 0b0001))
   }
@@ -47,10 +49,11 @@
     use(any2)
     if (isTraceInProgress()) {
       traceEventEnd()
     }
   } else {
+    ComposeInvestigatorConfig.logger.log(ComposableInformation("blockStableComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "readonlyComposable.kt"), Skipped)
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
@@ -19,10 +20,11 @@
     use(any2)
     if (isTraceInProgress()) {
       traceEventEnd()
     }
   } else {
+    ComposeInvestigatorConfig.logger.log(ComposableInformation("blockComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "restartableComposable.kt"), Skipped)
     %composer.skipToGroupEnd()
   }
   %composer.endRestartGroup()?.updateScope { %composer: Composer?, %force: Int ->
     blockComposable(any, any2, %composer, updateChangedFlags(%changed or 0b0001))
   }
@@ -45,10 +47,11 @@
     use(any2)
     if (isTraceInProgress()) {
       traceEventEnd()
     }
   } else {
+    ComposeInvestigatorConfig.logger.log(ComposableInformation("blockStableComposable", "land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip", "restartableComposable.kt"), Skipped)
     %composer.skipToGroupEnd()
   }
   %composer.endRestartGroup()?.updateScope { %composer: Composer?, %force: Int ->
     blockStableComposable(any, any2, %composer, updateChangedFlags(%changed or 0b0001))
   }
    """
  }
}
