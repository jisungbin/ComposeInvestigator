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
import assertk.assertions.containsOnly
import land.sungbin.composeinvestigator.compiler.test.source.logger.ComposableCallstackRoot_House
import land.sungbin.composeinvestigator.compiler.test.source.logger.findCallstacks
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComposableCallstackTest {
  @get:Rule
  val compose = createAndroidComposeRule<ComponentActivity>()

  @get:Rule
  val loggerTestRule = InvalidationLoggerTestRule()

  @Test fun static_callstacks() {
    compose.setContent { ComposableCallstackRoot_House() }
    compose.onNode(hasClickAction()).performClick()

    compose.runOnIdle {
      val house = findCallstacks("ComposableCallstackRoot_House")
      val door = findCallstacks("Door")
      val knock = findCallstacks("Knock")
      val window = findCallstacks("Window")
      val mirror = findCallstacks("Mirror")
      val spider = findCallstacks("Spider")

      assertThat(house.toSet()).containsOnly("")
      assertThat(door.toSet()).containsOnly("land.sungbin.composeinvestigator.compiler.test.source.logger.ComposableCallstackRoot_House -> Column\$content")
      assertThat(knock.toSet()).containsOnly("land.sungbin.composeinvestigator.compiler.test.source.logger.ComposableCallstackRoot_House -> Column\$content -> land.sungbin.composeinvestigator.compiler.test.source.logger.Door")
      assertThat(window.toSet()).containsOnly("land.sungbin.composeinvestigator.compiler.test.source.logger.ComposableCallstackRoot_House -> Column\$content")
      assertThat(mirror.toSet()).containsOnly("land.sungbin.composeinvestigator.compiler.test.source.logger.ComposableCallstackRoot_House -> Column\$content -> land.sungbin.composeinvestigator.compiler.test.source.logger.Window")
      assertThat(spider.toSet()).containsOnly("land.sungbin.composeinvestigator.compiler.test.source.logger.ComposableCallstackRoot_House -> Column\$content -> land.sungbin.composeinvestigator.compiler.test.source.logger.Window -> land.sungbin.composeinvestigator.compiler.test.source.logger.Mirror")
    }
  }
}
