/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.tracker.logger

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import land.sungbin.composeinvestigator.compiler.test.source.logger.NestedLocalStateCapture
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

//@RunWith(AndroidJUnit4::class)
//class IrBugCheckTest {
//  @get:Rule
//  val compose = createAndroidComposeRule<ComponentActivity>()
//
//  @get:Rule
//  val loggerTest = InvalidationLoggerTestRule()
//
//  @Test
//  fun nested_state() {
//    compose.setContent { NestedLocalStateCapture() }
//  }
//}
