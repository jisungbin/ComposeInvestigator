/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test

import assertk.Assert
import assertk.assertions.hasClass

inline fun <reified T : Any> Assert<Any>.cast() = transform { given ->
  hasClass<T>()
  given as T
}
