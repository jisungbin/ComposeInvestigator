// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.runtime

/** Configures the runtime operation of ComposeInvestigator. */
public object ComposeInvestigator {
  public const val LOGGER_DEFAULT_TAG: String = "ComposeInvestigator"

  /**
   * This logger is called whenever an recomposition is processed or skipped. This field
   * is variable, so you can easily change this.
   */
  public var logger: ComposableInvalidationLogger = ComposableInvalidationLogger { composable, result ->
    println("[$LOGGER_DEFAULT_TAG] The '${composable.name}' composable has been recomposed.\n$result")
  }
}
