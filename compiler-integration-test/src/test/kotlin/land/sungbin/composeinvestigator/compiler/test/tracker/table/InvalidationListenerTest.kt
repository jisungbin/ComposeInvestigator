/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.tracker.table

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.matchers.collections.shouldBeSameSizeAs
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import land.sungbin.composeinvestigator.compiler.test.source.table.listener.RegisterListener_InvalidationSkippedRoot
import land.sungbin.composeinvestigator.compiler.test.source.table.listener.findInvalidationListens
import land.sungbin.composeinvestigator.runtime.ComposableInvalidationType
import land.sungbin.composeinvestigator.runtime.InvalidationReason
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InvalidationListenerTest {
  @get:Rule
  val compose = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun invalidation_listens_via_registerListener() {
    compose.setContent { RegisterListener_InvalidationSkippedRoot() }
    compose.onNode(hasClickAction()).performClick()

    compose.runOnIdle {
      val rootListens = findInvalidationListens("RegisterListener_InvalidationSkippedRoot")
      val childListens = findInvalidationListens("RegisterListener_InvalidationSkippedChild")

      rootListens shouldHaveSize 1
      rootListens shouldBeSameSizeAs childListens

      // The initial composition is not callbacked because the listener is registered after the initial composition (the first run of the composable).
      rootListens[0] shouldBe ComposableInvalidationType.Processed(InvalidationReason.Unknown(params = emptyList()))
      childListens[0] shouldBe ComposableInvalidationType.Skipped
    }
  }
}