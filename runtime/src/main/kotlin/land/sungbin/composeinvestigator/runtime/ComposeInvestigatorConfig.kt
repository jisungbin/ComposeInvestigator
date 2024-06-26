/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

import android.util.Log

/**
 * Customize how ComposeInvestigator reports. Used globally for
 * ComposeInvestigator in the current module.
 */
public object ComposeInvestigatorConfig {
  @Suppress("MemberVisibilityCanBePrivate")
  public const val LOGGER_DEFAULT_TAG: String = "ComposeInvestigator"

  /**
   * This logger is called whenever an recomposition is processed.
   * This field is variable, so you can easily change this.
   */
  public var invalidationLogger: ComposableInvalidationLogger = ComposableInvalidationLogger { composable, type ->
    Log.d(LOGGER_DEFAULT_TAG, "The '${composable.name}' composable has been recomposed.\n$type")
  }
}
