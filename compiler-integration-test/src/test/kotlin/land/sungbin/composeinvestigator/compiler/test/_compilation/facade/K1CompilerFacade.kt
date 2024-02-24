/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test._compilation.facade

import land.sungbin.composeinvestigator.compiler.test._compilation.exception.TestCompilerException
import org.jetbrains.kotlin.backend.jvm.JvmIrCodegenFactory
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.jvm.compiler.CliBindingTrace
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.TopDownAnalyzerFacadeForJVM
import org.jetbrains.kotlin.codegen.ClassBuilderFactories
import org.jetbrains.kotlin.codegen.CodegenFactory
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.AnalyzingUtils
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.multiplatform.isCommonSource

class K1AnalysisResult(
  override val files: List<KtFile>,
  val moduleDescriptor: ModuleDescriptor,
  val bindingContext: BindingContext,
) : AnalysisResult {
  override val diagnostics: Map<String, List<AnalysisResult.Diagnostic>>
    get() = bindingContext.diagnostics.all().groupBy(
      keySelector = { key -> key.psiFile.name },
      valueTransform = { value -> AnalysisResult.Diagnostic(value.factoryName, value.textRanges) },
    )
}

private class K1FrontendResult(
  val state: GenerationState,
  val backendInput: JvmIrCodegenFactory.JvmIrBackendInput,
  val codegenFactory: JvmIrCodegenFactory,
)

class K1CompilerFacade(environment: KotlinCoreEnvironment) : KotlinCompilerFacade(environment) {
  override fun analyze(platformFiles: List<SourceFile>, commonFiles: List<SourceFile>): K1AnalysisResult {
    val allKtFiles =
      platformFiles.map { file -> file.toKtFile(environment.project) } +
        commonFiles.map { file -> file.toKtFile(environment.project).apply { isCommonSource = true } }

    val result = TopDownAnalyzerFacadeForJVM.analyzeFilesWithJavaIntegration(
      project = environment.project,
      files = allKtFiles,
      trace = CliBindingTrace(),
      configuration = environment.configuration,
      packagePartProvider = environment::createPackagePartProvider,
    )

    try {
      result.throwIfError()
    } catch (exception: Exception) {
      throw TestCompilerException(exception)
    }

    return K1AnalysisResult(
      files = allKtFiles,
      moduleDescriptor = result.moduleDescriptor,
      bindingContext = result.bindingContext,
    )
  }

  private fun frontend(platformFiles: List<SourceFile>, commonFiles: List<SourceFile>): K1FrontendResult {
    val analysisResult = analyze(platformFiles = platformFiles, commonFiles = commonFiles)

    // `analyze` only throws if the analysis itself failed, since we use it to test code
    // with errors. That's why we have to check for errors before we run psi2ir.
    try {
      AnalyzingUtils.throwExceptionOnErrors(analysisResult.bindingContext)
    } catch (e: Exception) {
      throw TestCompilerException(e)
    }

    val codegenFactory = JvmIrCodegenFactory(
      configuration = environment.configuration,
      phaseConfig = environment.configuration.get(CLIConfigurationKeys.PHASE_CONFIG),
    )

    val state = GenerationState.Builder(
      project = environment.project,
      builderFactory = ClassBuilderFactories.TEST,
      module = analysisResult.moduleDescriptor,
      bindingContext = analysisResult.bindingContext,
      files = analysisResult.files,
      configuration = environment.configuration,
    )
      .isIrBackend(true)
      .codegenFactory(codegenFactory)
      .build()
    state.beforeCompile()

    val psi2irInput = CodegenFactory.IrConversionInput.fromGenerationStateAndFiles(
      state = state,
      files = analysisResult.files,
    )
    val backendInput = codegenFactory.convertToIr(psi2irInput)

    // For JVM-specific errors
    try {
      AnalyzingUtils.throwExceptionOnErrors(state.collectedExtraJvmDiagnostics)
    } catch (exception: Exception) {
      throw TestCompilerException(exception)
    }

    return K1FrontendResult(
      state = state,
      backendInput = backendInput,
      codegenFactory = codegenFactory,
    )
  }

  override fun compileToIr(files: List<SourceFile>): IrModuleFragment =
    frontend(platformFiles = files, commonFiles = emptyList()).backendInput.irModuleFragment

  override fun compile(platformFiles: List<SourceFile>, commonFiles: List<SourceFile>): GenerationState =
    try {
      frontend(platformFiles = platformFiles, commonFiles = commonFiles)
        .apply {
          codegenFactory.generateModule(state, backendInput)
          state.factory.done()
        }
        .state
    } catch (exception: Exception) {
      throw TestCompilerException(exception)
    }
}
