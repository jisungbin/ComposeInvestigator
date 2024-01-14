/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test

import androidx.compose.compiler.plugins.kotlin.ComposeCommandLineProcessor
import androidx.compose.compiler.plugins.kotlin.ComposePluginRegistrar
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.PluginOption
import com.tschuchort.compiletesting.SourceFile
import land.sungbin.composeinvestigator.compiler.ComposeInvestigatorCommandLineProcessor
import land.sungbin.composeinvestigator.compiler.ComposeInvestigatorPluginRegistrar
import land.sungbin.composeinvestigator.compiler.test.utils.source
import org.jetbrains.kotlin.cli.common.toBooleanLenient
import org.jetbrains.kotlin.config.JvmTarget
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class IrDumpingTest {
  @get:Rule
  val tempDir: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

  @Test
  fun debug() {
    compile(source("SourceForIrDump.kt"))
  }

  private fun compile(vararg sourceFiles: SourceFile) = prepareCompilation(*sourceFiles).compile()

  private fun prepareCompilation(vararg sourceFiles: SourceFile) =
    KotlinCompilation().apply {
      workingDir = tempDir.root
      sources = sourceFiles.asList()
      jvmTarget = JvmTarget.JVM_17.toString()
      inheritClassPath = true
      supportsK2 = false
      pluginOptions = listOf(
        PluginOption(
          pluginId = ComposeInvestigatorCommandLineProcessor.PLUGIN_ID,
          optionName = ComposeInvestigatorCommandLineProcessor.OPTION_VERBOSE.optionName,
          optionValue = if (System.getenv("CI").toBooleanLenient() == true) "false" else "true",
        ),
        PluginOption(
          pluginId = ComposeCommandLineProcessor.PLUGIN_ID,
          optionName = ComposeCommandLineProcessor.LIVE_LITERALS_V2_ENABLED_OPTION.optionName,
          optionValue = "false",
        ),
      )
      @Suppress("DEPRECATION")
      componentRegistrars = listOf(ComposePluginRegistrar(), ComposeInvestigatorPluginRegistrar())
      commandLineProcessors = listOf(ComposeCommandLineProcessor(), ComposeInvestigatorCommandLineProcessor())
    }
}
