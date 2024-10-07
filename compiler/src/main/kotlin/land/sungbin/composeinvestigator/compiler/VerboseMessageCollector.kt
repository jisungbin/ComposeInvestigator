// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

/**
 * A [MessageCollector] that outputs messages only when verbose is `true`. The verbose value
 * can be set to `true` by using the [verbose] function.
 */
internal class VerboseMessageCollector(private val delegate: MessageCollector) : MessageCollector by delegate {
  private var verbose = false

  /** Enables verbose logging. */
  internal fun verbose(): VerboseMessageCollector = apply { verbose = true }

  override fun report(
    severity: CompilerMessageSeverity,
    message: String,
    location: CompilerMessageSourceLocation?,
  ) {
    if (!verbose) return
    delegate.report(severity, message, location)
  }
}

internal fun MessageCollector.log(message: String, location: CompilerMessageSourceLocation? = null) {
  report(CompilerMessageSeverity.LOGGING, message, location)
}

internal fun MessageCollector.error(message: String, location: CompilerMessageSourceLocation? = null) {
  report(CompilerMessageSeverity.ERROR, message, location)
}
