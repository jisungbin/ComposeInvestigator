// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.analysis

import androidx.compose.compiler.plugins.kotlin.EmptyModuleMetrics
import androidx.compose.compiler.plugins.kotlin.FeatureFlags
import androidx.compose.compiler.plugins.kotlin.analysis.StabilityInferencer
import androidx.compose.compiler.plugins.kotlin.irTrace
import androidx.compose.compiler.plugins.kotlin.lower.DurableKeyTransformer
import androidx.compose.compiler.plugins.kotlin.lower.DurableKeyVisitor
import land.sungbin.composeinvestigator.compiler.log
import land.sungbin.composeinvestigator.compiler.lower.irString
import land.sungbin.composeinvestigator.compiler.struct.IrComposableInformation
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.getCompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.name.FqName

/**
 * Visitor that generates a [ComposableKeyInfo] and stores it in
 * [DurationWritableSlices.DURABLE_FUNCTION_KEY].
 *
 * The rules for generating the [unique path][ComposableKeyInfo.keyName] are the
 * same as the [DurableKeyTransformer] in the Compose Compiler.
 */
// TODO Complete our own implementation using FIR.
public class DurableComposableKeyAnalyzer(
  context: IrPluginContext,
  stabilityInferencer: StabilityInferencer,
  private val messageCollector: MessageCollector, // TODO context.createDiagnosticReporter() (Blocked: "This API is not supported for K2")
  featureFlags: FeatureFlags = FeatureFlags(), // TODO Supports this feature
) : DurableKeyTransformer(
  context = context,
  keyVisitor = DurableKeyVisitor(),
  stabilityInferencer = stabilityInferencer,
  metrics = EmptyModuleMetrics,
  featureFlags = featureFlags,
) {
  private val irComposableInformation = IrComposableInformation(context)

  override fun visitFile(declaration: IrFile): IrFile =
    root(keys = mutableSetOf()) { super.visitFile(declaration) }

  override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
    val (keyName, success) = buildKey("fun-${declaration.signatureString()}")

    // TODO log -> error
    if (!success) messageCollector.log("Duplicate key: $keyName", declaration.getCompilerMessageLocation(declaration.file))

    val composable = irComposableInformation(
      name = context.irString(declaration.name.asString()),
      packageName = context.irString((declaration.fqNameWhenAvailable?.parent() ?: FqName.ROOT).asString()),
      fileName = context.irString(declaration.file.name),
    )

    val keyInfo = ComposableKeyInfo(keyName = keyName, composable = composable)
    context.irTrace[DurationWritableSlices.DURABLE_FUNCTION_KEY, declaration] = keyInfo

    return super.visitSimpleFunction(declaration)
  }
}
