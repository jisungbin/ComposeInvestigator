/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler._compilation

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.search.ProjectScope
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.jvm.JvmIrDeserializerImpl
import org.jetbrains.kotlin.cli.common.fir.FirDiagnosticsCompilerResultsReporter
import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.PsiBasedProjectFileSearchScope
import org.jetbrains.kotlin.cli.jvm.compiler.TopDownAnalyzerFacadeForJVM
import org.jetbrains.kotlin.cli.jvm.compiler.VfsBasedProjectEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.pipeline.convertToIrAndActualizeForJvm
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.languageVersionSettings
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
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.CommonPlatforms
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms

class FirAnalysisResult(
  val result: FirResult,
  val reporter: BaseDiagnosticsCollector,
) : AnalysisResult {
  private val sourceLocationMap by lazy { reporter.withCompilerMessageSourceLocation() }

  override val diagnostics by lazy {
    reporter.diagnostics.groupBy(
      keySelector = { diagnostics -> diagnostics.factoryName },
      valueTransform = { diagnostic ->
        AnalysisResult.Diagnostic(
          message = run {
            val severity = AnalyzerWithCompilerReport.convertSeverity(diagnostic.severity)
            val renderer = RootDiagnosticRendererFactory(diagnostic)
            val location = sourceLocationMap[diagnostic]

            MessageRenderer.PLAIN_FULL_PATHS.render(
              severity,
              renderer.render(diagnostic),
              location,
            )
          },
          ranges = diagnostic.textRanges,
        )
      },
    )
  }
}

class K2CompilerFacade(environment: KotlinCoreEnvironment) : KotlinCompilerFacade(environment) {
  private val project: Project get() = environment.project
  private val configuration: CompilerConfiguration get() = environment.configuration

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
      jvmTarget = configuration.get(JVMConfigurationKeys.JVM_TARGET) ?: error("JVM_TARGET is not specified in compiler configuration"),
      lookupTracker = configuration.get(CommonConfigurationKeys.LOOKUP_TRACKER),
      enumWhenTracker = configuration.get(CommonConfigurationKeys.ENUM_WHEN_TRACKER),
      importTracker = configuration.get(CommonConfigurationKeys.IMPORT_TRACKER),
      predefinedJavaComponents = null,
      needRegisterJavaElementFinder = true,
      registerExtraComponents = {},
      init = {},
    )

  override fun analyze(file: SourceFile): FirAnalysisResult {
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

  private fun frontend(file: SourceFile): Fir2IrActualizedResult {
    val analysis = analyze(file)

    FirDiagnosticsCompilerResultsReporter.throwFirstErrorAsException(
      diagnosticsCollector = analysis.reporter,
      messageRenderer = MessageRenderer.PLAIN_FULL_PATHS,
    )

    val fir2IrResult = analysis.result.convertToIrAndActualizeForJvm(
      fir2IrExtensions = JvmFir2IrExtensions(configuration = configuration, irDeserializer = JvmIrDeserializerImpl()),
      configuration = configuration,
      diagnosticsReporter = analysis.reporter,
      irGeneratorExtensions = IrGenerationExtension.getInstances(project),
    )

    return fir2IrResult
  }

  override fun compile(file: SourceFile): IrModuleFragment = frontend(file).irModuleFragment
}
