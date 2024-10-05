// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.struct

import org.jetbrains.kotlin.ir.declarations.IrFile

public fun interface IrInvalidationTraceTableHolder {
  public fun tableByFile(file: IrFile): IrInvalidationTraceTable
}

public operator fun IrInvalidationTraceTableHolder.get(file: IrFile): IrInvalidationTraceTable =
  tableByFile(file)
