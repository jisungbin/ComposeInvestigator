/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler._source

import java.io.File
import land.sungbin.composeinvestigator.compiler._compilation.SourceFile

fun source(filename: String): SourceFile =
  SourceFile(
    name = filename.substringAfterLast('/'),
    source = sourceString(filename),
    path = sourcePath(filename).substringBeforeLast('/'),
  )

fun sourcePath(filename: String): String =
  "src/test/kotlin/land/sungbin/composeinvestigator/compiler/_source/$filename"

fun sourceString(filename: String): String =
  File(sourcePath(filename))
    .also { file -> require(file.isFile) { "source file not found: $filename" } }
    .readText()
    .also { code -> require(code.isNotBlank()) { "source file is empty: $filename" } }
