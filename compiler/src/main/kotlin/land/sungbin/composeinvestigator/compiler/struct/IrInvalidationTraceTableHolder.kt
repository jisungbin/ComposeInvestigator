// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.struct

import org.jetbrains.kotlin.ir.declarations.IrFile

/**
 * An interface that holds an initialized `ComposableInvalidationTraceTable` for
 * each file.
 */
public fun interface IrInvalidationTraceTableHolder {
  /**
   * Returns an instantiated [IrInvalidationTraceTable] class for the given file.
   * If it cannot be found, an exception is thrown.
   */
  public fun tableByFile(file: IrFile): IrInvalidationTraceTable
}

/** Same as [IrInvalidationTraceTableHolder.tableByFile]. */
public operator fun IrInvalidationTraceTableHolder.get(file: IrFile): IrInvalidationTraceTable =
  tableByFile(file)
