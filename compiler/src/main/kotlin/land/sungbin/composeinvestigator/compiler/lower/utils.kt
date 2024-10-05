// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.lower

import androidx.compose.compiler.plugins.kotlin.hasComposableAnnotation
import androidx.compose.compiler.plugins.kotlin.lower.dumpSrc
import org.jetbrains.kotlin.backend.common.ScopeWithIr
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.utils.addToStdlib.safeAs
import org.jetbrains.kotlin.utils.exceptions.rethrowIntellijPlatformExceptionIfNeeded

internal fun IrPluginContext.irString(
  value: String,
  startOffset: Int = UNDEFINED_OFFSET,
  endOffset: Int = UNDEFINED_OFFSET,
): IrConst = IrConstImpl.string(
  startOffset = startOffset,
  endOffset = endOffset,
  type = irBuiltIns.stringType,
  value = value,
)

internal fun List<ScopeWithIr>.lastComposable() =
  this
    .lastOrNull { scope -> scope.irElement.safeAs<IrSimpleFunction>()?.hasComposableAnnotation() == true }
    ?.irElement?.safeAs<IrSimpleFunction>()

internal inline fun <T> includeFileIRInExceptionTrace(file: IrFile, body: () -> T): T {
  try {
    return body()
  } catch (exception: Exception) {
    rethrowIntellijPlatformExceptionIfNeeded(exception)
    throw Exception(
      "IR lowering fail: ${file.dump()}\n\n\n${file.dumpSrc(useFir = true)}",
      exception,
    )
  }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun <T> unsafeLazy(noinline initializer: () -> T): Lazy<T> =
  lazy(LazyThreadSafetyMode.NONE, initializer)
