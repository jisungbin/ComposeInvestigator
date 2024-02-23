/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.kotlincompiler.facade

import androidx.compose.compiler.plugins.kotlin.ComposePluginRegistrar
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtilRt
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.PsiFileFactoryImpl
import com.intellij.testFramework.LightVirtualFile
import land.sungbin.composeinvestigator.compiler.test.kotlincompiler.exception.TestCompilerException
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.IrMessageCollector
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.IrMessageLogger
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.AnalyzingUtils
import java.nio.charset.StandardCharsets

class SourceFile(
  val name: String,
  val source: String,
  private val ignoreParseErrors: Boolean = false,
  val path: String = "",
) {
  fun toKtFile(project: Project): KtFile {
    val shortName = name.substring(name.lastIndexOf('/') + 1).let { name ->
      name.substring(name.lastIndexOf('\\') + 1)
    }

    val virtualFile = object : LightVirtualFile(
      /* name = */
      shortName,
      /* language = */
      KotlinLanguage.INSTANCE,
      /* text = */
      StringUtilRt.convertLineSeparators(source),
    ) {
      override fun getPath(): String = "${this@SourceFile.path}/$name"
    }
    virtualFile.charset = StandardCharsets.UTF_8

    val factory = PsiFileFactory.getInstance(project) as PsiFileFactoryImpl
    val ktFile = factory.trySetupPsiForFile(
      /* virtualFile = */
      virtualFile,
      /* language = */
      KotlinLanguage.INSTANCE,
      /* physical = */
      true,
      /* markAsCopy = */
      false,
    ) as KtFile

    if (!ignoreParseErrors) {
      try {
        AnalyzingUtils.checkForSyntacticErrors(ktFile)
      } catch (exception: Exception) {
        throw TestCompilerException(exception)
      }
    }
    return ktFile
  }
}

interface AnalysisResult {
  data class Diagnostic(val factoryName: String, val textRanges: List<TextRange>)

  val files: List<KtFile>
  val diagnostics: Map<String, List<Diagnostic>>
}

abstract class KotlinCompilerFacade(val environment: KotlinCoreEnvironment) {
  abstract fun analyze(platformFiles: List<SourceFile>, commonFiles: List<SourceFile>): AnalysisResult

  abstract fun compile(platformFiles: List<SourceFile>, commonFiles: List<SourceFile>): GenerationState
  abstract fun compileToIr(files: List<SourceFile>): IrModuleFragment

  companion object {
    fun create(
      disposable: Disposable,
      updateConfiguration: CompilerConfiguration.() -> Unit,
      registerExtensions: Project.(CompilerConfiguration) -> Unit,
    ): KotlinCompilerFacade {
      val configuration = CompilerConfiguration().apply {
        put(CommonConfigurationKeys.MODULE_NAME, "test-module")
        put(JVMConfigurationKeys.IR, true)
        put(JVMConfigurationKeys.VALIDATE_IR, true)
        put(JVMConfigurationKeys.JVM_TARGET, JvmTarget.JVM_17)
        put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, TestMessageCollector)
        put(IrMessageLogger.IR_MESSAGE_LOGGER, IrMessageCollector(TestMessageCollector))
        updateConfiguration()
        put(CommonConfigurationKeys.USE_FIR, languageVersionSettings.languageVersion.usesK2)
      }

      val environment = KotlinCoreEnvironment.createForTests(
        parentDisposable = disposable,
        initialConfiguration = configuration,
        extensionConfigs = EnvironmentConfigFiles.JVM_CONFIG_FILES,
      )

      ComposePluginRegistrar.checkCompilerVersion(configuration)

      environment.project.registerExtensions(configuration)

      return if (configuration.languageVersionSettings.languageVersion.usesK2) {
        K2CompilerFacade(environment)
      } else {
        K1CompilerFacade(environment)
      }
    }
  }
}

object TestMessageCollector : MessageCollector {
  override fun clear() {}
  override fun hasErrors() = false

  override fun report(
    severity: CompilerMessageSeverity,
    message: String,
    location: CompilerMessageSourceLocation?,
  ) {
    if (severity === CompilerMessageSeverity.ERROR) {
      val finalMessage = if (location == null) message else "(${location.path}:${location.line}:${location.column}) $message"
      throw AssertionError(finalMessage)
    }
  }
}
