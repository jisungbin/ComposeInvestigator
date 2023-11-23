/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.tracker

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import land.sungbin.composeinvestigator.compiler.internal.tracker.key.DurableWritableSlices
import land.sungbin.composeinvestigator.compiler.internal.tracker.key.KeyBindingTrace
import land.sungbin.composeinvestigator.compiler.internal.tracker.key.irTracee
import land.sungbin.composeinvestigator.compiler.test.IrBaseTest
import land.sungbin.composeinvestigator.compiler.test.buildIrVisiterRegistrar
import land.sungbin.composeinvestigator.compiler.test.kotlinCompilation
import land.sungbin.composeinvestigator.compiler.test.utils.source
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid

class DurableKeyTest : ShouldSpec(), IrBaseTest {
  init {
    should("Generates a unique key for the same function name") {
      val irFunctions = mutableListOf<IrSimpleFunction>()
      var irTrace: KeyBindingTrace? = null

      val irVisitor = buildIrVisiterRegistrar { context ->
        object : IrElementVisitorVoid {
          override fun visitModuleFragment(declaration: IrModuleFragment) {
            irFunctions += declaration.files.single().declarations.filterIsInstance<IrSimpleFunction>()
            irTrace = context.irTracee
            return super.visitModuleFragment(declaration)
          }
        }
      }

      kotlinCompilation(
        workingDir = tempdir(),
        enableComposeCompiler = false,
        enableVerboseLogging = false,
        additionalVisitor = irVisitor,
        sourceFiles = arrayOf(source("tracker/DurableKeyTestSource.kt")),
      )

      val zeroArgFn = irFunctions.single { fn -> fn.valueParameters.isEmpty() }
      val oneArgFn = irFunctions.single { fn -> fn.valueParameters.size == 1 }
      val twoArgFn = irFunctions.single { fn -> fn.valueParameters.size == 2 }
      val threeArgFn = irFunctions.single { fn -> fn.valueParameters.size == 3 }

      val zeroArgFnKey = irTrace!![DurableWritableSlices.DURABLE_FUNCTION_KEY, zeroArgFn]!!
      val oneArgFnKey = irTrace!![DurableWritableSlices.DURABLE_FUNCTION_KEY, oneArgFn]!!
      val twoArgFnKey = irTrace!![DurableWritableSlices.DURABLE_FUNCTION_KEY, twoArgFn]!!
      val threeArgFnKey = irTrace!![DurableWritableSlices.DURABLE_FUNCTION_KEY, threeArgFn]!!

      zeroArgFnKey.keyName shouldBe "fun-one()Unit/pkg-land.sungbin.composeinvestigator.compiler.test.source.tracker/file-DurableKeyTestSource.kt"
      oneArgFnKey.keyName shouldBe "fun-one(Any)Unit/pkg-land.sungbin.composeinvestigator.compiler.test.source.tracker/file-DurableKeyTestSource.kt"
      twoArgFnKey.keyName shouldBe "fun-one(Any,Any)Unit/pkg-land.sungbin.composeinvestigator.compiler.test.source.tracker/file-DurableKeyTestSource.kt"
      threeArgFnKey.keyName shouldBe "fun-one(Any,Any,Any)Unit/pkg-land.sungbin.composeinvestigator.compiler.test.source.tracker/file-DurableKeyTestSource.kt"
    }
  }
}
