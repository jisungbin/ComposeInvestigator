/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE")

package land.sungbin.composeinvestigator.compiler.test._source

import land.sungbin.composeinvestigator.compiler.test._compilation.compiler.SourceFile
import java.io.File

inline fun source(filename: String): SourceFile =
  SourceFile(name = filename, source = sourceString(filename))

inline fun sourceString(filename: String): String =
  File("src/test/kotlin/land/sungbin/composeinvestigator/compiler/test/_source/$filename").readText()
