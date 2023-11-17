/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

@file:OptIn(ComposeInvalidatorCompilerApi::class)

package land.sungbin.composeinvalidator.runtime

object ComposableInvalidationTrackTableScope : ComposableInvalidationTrackTable()

class ComposableInvalidationTrackTableTest {
  init {
    ComposableInvalidationTrackTableScope
      .putParamsIfAbsent(
        name = "name",
        ParameterInfo(
          name = "Tracie Travis",
          declarationStability = DeclarationStability.Runtime(""),
          value = "lorem",
          hashCode = 6733
        ),
      )
  }
}
