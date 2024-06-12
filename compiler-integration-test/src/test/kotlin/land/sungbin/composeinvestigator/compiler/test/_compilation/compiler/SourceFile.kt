package land.sungbin.composeinvestigator.compiler.test._compilation.compiler

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtilRt
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.PsiFileFactoryImpl
import com.intellij.testFramework.LightVirtualFile
import java.nio.charset.StandardCharsets
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.AnalyzingUtils

data class SourceFile(
  val name: String,
  val source: String,
  private val ignoreParseErrors: Boolean = false,
  val path: String = "",
) {
  fun toKtFile(project: Project): KtFile {
    val shortName = name.substring(name.lastIndexOf('/') + 1).let {
      it.substring(it.lastIndexOf('\\') + 1)
    }

    val virtualFile = object : LightVirtualFile(
      /* name = */ shortName,
      /* language = */ KotlinLanguage.INSTANCE,
      /* text = */ StringUtilRt.convertLineSeparators(source)
    ) {
      override fun getPath() = "${this@SourceFile.path}/$name"
    }.apply {
      charset = StandardCharsets.UTF_8
    }

    val factory = PsiFileFactory.getInstance(project) as PsiFileFactoryImpl
    val ktFile = factory.trySetupPsiForFile(
      /* lightVirtualFile = */ virtualFile,
      /* language = */ KotlinLanguage.INSTANCE,
      /* physical = */ true,
      /* markAsCopy = */ false,
    ) as KtFile

    if (!ignoreParseErrors) AnalyzingUtils.checkForSyntacticErrors(ktFile)
    return ktFile
  }
}