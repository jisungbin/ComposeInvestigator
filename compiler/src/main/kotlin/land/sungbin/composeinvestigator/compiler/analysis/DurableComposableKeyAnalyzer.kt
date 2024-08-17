/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.analysis

import androidx.compose.compiler.plugins.kotlin.EmptyModuleMetrics
import androidx.compose.compiler.plugins.kotlin.FeatureFlags
import androidx.compose.compiler.plugins.kotlin.analysis.StabilityInferencer
import androidx.compose.compiler.plugins.kotlin.irTrace
import androidx.compose.compiler.plugins.kotlin.lower.ComposableSymbolRemapper
import androidx.compose.compiler.plugins.kotlin.lower.DurableKeyTransformer
import androidx.compose.compiler.plugins.kotlin.lower.DurableKeyVisitor
import land.sungbin.composeinvestigator.compiler.ComposeInvestigatorCommandLineProcessor.Companion.PLUGIN_ID
import land.sungbin.composeinvestigator.compiler.error
import land.sungbin.composeinvestigator.compiler.lower.irString
import land.sungbin.composeinvestigator.compiler.lower.unsafeLazy
import land.sungbin.composeinvestigator.compiler.struct.IrComposableInformation
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.getCompilerMessageLocation
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.name.FqName

// TODO complete our own implementation using FIR.
public class DurableComposableKeyAnalyzer(
  context: IrPluginContext,
  stabilityInferencer: StabilityInferencer,
  featureFlags: FeatureFlags = FeatureFlags(), // TODO supports this feature
) : DurableKeyTransformer(
  context = context,
  keyVisitor = DurableKeyVisitor(),
  symbolRemapper = ComposableSymbolRemapper(),
  stabilityInferencer = stabilityInferencer,
  metrics = EmptyModuleMetrics,
  featureFlags = featureFlags,
) {
  private val messageCollector by unsafeLazy { context.createDiagnosticReporter(PLUGIN_ID) }

  private var currentKeys = mutableListOf<ComposableKeyInfo>()
  private val irComposableInformation = IrComposableInformation(context)

  override fun visitFile(declaration: IrFile): IrFile {
    val stringKeys = mutableSetOf<String>()
    return root(stringKeys) {
      val prev = currentKeys
      val next = mutableListOf<ComposableKeyInfo>()
      try {
        currentKeys = next
        super.visitFile(declaration)
      } finally {
        currentKeys = prev
      }
    }
  }

  override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
    val (keyName, success) = buildKey("fun-${declaration.signatureString()}")
    if (!success) messageCollector.error("Duplicate key: $keyName", declaration.getCompilerMessageLocation(declaration.file))

    val composable = irComposableInformation(
      name = context.irString(declaration.name.asString()),
      packageName = context.irString((declaration.fqNameWhenAvailable?.parent() ?: FqName.ROOT).asString()),
      fileName = context.irString(declaration.file.name),
    )

    val keyInfo = ComposableKeyInfo(keyName = keyName, composable = composable)
    currentKeys += keyInfo
    context.irTrace[DurationWritableSlices.DURABLE_FUNCTION_KEY, declaration] = keyInfo

    return super.visitSimpleFunction(declaration)
  }
}
