/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.logger

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isDataClassEqualTo
import land.sungbin.composeinvestigator.compiler.test.source.logger.InvalidationProcessedRoot
import land.sungbin.composeinvestigator.compiler.test.source.logger.InvalidationSkippedRoot
import land.sungbin.composeinvestigator.compiler.test.source.logger.InvalidationSkippedRoot_CustomName
import land.sungbin.composeinvestigator.compiler.test.source.logger.findInvalidationLog
import land.sungbin.composeinvestigator.compiler.test.source.logger.invalidationLog
import land.sungbin.composeinvestigator.runtime.ComposableInvalidationType
import land.sungbin.composeinvestigator.runtime.FieldChanged
import land.sungbin.composeinvestigator.runtime.InvalidationReason
import land.sungbin.composeinvestigator.runtime.Stability
import land.sungbin.composeinvestigator.runtime.affect.AffectedComposable
import land.sungbin.composeinvestigator.runtime.affect.AffectedField
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InvalidationLoggerTest {
  @get:Rule
  val compose = createAndroidComposeRule<ComponentActivity>()

  @get:Rule
  val loggerTestRule = InvalidationLoggerTestRule()

  @Test fun affected_composable() {
    compose.setContent { InvalidationSkippedRoot() }

    compose.runOnIdle {
      val affectedRoot = invalidationLog.keys.first { affected -> affected.name == "InvalidationSkippedRoot" }
      val affectedChild = invalidationLog.keys.first { affected -> affected.name == "InvalidationSkippedChild" }

      assertThat(affectedRoot).isDataClassEqualTo(
        AffectedComposable(
          name = "InvalidationSkippedRoot",
          pkg = "land.sungbin.composeinvestigator.compiler.test.source.logger",
          filename = affectedRoot.filename,
        ),
      )
      assertThat(affectedChild).isDataClassEqualTo(
        AffectedComposable(
          name = "InvalidationSkippedChild",
          pkg = "land.sungbin.composeinvestigator.compiler.test.source.logger",
          filename = affectedChild.filename,
        ),
      )
    }
  }

  @Test fun invalidation_skipped() {
    compose.setContent { InvalidationSkippedRoot() }
    compose.onNode(hasClickAction()).performClick()

    compose.runOnIdle {
      val rootLogs = findInvalidationLog("InvalidationSkippedRoot")
      val childLogs = findInvalidationLog("InvalidationSkippedChild")

      assertThat(rootLogs).containsExactly(
        listOf(
          ComposableInvalidationType.Processed(InvalidationReason.Initial),
          ComposableInvalidationType.Processed(InvalidationReason.Invalidate),
          ComposableInvalidationType.Processed(InvalidationReason.Unknown(params = emptyList())),
        ),
      )
      assertThat(childLogs).containsExactly(
        listOf(
          ComposableInvalidationType.Processed(InvalidationReason.Initial),
          ComposableInvalidationType.Skipped,
        ),
      )
    }
  }

  @Test fun invalidation_skipped_custom_name() {
    compose.setContent { InvalidationSkippedRoot_CustomName() }
    compose.onNode(hasClickAction()).performClick()

    compose.runOnIdle {
      val rootLogs = findInvalidationLog("InvalidationSkippedRoot_custom_name")
      val childLogs = findInvalidationLog("InvalidationSkippedChild_custom_name")

      assertThat(rootLogs).containsExactly(
        listOf(
          ComposableInvalidationType.Processed(InvalidationReason.Initial),
          ComposableInvalidationType.Processed(InvalidationReason.Invalidate),
          ComposableInvalidationType.Processed(InvalidationReason.Unknown(params = emptyList())),
        ),
      )
      assertThat(childLogs).containsExactly(
        listOf(
          ComposableInvalidationType.Processed(InvalidationReason.Initial),
          ComposableInvalidationType.Skipped,
        ),
      )
    }
  }

  @Test fun invalidation_processed() {
    compose.setContent { InvalidationProcessedRoot() }
    repeat(2) { compose.onNode(hasClickAction()).performClick() }

    compose.runOnIdle {
      val rootLogs = findInvalidationLog("InvalidationProcessedRoot")
      val childLogs = findInvalidationLog("InvalidationProcessedChild")

      assertThat(rootLogs).containsExactly(
        listOf(
          ComposableInvalidationType.Processed(InvalidationReason.Initial),
          ComposableInvalidationType.Processed(InvalidationReason.Invalidate),
          ComposableInvalidationType.Processed(InvalidationReason.Unknown(params = emptyList())),
          ComposableInvalidationType.Processed(InvalidationReason.Invalidate),
          ComposableInvalidationType.Processed(InvalidationReason.Unknown(params = emptyList())),
        ),
      )

      assertThat(childLogs).containsExactly(
        listOf(
          ComposableInvalidationType.Processed(InvalidationReason.Initial),
          ComposableInvalidationType.Processed(
            InvalidationReason.FieldChanged(
              changed = listOf(
                FieldChanged(
                  old = AffectedField.ValueParameter(
                    name = "count",
                    typeName = "kotlin.Int",
                    valueString = "0",
                    valueHashCode = 0,
                    stability = Stability.Stable,
                  ),
                  new = AffectedField.ValueParameter(
                    name = "count",
                    typeName = "kotlin.Int",
                    valueString = "1",
                    valueHashCode = 1,
                    stability = Stability.Stable,
                  ),
                ),
              ),
            ),
          ),
          ComposableInvalidationType.Processed(
            InvalidationReason.FieldChanged(
              changed = listOf(
                FieldChanged(
                  old = AffectedField.ValueParameter(
                    name = "count",
                    typeName = "kotlin.Int",
                    valueString = "1",
                    valueHashCode = 1,
                    stability = Stability.Stable,
                  ),
                  new = AffectedField.ValueParameter(
                    name = "count",
                    typeName = "kotlin.Int",
                    valueString = "2",
                    valueHashCode = 2,
                    stability = Stability.Stable,
                  ),
                ),
              ),
            ),
          ),
        ),
      )
    }
  }
}
