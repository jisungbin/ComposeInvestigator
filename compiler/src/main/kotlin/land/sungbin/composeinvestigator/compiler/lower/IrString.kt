package land.sungbin.composeinvestigator.compiler.lower

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl

internal fun IrPluginContext.irString(
  value: String,
  startOffset: Int = UNDEFINED_OFFSET,
  endOffset: Int = UNDEFINED_OFFSET,
): IrConst<String> = IrConstImpl.string(
  startOffset = startOffset,
  endOffset = endOffset,
  type = irBuiltIns.stringType,
  value = value,
)
