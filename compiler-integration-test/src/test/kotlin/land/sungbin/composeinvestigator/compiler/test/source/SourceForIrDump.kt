/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("unused")

package land.sungbin.composeinvestigator.compiler.test.source

fun getString() = "1"
val hi = getString()

//val table1 = currentComposableInvalidationTracker
//
//fun table1() {
//  table1 shouldBeSameInstanceAs currentComposableInvalidationTracker
//}
//
//fun currentComposableName1() {
//  val prevComposableName = table1.currentComposableName
//  prevComposableName shouldBe "currentComposableName1"
//
//  table1.currentComposableName = "ChangedComposableName"
//  table1.currentComposableName shouldBe "ChangedComposableName"
//}