// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.runtime

import androidx.compose.runtime.Composer
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * Indicates which Composables were affected by the state change.
 *
 * @param simpleName Composable function name
 * @param packageName Package name that the Composable function is defined
 * @param fileName File name that the Composable function is defined
 * @param compoundKey Same as [Composer.compoundKeyHash]. If no lookup
 * is possible, `null`.
 */
@Immutable public data class ComposableInformation(
  public val fileName: String,
  public val packageName: String,
  public val simpleName: String,
  public val compoundKey: Int? = null,
  public val origin: Origin = Origin.Function,
) {
  public enum class Origin {
    Function,
    Lambda,
  }
}

/** Fully-qualified name of the Composable function. */
public val ComposableInformation.qualifiedName: String
  @Stable get() = packageName.takeUnless(String::isEmpty)?.plus(".").orEmpty() + simpleName
