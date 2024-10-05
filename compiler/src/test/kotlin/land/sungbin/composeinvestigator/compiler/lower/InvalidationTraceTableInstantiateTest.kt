// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.lower

import kotlin.test.Ignore
import kotlin.test.Test
import land.sungbin.composeinvestigator.compiler.FeatureFlag
import land.sungbin.composeinvestigator.compiler._compilation.AbstractCompilerTest
import org.jetbrains.kotlin.utils.addToStdlib.enumSetOf

class InvalidationTraceTableInstantiateTest : AbstractCompilerTest(
  enumSetOf(FeatureFlag.StateInitializerTracking),
  sourceRoot = "lower/traceTableInstantiate",
) {
  @Ignore("FIXME `fun c(l: @Composable () -> Unit)` ==> NO TABLE GENERATED")
  @Test fun allComposableNoInvestigationFile() = clean(source("allComposableNoInvestigationFile.kt"))

  @Test fun composableFunctionFile() = diff(source("composableFunctionFile.kt"), contextSize = 0) {
    """
@@ -1,0 +1,1 @@
+val ComposableInvalidationTraceTableImpl%ComposableFunctionFileKt: ComposableInvalidationTraceTable = ComposableInvalidationTraceTable()
    """
  }

  @Ignore("FIXME `fun c(l: @Composable () -> Unit)` ==> NO TABLE GENERATED")
  @Test fun composableLambdaFile() = diff(source("composableLambdaFile.kt"), contextSize = 0) {
    """
      
    """
  }

  @Test fun fileNameWithWhitespace() = diff(source("file name with whitespace.kt"), contextSize = 0) {
    """
@@ -1,0 +1,1 @@
+val ComposableInvalidationTraceTableImpl%File_name_with_whitespaceKt: ComposableInvalidationTraceTable = ComposableInvalidationTraceTable()
    """
  }

  @Test fun fileNameWithDash() = diff(source("file-name-with-dash.kt"), contextSize = 0) {
    """
@@ -1,0 +1,1 @@
+val ComposableInvalidationTraceTableImpl%File_name_with_dashKt: ComposableInvalidationTraceTable = ComposableInvalidationTraceTable()
    """
  }

  @Test fun noInvestigationFile() = clean(source("noInvestigationFile.kt"))
}
