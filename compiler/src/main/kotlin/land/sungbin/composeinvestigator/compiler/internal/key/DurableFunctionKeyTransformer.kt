/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.internal.key

import androidx.compose.compiler.plugins.kotlin.EmptyModuleMetrics
import androidx.compose.compiler.plugins.kotlin.analysis.StabilityInferencer
import androidx.compose.compiler.plugins.kotlin.irTrace
import androidx.compose.compiler.plugins.kotlin.lower.ComposableSymbolRemapper
import androidx.compose.compiler.plugins.kotlin.lower.DurableKeyTransformer
import androidx.compose.compiler.plugins.kotlin.lower.DurableKeyVisitor
import land.sungbin.composeinvestigator.compiler.internal.tracker.affect.IrAffectedComposable
import land.sungbin.composeinvestigator.compiler.util.getSafelyLocation
import land.sungbin.composeinvestigator.compiler.util.irInt
import land.sungbin.composeinvestigator.compiler.util.irString
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.name.FqName

internal class DurableFunctionKeyTransformer(
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
    val (keyName) = buildKey("fun-${declaration.signatureString()}")
    val location = declaration.getSafelyLocation()

    val affectedComposable = IrAffectedComposable.irAffectedComposable(
      composableName = irString(declaration.name.asString()),
      packageName = irString((declaration.fqNameWhenAvailable?.parent() ?: FqName.ROOT).asString()),
      filePath = irString(location.file),
      startLine = irInt(location.line),
      startColumn = irInt(location.column),
    )

    val keyInfo = KeyInfo(keyName = keyName, affectedComposable = affectedComposable)
    currentKeys += keyInfo
    irTrace[DurableWritableSlices.DURABLE_FUNCTION_KEY, declaration] = keyInfo

    return super.visitSimpleFunction(declaration)
  }
}
