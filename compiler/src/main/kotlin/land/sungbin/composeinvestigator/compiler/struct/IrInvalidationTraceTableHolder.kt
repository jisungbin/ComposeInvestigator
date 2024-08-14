package land.sungbin.composeinvestigator.compiler.struct

import org.jetbrains.kotlin.ir.declarations.IrFile

internal interface IrInvalidationTraceTableHolder {
  fun getByFile(file: IrFile): IrInvalidationTraceTable
}
