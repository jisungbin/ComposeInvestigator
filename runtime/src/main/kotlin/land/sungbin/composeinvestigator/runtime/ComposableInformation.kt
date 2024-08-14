/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * Indicates which Composables were affected by the state change.
 *
 * @param name Composable function name
 * @param packageName Package name that the Composable function is defined
 * @param fileName File name that the Composable function is defined
 */
@Immutable
public data class ComposableInformation(
  public val name: String,
  public val packageName: String,
  public val fileName: String,
)

/** Fully-qualified name of the Composable function. */
public val ComposableInformation.fqPackageName: String
  @Stable get() = packageName.takeUnless(String::isEmpty)?.plus(".").orEmpty() + name
