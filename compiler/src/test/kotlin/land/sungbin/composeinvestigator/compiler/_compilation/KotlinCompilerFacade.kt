/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler._compilation

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtilRt
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.PsiFileFactoryImpl
import com.intellij.testFramework.LightVirtualFile
import java.nio.charset.StandardCharsets
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.IrVerificationMode
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.config.messageCollector
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.AnalyzingUtils

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
      /* name = */ shortName,
      /* language = */ KotlinLanguage.INSTANCE,
      /* text = */ StringUtilRt.convertLineSeparators(source),
    ) {
      override fun getPath(): String = "${this@SourceFile.path}/$name"
    }
    virtualFile.charset = StandardCharsets.UTF_8

    val factory = PsiFileFactory.getInstance(project) as PsiFileFactoryImpl
    val ktFile = factory.trySetupPsiForFile(
      /* lightVirtualFile = */ virtualFile,
      /* language = */ KotlinLanguage.INSTANCE,
      /* physical = */ true,
      /* markAsCopy = */ false,
    ) as KtFile

    if (!ignoreParseErrors) AnalyzingUtils.checkForSyntacticErrors(ktFile)
    return ktFile
  }
}

interface AnalysisResult {
  data class Diagnostic(val message: String, val ranges: List<TextRange>)

  val diagnostics: Map<String, List<Diagnostic>>
}

abstract class KotlinCompilerFacade(val environment: KotlinCoreEnvironment) {
  abstract fun analyze(file: SourceFile): AnalysisResult
  abstract fun compile(file: SourceFile): IrModuleFragment

  companion object {
    private const val TEST_MODULE_NAME = "test-module"

    fun create(
      disposable: Disposable,
      updateConfiguration: CompilerConfiguration.() -> Unit,
      registerExtensions: Project.(CompilerConfiguration) -> Unit,
    ): KotlinCompilerFacade {
      val configuration = CompilerConfiguration().apply {
        put(CommonConfigurationKeys.MODULE_NAME, TEST_MODULE_NAME)
        put(JVMConfigurationKeys.IR, true)
        put(CommonConfigurationKeys.USE_FIR, true)
        put(CommonConfigurationKeys.VERIFY_IR, IrVerificationMode.ERROR)
        put(CommonConfigurationKeys.ENABLE_IR_VISIBILITY_CHECKS, true)
        put(JVMConfigurationKeys.JVM_TARGET, JvmTarget.JVM_17)
        messageCollector = TestMessageCollector
        updateConfiguration()
      }

      val environment = KotlinCoreEnvironment.createForTests(
        parentDisposable = disposable,
        initialConfiguration = configuration,
        extensionConfigs = EnvironmentConfigFiles.JVM_CONFIG_FILES,
      )
      environment.project.registerExtensions(configuration)

      return K2CompilerFacade(environment)
    }
  }
}

private object TestMessageCollector : MessageCollector {
  override fun clear() {}

  override fun report(
    severity: CompilerMessageSeverity,
    message: String,
    location: CompilerMessageSourceLocation?,
  ) {
    if (severity.isError) error(message)
  }

  override fun hasErrors(): Boolean = false
}
