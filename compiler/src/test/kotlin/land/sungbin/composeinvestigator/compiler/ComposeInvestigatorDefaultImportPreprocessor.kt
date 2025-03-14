// Copyright 2025 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

import org.jetbrains.kotlin.test.model.TestFile
import org.jetbrains.kotlin.test.services.ReversibleSourceFilePreprocessor
import org.jetbrains.kotlin.test.services.TestServices

class ComposeInvestigatorDefaultImportPreprocessor(service: TestServices) : ReversibleSourceFilePreprocessor(service) {
  private val additionalImportStatements
    get() =
      """
        import androidx.compose.runtime.*
        import land.sungbin.composeinvestigator.runtime.*
      """.trimIndent()

  override fun process(file: TestFile, content: String): String {
    if (file.isAdditional) return content

    val lines = content.lines().toMutableList()
    when (val packageIndex = lines.indexOfFirst { it.startsWith("package ") }) {
      // No package declaration found.
      -1 ->
        when (val nonBlankIndex = lines.indexOfFirst { it.isNotBlank() }) {
          // No non-blank lines? Place imports at the very beginning...
          -1 -> lines.add(0, additionalImportStatements)

          // Place imports before first non-blank line.
          else -> lines.add(nonBlankIndex, additionalImportStatements)
        }

      // Place imports just after package declaration.
      else -> lines.add(packageIndex + 1, additionalImportStatements)
    }
    return lines.joinToString(separator = "\n")
  }

  override fun revert(file: TestFile, actualContent: String): String {
    if (file.isAdditional) return actualContent
    return actualContent.replace(additionalImportStatements + "\n", "")
  }
}
