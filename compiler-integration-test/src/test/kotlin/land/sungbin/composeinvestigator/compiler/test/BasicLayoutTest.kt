package land.sungbin.composeinvestigator.compiler.test

import androidx.compose.runtime.mock.compositionTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class BasicLayoutTest {
  @BeforeTest fun prepare() {
    TestConfiguration.reset()
  }

  @Test fun basic(): Unit = compositionTest {
    compose { BasicLayout() }
  }
}
