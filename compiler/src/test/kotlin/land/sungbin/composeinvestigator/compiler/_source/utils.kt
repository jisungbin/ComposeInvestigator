// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler._source

import java.io.File

fun sourcePath(filename: String): String =
  "src/test/kotlin/land/sungbin/composeinvestigator/compiler/_source/$filename"

fun sourceString(filename: String): String =
  File(sourcePath(filename))
    .also { file -> require(file.isFile) { "source file not found: $filename" } }
    .readText()
    .also { code -> require(code.isNotBlank()) { "source file is empty: $filename" } }
