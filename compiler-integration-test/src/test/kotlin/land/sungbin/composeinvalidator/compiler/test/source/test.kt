/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

@file:Suppress("unused")
@file:OptIn(ComposeInvalidatorCompilerApi::class)

package land.sungbin.composeinvalidator.compiler.test.source

import land.sungbin.composeinvalidator.runtime.ComposableInvalidationTrackTable
import land.sungbin.composeinvalidator.runtime.ComposeInvalidatorCompilerApi
import land.sungbin.composeinvalidator.runtime.DeclarationStability
import land.sungbin.composeinvalidator.runtime.ParameterInfo

fun entry(one: Int, two: String) {
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

private val ComposableInvalidationTrackTableScope = ComposableInvalidationTrackTable()
