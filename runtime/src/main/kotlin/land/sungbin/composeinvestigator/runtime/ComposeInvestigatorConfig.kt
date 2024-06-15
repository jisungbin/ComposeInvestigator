/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

import android.util.Log

/** Customize how ComposeInvestigator reports. */
@ExperimentalComposeInvestigatorApi
public object ComposeInvestigatorConfig {
  @Suppress("MemberVisibilityCanBePrivate")
  public const val LOGGER_DEFAULT_TAG: String = "ComposeInvestigator"

  /**
   * This logger is called whenever an invalidation is performed.
   * This field is variable, so you can easily customize this.
   */
  public var invalidationLogger: ComposableInvalidationLogger = ComposableInvalidationLogger { _, composable, type ->
    Log.d(LOGGER_DEFAULT_TAG, "The '${composable.name}' composable has been invalidated.\n$type")
  }

  /**
   * This callback is called whenever the state value being traced changes.
   * This field is variable, so you can easily customize this.
   */
  public var stateChangedListener: StateChangedListener = StateChangedListener { composable, name, previousValue, newValue ->
    Log.d(
      LOGGER_DEFAULT_TAG,
      "The state of '$name' inside '${composable.name}' composable has changed. previousValue=$previousValue, newValue=$newValue",
    )
  }
}
