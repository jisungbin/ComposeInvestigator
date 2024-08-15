/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.struct

import org.jetbrains.kotlin.ir.declarations.IrFile

public fun interface IrInvalidationTraceTableHolder {
  public fun tableByFile(file: IrFile): IrInvalidationTraceTable
}

public operator fun IrInvalidationTraceTableHolder.get(file: IrFile): IrInvalidationTraceTable =
  tableByFile(file)
