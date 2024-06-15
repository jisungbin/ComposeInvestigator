/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.key

import androidx.compose.compiler.plugins.kotlin.WeakBindingTrace
import androidx.compose.compiler.plugins.kotlin.irTrace
import assertk.assertThat
import assertk.assertions.isEqualTo
import land.sungbin.composeinvestigator.compiler.analysis.DurationWritableSlices
import land.sungbin.composeinvestigator.compiler.test._compilation.AbstractK2CompilerTest
import land.sungbin.composeinvestigator.compiler.test._source.source
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.utils.addToStdlib.safeAs
import org.junit.Test

class TracerKeyTest : AbstractK2CompilerTest() {
  @Test fun generates_a_unique_key_for_the_same_function_name() {
    @Suppress("LocalVariableName")
    var _irTrace: WeakBindingTrace? = null
    val result = compileToIr(
      sourceFiles = listOf(source("key/TracerKeyTestSource.kt")),
      additionalRegisterExtensions = {
        IrGenerationExtension.registerExtension(
          project = this,
          extension = object : IrGenerationExtension {
            override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
              _irTrace = pluginContext.irTrace
            }
          },
        )
      },
    )

    val irFunctions = result.files.single().declarations.filterIsInstance<IrSimpleFunction>()
    val irTrace = _irTrace!!

    val zeroArgFn = irFunctions.single { fn -> fn.valueParameters.isEmpty() }
    val oneArgFn = irFunctions.single { fn -> fn.valueParameters.size == 1 }
    val twoArgFn = irFunctions.single { fn -> fn.valueParameters.size == 2 }
    val threeArgFn = irFunctions.single { fn -> fn.valueParameters.size == 3 }

    val zeroArgFnKey = irTrace[DurationWritableSlices.DURABLE_FUNCTION_KEY, zeroArgFn]!!
    val oneArgFnKey = irTrace[DurationWritableSlices.DURABLE_FUNCTION_KEY, oneArgFn]!!
    val twoArgFnKey = irTrace[DurationWritableSlices.DURABLE_FUNCTION_KEY, twoArgFn]!!
    val threeArgFnKey = irTrace[DurationWritableSlices.DURABLE_FUNCTION_KEY, threeArgFn]!!

    fun assertAffectedComposable(
      target: IrConstructorCall,
      expectName: String,
      expectPkg: String,
      expectStartLine: Int,
      expectStartColumn: Int,
    ) {
      assertThat(target.getValueArgument(0).safeAs<IrConst<String>>()?.value).isEqualTo(expectName)
      assertThat(target.getValueArgument(1).safeAs<IrConst<String>>()?.value).isEqualTo(expectPkg)
      // 'filePath' value is machine dependent, so we don't test it.
      assertThat(target.getValueArgument(3).safeAs<IrConst<Int>>()?.value).isEqualTo(expectStartLine)
      assertThat(target.getValueArgument(4).safeAs<IrConst<Int>>()?.value).isEqualTo(expectStartColumn)
      // TODO: assert parent (not yet implemented feature)
    }

    assertThat(zeroArgFnKey.keyName).isEqualTo("fun-one()Unit/pkg-land.sungbin.composeinvestigator.compiler.test._source.key/file-TracerKeyTestSource.kt")
    assertAffectedComposable(
      zeroArgFnKey.affectedComposable,
      expectName = "one",
      expectPkg = "land.sungbin.composeinvestigator.compiler.test._source.key",
      expectStartLine = 12,
      expectStartColumn = 0,
    )

    assertThat(oneArgFnKey.keyName).isEqualTo("fun-one(Any)Unit/pkg-land.sungbin.composeinvestigator.compiler.test._source.key/file-TracerKeyTestSource.kt")
    assertAffectedComposable(
      oneArgFnKey.affectedComposable,
      expectName = "one",
      expectPkg = "land.sungbin.composeinvestigator.compiler.test._source.key",
      expectStartLine = 13,
      expectStartColumn = 0,
    )

    assertThat(twoArgFnKey.keyName).isEqualTo("fun-one(Any,Any)Unit/pkg-land.sungbin.composeinvestigator.compiler.test._source.key/file-TracerKeyTestSource.kt")
    assertAffectedComposable(
      twoArgFnKey.affectedComposable,
      expectName = "one",
      expectPkg = "land.sungbin.composeinvestigator.compiler.test._source.key",
      expectStartLine = 14,
      expectStartColumn = 0,
    )

    assertThat(threeArgFnKey.keyName).isEqualTo("fun-one(Any,Any,Any)Unit/pkg-land.sungbin.composeinvestigator.compiler.test._source.key/file-TracerKeyTestSource.kt")
    assertAffectedComposable(
      threeArgFnKey.affectedComposable,
      expectName = "one",
      expectPkg = "land.sungbin.composeinvestigator.compiler.test._source.key",
      expectStartLine = 15,
      expectStartColumn = 0,
    )
  }
}
