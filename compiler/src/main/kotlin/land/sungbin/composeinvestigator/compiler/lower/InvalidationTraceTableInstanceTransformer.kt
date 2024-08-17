/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.lower

import land.sungbin.composeinvestigator.compiler.COMPOSABLE_INVALIDATION_TRACE_TABLE_FQN
import land.sungbin.composeinvestigator.compiler.NO_INVESTIGATION_FQN
import land.sungbin.composeinvestigator.compiler.log
import land.sungbin.composeinvestigator.compiler.struct.IrInvalidationTraceTable
import land.sungbin.composeinvestigator.compiler.struct.IrInvalidationTraceTableHolder
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.jvm.ir.getKtFile
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.codegen.CompilationException
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.findDeclaration
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.setDeclarationsParent
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

public class InvalidationTraceTableInstanceTransformer(
  private val context: IrPluginContext,
  private val messageCollector: MessageCollector, // TODO context.createDiagnosticReporter()
) : IrElementTransformerVoid(), IrInvalidationTraceTableHolder {
  private val tables = mutableMapOf<IrFile, IrInvalidationTraceTable>()

  override fun tableByFile(file: IrFile): IrInvalidationTraceTable =
    tables[file] ?: throw CompilationException(
      "No table for ${file.name}",
      /* cause = */ null,
      /* element = */ file.getKtFile(),
    )

  // TODO don't instantiate the table if the current file doesn't contain any Composables.
  override fun visitFile(declaration: IrFile): IrFile =
    includeFileIRInExceptionTrace(declaration) {
      if (declaration.hasAnnotation(NO_INVESTIGATION_FQN)) return declaration

      val existsTable = declaration.findDeclaration<IrProperty> { property ->
        property.backingField?.type?.classFqName == COMPOSABLE_INVALIDATION_TRACE_TABLE_FQN
      }
      val table = existsTable?.let(IrInvalidationTraceTable::from) ?: run {
        IrInvalidationTraceTable.create(context, declaration).also { table ->
          declaration.declarations.add(0, table.rawProp.also { prop -> prop.setDeclarationsParent(declaration) })
        }
      }

      tables[declaration] = table
      messageCollector.log("Instantiated InvalidationTraceTable for ${declaration.name}")

      super.visitFile(declaration)
    }
}
