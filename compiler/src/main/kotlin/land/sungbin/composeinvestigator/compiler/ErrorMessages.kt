/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler

internal object ErrorMessages {
  const val COMPOSABLE_NAME_ONLY_HARDCODED =
    "Currently, only string hardcodes are supported as arguments to ComposableName."

  const val SUPPORTS_K2_ONLY = "ComposeInvestigator plugin supports only Kotlin 2.0.0 or higher."
}
