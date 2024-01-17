/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.util

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

internal fun IrPluginContext.irInt(value: Int): IrConst<Int> =
  IrConstImpl.int(
    startOffset = UNDEFINED_OFFSET,
    endOffset = UNDEFINED_OFFSET,
    type = irBuiltIns.intType,
    value = value,
  )

internal fun IrPluginContext.irBoolean(value: Boolean): IrConst<Boolean> =
  IrConstImpl.boolean(
    startOffset = UNDEFINED_OFFSET,
    endOffset = UNDEFINED_OFFSET,
    type = irBuiltIns.booleanType,
    value = value,
  )
