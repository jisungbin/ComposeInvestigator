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
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.PluginOption
import com.tschuchort.compiletesting.SourceFile
import land.sungbin.composeinvestigator.compiler.callstack.ComposableCallstackTrackPluginRegistrar
import land.sungbin.composeinvestigator.compiler.callstack.ComposableCallstackTrackerCommandLineProcessor
import land.sungbin.composeinvestigator.compiler.invalidation.ComposableInvalidationTrackPluginRegistrar
import land.sungbin.composeinvestigator.compiler.invalidation.ComposableInvalidationTrackerCommandLineProcessor
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.toBooleanLenient
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.extensions.LoadingOrder
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
          LoadingOrder.LAST,
          project,
        )
    }
  }

fun IrBaseTest.kotlinCompilation(
  workingDir: File,
  composeCompiling: Boolean = true,
  verboseLogging: Boolean = true,
  @Suppress("DEPRECATION") additionalVisitor: ComponentRegistrar? = null,
  vararg sourceFiles: SourceFile,
): JvmCompilationResult = KotlinCompilation().apply {
  this.workingDir = workingDir
  sources = sourceFiles.asList()
  jvmTarget = JvmTarget.JVM_17.toString()
  inheritClassPath = true
  supportsK2 = false
  pluginOptions = listOf(
    PluginOption(
      pluginId = ComposableCallstackTrackerCommandLineProcessor.PLUGIN_ID,
      optionName = ComposableCallstackTrackerCommandLineProcessor.OPTION_VERBOSE.optionName,
      optionValue = if (System.getenv("CI").toBooleanLenient() == true) "false" else verboseLogging.toString(),
    ),
    PluginOption(
      pluginId = ComposableInvalidationTrackerCommandLineProcessor.PLUGIN_ID,
      optionName = ComposableInvalidationTrackerCommandLineProcessor.OPTION_VERBOSE.optionName,
      optionValue = if (System.getenv("CI").toBooleanLenient() == true) "false" else verboseLogging.toString(),
    ),
  )
  @Suppress("DEPRECATION")
  componentRegistrars = mutableListOf(
    ComposableCallstackTrackPluginRegistrar(),
    ComposableInvalidationTrackPluginRegistrar(),
  ).also { registrars ->
    if (composeCompiling) registrars.add(0, ComposePluginRegistrar())
    if (additionalVisitor != null) registrars.add(additionalVisitor)
  }
  commandLineProcessors = mutableListOf(
    ComposableCallstackTrackerCommandLineProcessor(),
    ComposableInvalidationTrackerCommandLineProcessor(),
  ).also { processors ->
    if (composeCompiling) processors.add(0, ComposeCommandLineProcessor())
  }
}.compile()
