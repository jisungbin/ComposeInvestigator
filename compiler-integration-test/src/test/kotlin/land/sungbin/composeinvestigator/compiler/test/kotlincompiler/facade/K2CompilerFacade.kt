/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.kotlincompiler.facade

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.search.ProjectScope
import org.jetbrains.kotlin.analyzer.common.CommonPlatformAnalyzerServices
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.jvm.JvmGeneratorExtensions
import org.jetbrains.kotlin.backend.jvm.JvmIrCodegenFactory
import org.jetbrains.kotlin.backend.jvm.JvmIrDeserializerImpl
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.fir.FirDiagnosticsCompilerResultsReporter
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.NoScopeRecordCliBindingTrace
import org.jetbrains.kotlin.cli.jvm.compiler.PsiBasedProjectFileSearchScope
import org.jetbrains.kotlin.cli.jvm.compiler.TopDownAnalyzerFacadeForJVM
import org.jetbrains.kotlin.cli.jvm.compiler.VfsBasedProjectEnvironment
import org.jetbrains.kotlin.codegen.ClassBuilderFactories
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.constant.EvaluatedConstTracker
import org.jetbrains.kotlin.diagnostics.DiagnosticReporterFactory
import org.jetbrains.kotlin.diagnostics.impl.BaseDiagnosticsCollector
import org.jetbrains.kotlin.fir.BinaryModuleData
import org.jetbrains.kotlin.fir.DependencyListForCliModule
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.FirModuleDataImpl
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.backend.Fir2IrConfiguration
import org.jetbrains.kotlin.fir.backend.jvm.FirJvmBackendClassResolver
import org.jetbrains.kotlin.fir.backend.jvm.FirJvmBackendExtension
import org.jetbrains.kotlin.fir.backend.jvm.JvmFir2IrExtensions
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.java.FirProjectSessionProvider
import org.jetbrains.kotlin.fir.pipeline.Fir2IrActualizedResult
import org.jetbrains.kotlin.fir.pipeline.FirResult
import org.jetbrains.kotlin.fir.pipeline.buildResolveAndCheckFirFromKtFiles
import org.jetbrains.kotlin.fir.pipeline.convertToIrAndActualizeForJvm
import org.jetbrains.kotlin.fir.session.FirJvmSessionFactory
import org.jetbrains.kotlin.fir.session.environment.AbstractProjectEnvironment
import org.jetbrains.kotlin.ir.backend.jvm.serialization.JvmIrMangler
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.CommonPlatforms
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.jvm.platform.JvmPlatformAnalyzerServices

class FirAnalysisResult(
  val firResult: FirResult,
  override val files: List<KtFile>,
  val reporter: BaseDiagnosticsCollector,
) : AnalysisResult {
  override val diagnostics: Map<String, List<AnalysisResult.Diagnostic>>
    get() = reporter.diagnostics.groupBy(
      keySelector = { key -> key.psiElement.containingFile.name },
      valueTransform = { value -> AnalysisResult.Diagnostic(value.factoryName, value.textRanges) },
    )
}

private class FirFrontendResult(
  val firResult: Fir2IrActualizedResult,
  val generatorExtensions: JvmGeneratorExtensions,
)

class K2CompilerFacade(environment: KotlinCoreEnvironment) : KotlinCompilerFacade(environment) {
  private val project: Project get() = environment.project

  private val configuration: CompilerConfiguration get() = environment.configuration

  private fun createSourceSession(
    moduleData: FirModuleData,
    projectSessionProvider: FirProjectSessionProvider,
    projectEnvironment: AbstractProjectEnvironment,
  ): FirSession = FirJvmSessionFactory.createModuleBasedSession(
    moduleData = moduleData,
    sessionProvider = projectSessionProvider,
    javaSourcesScope = PsiBasedProjectFileSearchScope(TopDownAnalyzerFacadeForJVM.AllJavaSourcesInProjectScope(project)),
    projectEnvironment = projectEnvironment,
    incrementalCompilationContext = null,
    extensionRegistrars = FirExtensionRegistrar.getInstances(project),
    languageVersionSettings = configuration.languageVersionSettings,
    lookupTracker = configuration.get(CommonConfigurationKeys.LOOKUP_TRACKER),
    enumWhenTracker = configuration.get(CommonConfigurationKeys.ENUM_WHEN_TRACKER),
    needRegisterJavaElementFinder = true,
  )

  override fun analyze(platformFiles: List<SourceFile>, commonFiles: List<SourceFile>): FirAnalysisResult {
    val rootModuleName = configuration.get(CommonConfigurationKeys.MODULE_NAME, "main")

    val projectSessionProvider = FirProjectSessionProvider()
    val binaryModuleData = BinaryModuleData.initialize(
      mainModuleName = Name.identifier(rootModuleName),
      platform = CommonPlatforms.defaultCommonPlatform,
      analyzerServices = CommonPlatformAnalyzerServices,
    )
    val dependencies = DependencyListForCliModule.build(binaryModuleData)
    val projectEnvironment = VfsBasedProjectEnvironment(
      project = project,
      localFileSystem = VirtualFileManager.getInstance().getFileSystem(StandardFileSystems.FILE_PROTOCOL),
      getPackagePartProviderFn = environment::createPackagePartProvider,
    )
    val librariesScope = PsiBasedProjectFileSearchScope(ProjectScope.getLibrariesScope(project))

    FirJvmSessionFactory.createLibrarySession(
      mainModuleName = Name.identifier(rootModuleName),
      sessionProvider = projectSessionProvider,
      moduleDataProvider = dependencies.moduleDataProvider,
      projectEnvironment = projectEnvironment,
      extensionRegistrars = FirExtensionRegistrar.getInstances(project),
      scope = librariesScope,
      packagePartProvider = projectEnvironment.getPackagePartProvider(librariesScope),
      languageVersionSettings = configuration.languageVersionSettings,
      registerExtraComponents = {},
    )

    val commonModuleData = FirModuleDataImpl(
      name = Name.identifier("$rootModuleName-common"),
      dependencies = dependencies.regularDependencies,
      dependsOnDependencies = dependencies.dependsOnDependencies,
      friendDependencies = dependencies.friendsDependencies,
      platform = CommonPlatforms.defaultCommonPlatform,
      analyzerServices = CommonPlatformAnalyzerServices,
    )
    val platformModuleData = FirModuleDataImpl(
      name = Name.identifier(rootModuleName),
      dependencies = dependencies.regularDependencies,
      dependsOnDependencies = dependencies.dependsOnDependencies + commonModuleData,
      friendDependencies = dependencies.friendsDependencies,
      platform = JvmPlatforms.jvm17,
      analyzerServices = JvmPlatformAnalyzerServices,
    )
    val commonSession = createSourceSession(
      moduleData = commonModuleData,
      projectSessionProvider = projectSessionProvider,
      projectEnvironment = projectEnvironment,
    )
    val platformSession = createSourceSession(
      moduleData = platformModuleData,
      projectSessionProvider = projectSessionProvider,
      projectEnvironment = projectEnvironment,
    )

    val commonKtFiles = commonFiles.map { file -> file.toKtFile(project) }
    val platformKtFiles = platformFiles.map { file -> file.toKtFile(project) }

    val reporter = DiagnosticReporterFactory.createReporter()
    val commonAnalysis = buildResolveAndCheckFirFromKtFiles(
      session = commonSession,
      ktFiles = commonKtFiles,
      diagnosticsReporter = reporter,
    )
    val platformAnalysis = buildResolveAndCheckFirFromKtFiles(
      session = platformSession,
      ktFiles = platformKtFiles,
      diagnosticsReporter = reporter,
    )

    return FirAnalysisResult(
      firResult = FirResult(outputs = listOf(commonAnalysis, platformAnalysis)),
      files = commonKtFiles + platformKtFiles,
      reporter = reporter,
    )
  }

  private fun frontend(platformFiles: List<SourceFile>, commonFiles: List<SourceFile>): FirFrontendResult {
    val analysisResult = analyze(platformFiles = platformFiles, commonFiles = commonFiles)

    FirDiagnosticsCompilerResultsReporter.throwFirstErrorAsException(
      diagnosticsCollector = analysisResult.reporter,
      messageRenderer = MessageRenderer.PLAIN_FULL_PATHS,
    )

    val fir2IrExtensions = JvmFir2IrExtensions(
      configuration = configuration,
      irDeserializer = JvmIrDeserializerImpl(),
      mangler = JvmIrMangler,
    )

    val fir2IrResult = analysisResult.firResult.convertToIrAndActualizeForJvm(
      fir2IrExtensions = fir2IrExtensions,
      fir2IrConfiguration = Fir2IrConfiguration(
        languageVersionSettings = configuration.languageVersionSettings,
        diagnosticReporter = analysisResult.reporter,
        linkViaSignatures = configuration.getBoolean(JVMConfigurationKeys.LINK_VIA_SIGNATURES),
        evaluatedConstTracker = EvaluatedConstTracker.create(),
        inlineConstTracker = configuration[CommonConfigurationKeys.INLINE_CONST_TRACKER],
        expectActualTracker = configuration[CommonConfigurationKeys.EXPECT_ACTUAL_TRACKER],
        allowNonCachedDeclarations = false,
      ),
      irGeneratorExtensions = IrGenerationExtension.getInstances(project),
    )

    return FirFrontendResult(firResult = fir2IrResult, generatorExtensions = fir2IrExtensions)
  }

  override fun compileToIr(files: List<SourceFile>): IrModuleFragment =
    frontend(platformFiles = files, commonFiles = emptyList()).firResult.irModuleFragment

  override fun compile(platformFiles: List<SourceFile>, commonFiles: List<SourceFile>): GenerationState {
    val frontendResult = frontend(platformFiles = platformFiles, commonFiles = commonFiles)
    val irModuleFragment = frontendResult.firResult.irModuleFragment
    val components = frontendResult.firResult.components

    val generationState = GenerationState.Builder(
      project = project,
      builderFactory = ClassBuilderFactories.TEST,
      module = irModuleFragment.descriptor,
      bindingContext = NoScopeRecordCliBindingTrace().bindingContext,
      configuration = configuration,
    )
      .isIrBackend(true)
      .jvmBackendClassResolver(FirJvmBackendClassResolver(components))
      .build()
    generationState.beforeCompile()

    val codegenFactory = JvmIrCodegenFactory(
      configuration = configuration,
      phaseConfig = configuration.get(CLIConfigurationKeys.PHASE_CONFIG),
    )
    codegenFactory.generateModuleInFrontendIRMode(
      state = generationState,
      irModuleFragment = irModuleFragment,
      symbolTable = components.symbolTable,
      irProviders = components.irProviders,
      extensions = frontendResult.generatorExtensions,
      backendExtension = FirJvmBackendExtension(
        components = components,
        irActualizedResult = frontendResult.firResult.irActualizedResult,
      ),
      irPluginContext = frontendResult.firResult.pluginContext,
    )
    generationState.factory.done()

    return generationState
  }
}
