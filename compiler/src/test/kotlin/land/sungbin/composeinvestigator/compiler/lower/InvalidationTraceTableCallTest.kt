// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.lower

import land.sungbin.composeinvestigator.compiler.FeatureFlag
import land.sungbin.composeinvestigator.compiler._compilation.AbstractCompilerTest
import org.jetbrains.kotlin.utils.addToStdlib.enumSetOf
import org.junit.jupiter.api.Test

class InvalidationTraceTableCallTest : AbstractCompilerTest(
  enumSetOf(FeatureFlag.InvalidationTraceTableIntrinsicCall),
  sourceRoot = "lower/traceTableCall",
) {
  @Test fun getCurrentComposableKeyName() = diff(source("getCurrentComposableKeyName.kt"), contextSize = 0) {
    """
@@ -1,0 +1,1 @@
+val ComposableInvalidationTraceTableImpl%GetCurrentComposableKeyNameKt: ComposableInvalidationTraceTable = ComposableInvalidationTraceTable()
@@ -8,2 +9,2 @@
-    currentComposableInvalidationTracer.currentComposableKeyName
-    val inVariable = currentComposableInvalidationTracer
+    "fun-getCurrentComposableKeyName()Unit/pkg-land.sungbin.composeinvestigator.compiler._source.lower.traceTableCall/file-getCurrentComposableKeyName.kt"
+    val inVariable = ComposableInvalidationTraceTableImpl%GetCurrentComposableKeyNameKt
@@ -17,1 +18,1 @@
-      currentComposableInvalidationTracer.currentComposableKeyName
+      "fun-nested()Unit/fun-getCurrentComposableKeyName()Unit/pkg-land.sungbin.composeinvestigator.compiler._source.lower.traceTableCall/file-getCurrentComposableKeyName.kt"
@@ -38,1 +39,1 @@
-      keyed = currentComposableInvalidationTracer.currentComposableKeyName
+      keyed = "fun-inFunctionDefaultArgument(String)Unit/pkg-land.sungbin.composeinvestigator.compiler._source.lower.traceTableCall/file-getCurrentComposableKeyName.kt"
    """
  }

  @Test fun getCurrentComposableName() = diff(source("getCurrentComposableName.kt"), contextSize = 0) {
    """
@@ -1,0 +1,1 @@
+val ComposableInvalidationTraceTableImpl%GetCurrentComposableNameKt: ComposableInvalidationTraceTable = ComposableInvalidationTraceTable()
@@ -8,2 +9,2 @@
-    currentComposableInvalidationTracer.currentComposableName
-    val inVariable = currentComposableInvalidationTracer.currentComposableName
+    @ComposableName(name = "getCurrentComposableName")
+    val inVariable = @ComposableName(name = "getCurrentComposableName")
@@ -17,1 +18,1 @@
-      currentComposableInvalidationTracer.currentComposableName
+      @ComposableName(name = "nested")
@@ -41,1 +42,1 @@
-        name = currentComposableInvalidationTracer.currentComposableName
+        name = @ComposableName(name = "inFunctionDefaultArgument")
    """
  }

  @Test fun getCurrentTable() = diff(source("getCurrentTable.kt"), contextSize = 0) {
    """
@@ -1,0 +1,1 @@
+val ComposableInvalidationTraceTableImpl%GetCurrentTableKt: ComposableInvalidationTraceTable = ComposableInvalidationTraceTable()
@@ -8,2 +9,2 @@
-    currentComposableInvalidationTracer
-    val inVariable = currentComposableInvalidationTracer
+    ComposableInvalidationTraceTableImpl%GetCurrentTableKt
+    val inVariable = ComposableInvalidationTraceTableImpl%GetCurrentTableKt
@@ -17,1 +18,1 @@
-      currentComposableInvalidationTracer
+      ComposableInvalidationTraceTableImpl%GetCurrentTableKt
@@ -36,3 +37,13 @@
-  if (%changed and 0b0001 != 0 || !%composer.skipping) {
-    if (%default and 0b0001 != 0) {
-      table = currentComposableInvalidationTracer
+  val %dirty = %changed
+  if (%dirty and 0b0001 != 0 || !%composer.skipping) {
+    %composer.startDefaults()
+    if (%changed and 0b0001 == 0 || %composer.defaultsInvalid) {
+      if (%default and 0b0001 != 0) {
+        table = ComposableInvalidationTraceTableImpl%GetCurrentTableKt
+        %dirty = %dirty and 0b1110.inv()
+      }
+    } else {
+      %composer.skipToGroupEnd()
+      if (%default and 0b0001 != 0) {
+        %dirty = %dirty and 0b1110.inv()
+      }
@@ -40,0 +51,1 @@
+    %composer.endDefaults()
@@ -41,1 +53,1 @@
-      traceEventStart(<>, %changed, -1, <>)
+      traceEventStart(<>, %dirty, -1, <>)
    """
  }

  @Test fun setCurrentComposableName() = diff(source("setCurrentComposableName.kt"), contextSize = 0) {
    """
@@ -1,0 +1,1 @@
+val ComposableInvalidationTraceTableImpl%SetCurrentComposableNameKt: ComposableInvalidationTraceTable = ComposableInvalidationTraceTable()
@@ -8,1 +9,1 @@
-    currentComposableInvalidationTracer.currentComposableName = @ComposableName(name = "AA!")
+    Unit
@@ -15,1 +16,1 @@
-      currentComposableInvalidationTracer.currentComposableName = @ComposableName(name = "BB!")
+      Unit
    """
  }
}
