/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("unused", "TestFunctionName", "LocalVariableName")

package land.sungbin.composeinvestigator.compiler.test.source.tracker

import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun InTopLevel(param: Any) {
  Text("Hello, World!: $param")
}

@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun InTopLevelInline(param: Any) {
  Text("Hello, World!: $param")
}

class Class {
  @Composable
  fun InClass(param: Any) {
    Text("Hello, World!: $param")
  }
}

object Object {
  @Composable
  fun InObject(param: Any) {
    Text("Hello, World!: $param")
  }
}

fun Local() {
  @Composable
  fun InLocal(param: Any) {
    Text("Hello, World!: $param")
  }
}

fun Lambda(param: Any) {
  @Suppress("UNUSED_VARIABLE")
  val InLambda = @Composable {
    Text("Hello, World!: $param")
  }
}

@Composable
fun InParameter(content: @Composable (param: Any) -> Unit = { param -> Text("Hello, World!: $param") }) {
  content(Any())
}

class CompanionObject {
  companion object {
    @Composable
    fun InCompanionObject(param: Any) {
      Text("Hello, World!: $param")
    }
  }
}

fun AnonymousObject() {
  @Suppress("UNUSED_VARIABLE")
  val anonymousObject = object {
    @Composable
    fun InAnonymousObject(param: Any) {
      Text("Hello, World!: $param")
    }
  }
}
