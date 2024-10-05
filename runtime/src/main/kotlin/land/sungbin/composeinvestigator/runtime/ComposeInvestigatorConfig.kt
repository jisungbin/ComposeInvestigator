// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.runtime

import java.util.logging.Level
import java.util.logging.Logger

/**
 * Customize how ComposeInvestigator reports. Used globally for ComposeInvestigator in the
 * current module.
 */
public object ComposeInvestigatorConfig {
  private val realLogger by lazy { Logger.getLogger(LOGGER_DEFAULT_TAG).apply { level = Level.FINE } }

  public const val LOGGER_DEFAULT_TAG: String = "ComposeInvestigator"

  /**
   * This logger is called whenever an recomposition is processed. This field is variable,
   * so you can easily change this.
   */
  public var logger: ComposableInvalidationLogger = ComposableInvalidationLogger { composable, type ->
    realLogger.log(Level.FINE, "The '{}' composable has been recomposed.\n$type", composable.name)
  }
}
