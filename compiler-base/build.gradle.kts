plugins {
  kotlin("jvm")
  id(libs.plugins.gradle.publish.maven.get().pluginId)
}

sourceSets {
  getByName("main").java.srcDir("src/main/kotlin")
}

kotlin {
  explicitApi()
}

dependencies {
  implementation(libs.kotlin.compiler.embedded)
  implementation(libs.fastlist)
}
