/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.util

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl

public fun IrPluginContext.irString(
  value: String,
  startOffset: Int = UNDEFINED_OFFSET,
  endOffset: Int = UNDEFINED_OFFSET,
): IrConst<String> = IrConstImpl.string(
  startOffset = startOffset,
  endOffset = endOffset,
  type = irBuiltIns.stringType,
  value = value,
)

public fun IrPluginContext.irInt(
  value: Int,
  startOffset: Int = UNDEFINED_OFFSET,
  endOffset: Int = UNDEFINED_OFFSET,
): IrConst<Int> = IrConstImpl.int(
  startOffset = startOffset,
  endOffset = endOffset,
  type = irBuiltIns.intType,
  value = value,
)

public fun IrPluginContext.irBoolean(
  value: Boolean,
  startOffset: Int = UNDEFINED_OFFSET,
  endOffset: Int = UNDEFINED_OFFSET,
): IrConst<Boolean> = IrConstImpl.boolean(
  startOffset = startOffset,
  endOffset = endOffset,
  type = irBuiltIns.booleanType,
  value = value,
)
