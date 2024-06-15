/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.stability

import androidx.compose.compiler.plugins.kotlin.analysis.Stability
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import assertk.assertions.single
import io.mockk.every
import io.mockk.mockk
import land.sungbin.composeinvestigator.compiler.analysis.toIrOwnStability
import land.sungbin.composeinvestigator.compiler.test._compilation.AbstractK2CompilerTest
import land.sungbin.composeinvestigator.compiler.test._source.source
import land.sungbin.composeinvestigator.compiler.test.cast
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
import org.junit.Test

class StabilityWrapperTest : AbstractK2CompilerTest() {
  @Test fun stability_certain_to_IrDeclarationStability() {
    var stableStability: IrConstructorCall? = null
    var unstableStability: IrConstructorCall? = null

    compileToIr(
      sourceFiles = listOf(source("EmptySource.kt")),
      additionalRegisterExtensions = {
        IrGenerationExtension.registerExtension(
          project = this,
          extension = object : IrGenerationExtension {
            override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
              stableStability = Stability.Stable.toIrOwnStability(pluginContext)
              unstableStability = Stability.Unstable.toIrOwnStability(pluginContext)
            }
          },
        )
      },
    )

    assertThat(stableStability!!.type.classFqName?.asString()).isEqualTo("laIrExpressionnd.sungbin.composeinvestigator.runtime.DeclarationStability.Certain")
    assertThat(unstableStability!!.type.classFqName?.asString()).isEqualTo("land.sungbin.composeinvestigator.runtime.DeclarationStability.Certain")

    assertThat(stableStability!!.valueArguments)
      .single()
      .isNotNull()
      .cast<IrConst<Boolean>>()
      .prop(IrConst<Boolean>::value)
      .isTrue()
    assertThat(unstableStability!!.valueArguments)
      .single()
      .isNotNull()
      .cast<IrConst<Boolean>>()
      .prop(IrConst<Boolean>::value)
      .isFalse()
  }

  @Test fun stability_runtime_to_IrDeclarationStability() {
    var stability: IrConstructorCall? = null
    val stabilityDeclarationClass = mockk<IrClass> {
      every { name } returns Name.identifier("RuntimeDeclaration")
    }

    compileToIr(
      sourceFiles = listOf(source("EmptySource.kt")),
      additionalRegisterExtensions = {
        IrGenerationExtension.registerExtension(
          project = this,
          extension = object : IrGenerationExtension {
            override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
              stability = Stability.Runtime(stabilityDeclarationClass).toIrOwnStability(pluginContext)
            }
          },
        )
      },
    )

    assertThat(stability!!.type.classFqName?.asString()).isEqualTo("land.sungbin.composeinvestigator.runtime.DeclarationStability.Runtime")

    assertThat(stability!!.valueArguments)
      .single()
      .isNotNull()
      .cast<IrConst<String>>()
      .prop(IrConst<String>::value)
      .isEqualTo("RuntimeDeclaration")
  }

  @Test fun stability_unknown_to_IrDeclarationStability() {
    var stability: IrConstructorCall? = null
    val stabilityDeclarationClass = mockk<IrClass> {
      every { name } returns Name.identifier("UnknownDeclaration")
    }

    compileToIr(
      sourceFiles = listOf(source("EmptySource.kt")),
      additionalRegisterExtensions = {
        IrGenerationExtension.registerExtension(
          project = this,
          extension = object : IrGenerationExtension {
            override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
              stability = Stability.Unknown(stabilityDeclarationClass).toIrOwnStability(pluginContext)
            }
          },
        )
      },
    )

    assertThat(stability!!.type.classFqName?.asString()).isEqualTo("land.sungbin.composeinvestigator.runtime.DeclarationStability.Unknown")

    assertThat(stability!!.valueArguments)
      .single()
      .isNotNull()
      .cast<IrConst<String>>()
      .prop(IrConst<String>::value)
      .isEqualTo("UnknownDeclaration")
  }

  @Test fun stability_parameter_to_IrDeclarationStability() {
    var stability: IrConstructorCall? = null
    val stabilityDeclarationClass = mockk<IrTypeParameter> {
      every { name } returns Name.identifier("ParameterDeclaration")
    }

    compileToIr(
      sourceFiles = listOf(source("EmptySource.kt")),
      additionalRegisterExtensions = {
        IrGenerationExtension.registerExtension(
          project = this,
          extension = object : IrGenerationExtension {
            override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
              stability = Stability.Parameter(stabilityDeclarationClass).toIrOwnStability(pluginContext)
            }
          },
        )
      },
    )

    assertThat(stability!!.type.classFqName?.asString()).isEqualTo("land.sungbin.composeinvestigator.runtime.DeclarationStability.Parameter")

    assertThat(stability!!.valueArguments)
      .single()
      .isNotNull()
      .cast<IrConst<String>>()
      .prop(IrConst<String>::value)
      .isEqualTo("ParameterDeclaration")
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
      additionalRegisterExtensions = {
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
              ).toIrOwnStability(pluginContext)
            }
          },
        )
      },
    )

    assertThat(stability!!.type.classFqName?.asString()).isEqualTo("land.sungbin.composeinvestigator.runtime.DeclarationStability.Combined")

    assertThat(stability!!.valueArguments)
      .single()
      .isNotNull()
      .cast<IrVararg>()
      .prop(IrVararg::elements)
      .also { it.hasSize(5) }
      .given { actual ->
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

        actual.forEachIndexed { index, element ->
          assertThat(element).hasClass<IrConstructorCall>()
          element as IrConstructorCall

          assertThat(element)
            .transform { it.type.classFqName?.asString() }
            .isEqualTo(matchFqNames[index])

          assertThat(element.valueArguments)
            .single()
            .isNotNull()
            .cast<IrConst<*>>()
            .prop(IrConst<*>::value)
            .isEqualTo(matchValues[index])
        }
      }
  }
}
