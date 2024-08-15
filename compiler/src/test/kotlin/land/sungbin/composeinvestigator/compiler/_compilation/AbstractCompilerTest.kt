/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package land.sungbin.composeinvestigator.compiler._compilation

import androidx.compose.compiler.plugins.kotlin.ComposePluginRegistrar
import androidx.compose.runtime.Composable
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

    val defaultClassPath by lazy {
      listOf(
        Classpath.kotlinStdlibJar(),
        Classpath.composeRuntimeJar(),
        Classpath.composeInvestigatorRumtimeJar(),
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

  private fun createCompilerFacade(additionalPaths: List<File> = emptyList()) =
    KotlinCompilerFacade.create(
      disposable = disposable,
      updateConfiguration = {
        val languageVersion = LanguageVersion.fromFullVersionString(KotlinVersion.CURRENT.toString())!!.also { version ->
          check(version.usesK2) { "Kotlin version $version is not a K2 version" }
        }
        val analysisFlags: Map<AnalysisFlag<*>, Any> = mapOf(
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

        addJvmClasspathRoots(additionalPaths)
        addJvmClasspathRoots(defaultClassPath)

        if (!getBoolean(JVMConfigurationKeys.NO_JDK) && get(JVMConfigurationKeys.JDK_HOME) == null) {
          put(JVMConfigurationKeys.JDK_HOME, File(System.getProperty("java.home")!!))
        }
        configureJdkClasspathRoots()
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

private object Classpath {
  fun kotlinStdlibJar() = jarFor<Unit>()
  fun composeRuntimeJar() = jarFor<Composable>()
  fun composeInvestigatorRumtimeJar() = jarFor<ComposeInvestigatorConfig>()

  private inline fun <reified T> jarFor() = File(PathUtil.getJarPathForClass(T::class.java))
}
