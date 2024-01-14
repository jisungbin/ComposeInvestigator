// COPIED FROM https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/runtime/runtime/src/nonEmulatorCommonTest/kotlin/androidx/compose/runtime/mock/MockViewValidator.kt;drc=4d53400eca9f3ac90c3a3f6cffcbc5bf492ec536.

/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("TestFunctionName")

package land.sungbin.composeinvestigator.runtime.mock

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

interface MockViewValidator {
  val view: View
  fun next(): Boolean
}

class MockViewListValidator(private val views: List<View>) :
  MockViewValidator {
  override lateinit var view: View

  override fun next(): Boolean {
    if (iterator.hasNext()) {
      view = iterator.next()
      return true
    }
    return false
  }

  private val iterator by lazy { views.iterator() }

  fun validate(block: (MockViewValidator.() -> Unit)?) {
    if (block != null) {
      this.block()
      val hasNext = next()
      assertEquals(false, hasNext, "Expected children but none found")
    } else {
      assertEquals(0, views.size, "Not expecting children but some found")
    }
  }

  inline fun inlineValidate(block: MockViewListValidator.() -> Unit) {
    this.block()
    val hasNext = next()
    assertEquals(false, hasNext, "Expected children but none found")
  }
}

fun MockViewValidator.view(name: String, block: (MockViewValidator.() -> Unit)? = null) {
  val hasNext = next()
  assertTrue(hasNext, "Expected a $name, but none found")
  assertEquals(name, view.name)
  MockViewListValidator(view.children).validate(block)
}

inline fun MockViewValidator.inlineView(name: String, block: MockViewValidator.() -> Unit) {
  val hasNext = next()
  assertTrue(hasNext, "Expected a $name, but none found")
  assertEquals(name, view.name)
  MockViewListValidator(view.children).inlineValidate(block)
}

fun <T> MockViewValidator.Repeated(of: Iterable<T>, block: MockViewValidator.(value: T) -> Unit) {
  for (value in of) {
    block(value)
  }
}

fun MockViewValidator.Linear() = view("linear", null)
fun MockViewValidator.Linear(block: MockViewValidator.() -> Unit) = view("linear", block)

inline fun MockViewValidator.InlineLinear(block: MockViewValidator.() -> Unit) =
  inlineView("linear", block)

fun MockViewValidator.box(block: MockViewValidator.() -> Unit) = view("box", block)

fun MockViewValidator.Text(value: String) {
  view("text")
  assertEquals(value, view.attributes["text"])
}

fun MockViewValidator.Edit(value: String) {
  view("edit")
  assertEquals(value, view.attributes["value"])
}

fun MockViewValidator.SelectBox(selected: Boolean, block: MockViewValidator.() -> Unit) {
  if (selected) {
    box {
      block()
    }
  } else {
    block()
  }
}

fun MockViewValidator.skip(times: Int = 1) {
  repeat(times) {
    val hasNext = next()
    assertEquals(true, hasNext)
  }
}
