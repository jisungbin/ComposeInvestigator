/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */


package land.sungbin.composeinvestigator.compiler.test._source

import land.sungbin.composeinvestigator.compiler.test.kotlincompiler.facade.SourceFile
import java.io.File

@Suppress("NOTHING_TO_INLINE")
inline fun source(filename: String): SourceFile =
  SourceFile(
    name = filename,
    source = File("src/test/kotlin/land/sungbin/composeinvestigator/compiler/test/_source/$filename").readText(),
  )
