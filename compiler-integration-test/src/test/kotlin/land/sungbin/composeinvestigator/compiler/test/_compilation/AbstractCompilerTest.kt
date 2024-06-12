@file:Suppress("UnstableApiUsage")

package land.sungbin.composeinvestigator.compiler.test._compilation

import androidx.compose.compiler.plugins.kotlin.ComposeConfiguration
import androidx.compose.compiler.plugins.kotlin.ComposePluginRegistrar
import com.intellij.openapi.extensions.LoadingOrder
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.io.FileUtil
import java.io.File
import land.sungbin.composeinvestigator.compiler.ComposableCallstackTrackingExtension
import land.sungbin.composeinvestigator.compiler.ComposableInvalidationTrackingExtension
import land.sungbin.composeinvestigator.compiler.VerboseLogger
import land.sungbin.composeinvestigator.compiler.test._compilation.compiler.AnalysisResult
import land.sungbin.composeinvestigator.compiler.test._compilation.compiler.ErrorMessageCollector
import land.sungbin.composeinvestigator.compiler.test._compilation.compiler.KotlinCompiler
import land.sungbin.composeinvestigator.compiler.test._compilation.compiler.SourceFile
import org.intellij.lang.annotations.MagicConstant
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.cli.jvm.config.configureJdkClasspathRoots
import org.jetbrains.kotlin.config.AnalysisFlag
import org.jetbrains.kotlin.config.AnalysisFlags
import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.junit.After
import org.junit.BeforeClass

abstract class AbstractCompilerTest {
  object Flags {
    @Suppress("unused")
    const val NONE = 0

    /** Enable the Compose compiler */
    const val COMPOSE = 1 shl 0

    /** Enable LiveLiteral in Compose */
    const val LIVE_LITERAL = 1 shl 1

    /** Disable callstack tracking */
    const val NO_CALLSTACK_TRACKING = 1 shl 2
  }

  companion object {
    private fun File.applyExistenceCheck() =
      apply { if (!exists()) throw NoSuchFileException(this) }

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

    private val defaultClassPath by lazy {
      System.getProperty("java.class.path")!!.split(File.pathSeparator).map(::File)
    }

    private val defaultClassPathRoots by lazy {
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

  private fun createCompiler(
    additionalPaths: List<File> = emptyList(),
    @MagicConstant(flagsFromClass = Flags::class) flags: Int = Flags.COMPOSE,
  ) =
    KotlinCompiler.create(
      disposable = testRootDisposable,
      updateConfiguration = {
        val analysisFlags = mapOf<AnalysisFlag<*>, Any>(
          // For tests, allow unstable artifacts compiled with a pre-release compiler
          // as input to stable compilations.
          AnalysisFlags.allowUnstableDependencies to true,
          AnalysisFlags.skipPrereleaseCheck to true,
          AnalysisFlags.optIn to listOf(
            "land.sungbin.composeinvestigator.runtime.ComposeInvestigatorCompilerApi",
            "land.sungbin.composeinvestigator.runtime.ExperimentalComposeInvestigatorApi",
          ),
        )
        languageVersionSettings = LanguageVersionSettingsImpl(
          languageVersion = LanguageVersion.LATEST_STABLE,
          apiVersion = ApiVersion.createByLanguageVersion(LanguageVersion.LATEST_STABLE),
          analysisFlags = analysisFlags,
        )
        addJvmClasspathRoots(additionalPaths)
        addJvmClasspathRoots(defaultClassPathRoots)
        if (!getBoolean(JVMConfigurationKeys.NO_JDK) && get(JVMConfigurationKeys.JDK_HOME) == null) {
          // We need to set `JDK_HOME` explicitly to use JDK 17
          put(JVMConfigurationKeys.JDK_HOME, File(System.getProperty("java.home")!!))
        }
        configureJdkClasspathRoots()
      },
      registerExtensions = { configuration ->
        if ((flags and Flags.COMPOSE) != 0) {
          ComposePluginRegistrar.registerCommonExtensions(this)
          IrGenerationExtension.registerExtension(
            project = this,
            extension = ComposePluginRegistrar.createComposeIrExtension(configuration),
          )
        }

        val useLiveLiteral = (flags and Flags.LIVE_LITERAL) != 0
        configuration.put(ComposeConfiguration.LIVE_LITERALS_ENABLED_KEY, useLiveLiteral)
        configuration.put(ComposeConfiguration.LIVE_LITERALS_V2_ENABLED_KEY, useLiveLiteral)

        val logger = VerboseLogger(messageCollector = ErrorMessageCollector).apply { verbose() }
        if ((flags and Flags.NO_CALLSTACK_TRACKING) == 0) {
          extensionArea
            .getExtensionPoint(IrGenerationExtension.extensionPointName)
            .registerExtension(
              ComposableCallstackTrackingExtension(logger = logger),
              LoadingOrder.FIRST,
              this,
            )
        }
        extensionArea
          .getExtensionPoint(IrGenerationExtension.extensionPointName)
          .registerExtension(
            ComposableInvalidationTrackingExtension(logger = logger),
            LoadingOrder.LAST,
            this,
          )
      }
    )

  protected fun analyze(
    platformSources: List<SourceFile>,
    commonSources: List<SourceFile> = listOf(),
    @MagicConstant(flagsFromClass = Flags::class) flags: Int = Flags.COMPOSE,
  ): AnalysisResult =
    createCompiler(flags = flags)
      .analyze(platformFiles = platformSources, commonFiles = commonSources)

  protected fun compileToIr(
    sourceFiles: List<SourceFile>,
    additionalPaths: List<File> = emptyList(),
    @MagicConstant(flagsFromClass = Flags::class) flags: Int = Flags.COMPOSE,
  ): IrModuleFragment =
    createCompiler(additionalPaths = additionalPaths, flags = flags)
      .compileToIr(sourceFiles)
}
