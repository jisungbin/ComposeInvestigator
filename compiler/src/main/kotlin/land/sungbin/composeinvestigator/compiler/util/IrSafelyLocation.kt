/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.util

import land.sungbin.composeinvestigator.compiler.UNKNOWN_STRING
import org.jetbrains.kotlin.backend.wasm.ir2wasm.getSourceLocation
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.wasm.ir.source.location.SourceLocation

public fun IrFunction.getSafelyLocation(): SourceLocation.Location =
  getSourceLocation(file.fileEntry).let { location ->
    if (location is SourceLocation.Location) location.copy(line = location.line + 1) // Humans read from 1.
    else SourceLocation.Location(file = SpecialNames.UNKNOWN_STRING, line = UNDEFINED_OFFSET, column = UNDEFINED_OFFSET)
  }
