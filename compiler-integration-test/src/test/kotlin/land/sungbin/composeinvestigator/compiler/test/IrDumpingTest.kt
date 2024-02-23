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

// FIXME: Failed to lookup symbols with 'fqName == kotlin.collections.MutableList.add',
//  'fn.owner.valueParameters.size == 1' in Kotlin 2.0. Needs to be fixed in the future.
class IrDumpingTest(@Suppress("UNUSED_PARAMETER") useFir: Boolean) : AbstractCompilerTest(useFir = false) {
  @Test fun dump() {
    val ir = compileToIr(sourceFiles = listOf(source("SourceForIrDump.kt")))
    println(ir.dumpSrc(useFir = useFir))
  }
}
