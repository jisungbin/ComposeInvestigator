/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test._compilation.compiler

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.search.ProjectScope
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
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
import org.jetbrains.kotlin.cli.jvm.compiler.pipeline.convertToIrAndActualizeForJvm
import org.jetbrains.kotlin.codegen.ClassBuilderFactories
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.diagnostics.DiagnosticReporterFactory
import org.jetbrains.kotlin.fir.BinaryModuleData
import org.jetbrains.kotlin.fir.DependencyListForCliModule
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.FirModuleDataImpl
import org.jetbrains.kotlin.fir.backend.jvm.FirJvmBackendClassResolver
import org.jetbrains.kotlin.fir.backend.jvm.FirJvmBackendExtension
import org.jetbrains.kotlin.fir.backend.jvm.JvmFir2IrExtensions
import org.jetbrains.kotlin.fir.backend.utils.extractFirDeclarations
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.java.FirProjectSessionProvider
import org.jetbrains.kotlin.fir.pipeline.FirResult
import org.jetbrains.kotlin.fir.pipeline.buildResolveAndCheckFirFromKtFiles
import org.jetbrains.kotlin.fir.session.FirJvmSessionFactory
import org.jetbrains.kotlin.fir.session.environment.AbstractProjectEnvironment
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.CommonPlatforms
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms

class K2CompilerProvider(environment: KotlinCoreEnvironment) : KotlinCompiler(environment) {
  private val project: Project get() = environment.project
  private val configuration: CompilerConfiguration get() = environment.configuration

  private fun createSourceSession(
    moduleData: FirModuleData,
    projectSessionProvider: FirProjectSessionProvider,
    projectEnvironment: AbstractProjectEnvironment,
  ) =
    FirJvmSessionFactory.createModuleBasedSession(
      moduleData = moduleData,
      sessionProvider = projectSessionProvider,
      javaSourcesScope = PsiBasedProjectFileSearchScope(TopDownAnalyzerFacadeForJVM.AllJavaSourcesInProjectScope(project)),
      projectEnvironment = projectEnvironment,
      createIncrementalCompilationSymbolProviders = { null },
      extensionRegistrars = FirExtensionRegistrar.getInstances(project),
      languageVersionSettings = configuration.languageVersionSettings,
      jvmTarget = configuration[JVMConfigurationKeys.JVM_TARGET] ?: error("JVM_TARGET is not specified in compiler configuration"),
      lookupTracker = configuration[CommonConfigurationKeys.LOOKUP_TRACKER],
      enumWhenTracker = configuration[CommonConfigurationKeys.ENUM_WHEN_TRACKER],
      importTracker = configuration[CommonConfigurationKeys.IMPORT_TRACKER],
      predefinedJavaComponents = null,
      needRegisterJavaElementFinder = true,
      registerExtraComponents = {},
      init = {},
    )

  override fun analyze(platformFiles: List<SourceFile>, commonFiles: List<SourceFile>): FirAnalysisResult {
    val rootModuleName = configuration[CommonConfigurationKeys.MODULE_NAME, "test-main"]
    val projectSessionProvider = FirProjectSessionProvider()
    val binaryModuleData = BinaryModuleData.initialize(
      mainModuleName = Name.identifier(rootModuleName),
      platform = CommonPlatforms.defaultCommonPlatform,
    )
    val dependencyList = DependencyListForCliModule.build(binaryModuleData)
    val projectEnvironment = VfsBasedProjectEnvironment(
      project = project,
      localFileSystem = VirtualFileManager.getInstance().getFileSystem(StandardFileSystems.FILE_PROTOCOL),
      getPackagePartProviderFn = environment::createPackagePartProvider,
    )
    val librariesScope = PsiBasedProjectFileSearchScope(ProjectScope.getLibrariesScope(project))

    FirJvmSessionFactory.createLibrarySession(
      mainModuleName = Name.identifier(rootModuleName),
      sessionProvider = projectSessionProvider,
      moduleDataProvider = dependencyList.moduleDataProvider,
      projectEnvironment = projectEnvironment,
      extensionRegistrars = FirExtensionRegistrar.getInstances(project),
      scope = librariesScope,
      packagePartProvider = projectEnvironment.getPackagePartProvider(librariesScope),
      languageVersionSettings = configuration.languageVersionSettings,
      predefinedJavaComponents = null,
      registerExtraComponents = {},
    )

    val commonModuleData = FirModuleDataImpl(
      name = Name.identifier("$rootModuleName-common"),
      dependencies = dependencyList.regularDependencies,
      dependsOnDependencies = dependencyList.dependsOnDependencies,
      friendDependencies = dependencyList.friendsDependencies,
      platform = CommonPlatforms.defaultCommonPlatform,
    )
    val platformModuleData = FirModuleDataImpl(
      name = Name.identifier(rootModuleName),
      dependencies = dependencyList.regularDependencies,
      dependsOnDependencies = dependencyList.dependsOnDependencies + commonModuleData,
      friendDependencies = dependencyList.friendsDependencies,
      platform = JvmPlatforms.jvm17,
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
    )

    val fir2IrResult = analysisResult.firResult.convertToIrAndActualizeForJvm(
      fir2IrExtensions = fir2IrExtensions,
      configuration = configuration,
      diagnosticsReporter = analysisResult.reporter,
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

    val generationState =
      GenerationState.Builder(
        project = project,
        builderFactory = ClassBuilderFactories.TEST,
        module = irModuleFragment.descriptor,
        bindingContext = NoScopeRecordCliBindingTrace(project).bindingContext,
        configuration = configuration,
      )
        .isIrBackend(true)
        .jvmBackendClassResolver(FirJvmBackendClassResolver(components))
        .build()
        .also { it.beforeCompile() }

    val codegenFactory = JvmIrCodegenFactory(
      configuration = configuration,
      phaseConfig = configuration[CLIConfigurationKeys.PHASE_CONFIG],
    )
    codegenFactory.generateModuleInFrontendIRMode(
      state = generationState,
      irModuleFragment = irModuleFragment,
      symbolTable = frontendResult.firResult.symbolTable,
      irProviders = components.irProviders,
      extensions = frontendResult.generatorExtensions,
      backendExtension = FirJvmBackendExtension(
        components = components,
        actualizedExpectDeclarations = run {
          frontendResult.firResult.irActualizedResult
            ?.actualizedExpectDeclarations
            ?.extractFirDeclarations()
        },
      ),
      irPluginContext = frontendResult.firResult.pluginContext,
    )
    generationState.factory.done()

    return generationState
  }
}
