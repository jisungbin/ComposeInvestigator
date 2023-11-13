@file:Suppress("NOTHING_TO_INLINE")

package land.sungbin.composeinvalidator.compiler.utils

import com.tschuchort.compiletesting.SourceFile
import java.io.File

inline fun source(filename: String) =
  SourceFile.fromPath(File("src/test/kotlin/land/sungbin/composeinvalidator/compiler/source/$filename"))
