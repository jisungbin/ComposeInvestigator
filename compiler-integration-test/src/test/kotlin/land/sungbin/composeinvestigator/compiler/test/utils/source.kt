/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "unused")

package land.sungbin.composeinvestigator.compiler.test.utils

import com.tschuchort.compiletesting.SourceFile
import java.io.File

inline fun source(filename: String): SourceFile =
  SourceFile.fromPath(File("src/test/kotlin/land/sungbin/composeinvestigator/compiler/test/_source/$filename"))

inline fun sourceString(filename: String): String =
  File("src/test/kotlin/land/sungbin/composeinvestigator/compiler/test/_source/$filename").readText()
