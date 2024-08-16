/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler._compilation

import androidx.compose.compiler.plugins.kotlin.ComposePluginRegistrar
import com.intellij.openapi.extensions.LoadingOrder
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.PathUtil
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import land.sungbin.composeinvestigator.compiler.ComposeInvestigatorFirstPhaseExtension
import land.sungbin.composeinvestigator.compiler.ComposeInvestigatorLastPhaseExtension
import land.sungbin.composeinvestigator.compiler.frontend.ComposeInvestigatorFirExtensionRegistrar
import land.sungbin.composeinvestigator.runtime.ComposeInvestigatorConfig
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.cli.jvm.config.configureJdkClasspathRoots
import org.jetbrains.kotlin.compiler.plugin.registerExtensionsForTest
import org.jetbrains.kotlin.config.AnalysisFlag
import org.jetbrains.kotlin.config.AnalysisFlags
import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.config.messageCollector
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

@Execution(ExecutionMode.SAME_THREAD, reason = "KotlinCompilerFacade does not support parallel execution.")
abstract class AbstractCompilerTest {
  companion object {
    private fun File.applyExistenceCheck(): File = apply {
      if (!exists()) throw NoSuchFileException(this)
    }

    private val homeDir: String = run {
      val userDir = System.getProperty("user.dir")
      val dir = File(userDir ?: ".")
      val path = FileUtil.toCanonicalPath(dir.absolutePath)
      File(path).applyExistenceCheck().absolutePath
    }

    // https://github.com/JetBrains/kotlin/blob/bb25d2f8aa74406ff0af254b2388fd601525386a/plugins/compose/compiler-hosted/integration-tests/src/jvmTest/kotlin/androidx/compose/compiler/plugins/kotlin/AbstractCompilerTest.kt#L212-L228
    val defaultClassPath by lazy {
      listOf(
        jarFor<Unit>(),
        jarFor<kotlinx.coroutines.CoroutineScope>(),
        jarFor<androidx.compose.runtime.Composable>(),
        jarFor<androidx.compose.animation.EnterTransition>(),
        jarFor<androidx.compose.ui.Modifier>(),
        jarFor<androidx.compose.ui.graphics.ColorProducer>(),
        jarFor<androidx.compose.ui.unit.Dp>(),
        jarFor<androidx.compose.ui.text.input.TextFieldValue>(),
        jarFor<androidx.compose.foundation.Indication>(),
        jarFor<androidx.compose.foundation.text.KeyboardActions>(),
        jarFor<androidx.compose.foundation.layout.RowScope>(),
        jarFor<ComposeInvestigatorConfig>(),
      )
    }
  }

  private val disposable = Disposer.newDisposable()

  @BeforeTest fun setSystemProperties() {
    System.setProperty("idea.home", homeDir)
    System.setProperty("user.dir", homeDir)
    System.setProperty("idea.ignore.disabled.plugins", "true")
  }

  @AfterTest fun disposeTestRootDisposable() {
    Disposer.dispose(disposable)
  }

  @Suppress("UnstableApiUsage")
  private fun createCompilerFacade() =
    KotlinCompilerFacade.create(
      disposable = disposable,
      updateConfiguration = {
        val languageVersion = LanguageVersion.fromFullVersionString(KotlinVersion.CURRENT.toString())!!.also { version ->
          check(version.usesK2) { "Kotlin version $version is not a K2 version" }
        }
        val analysisFlags = mapOf<AnalysisFlag<*>, Any>(
          AnalysisFlags.allowUnstableDependencies to true,
          AnalysisFlags.skipPrereleaseCheck to true,
          AnalysisFlags.optIn to listOf(
            "land.sungbin.composeinvestigator.runtime.ComposeInvestigatorCompilerApi",
            "land.sungbin.composeinvestigator.runtime.ExperimentalComposeInvestigatorApi",
          ),
        )

        languageVersionSettings = LanguageVersionSettingsImpl(
          languageVersion = languageVersion,
          apiVersion = ApiVersion.createByLanguageVersion(languageVersion),
          analysisFlags = analysisFlags,
        )

        configureJdkClasspathRoots()
        addJvmClasspathRoots(defaultClassPath)

        if (!getBoolean(JVMConfigurationKeys.NO_JDK) && get(JVMConfigurationKeys.JDK_HOME) == null) {
          put(JVMConfigurationKeys.JDK_HOME, File(System.getProperty("java.home")!!))
        }
      },
      registerExtensions = { configuration ->
        registerExtensionsForTest(this, configuration) {
          FirExtensionRegistrarAdapter.registerExtension(ComposeInvestigatorFirExtensionRegistrar())
          ComposePluginRegistrar.registerCommonExtensions(this@create, null)
        }
        extensionArea.getExtensionPoint(IrGenerationExtension.extensionPointName).run {
          registerExtension(ComposeInvestigatorFirstPhaseExtension(configuration.messageCollector), LoadingOrder.FIRST, this@create)
          registerExtension(ComposeInvestigatorLastPhaseExtension(configuration.messageCollector), LoadingOrder.LAST, this@create)
        }
        IrGenerationExtension.registerExtension(
          this,
          ComposePluginRegistrar.createComposeIrExtension(configuration),
        )
      },
    )

  protected fun analyze(file: SourceFile): AnalysisResult =
    createCompilerFacade().analyze(file)

  protected fun compile(file: SourceFile): IrModuleFragment =
    createCompilerFacade().compile(file)
}

private inline fun <reified T> jarFor() = File(PathUtil.getJarPathForClass(T::class.java))