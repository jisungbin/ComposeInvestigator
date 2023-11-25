/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.source.table.invalidationtracktablecall

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import land.sungbin.composeinvestigator.runtime.ComposableName
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracker
import land.sungbin.composeinvestigator.runtime.getValue

val table1 = currentComposableInvalidationTracker

fun table1() {
  table1 shouldBeSameInstanceAs currentComposableInvalidationTracker
}

fun currentComposableName1() {
  val prevComposableName by table1.currentComposableName
  prevComposableName shouldBe "currentComposableName1"

  table1.currentComposableName = ComposableName("ChangedComposableName1")
  table1.currentComposableName.name shouldBe "ChangedComposableName1"
}

fun currentComposableKeyName1() {
  table1.currentComposableKeyName shouldBe "fun-currentComposableKeyName1()Unit/pkg-land.sungbin.composeinvestigator.compiler.test.source.table.invalidationtracktablecall/file-TableCallFile1.kt"
}