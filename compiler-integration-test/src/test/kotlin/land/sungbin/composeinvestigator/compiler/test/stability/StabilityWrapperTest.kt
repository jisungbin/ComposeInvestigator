/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.stability

import androidx.compose.compiler.plugins.kotlin.analysis.Stability
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import land.sungbin.composeinvestigator.compiler.analysis.toIrDeclarationStability
import land.sungbin.composeinvestigator.compiler.test._source.source
import land.sungbin.composeinvestigator.compiler.test.kotlincompiler.AbstractCompilerTest
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.addToStdlib.cast
import org.junit.Test

// FIXME: Failed to lookup symbols with 'fqName == kotlin.collections.MutableList.add',
//  'fn.owner.valueParameters.size == 1' in Kotlin 2.0. Needs to be fixed in the future.
class StabilityWrapperTest(@Suppress("UNUSED_PARAMETER") useFir: Boolean) : AbstractCompilerTest(useFir = false) {
  @Test fun stability_certain_to_IrDeclarationStability() {
    var stableStability: IrConstructorCall? = null
    var unstableStability: IrConstructorCall? = null

    compileToIr(
      sourceFiles = listOf(source("EmptySource.kt")),
      registerExtensions = {
        IrGenerationExtension.registerExtension(
          project = this,
          extension = object : IrGenerationExtension {
            override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
              stableStability = Stability.Stable.toIrDeclarationStability(pluginContext)
              unstableStability = Stability.Unstable.toIrDeclarationStability(pluginContext)
            }
          },
        )
      },
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

  @Test fun stability_runtime_to_IrDeclarationStability() {
    var stability: IrConstructorCall? = null
    val stabilityDeclarationClass = mockk<IrClass> {
      every { name } returns Name.identifier("RuntimeDeclaration")
    }

    compileToIr(
      sourceFiles = listOf(source("EmptySource.kt")),
      registerExtensions = {
        IrGenerationExtension.registerExtension(
          project = this,
          extension = object : IrGenerationExtension {
            override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
              stability = Stability.Runtime(stabilityDeclarationClass).toIrDeclarationStability(pluginContext)
            }
          },
        )
      },
    )

    stability!!.type.classFqName?.asString() shouldBe "land.sungbin.composeinvestigator.runtime.DeclarationStability.Runtime"

    with(stability!!.valueArguments.single()) {
      shouldBeInstanceOf<IrConst<String>>().value shouldBe "RuntimeDeclaration"
    }
  }

  @Test fun stability_unknown_to_IrDeclarationStability() {
    var stability: IrConstructorCall? = null
    val stabilityDeclarationClass = mockk<IrClass> {
      every { name } returns Name.identifier("UnknownDeclaration")
    }

    compileToIr(
      sourceFiles = listOf(source("EmptySource.kt")),
      registerExtensions = {
        IrGenerationExtension.registerExtension(
          project = this,
          extension = object : IrGenerationExtension {
            override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
              stability = Stability.Unknown(stabilityDeclarationClass).toIrDeclarationStability(pluginContext)
            }
          },
        )
      },
    )

    stability!!.type.classFqName?.asString() shouldBe "land.sungbin.composeinvestigator.runtime.DeclarationStability.Unknown"

    with(stability!!.valueArguments.single()) {
      shouldBeInstanceOf<IrConst<String>>().value shouldBe "UnknownDeclaration"
    }
  }

  @Test fun stability_parameter_to_IrDeclarationStability() {
    var stability: IrConstructorCall? = null
    val stabilityDeclarationClass = mockk<IrTypeParameter> {
      every { name } returns Name.identifier("ParameterDeclaration")
    }

    compileToIr(
      sourceFiles = listOf(source("EmptySource.kt")),
      registerExtensions = {
        IrGenerationExtension.registerExtension(
          project = this,
          extension = object : IrGenerationExtension {
            override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
              stability = Stability.Parameter(stabilityDeclarationClass).toIrDeclarationStability(pluginContext)
            }
          },
        )
      },
    )

    stability!!.type.classFqName?.asString() shouldBe "land.sungbin.composeinvestigator.runtime.DeclarationStability.Parameter"

    with(stability!!.valueArguments.single()) {
      shouldBeInstanceOf<IrConst<String>>().value shouldBe "ParameterDeclaration"
    }
  }

  @Test fun stability_combined_to_IrDeclarationStability() {
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

    compileToIr(
      sourceFiles = listOf(source("EmptySource.kt")),
      registerExtensions = {
        IrGenerationExtension.registerExtension(
          project = this,
          extension = object : IrGenerationExtension {
            override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
              stability = Stability.Combined(
                listOf(
                  Stability.Stable,
                  Stability.Unstable,
                  Stability.Runtime(runtimeDeclarationClass),
                  Stability.Unknown(unknownDeclarationClass),
                  Stability.Parameter(parameterDeclarationClass),
                ),
              ).toIrDeclarationStability(pluginContext)
            }
          },
        )
      },
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
