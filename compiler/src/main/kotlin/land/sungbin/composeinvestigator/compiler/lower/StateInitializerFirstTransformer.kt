/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.lower

import land.sungbin.composeinvestigator.compiler.log
import land.sungbin.composeinvestigator.compiler.struct.IrInvalidationTraceTable
import land.sungbin.composeinvestigator.compiler.struct.IrInvalidationTraceTableHolder
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.getCompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.name.Name

internal class StateInitializerFirstTransformer(
  context: IrPluginContext,
  messageCollector: MessageCollector,
  tables: IrInvalidationTraceTableHolder,
) : ComposeInvestigatorBaseLower(context, messageCollector, tables) {
  override fun firstTransformStateInitializer(
    name: Name,
    initializer: IrExpression,
    table: IrInvalidationTraceTable,
  ): IrExpression {
    messageCollector.log(
      "Visit state initializer: ${name.asString()}",
      initializer.getCompilerMessageLocation(table.rawProp.file),
    )

    return table.irRegisterStateObject(initializer, irString(name.asString())).also {
      messageCollector.log(
        "Transform state initializer succeed: ${name.asString()}",
        initializer.getCompilerMessageLocation(table.rawProp.file),
      )
    }
  }
}
