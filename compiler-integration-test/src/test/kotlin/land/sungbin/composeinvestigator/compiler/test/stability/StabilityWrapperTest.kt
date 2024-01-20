/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.stability

import androidx.compose.compiler.plugins.kotlin.analysis.Stability
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import land.sungbin.composeinvestigator.compiler.analysis.toIrDeclarationStability
import land.sungbin.composeinvestigator.compiler.test.IrBaseTest
import land.sungbin.composeinvestigator.compiler.test.buildIrVisiterRegistrar
import land.sungbin.composeinvestigator.compiler.test.emptyIrElementVisitor
import land.sungbin.composeinvestigator.compiler.test.kotlinCompilation
import land.sungbin.composeinvestigator.compiler.test.utils.source
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.addToStdlib.cast

class StabilityWrapperTest : FunSpec(), IrBaseTest {
  init {
    test("Stability.Certain to IrDeclarationStability") {
      var stableStability: IrConstructorCall? = null
      var unstableStability: IrConstructorCall? = null

      val irVisitor = buildIrVisiterRegistrar { context ->
        stableStability = Stability.Stable.toIrDeclarationStability(context)
        unstableStability = Stability.Unstable.toIrDeclarationStability(context)

        emptyIrElementVisitor()
      }

      kotlinCompilation(
        workingDir = tempdir(),
        composeCompiling = false,
        verboseLogging = false,
        additionalVisitor = irVisitor,
        sourceFiles = arrayOf(source("EmptySource.kt")),
      )

      stableStability!!.type.classFqName?.asString() shouldBe "land.sungbin.composeinvestigator.runtime.DeclarationStability.Certain"
      unstableStability!!.type.classFqName?.asString() shouldBe "land.sungbin.composeinvestigator.runtime.DeclarationStability.Certain"

      with(stableStability!!.valueArguments.single()) {
        shouldBeInstanceOf<IrConst<Boolean>>().value shouldBe true
      }
      with(unstableStability!!.valueArguments.single()) {
        shouldBeInstanceOf<IrConst<Boolean>>().value shouldBe false
      }
    }
    test("Stability.Runtime to IrDeclarationStability") {
      var stability: IrConstructorCall? = null
      val stabilityDeclarationClass = mockk<IrClass> {
        every { name } returns Name.identifier("RuntimeDeclaration")
      }

      val irVisitor = buildIrVisiterRegistrar { context ->
        stability = Stability.Runtime(stabilityDeclarationClass).toIrDeclarationStability(context)

        emptyIrElementVisitor()
      }

      kotlinCompilation(
        workingDir = tempdir(),
        composeCompiling = false,
        verboseLogging = false,
        additionalVisitor = irVisitor,
        sourceFiles = arrayOf(source("EmptySource.kt")),
      )

      stability!!.type.classFqName?.asString() shouldBe "land.sungbin.composeinvestigator.runtime.DeclarationStability.Runtime"

      with(stability!!.valueArguments.single()) {
        shouldBeInstanceOf<IrConst<String>>().value shouldBe "RuntimeDeclaration"
      }
    }
    test("Stability.Unknown to IrDeclarationStability") {
      var stability: IrConstructorCall? = null
      val stabilityDeclarationClass = mockk<IrClass> {
        every { name } returns Name.identifier("UnknownDeclaration")
      }

      val irVisitor = buildIrVisiterRegistrar { context ->
        stability = Stability.Unknown(stabilityDeclarationClass).toIrDeclarationStability(context)

        emptyIrElementVisitor()
      }

      kotlinCompilation(
        workingDir = tempdir(),
        composeCompiling = false,
        verboseLogging = false,
        additionalVisitor = irVisitor,
        sourceFiles = arrayOf(source("EmptySource.kt")),
      )

      stability!!.type.classFqName?.asString() shouldBe "land.sungbin.composeinvestigator.runtime.DeclarationStability.Unknown"

      with(stability!!.valueArguments.single()) {
        shouldBeInstanceOf<IrConst<String>>().value shouldBe "UnknownDeclaration"
      }
    }
    test("Stability.Parameter to IrDeclarationStability") {
      var stability: IrConstructorCall? = null
      val stabilityDeclarationClass = mockk<IrTypeParameter> {
        every { name } returns Name.identifier("ParameterDeclaration")
      }

      val irVisitor = buildIrVisiterRegistrar { context ->
        stability = Stability.Parameter(stabilityDeclarationClass).toIrDeclarationStability(context)

        emptyIrElementVisitor()
      }

      kotlinCompilation(
        workingDir = tempdir(),
        composeCompiling = false,
        verboseLogging = false,
        additionalVisitor = irVisitor,
        sourceFiles = arrayOf(source("EmptySource.kt")),
      )

      stability!!.type.classFqName?.asString() shouldBe "land.sungbin.composeinvestigator.runtime.DeclarationStability.Parameter"

      with(stability!!.valueArguments.single()) {
        shouldBeInstanceOf<IrConst<String>>().value shouldBe "ParameterDeclaration"
      }
    }
    test("Stability.Combined to IrDeclarationStability") {
      var stability: IrConstructorCall? = null
      val runtimeDeclarationClass = mockk<IrClass> {
        every { name } returns Name.identifier("RuntimeDeclaration")
      }
      val unknownDeclarationClass = mockk<IrClass> {
        every { name } returns Name.identifier("UnknownDeclaration")
      }
      val parameterDeclarationClass = mockk<IrTypeParameter> {
        every { name } returns Name.identifier("ParameterDeclaration")
      }

      val irVisitor = buildIrVisiterRegistrar { context ->
        stability = Stability.Combined(
          listOf(
            Stability.Stable,
            Stability.Unstable,
            Stability.Runtime(runtimeDeclarationClass),
            Stability.Unknown(unknownDeclarationClass),
            Stability.Parameter(parameterDeclarationClass),
          ),
        ).toIrDeclarationStability(context)

        emptyIrElementVisitor()
      }

      kotlinCompilation(
        workingDir = tempdir(),
        composeCompiling = false,
        verboseLogging = false,
        additionalVisitor = irVisitor,
        sourceFiles = arrayOf(source("EmptySource.kt")),
      )

      stability!!.type.classFqName?.asString() shouldBe "land.sungbin.composeinvestigator.runtime.DeclarationStability.Combined"

      with(stability!!.valueArguments.single()) {
        shouldBeInstanceOf<IrVararg>().elements shouldHaveSize 5

        val matchFqNames = listOf(
          "land.sungbin.composeinvestigator.runtime.DeclarationStability.Certain", // Certain(true)
          "land.sungbin.composeinvestigator.runtime.DeclarationStability.Certain", // Certain(false)
          "land.sungbin.composeinvestigator.runtime.DeclarationStability.Runtime", // Runtime
          "land.sungbin.composeinvestigator.runtime.DeclarationStability.Unknown", // Unknown
          "land.sungbin.composeinvestigator.runtime.DeclarationStability.Parameter", // Parameter
        )
        val matchValues = listOf(
          true, // Certain(true)
          false, // Certain(false)
          "RuntimeDeclaration", // Runtime(RuntimeDeclaration)
          "UnknownDeclaration", // Unknown(UnknownDeclaration)
          "ParameterDeclaration", // Parameter(ParameterDeclaration)
        )

        cast<IrVararg>().elements.forEachIndexed { index, element ->
          with(element) {
            shouldBeInstanceOf<IrConstructorCall>().type.classFqName?.asString() shouldBe matchFqNames[index]
            valueArguments.single().shouldBeInstanceOf<IrConst<*>>().value shouldBe matchValues[index]
          }
        }
      }
    }
  }
}
