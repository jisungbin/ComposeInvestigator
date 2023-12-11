/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

import android.util.Log

@Suppress("MemberVisibilityCanBePrivate")
public object ComposeInvestigatorConfig {
  public const val LOGGER_DEFAULT_TAG: String = "ComposeInvestigator"

  public var invalidationLogger: ComposableInvalidationLogger = ComposableInvalidationLogger { composable, type ->
    Log.d(LOGGER_DEFAULT_TAG, "The '${composable.name}' composable has been invalidated.\n$type")
  }
}
