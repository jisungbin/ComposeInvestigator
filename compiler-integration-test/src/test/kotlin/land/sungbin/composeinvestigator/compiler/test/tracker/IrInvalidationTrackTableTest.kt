/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.tracker

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import land.sungbin.composeinvestigator.compiler.internal.tracker.IrInvalidationTrackTable
import land.sungbin.composeinvestigator.compiler.test.IrBaseTest
import land.sungbin.composeinvestigator.compiler.test.buildIrVisiterRegistrar
import land.sungbin.composeinvestigator.compiler.test.kotlinCompilation
import land.sungbin.composeinvestigator.compiler.test.utils.source
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.Scope
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.utils.addToStdlib.cast

class IrInvalidationTrackTableTest : FunSpec(), IrBaseTest {
  init {
    test("creation") {
      var table: IrInvalidationTrackTable? = null

      val irVisitor = buildIrVisiterRegistrar { context ->
        object : IrElementVisitorVoid {
          override fun visitModuleFragment(declaration: IrModuleFragment) {
            val currentFile = declaration.files.single()
            table = IrInvalidationTrackTable.create(context, currentFile)
            return super.visitModuleFragment(declaration)
          }
        }
      }

      kotlinCompilation(
        workingDir = tempdir(),
        enableComposeCompiler = false,
        enableVerboseLogging = false,
        additionalVisitor = irVisitor,
        sourceFiles = arrayOf(source("hello.kt")),
      ).compile()

      val tableProp = table!!.prop

      tableProp.name.asString() shouldBe "ComposableInvalidationTrackTableImpl\$HelloKt"
      tableProp.visibility shouldBe DescriptorVisibilities.PRIVATE
      tableProp.modality shouldBe Modality.FINAL

      tableProp.backingField!!.visibility shouldBe DescriptorVisibilities.PRIVATE
      tableProp.backingField!!.type.classFqName?.asString() shouldBe "land.sungbin.composeinvestigator.runtime.ComposableInvalidationTrackTable"

      tableProp.getter!!.visibility shouldBe DescriptorVisibilities.PRIVATE
      tableProp.getter!!.returnType.classFqName?.asString() shouldBe "land.sungbin.composeinvestigator.runtime.ComposableInvalidationTrackTable"
    }
    test("obtainParameterInfo") {
      var table: IrInvalidationTrackTable? = null
      var fileScope: Scope? = null
      var anyType: IrType? = null

      val irVisitor = buildIrVisiterRegistrar { context ->
        object : IrElementVisitorVoid {
          override fun visitModuleFragment(declaration: IrModuleFragment) {
            val currentFile = declaration.files.single()
            table = IrInvalidationTrackTable.create(context, currentFile)
            fileScope = Scope(currentFile.symbol)
            anyType = context.irBuiltIns.anyType
            return super.visitModuleFragment(declaration)
          }
        }
      }

      kotlinCompilation(
        workingDir = tempdir(),
        enableComposeCompiler = false,
        enableVerboseLogging = false,
        additionalVisitor = irVisitor,
        sourceFiles = arrayOf(source("hello.kt")),
      ).compile()

      val anyExpression = mockk<IrExpression> {
        every { type } returns anyType!!
        every { startOffset } returns UNDEFINED_OFFSET
        every { endOffset } returns UNDEFINED_OFFSET
      }

      val name = fileScope!!.createTemporaryVariable(anyExpression)
      val stability = fileScope!!.createTemporaryVariable(anyExpression)
      val valueString = fileScope!!.createTemporaryVariable(anyExpression)
      val valueHashCode = fileScope!!.createTemporaryVariable(anyExpression)

      val paramInfo = table!!.obtainParameterInfo(
        name = name,
        stability = stability,
        valueString = valueString,
        valueHashCode = valueHashCode,
      )

      paramInfo.type.classFqName?.asString() shouldBe "land.sungbin.composeinvestigator.runtime.ParameterInfo"

      paramInfo.valueArgumentsCount shouldBe 4
      paramInfo.getValueArgument(0).cast<IrGetValue>().symbol shouldBe name.symbol
      paramInfo.getValueArgument(1).cast<IrGetValue>().symbol shouldBe stability.symbol
      paramInfo.getValueArgument(2).cast<IrGetValue>().symbol shouldBe valueString.symbol
      paramInfo.getValueArgument(3).cast<IrGetValue>().symbol shouldBe valueHashCode.symbol
    }
    test("computeDiffParamsIfPresent") {
      var table: IrInvalidationTrackTable? = null

      val irVisitor = buildIrVisiterRegistrar { context ->
        object : IrElementVisitorVoid {
          override fun visitModuleFragment(declaration: IrModuleFragment) {
            val currentFile = declaration.files.single()
            table = IrInvalidationTrackTable.create(context, currentFile)
            return super.visitModuleFragment(declaration)
          }
        }
      }

      kotlinCompilation(
        workingDir = tempdir(),
        enableComposeCompiler = false,
        enableVerboseLogging = false,
        additionalVisitor = irVisitor,
        sourceFiles = arrayOf(source("hello.kt")),
      ).compile()

      val keyName = mockk<IrConst<String>>()
      val originalName = mockk<IrConst<String>>()
      val paramInfos = mockk<IrVararg>()

      val computedDiffParams = table!!.irComputeDiffParamsIfPresent(
        keyName = keyName,
        originalName = originalName,
        paramInfos = paramInfos,
      )

      computedDiffParams.type.classFqName?.asString() shouldBe "land.sungbin.composeinvestigator.runtime.DiffParams"

      computedDiffParams.valueArgumentsCount shouldBe 3
      computedDiffParams.getValueArgument(0) shouldBe keyName
      computedDiffParams.getValueArgument(1) shouldBe originalName
      computedDiffParams.getValueArgument(2) shouldBe paramInfos
    }
  }
}
