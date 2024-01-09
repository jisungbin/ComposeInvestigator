/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.internal.tracker.key

import androidx.compose.compiler.plugins.kotlin.EmptyModuleMetrics
import androidx.compose.compiler.plugins.kotlin.analysis.StabilityInferencer
import androidx.compose.compiler.plugins.kotlin.irTrace
import androidx.compose.compiler.plugins.kotlin.lower.ComposableSymbolRemapper
import androidx.compose.compiler.plugins.kotlin.lower.DurableKeyTransformer
import androidx.compose.compiler.plugins.kotlin.lower.DurableKeyVisitor
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

internal class TrackerFunctionKeyVisitor(
  context: IrPluginContext,
  stabilityInferencer: StabilityInferencer,
) : DurableKeyTransformer(
  keyVisitor = DurableKeyVisitor(),
  context = context,
  symbolRemapper = ComposableSymbolRemapper(),
  stabilityInferencer = stabilityInferencer,
  metrics = EmptyModuleMetrics,
), IrPluginContext by context {
  private var currentKeys = mutableListOf<KeyInfo>()

  override fun visitFile(declaration: IrFile): IrFile {
    val stringKeys = mutableSetOf<String>()
    return root(stringKeys) {
      val prev = currentKeys
      val next = mutableListOf<KeyInfo>()
      try {
        currentKeys = next
        super.visitFile(declaration)
      } finally {
        currentKeys = prev
      }
    }
  }

  override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
    val signature = declaration.signatureString()
    val (keyName) = buildKey("fun-$signature")
    val keyInfo = KeyInfo(keyName = keyName, userProvideName = null)
    currentKeys += keyInfo
    irTrace[TrackerWritableSlices.SIMPLE_FUNCTION_KEY, declaration] = keyInfo
    return super.visitSimpleFunction(declaration)
  }
}
