/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("PackageDirectoryMismatch")

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeSameInstanceAs
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracker

val currentTable = currentComposableInvalidationTracker

fun assert(own: Class<*>) {
  val impl = own.declaredFields.find { field ->
    field.name == "ComposableInvalidationTrackTableImpl$${own.simpleName}"
  }?.apply { isAccessible = true }
  impl.shouldNotBeNull()

  val implValue = impl.get(own)
  currentTable shouldBeSameInstanceAs implValue
}
