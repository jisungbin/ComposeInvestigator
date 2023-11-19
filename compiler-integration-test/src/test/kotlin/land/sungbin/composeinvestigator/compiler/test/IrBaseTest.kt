/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("UnusedReceiverParameter")

package land.sungbin.composeinvestigator.compiler.test

import androidx.compose.compiler.plugins.kotlin.ComposeCommandLineProcessor
import androidx.compose.compiler.plugins.kotlin.ComposePluginRegistrar
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.PluginOption
import com.tschuchort.compiletesting.SourceFile
import land.sungbin.composeinvestigator.compiler.COMPOSE_INVESTIGATOR_PLUGIN_ID
import land.sungbin.composeinvestigator.compiler.ComposeInvestigatorCommandLineProcessor
import land.sungbin.composeinvestigator.compiler.ComposeInvestigatorPluginRegistrar
import land.sungbin.composeinvestigator.compiler.OPTION_VERBOSE
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.extensions.LoadingOrder
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import java.io.File

interface IrBaseTest

fun IrBaseTest.emptyIrElementVisitor() = object : IrElementVisitorVoid {}

@Suppress("DEPRECATION")
fun IrBaseTest.buildIrVisiterRegistrar(builder: (context: IrPluginContext) -> IrElementVisitorVoid): ComponentRegistrar =
  object : ComponentRegistrar {
    override val supportsK2 = false
    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
      @Suppress("UnstableApiUsage")
      project.extensionArea
        .getExtensionPoint(IrGenerationExtension.extensionPointName)
        .registerExtension(
          object : IrGenerationExtension {
            override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
              moduleFragment.acceptVoid(builder(pluginContext))
            }
          },
          // TODO: LoadingOrder("after $COMPOSE_INVESTIGATOR_PLUGIN_ID"),
          // https://github.com/JetBrains/intellij-community/blob/17ca1d0afb43f824cda948fc3ea4467ebe55b346/platform/extensions/src/com/intellij/openapi/extensions/LoadingOrder.kt#L24
          LoadingOrder.LAST,
          project,
        )
    }
  }

fun IrBaseTest.kotlinCompilation(
  workingDir: File,
  enableComposeCompiler: Boolean = true,
  enableVerboseLogging: Boolean = true,
  @Suppress("DEPRECATION") additionalVisitor: ComponentRegistrar? = null,
  vararg sourceFiles: SourceFile,
) = KotlinCompilation().apply {
  this.workingDir = workingDir
  sources = sourceFiles.asList()
  jvmTarget = JvmTarget.JVM_17.toString()
  inheritClassPath = true
  supportsK2 = false
  pluginOptions = listOf(
    PluginOption(
      pluginId = COMPOSE_INVESTIGATOR_PLUGIN_ID,
      optionName = OPTION_VERBOSE.optionName,
      optionValue = enableVerboseLogging.toString(),
    ),
  )
  @Suppress("DEPRECATION")
  componentRegistrars = mutableListOf<ComponentRegistrar>(ComposeInvestigatorPluginRegistrar()).also { registrars ->
    if (enableComposeCompiler) registrars.add(0, ComposePluginRegistrar())
    if (additionalVisitor != null) registrars.add(additionalVisitor)
  }
  commandLineProcessors = mutableListOf<CommandLineProcessor>(ComposeInvestigatorCommandLineProcessor()).also { processors ->
    if (enableComposeCompiler) processors.add(0, ComposeCommandLineProcessor())
  }
}
