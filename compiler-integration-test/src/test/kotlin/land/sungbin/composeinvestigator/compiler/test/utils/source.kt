@file:Suppress("NOTHING_TO_INLINE", "unused")

package land.sungbin.composeinvestigator.compiler.test.utils

import com.tschuchort.compiletesting.SourceFile
import java.io.File

inline fun source(filename: String): SourceFile =
  SourceFile.fromPath(File("src/test/kotlin/land/sungbin/composeinvestigator/compiler/test/source/$filename"))

inline fun sourceString(filename: String): String =
  File("src/test/kotlin/land/sungbin/composeinvestigator/compiler/test/source/$filename").readText()
