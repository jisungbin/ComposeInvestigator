@file:Suppress("NOTHING_TO_INLINE")

package land.sungbin.composeinvestigator.compiler.test.utils

import com.tschuchort.compiletesting.SourceFile
import java.io.File

inline fun source(filename: String) =
  SourceFile.fromPath(File("src/test/kotlin/land/sungbin/composeinvestigator/compiler/test/source/$filename"))
