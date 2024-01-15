/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.key

import androidx.compose.compiler.plugins.kotlin.WeakBindingTrace
import androidx.compose.compiler.plugins.kotlin.irTrace
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import land.sungbin.composeinvestigator.compiler.internal.key.DurableWritableSlices
import land.sungbin.composeinvestigator.compiler.test.IrBaseTest
import land.sungbin.composeinvestigator.compiler.test.buildIrVisiterRegistrar
import land.sungbin.composeinvestigator.compiler.test.kotlinCompilation
import land.sungbin.composeinvestigator.compiler.test.utils.source
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

class TrackerKeyTest : ShouldSpec(), IrBaseTest {
  init {
    should("Generates a unique key for the same function name") {
      @Suppress("LocalVariableName")
      var _irTrace: WeakBindingTrace? = null
      val irFunctions = mutableListOf<IrSimpleFunction>()

      val irVisitor = buildIrVisiterRegistrar { context ->
        object : IrElementVisitorVoid {
          override fun visitModuleFragment(declaration: IrModuleFragment) {
            irFunctions += declaration.files.single().declarations.filterIsInstance<IrSimpleFunction>()
            _irTrace = context.irTrace
            return super.visitModuleFragment(declaration)
          }
        }
      }

      kotlinCompilation(
        workingDir = tempdir(),
        composeCompiling = false,
        verboseLogging = false,
        additionalVisitor = irVisitor,
        sourceFiles = arrayOf(source("key/TrackerKeyTestSource.kt")),
      )

      val irTrace = _irTrace!!

      val zeroArgFn = irFunctions.single { fn -> fn.valueParameters.isEmpty() }
      val oneArgFn = irFunctions.single { fn -> fn.valueParameters.size == 1 }
      val twoArgFn = irFunctions.single { fn -> fn.valueParameters.size == 2 }
      val threeArgFn = irFunctions.single { fn -> fn.valueParameters.size == 3 }

      val zeroArgFnKey = irTrace[DurableWritableSlices.DURABLE_FUNCTION_KEY, zeroArgFn]!!
      val oneArgFnKey = irTrace[DurableWritableSlices.DURABLE_FUNCTION_KEY, oneArgFn]!!
      val twoArgFnKey = irTrace[DurableWritableSlices.DURABLE_FUNCTION_KEY, twoArgFn]!!
      val threeArgFnKey = irTrace[DurableWritableSlices.DURABLE_FUNCTION_KEY, threeArgFn]!!

      fun assertAffectedComposable(
        target: IrConstructorCall,
        expectName: String,
        expectPkg: String,
        expectStartLine: Int,
        expectStartColumn: Int,
      ) {
        target.getValueArgument(0).safeAs<IrConst<String>>()?.value shouldBe expectName
        target.getValueArgument(1).safeAs<IrConst<String>>()?.value shouldBe expectPkg
        // 'filePath' value is machine dependent, so we don't test it.
        target.getValueArgument(3).safeAs<IrConst<Int>>()?.value shouldBe expectStartLine
        target.getValueArgument(4).safeAs<IrConst<Int>>()?.value shouldBe expectStartColumn
        // TODO: assert parent (not yet implemented feature)
      }

      zeroArgFnKey.keyName shouldBe "fun-one()Unit/pkg-land.sungbin.composeinvestigator.compiler.test.source.key/file-TrackerKeyTestSource.kt"
      assertAffectedComposable(
        zeroArgFnKey.affectedComposable,
        expectName = "one",
        expectPkg = "land.sungbin.composeinvestigator.compiler.test.source.key",
        expectStartLine = 12,
        expectStartColumn = 0,
      )

      oneArgFnKey.keyName shouldBe "fun-one(Any)Unit/pkg-land.sungbin.composeinvestigator.compiler.test.source.key/file-TrackerKeyTestSource.kt"
      assertAffectedComposable(
        oneArgFnKey.affectedComposable,
        expectName = "one",
        expectPkg = "land.sungbin.composeinvestigator.compiler.test.source.key",
        expectStartLine = 13,
        expectStartColumn = 0,
      )

      twoArgFnKey.keyName shouldBe "fun-one(Any,Any)Unit/pkg-land.sungbin.composeinvestigator.compiler.test.source.key/file-TrackerKeyTestSource.kt"
      assertAffectedComposable(
        twoArgFnKey.affectedComposable,
        expectName = "one",
        expectPkg = "land.sungbin.composeinvestigator.compiler.test.source.key",
        expectStartLine = 14,
        expectStartColumn = 0,
      )

      threeArgFnKey.keyName shouldBe "fun-one(Any,Any,Any)Unit/pkg-land.sungbin.composeinvestigator.compiler.test.source.key/file-TrackerKeyTestSource.kt"
      assertAffectedComposable(
        threeArgFnKey.affectedComposable,
        expectName = "one",
        expectPkg = "land.sungbin.composeinvestigator.compiler.test.source.key",
        expectStartLine = 15,
        expectStartColumn = 0,
      )
    }
  }
}
