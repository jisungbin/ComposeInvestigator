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
// TODO when requesting an instance at the IR level, an existing instance
//  should be reused if it has the same signature. Currently, we create a
//  new instance for every time, which can result in numerous GCs being
//  requested for every recomposition.
// TODO add `compoundKey` field
@Immutable
public data class ComposableInformation(
  public val name: String,
  public val packageName: String,
  public val fileName: String,
)

/** Fully-qualified name of the Composable function. */
public val ComposableInformation.fqPackageName: String
  @Stable get() = packageName.takeUnless(String::isEmpty)?.plus(".").orEmpty() + name
