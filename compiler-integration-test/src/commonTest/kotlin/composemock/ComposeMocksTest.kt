// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package composemock

import androidx.compose.runtime.mutableStateOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest

// Copyright 2020-2023 JetBrains s.r.o. and respective authors and developers.
// https://github.com/JetBrains/compose-multiplatform/blob/1b2669f59d8f9a5cda1a26388a51e8c6793981c8/compose/integrations/composable-test-cases/common/src/commonTest/kotlin/com.example.common/Test.kt

// Modified by Ji Sungbin: code style changes

class Test {
  @Test fun testEmptyPlainTextNode() {
    val root = runCompose {}
    assertEquals("root:{}", root.toString())
  }

  @Test fun testPlainTextNode() {
    val root = runCompose { Text("Hello World!") }
    assertEquals("root:{Hello World!}", root.toString())
  }

  @Test fun testTextContainerNodeEmpty() {
    val root = runCompose { Container("abc") {} }
    assertEquals("root:{abc:{}}", root.toString())
  }

  @Test fun testTextContainerNode() {
    val root = runCompose {
      Container("abc") {
        Text("Hello World!")
      }
    }
    assertEquals("root:{abc:{Hello World!}}", root.toString())
  }

  @Test fun testRecomposition() = runTest {
    val index = mutableStateOf(1)

    val job = Job()
    val root = runCompose(coroutineContext + job) {
      Container("abc${index.value}") {
        Text("Hello World!")
      }
    }

    assertEquals("root:{abc1:{Hello World!}}", root.toString())

    index.value = 2
    testScheduler.advanceUntilIdle()
    assertEquals("root:{abc2:{Hello World!}}", root.toString())

    index.value = 3
    testScheduler.advanceUntilIdle()
    assertEquals("root:{abc3:{Hello World!}}", root.toString())

    job.cancel()
  }
}
