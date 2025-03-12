// Copyright 2025 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

public class ComposeInvestigatorCompilationException(
  message: String,
  cause: Throwable? = null,
) : RuntimeException(message, cause)
