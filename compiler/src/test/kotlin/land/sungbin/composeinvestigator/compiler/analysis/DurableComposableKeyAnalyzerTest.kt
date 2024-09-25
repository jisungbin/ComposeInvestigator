/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.analysis

import androidx.compose.compiler.plugins.kotlin.irTrace
import kotlin.test.Test
import kotlin.test.assertEquals
import land.sungbin.composeinvestigator.compiler.FeatureFlag
import land.sungbin.composeinvestigator.compiler._compilation.AbstractCompilerTest
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.utils.addToStdlib.enumSetOf
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

class DurableComposableKeyAnalyzerTest : AbstractCompilerTest(
  enumSetOf(FeatureFlag.InvalidationProcessTracing),
  sourceRoot = "analysis/durableComposableKey",
) {
  @Test fun generates_a_unique_key_for_the_same_function_name() {
    val result = compile(source("sameFunctionNames.kt"))
    val trace = result.pluginContext.irTrace

    val functions = result.irModuleFragment
      .files.first()
      .declarations
      .filterIsInstance<IrSimpleFunction>()

    val zeroArgFn = functions.first { fn -> fn.valueParameters.isEmpty() }
    val oneArgFn = functions.first { fn -> fn.valueParameters.size == 1 }
    val twoArgFn = functions.first { fn -> fn.valueParameters.size == 2 }
    val threeArgFn = functions.first { fn -> fn.valueParameters.size == 3 }

    val zeroArgFnKey = trace[DurationWritableSlices.DURABLE_FUNCTION_KEY, zeroArgFn]!!
    val oneArgFnKey = trace[DurationWritableSlices.DURABLE_FUNCTION_KEY, oneArgFn]!!
    val twoArgFnKey = trace[DurationWritableSlices.DURABLE_FUNCTION_KEY, twoArgFn]!!
    val threeArgFnKey = trace[DurationWritableSlices.DURABLE_FUNCTION_KEY, threeArgFn]!!

    fun assertComposable(
      target: IrConstructorCall,
      expectName: String,
      expectPackage: String,
      expectFilename: String,
    ) {
      assertEquals(expectName, target.getValueArgument(0).safeAs<IrConst>()?.value)
      assertEquals(expectPackage, target.getValueArgument(1).safeAs<IrConst>()?.value)
      assertEquals(expectFilename, target.getValueArgument(2).safeAs<IrConst>()?.value)
    }

    assertEquals("fun-one()Unit/pkg-land.sungbin.composeinvestigator.compiler._source.analysis.durableComposableKey/file-sameFunctionNames.kt", zeroArgFnKey.keyName)
    assertComposable(
      zeroArgFnKey.composable,
      expectName = "one",
      expectPackage = "land.sungbin.composeinvestigator.compiler._source.analysis.durableComposableKey",
      expectFilename = "sameFunctionNames.kt",
    )

    assertEquals("fun-one(Any)Unit/pkg-land.sungbin.composeinvestigator.compiler._source.analysis.durableComposableKey/file-sameFunctionNames.kt", oneArgFnKey.keyName)
    assertComposable(
      oneArgFnKey.composable,
      expectName = "one",
      expectPackage = "land.sungbin.composeinvestigator.compiler._source.analysis.durableComposableKey",
      expectFilename = "sameFunctionNames.kt",
    )

    assertEquals("fun-one(Any,Any)Unit/pkg-land.sungbin.composeinvestigator.compiler._source.analysis.durableComposableKey/file-sameFunctionNames.kt", twoArgFnKey.keyName)
    assertComposable(
      twoArgFnKey.composable,
      expectName = "one",
      expectPackage = "land.sungbin.composeinvestigator.compiler._source.analysis.durableComposableKey",
      expectFilename = "sameFunctionNames.kt",
    )

    assertEquals("fun-one(Any,Any,Any)Unit/pkg-land.sungbin.composeinvestigator.compiler._source.analysis.durableComposableKey/file-sameFunctionNames.kt", threeArgFnKey.keyName)
    assertComposable(
      threeArgFnKey.composable,
      expectName = "one",
      expectPackage = "land.sungbin.composeinvestigator.compiler._source.analysis.durableComposableKey",
      expectFilename = "sameFunctionNames.kt",
    )
  }
}
