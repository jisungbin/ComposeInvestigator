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

class StateInitializerTransformTest : AbstractCompilerTest(
  enumSetOf(FeatureFlag.StateInitializerTracking),
  sourceRoot = "lower/stateInitializer",
) {
  @Test fun delegateStateVariable() = diff(source("delegateStateVariable.kt"), contextSize = 3) {
    """
@@ -1,3 +1,4 @@
+val ComposableInvalidationTraceTableImpl%DelegateStateVariableKt: ComposableInvalidationTraceTable = ComposableInvalidationTraceTable()
 private fun <T> State<T>?.getValue(thisObj: Any?, property: KProperty<*>) {
   return
 }
@@ -10,17 +11,17 @@
 }
 private fun delegateStateVariable() {
   val state by {
-    val state%delegate = mutableStateOf(
+    val state%delegate = ComposableInvalidationTraceTableImpl%DelegateStateVariableKt.registerStateObject(mutableStateOf(
       value = Unit
-    )
+    ), "state")
     get() {
       return state%delegate.getValue(null, ::state%delegate)
     }
   }
   var state2 by {
-    val state2%delegate = run {
+    val state2%delegate = ComposableInvalidationTraceTableImpl%DelegateStateVariableKt.registerStateObject(run {
       CustomState
-    }
+    }, "state2")
     get() {
       return state2%delegate.getValue(null, ::state2%delegate)
     }
@@ -57,20 +58,20 @@
       traceEventStart(<>, %changed, -1, <>)
     }
     val state by {
-      val state%delegate = <block>{
+      val state%delegate = ComposableInvalidationTraceTableImpl%DelegateStateVariableKt.registerStateObject(<block>{
         %composer.startReplaceGroup(<>)
         val tmp0_group = %composer.cache(false) {
           CustomState
         }
         %composer.endReplaceGroup()
         tmp0_group
-      }
+      }, "state")
       get() {
         return state%delegate.getValue(null, ::state%delegate)
       }
     }
     var state2 by {
-      val state2%delegate = <block>{
+      val state2%delegate = ComposableInvalidationTraceTableImpl%DelegateStateVariableKt.registerStateObject(<block>{
         %composer.startReplaceGroup(<>)
         val tmp1_group = %composer.cache(false) {
           run {
@@ -81,7 +82,7 @@
         }
         %composer.endReplaceGroup()
         tmp1_group
-      }
+      }, "state2")
       get() {
         return state2%delegate.getValue(null, ::state2%delegate)
       }
    """
  }

  @Test fun directStateVariable() = diff(source("directStateVariable.kt"), contextSize = 3) {
    """
@@ -1,3 +1,4 @@
+val ComposableInvalidationTraceTableImpl%DirectStateVariableKt: ComposableInvalidationTraceTable = ComposableInvalidationTraceTable()
 private object CustomState2 : State<Unit> {
   val value: Unit
     get() {
@@ -5,12 +6,12 @@
     }
 }
 private fun delegateStateVariable() {
-  val state = mutableStateOf(
+  val state = ComposableInvalidationTraceTableImpl%DirectStateVariableKt.registerStateObject(mutableStateOf(
     value = Unit
-  )
-  var state2 = run {
+  ), "state")
+  var state2 = ComposableInvalidationTraceTableImpl%DirectStateVariableKt.registerStateObject(run {
     CustomState2
-  }
+  }, "state2")
 }
 private fun delegateNullableStateVariable() {
   val nullableState = CustomState2
@@ -26,15 +27,15 @@
     if (isTraceInProgress()) {
       traceEventStart(<>, %changed, -1, <>)
     }
-    val state = <block>{
+    val state = ComposableInvalidationTraceTableImpl%DirectStateVariableKt.registerStateObject(<block>{
       %composer.startReplaceGroup(<>)
       val tmp0_group = %composer.cache(false) {
         CustomState2
       }
       %composer.endReplaceGroup()
       tmp0_group
-    }
-    var state2 = <block>{
+    }, "state")
+    var state2 = ComposableInvalidationTraceTableImpl%DirectStateVariableKt.registerStateObject(<block>{
       %composer.startReplaceGroup(<>)
       val tmp1_group = %composer.cache(false) {
         run {
@@ -45,7 +46,7 @@
       }
       %composer.endReplaceGroup()
       tmp1_group
-    }
+    }, "state2")
     if (isTraceInProgress()) {
       traceEventEnd()
     }
    """
  }
}
