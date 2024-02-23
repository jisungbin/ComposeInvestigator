/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test

import land.sungbin.composeinvestigator.compiler.test._source.source
import land.sungbin.composeinvestigator.compiler.test.kotlincompiler.AbstractCompilerTest
import land.sungbin.composeinvestigator.compiler.test.kotlincompiler.dumpSrc
import org.junit.Test

class IrDumpingTest(useFir: Boolean) : AbstractCompilerTest(useFir = useFir) {
  @Test fun dump() {
    val ir = compileToIr(sourceFiles = listOf(source("SourceForIrDump.kt")))
    println(ir.dumpSrc(useFir = useFir))
  }
}
