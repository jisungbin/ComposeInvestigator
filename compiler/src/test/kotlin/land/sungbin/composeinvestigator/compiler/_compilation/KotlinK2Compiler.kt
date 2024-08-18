/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler._compilation

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtilRt
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.PsiFileFactoryImpl
import com.intellij.psi.search.ProjectScope
import com.intellij.testFramework.LightVirtualFile
import java.nio.charset.StandardCharsets
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.jvm.JvmIrDeserializerImpl
import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.PsiBasedProjectFileSearchScope
import org.jetbrains.kotlin.cli.jvm.compiler.TopDownAnalyzerFacadeForJVM
import org.jetbrains.kotlin.cli.jvm.compiler.VfsBasedProjectEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.pipeline.convertToIrAndActualizeForJvm
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.IrVerificationMode
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.config.messageCollector
import org.jetbrains.kotlin.diagnostics.DiagnosticReporterFactory
import org.jetbrains.kotlin.diagnostics.impl.BaseDiagnosticsCollector
import org.jetbrains.kotlin.diagnostics.rendering.RootDiagnosticRendererFactory
import org.jetbrains.kotlin.fir.BinaryModuleData
import org.jetbrains.kotlin.fir.DependencyListForCliModule
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.FirModuleDataImpl
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.backend.jvm.JvmFir2IrExtensions
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.java.FirProjectSessionProvider
import org.jetbrains.kotlin.fir.pipeline.Fir2IrActualizedResult
import org.jetbrains.kotlin.fir.pipeline.FirResult
import org.jetbrains.kotlin.fir.pipeline.buildResolveAndCheckFirFromKtFiles
import org.jetbrains.kotlin.fir.session.FirJvmSessionFactory
import org.jetbrains.kotlin.fir.session.environment.AbstractProjectEnvironment
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.CommonPlatforms
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
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

class FirAnalysisResult(val result: FirResult, val reporter: BaseDiagnosticsCollector) {
  private val sourceLocationMap by lazy { reporter.withCompilerMessageSourceLocation() }

  val diagnostics: Map<String, List<String>> by lazy {
    val map = mutableMapOf<String, MutableList<String>>()
    reporter.diagnostics.forEach { diagnostic ->
      val key = diagnostic.factoryName
      val message = MessageRenderer.WITHOUT_PATHS.render(
        /* severity = */ AnalyzerWithCompilerReport.convertSeverity(diagnostic.severity),
        /* message = */ RootDiagnosticRendererFactory(diagnostic).render(diagnostic),
        /* location = */ sourceLocationMap[diagnostic],
      )
      map.getOrPut(key, ::mutableListOf).add(message)
    }
    map
  }
}

class KotlinK2Compiler private constructor(private val environment: KotlinCoreEnvironment) {
  private val project: Project get() = environment.project
  private val configuration: CompilerConfiguration get() = environment.configuration

  fun analyze(file: SourceFile): FirAnalysisResult {
    val moduleName = configuration.get(CommonConfigurationKeys.MODULE_NAME, "main")

    val projectSessionProvider = FirProjectSessionProvider()
    val binaryModuleData = BinaryModuleData.initialize(
      mainModuleName = Name.identifier(moduleName),
      platform = CommonPlatforms.defaultCommonPlatform,
    )
    val dependencies = DependencyListForCliModule.build(binaryModuleData)
    val projectEnvironment = VfsBasedProjectEnvironment(
      project = project,
      localFileSystem = VirtualFileManager.getInstance().getFileSystem(StandardFileSystems.FILE_PROTOCOL),
      getPackagePartProviderFn = environment::createPackagePartProvider,
    )
    val librariesScope = PsiBasedProjectFileSearchScope(ProjectScope.getLibrariesScope(project))

    FirJvmSessionFactory.createLibrarySession(
      mainModuleName = Name.identifier(moduleName),
      sessionProvider = projectSessionProvider,
      moduleDataProvider = dependencies.moduleDataProvider,
      projectEnvironment = projectEnvironment,
      extensionRegistrars = FirExtensionRegistrar.getInstances(project),
      scope = librariesScope,
      packagePartProvider = projectEnvironment.getPackagePartProvider(librariesScope),
      languageVersionSettings = configuration.languageVersionSettings,
      predefinedJavaComponents = null,
      registerExtraComponents = {},
    )

    val module = FirModuleDataImpl(
      name = Name.identifier(moduleName),
      dependencies = dependencies.regularDependencies,
      dependsOnDependencies = dependencies.dependsOnDependencies,
      friendDependencies = dependencies.friendsDependencies,
      platform = JvmPlatforms.jvm17,
    )
    val session = createSourceSession(
      moduleData = module,
      projectSessionProvider = projectSessionProvider,
      projectEnvironment = projectEnvironment,
    )

    val reporter = DiagnosticReporterFactory.createReporter()
    val analysis = buildResolveAndCheckFirFromKtFiles(session, listOf(file.toKtFile(project)), reporter)

    return FirAnalysisResult(FirResult(listOf(analysis)), reporter)
  }

  fun compile(file: SourceFile): Fir2IrActualizedResult {
    val analysis = analyze(file)
    val fir2IrResult = analysis.result.convertToIrAndActualizeForJvm(
      fir2IrExtensions = JvmFir2IrExtensions(configuration = configuration, irDeserializer = JvmIrDeserializerImpl()),
      configuration = configuration,
      diagnosticsReporter = analysis.reporter,
      irGeneratorExtensions = IrGenerationExtension.getInstances(project),
    )
    return fir2IrResult
  }

  private fun createSourceSession(
    moduleData: FirModuleData,
    projectSessionProvider: FirProjectSessionProvider,
    projectEnvironment: AbstractProjectEnvironment,
  ): FirSession =
    FirJvmSessionFactory.createModuleBasedSession(
      moduleData = moduleData,
      sessionProvider = projectSessionProvider,
      javaSourcesScope = PsiBasedProjectFileSearchScope(TopDownAnalyzerFacadeForJVM.AllJavaSourcesInProjectScope(project)),
      projectEnvironment = projectEnvironment,
      createIncrementalCompilationSymbolProviders = { null },
      extensionRegistrars = FirExtensionRegistrar.getInstances(project),
      languageVersionSettings = configuration.languageVersionSettings,
      jvmTarget = checkNotNull(configuration.get(JVMConfigurationKeys.JVM_TARGET)) { "JVM_TARGET is not specified in compiler configuration" },
      lookupTracker = configuration.get(CommonConfigurationKeys.LOOKUP_TRACKER),
      enumWhenTracker = configuration.get(CommonConfigurationKeys.ENUM_WHEN_TRACKER),
      importTracker = configuration.get(CommonConfigurationKeys.IMPORT_TRACKER),
      predefinedJavaComponents = null,
      needRegisterJavaElementFinder = true,
      registerExtraComponents = {},
      init = {},
    )

  companion object {
    private const val TEST_MODULE_NAME = "test-module"

    fun create(
      disposable: Disposable,
      updateConfiguration: CompilerConfiguration.() -> Unit,
      registerExtensions: Project.(CompilerConfiguration) -> Unit,
    ): KotlinK2Compiler {
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

      // KotlinCoreEnvironment.createForParallelTests
      val environment = KotlinCoreEnvironment.createForTests(
        parentDisposable = disposable,
        initialConfiguration = configuration,
        extensionConfigs = EnvironmentConfigFiles.JVM_CONFIG_FILES,
      )
      environment.project.registerExtensions(configuration)

      return KotlinK2Compiler(environment)
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
    println(message)
  }

  override fun hasErrors(): Boolean = false
}
