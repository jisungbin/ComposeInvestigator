/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("UnstableApiUsage")

package land.sungbin.composeinvestigator.compiler.test._compilation

import androidx.compose.compiler.plugins.kotlin.ComposePluginRegistrar
import com.intellij.openapi.extensions.LoadingOrder
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.io.FileUtil
import land.sungbin.composeinvestigator.compiler.ComposableCallstackTrackingExtension
import land.sungbin.composeinvestigator.compiler.ComposableInvalidationTrackingExtension
import land.sungbin.composeinvestigator.compiler.VerboseLogger
import land.sungbin.composeinvestigator.compiler.test._compilation.facade.AnalysisResult
import land.sungbin.composeinvestigator.compiler.test._compilation.facade.KotlinCompilerFacade
import land.sungbin.composeinvestigator.compiler.test._compilation.facade.SourceFile
import land.sungbin.composeinvestigator.compiler.test._compilation.facade.TestMessageCollector
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.cli.jvm.config.configureJdkClasspathRoots
import org.jetbrains.kotlin.codegen.GeneratedClassLoader
import org.jetbrains.kotlin.config.AnalysisFlag
import org.jetbrains.kotlin.config.AnalysisFlags
import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.junit.After
import org.junit.BeforeClass
import java.io.File
import java.net.URLClassLoader

// FIXME: Failed to lookup symbols with 'fqName == kotlin.collections.MutableList.add',
//  'fn.owner.valueParameters.size == 1' in Kotlin 2.0. Needs to be fixed in the future.
// @RunWith(Parameterized::class)
abstract class AbstractCompilerTest(val useFir: Boolean) {
  companion object {
    // @JvmStatic
    // @Parameterized.Parameters(name = "useFir = {0}")
    // fun data() = arrayOf<Any>(false, true)

    private fun File.applyExistenceCheck() = apply {
      if (!exists()) throw NoSuchFileException(this)
    }

    private val homeDir = run {
      val userDir = System.getProperty("user.dir")
      val dir = File(userDir ?: ".")
      val path = FileUtil.toCanonicalPath(dir.absolutePath)
      File(path).applyExistenceCheck().absolutePath
    }

    @JvmStatic
    @BeforeClass
    fun setSystemProperties() {
      System.setProperty("idea.home", homeDir)
      System.setProperty("user.dir", homeDir)
      System.setProperty("idea.ignore.disabled.plugins", "true")
    }

    val defaultClassPath by lazy {
      System.getProperty("java.class.path")!!.split(File.pathSeparator).map(::File)
    }

    val defaultClassPathRoots by lazy {
      defaultClassPath.filter { file ->
        !file.path.contains("robolectric") && file.extension != "xml"
      }
    }
  }

  private val testRootDisposable = Disposer.newDisposable()

  @After
  fun disposeTestRootDisposable() {
    Disposer.dispose(testRootDisposable)
  }

  protected open fun CompilerConfiguration.updateConfiguration() {}

  private fun createCompilerFacade(
    additionalPaths: List<File> = listOf(),
    forcedFirSetting: Boolean? = null,
    registerExtensions: Project.(CompilerConfiguration) -> Unit = {},
  ) = KotlinCompilerFacade.create(
    disposable = testRootDisposable,
    updateConfiguration = {
      val enableFir = forcedFirSetting ?: useFir
      val languageVersion = if (enableFir) LanguageVersion.KOTLIN_2_0 else LanguageVersion.KOTLIN_1_9
      // For tests, allow unstable artifacts compiled with a pre-release compiler
      // as input to stable compilations.
      val analysisFlags: Map<AnalysisFlag<*>, Any?> = mapOf(
        AnalysisFlags.allowUnstableDependencies to true,
        AnalysisFlags.skipPrereleaseCheck to true,
      )
      languageVersionSettings = LanguageVersionSettingsImpl(
        languageVersion = languageVersion,
        apiVersion = ApiVersion.createByLanguageVersion(languageVersion),
        analysisFlags = analysisFlags,
      )
      updateConfiguration()
      addJvmClasspathRoots(additionalPaths)
      addJvmClasspathRoots(defaultClassPathRoots)
      if (!getBoolean(JVMConfigurationKeys.NO_JDK) && get(JVMConfigurationKeys.JDK_HOME) == null) {
        // We need to set `JDK_HOME` explicitly to use JDK 17
        put(JVMConfigurationKeys.JDK_HOME, File(System.getProperty("java.home")!!))
      }
      configureJdkClasspathRoots()
    },
    registerExtensions = { configuration ->
      ComposePluginRegistrar.registerCommonExtensions(this)
      IrGenerationExtension.registerExtension(
        project = this,
        extension = ComposePluginRegistrar.createComposeIrExtension(configuration),
      )

      val logger = VerboseLogger(messageCollector = TestMessageCollector).apply { verbose() }
      extensionArea
        .getExtensionPoint(IrGenerationExtension.extensionPointName)
        .registerExtension(
          ComposableCallstackTrackingExtension(logger = logger),
          LoadingOrder.FIRST,
          this,
        )
      extensionArea
        .getExtensionPoint(IrGenerationExtension.extensionPointName)
        .registerExtension(
          ComposableInvalidationTrackingExtension(logger = logger),
          LoadingOrder.LAST,
          this,
        )

      registerExtensions(configuration)
    },
  )

  protected fun analyze(
    platformSources: List<SourceFile>,
    commonSources: List<SourceFile> = emptyList(),
  ): AnalysisResult =
    createCompilerFacade().analyze(platformFiles = platformSources, commonFiles = commonSources)

  protected fun compileToIr(
    sourceFiles: List<SourceFile>,
    additionalPaths: List<File> = emptyList(),
    registerExtensions: Project.(CompilerConfiguration) -> Unit = {},
  ): IrModuleFragment =
    createCompilerFacade(additionalPaths = additionalPaths, registerExtensions = registerExtensions)
      .compileToIr(sourceFiles)

  protected fun createClassLoader(
    platformSourceFiles: List<SourceFile>,
    commonSourceFiles: List<SourceFile> = emptyList(),
    additionalPaths: List<File> = emptyList(),
    forcedFirSetting: Boolean? = null,
  ): GeneratedClassLoader {
    @Suppress("InconsistentCommentForJavaParameter")
    val classLoader = URLClassLoader(
      /* URL[] = */
      (additionalPaths + defaultClassPath).map { file -> file.toURI().toURL() }.toTypedArray(),
      /* ClassLoader = */
      null,
    )
    return GeneratedClassLoader(
      /* factory = */
      createCompilerFacade(additionalPaths = additionalPaths, forcedFirSetting = forcedFirSetting)
        .compile(platformFiles = platformSourceFiles, commonFiles = commonSourceFiles)
        .factory,
      /* parentClassLoader = */
      classLoader,
    )
  }
}
