/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test

import androidx.compose.compiler.plugins.kotlin.lower.dumpSrc
import land.sungbin.composeinvestigator.compiler.test._compilation.AbstractK2CompilerTest
import land.sungbin.composeinvestigator.compiler.test._source.source
import org.junit.Test

class IrDumpingTest : AbstractK2CompilerTest() {
  @Test fun dump() {
    val ir = compileToIr(listOf(source("SourceForIrDump.kt")))
    println(ir.dumpSrc(useFir = true))
  }
}
