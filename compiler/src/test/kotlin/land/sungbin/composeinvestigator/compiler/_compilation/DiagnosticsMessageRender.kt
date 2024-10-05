// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler._compilation

import java.io.Closeable
import java.io.File
import java.io.InputStreamReader
import java.util.TreeSet
import org.jetbrains.kotlin.cli.common.fir.SequentialPositionFinder
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageUtil
import org.jetbrains.kotlin.diagnostics.DiagnosticUtils
import org.jetbrains.kotlin.diagnostics.KtDiagnostic
import org.jetbrains.kotlin.diagnostics.KtPsiDiagnostic
import org.jetbrains.kotlin.diagnostics.impl.BaseDiagnosticsCollector

// https://github.com/JetBrains/kotlin/blob/bb25d2f8aa74406ff0af254b2388fd601525386a/compiler/cli/src/org/jetbrains/kotlin/cli/common/fir/FirDiagnosticsCompilerResultsReporter.kt#L47
fun BaseDiagnosticsCollector.withCompilerMessageSourceLocation() =
  buildMap<KtDiagnostic, CompilerMessageSourceLocation> {
    for (filePath in diagnosticsByFilePath.keys) {
      val positionFinder = lazy {
        val file = filePath?.let(::File)
        if (file != null && file.isFile) SequentialFilePositionFinder(file.reader()) else null
      }

      try {
        val diagnostics = diagnosticsByFilePath[filePath].orEmpty()
        val offsetsToPositions = positionFinder.value?.let { finder ->
          val sortedOffsets = TreeSet<Int>().apply {
            for (diagnostic in diagnostics) {
              if (diagnostic !is KtPsiDiagnostic) {
                val range = DiagnosticUtils.firstRange(diagnostic.textRanges)
                add(range.startOffset)
                add(range.endOffset)
              }
            }
          }
          sortedOffsets.associateWith { pos -> finder.findNextPosition(pos) }
        }

        for (diagnostic in diagnostics.sortedWith(InFileDiagnosticsComparator)) {
          when (diagnostic) {
            is KtPsiDiagnostic -> {
              val file = diagnostic.element.psi.containingFile
              MessageUtil.psiFileToMessageLocation(
                /* file = */ file,
                /* defaultValue = */ file.name,
                /* range = */ DiagnosticUtils.getLineAndColumnRange(file, diagnostic.textRanges),
              )
            }
            else -> {
              offsetsToPositions?.let {
                val range = DiagnosticUtils.firstRange(diagnostic.textRanges)
                val start = offsetsToPositions[range.startOffset]!!
                val end = offsetsToPositions[range.endOffset]!!
                MessageUtil.createMessageLocation(
                  /* path = */ filePath,
                  /* lineContent = */ start.lineContent,
                  /* line = */ start.line,
                  /* column = */ start.column,
                  /* endLine = */ end.line,
                  /* endColumn = */ end.column,
                )
              }
            }
          }?.let { location ->
            put(diagnostic, location)
          }
        }
      } finally {
        if (positionFinder.isInitialized()) {
          positionFinder.value?.close()
        }
      }
    }
  }

private object InFileDiagnosticsComparator : Comparator<KtDiagnostic> {
  override fun compare(o1: KtDiagnostic, o2: KtDiagnostic): Int {
    val range1 = DiagnosticUtils.firstRange(o1.textRanges)
    val range2 = DiagnosticUtils.firstRange(o2.textRanges)

    return if (range1 != range2) {
      DiagnosticUtils.TEXT_RANGE_COMPARATOR.compare(range1, range2)
    } else {
      o1.factory.name.compareTo(o2.factory.name)
    }
  }
}

private class SequentialFilePositionFinder(private val reader: InputStreamReader) :
  Closeable by reader, SequentialPositionFinder(reader)
