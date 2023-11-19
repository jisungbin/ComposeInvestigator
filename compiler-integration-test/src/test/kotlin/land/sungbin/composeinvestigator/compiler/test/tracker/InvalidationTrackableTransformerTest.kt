/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.tracker

import io.kotest.assertions.fail
import io.kotest.core.annotation.Ignored
import io.kotest.core.spec.style.StringSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.types.shouldBeTypeOf
import land.sungbin.composeinvestigator.compiler.internal.origin.InvalidationTrackerOrigin
import land.sungbin.composeinvestigator.compiler.test.IrBaseTest
import land.sungbin.composeinvestigator.compiler.test.buildIrVisiterRegistrar
import land.sungbin.composeinvestigator.compiler.test.kotlinCompilation
import land.sungbin.composeinvestigator.compiler.test.utils.source
import org.jetbrains.kotlin.backend.jvm.JvmLoweredDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrElseBranch
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrWhenImpl
import org.jetbrains.kotlin.ir.symbols.IrVariableSymbol
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

// [transformation scenarios]
// - invalidation processed
// - invalidation skipped
//
// [function location]
// - top-level
// - top-level inline
// - class
// - object
// - local
// - lambda
// - parameter
// - companion object
// - anonymous object
@Ignored("TODO")
class InvalidationTrackableTransformerTest : StringSpec(), IrBaseTest {
  init {
    "A statement is added to print that the invalidation is in progress, and to look up the arguments that are different from the previous invalidation." {
      var file: IrFile? = null

      val irVisitor = buildIrVisiterRegistrar {
        object : IrElementVisitorVoid {
          override fun visitModuleFragment(declaration: IrModuleFragment) {
            file = declaration.files.single()
            return super.visitModuleFragment(declaration)
          }
        }
      }

      kotlinCompilation(
        workingDir = tempdir(),
        enableComposeCompiler = true,
        enableVerboseLogging = true,
        additionalVisitor = irVisitor,
        sourceFiles = arrayOf(source("tracker/InvalidationTrackableTransformerTestSource.kt")),
      ).compile()

      val classes = file!!.declarations.filterIsInstance<IrClass>()

      val jvmClz = classes.single { clz -> clz.name.asString() == "InvalidationTrackableTransformerTestSourceKt" }
      val classClz = classes.single { clz -> clz.name.asString() == "Class" }
      val objClz = classes.single { clz -> clz.name.asString() == "Object" }
      val companionObjClz = classes.single { clz -> clz.name.asString() == "CompanionObject" }

      jvmClz.irCheckForJvmClz()
      classClz.irCheckForClassClz()
      objClz.irCheckForObjClz()
      companionObjClz.irCheckForCompanionObjClz()
    }
  }

  private fun IrClass.irCheckForJvmClz() {
    val fns = declarations
      .filterIsInstance<IrSimpleFunction>()
      .filterNot { fn -> fn.origin == JvmLoweredDeclarationOrigin.CLASS_STATIC_INITIALIZER }

    for (fn in fns) {
      var valid = false
      fn.body.safeAs<IrBlockBody>()?.statements?.forEach { statement ->
        if (valid) return@forEach
        valid = statement.safeAs<IrBlock>()?.validateTransformed() ?: false
      }
      if (!valid) fail(
        "The transformation did not proceed as expected. " +
          "DUMP: ${fn.dump()}",
      )
    }
  }

  private fun IrClass.irCheckForClassClz() {

  }

  private fun IrClass.irCheckForObjClz() {

  }

  private fun IrClass.irCheckForCompanionObjClz() {

  }

  private fun IrBlock.validateTransformed(): Boolean {
    val expression = this
    if (expression.origin == InvalidationTrackerOrigin) {
      val printlnStatement = expression.statements[1]

      printlnStatement.shouldBeTypeOf<IrCall>()
      printlnStatement.valueArgumentsCount shouldBe 1

      with(printlnStatement.getValueArgument(0)) {
        shouldBeTypeOf<IrConst<String>>()
        value shouldStartWith "[INVALIDATION_TRACKER]"
        value shouldEndWith "invalidation processed"
      }

      for (statement in expression.statements.drop(2)) {
        if (statement is IrWhenImpl && statement.origin == IrStatementOrigin.SAFE_CALL) {
          if (statement.branches.size == 2) {
            val elseBranch = statement.branches.last().safeAs<IrElseBranch>() ?: continue
            val elseCondition = elseBranch.condition.safeAs<IrConst<Boolean>>() ?: continue
            val elseResult = elseBranch.result.safeAs<IrCall>() ?: continue

            if (elseCondition.value) {
              if (elseResult.valueArgumentsCount == 1) {
                val maybePrintlnCallArg = elseResult.getValueArgument(0)
                if (maybePrintlnCallArg is IrCall) { // irToString
                  if (maybePrintlnCallArg.valueArgumentsCount == 1) {
                    val maybeGetValueCall = maybePrintlnCallArg.getValueArgument(0)
                    if (maybeGetValueCall is IrGetValue) { // irGetValue
                      val getValueCallSymbol = maybeGetValueCall.symbol
                      if (getValueCallSymbol is IrVariableSymbol) { // irVariable
                        println(getValueCallSymbol.owner.name.asString())
                        if (getValueCallSymbol.owner.name.asString().endsWith("\$diffParams")) return true
                      }
                    }
                  }
                }
              }
            }
            return true
          }
        }
      }
    }
    return false
  }

  private fun IrElseBranch.validateTransformed() {

  }
}
