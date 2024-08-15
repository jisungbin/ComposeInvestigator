package land.sungbin.composeinvestigator.compiler._source

import java.io.File
import land.sungbin.composeinvestigator.compiler._compilation.SourceFile

fun source(filename: String): SourceFile =
  SourceFile(name = filename, source = sourceString(filename))

fun sourceString(filename: String): String =
  File("src/test/kotlin/land/sungbin/composeinvestigator/compiler/_source/$filename").readText()