/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test._compilation.compiler

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import land.sungbin.composeinvestigator.compiler.test._compilation.compiler.KotlinCompiler.Companion.create
import org.jetbrains.kotlin.cli.common.messages.IrMessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.IrVerificationMode
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.IrMessageLogger

/** Don't instantiate this class directly, use [create]. */
abstract class KotlinCompiler(val environment: KotlinCoreEnvironment) {
  abstract fun analyze(platformFiles: List<SourceFile>, commonFiles: List<SourceFile>): AnalysisResult
  abstract fun compile(platformFiles: List<SourceFile>, commonFiles: List<SourceFile>): GenerationState
  abstract fun compileToIr(files: List<SourceFile>): IrModuleFragment

  companion object {
    @Suppress("MemberVisibilityCanBePrivate")
    const val TEST_MODULE_NAME = "test-module"

    /** Create a [KotlinCompiler] for the K2 environment. */
    fun create(
      disposable: Disposable,
      updateConfiguration: CompilerConfiguration.() -> Unit = {},
      registerExtensions: Project.(configuration: CompilerConfiguration) -> Unit = {},
    ): KotlinCompiler {
      val configuration = CompilerConfiguration().apply {
        put(CommonConfigurationKeys.MODULE_NAME, TEST_MODULE_NAME)
        put(JVMConfigurationKeys.IR, true)
        put(CommonConfigurationKeys.USE_FIR, true)
        put(CommonConfigurationKeys.VERIFY_IR, IrVerificationMode.ERROR)
        put(CommonConfigurationKeys.ENABLE_IR_VISIBILITY_CHECKS, true)
        put(JVMConfigurationKeys.JVM_TARGET, JvmTarget.JVM_17)
        put(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY, ErrorMessageCollector)
        put(IrMessageLogger.IR_MESSAGE_LOGGER, IrMessageCollector(ErrorMessageCollector))
        updateConfiguration()
      }
      require(
        configuration[JVMConfigurationKeys.IR] == true &&
          configuration[CommonConfigurationKeys.USE_FIR] == true
      ) {
        "FIR and IR must be enabled for the Kotlin compiler."
      }

      val environment = KotlinCoreEnvironment.createForTests(
        parentDisposable = disposable,
        initialConfiguration = configuration,
        extensionConfigs = EnvironmentConfigFiles.JVM_CONFIG_FILES,
      )

      environment.project.registerExtensions(configuration)

      return K2CompilerProvider(environment)
    }
  }
}
