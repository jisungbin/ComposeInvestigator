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
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import land.sungbin.composeinvestigator.compiler.test.source.table.callback.Effects_InvalidationSkippedRoot
import land.sungbin.composeinvestigator.compiler.test.source.table.callback.RegisterListener_InvalidationSkippedRoot
import land.sungbin.composeinvestigator.compiler.test.source.table.callback.findInvalidationListensViaEffects
import land.sungbin.composeinvestigator.compiler.test.source.table.callback.findInvalidationListensViaManualRegister
import land.sungbin.composeinvestigator.runtime.ComposableInvalidationType
import land.sungbin.composeinvestigator.runtime.InvalidationReason
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InvalidationCallbackTest {
  @get:Rule
  val compose = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun invalidation_listens_via_register_listener() {
    compose.setContent { RegisterListener_InvalidationSkippedRoot() }
    compose.onNode(hasClickAction()).performClick()

    compose.runOnIdle {
      val rootListens = findInvalidationListensViaManualRegister("RegisterListener_InvalidationSkippedRoot")
      val childListens = findInvalidationListensViaManualRegister("RegisterListener_InvalidationSkippedChild")

      rootListens shouldHaveSize 2
      rootListens shouldBeSameSizeAs childListens

      rootListens shouldContainExactly listOf(
        ComposableInvalidationType.Processed(InvalidationReason.Invalidate),
        ComposableInvalidationType.Processed(InvalidationReason.Unknown()),
      )
      childListens shouldContainExactly listOf(
        ComposableInvalidationType.Processed(InvalidationReason.Invalidate),
        ComposableInvalidationType.Skipped,
      )
    }
  }

  @Test
  fun invalidation_listens_via_effects() {
    compose.setContent { Effects_InvalidationSkippedRoot() }
    compose.onNode(hasClickAction()).performClick()

    compose.runOnIdle {
      val rootListens = findInvalidationListensViaEffects("Effects_InvalidationSkippedRoot")
      val childListens = findInvalidationListensViaEffects("Effects_InvalidationSkippedChild")

      // The initial composition is not callbacked because the listener is registered after the initial composition (the first run of the composable).
      rootListens shouldHaveSize 1
      rootListens shouldBeSameSizeAs childListens

      rootListens shouldContainExactly listOf(ComposableInvalidationType.Processed(InvalidationReason.Unknown()))
      childListens shouldContainExactly listOf(ComposableInvalidationType.Skipped)
    }
  }
}
