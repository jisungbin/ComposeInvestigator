plugins {
  kotlin("jvm")
  alias(libs.plugins.gradle.publish.maven)
}

sourceSets {
  getByName("main").java.srcDir("src/main/kotlin")
  getByName("test").java.srcDir("src/main/kotlin")
}

kotlin {
  explicitApi()
  compilerOptions {
    optIn.add("land.sungbin.composeinvestigator.runtime.ComposeInvestigatorCompilerApi")
  }
}

dependencies {
  implementation(libs.jetbrains.annotation)
  testImplementation(libs.test.kotest)
}
