/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.internal.tracker

import land.sungbin.composeinvestigator.compiler.internal.COMPOSABLE_FQN
import land.sungbin.composeinvestigator.compiler.internal.COMPOSER_FQN
import land.sungbin.composeinvestigator.compiler.internal.HASH_CODE_FQN
import land.sungbin.composeinvestigator.compiler.internal.SKIP_TO_GROUP_END
import land.sungbin.composeinvestigator.compiler.internal.STATE_FQN
import land.sungbin.composeinvestigator.compiler.internal.UNKNOWN_STRING
import land.sungbin.composeinvestigator.compiler.internal.fromFqName
import land.sungbin.composeinvestigator.compiler.internal.origin.InvalidationTrackerOrigin
import land.sungbin.composeinvestigator.compiler.internal.tracker.table.InvalidationTrackTableCallTransformer
import land.sungbin.composeinvestigator.compiler.internal.tracker.table.IrInvalidationTrackTable
import land.sungbin.composeinvestigator.compiler.util.VerboseLogger
import land.sungbin.fastlist.fastLastOrNull
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.wasm.ir2wasm.getSourceLocation
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrLocalDelegatedProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrSymbolOwner
import org.jetbrains.kotlin.ir.declarations.IrValueDeclaration
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrElseBranch
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrStatementContainer
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.isInt
import org.jetbrains.kotlin.ir.types.isNullableAny
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.types.starProjectedType
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.setDeclarationsParent
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.utils.addToStdlib.cast
import org.jetbrains.kotlin.utils.addToStdlib.safeAs
import org.jetbrains.kotlin.wasm.ir.source.location.SourceLocation

internal abstract class AbstractInvalidationTrackingLower(
  private val context: IrPluginContext,
  private val logger: VerboseLogger,
) : IrElementTransformerVoidWithContext() {
  private class IrSymbolOwnerWithData<D>(private val owner: IrSymbolOwner, val data: D) : IrSymbolOwner by owner

  private val stateSymbol = context.referenceClass(ClassId.topLevel(STATE_FQN))!!
  private val composerSymbol = context.referenceClass(ClassId.topLevel(COMPOSER_FQN))!!
  private val hashCodeSymbol =
    context
      .referenceFunctions(CallableId.fromFqName(HASH_CODE_FQN))
      .single { symbol ->
        val extensionReceiver = symbol.owner.extensionReceiverParameter

        val isValidExtensionReceiver = extensionReceiver != null && extensionReceiver.type.isNullableAny()
        val isValidReturnType = symbol.owner.returnType.isInt()

        isValidExtensionReceiver && isValidReturnType
      }

  private val unsafeCurrentFunction: IrSimpleFunction
    get() = allScopes
      .fastLastOrNull { scope -> scope.irElement is IrSimpleFunction }
      ?.irElement?.cast() ?: error("Cannot find current function")

  protected fun getCurrentFunctionPackage() = unsafeCurrentFunction.kotlinFqName.asString()

  protected fun getCurrentFunctionNameIntercepttedAnonymous(userProvideName: String?): String {
    if (userProvideName != null) return userProvideName
    val currentFunctionName = unsafeCurrentFunction.name
    return if (currentFunctionName == SpecialNames.ANONYMOUS) {
      try {
        val parent = currentFunction!!.irElement.cast<IrSimpleFunction>().parent
        "${SpecialNames.ANONYMOUS_STRING} in ${parent.kotlinFqName.asString()}"
      } catch (_: Exception) {
        SpecialNames.ANONYMOUS_STRING
      }
    } else currentFunctionName.asString()
  }

  protected val currentInvalidationTrackTable: IrInvalidationTrackTable?
    get() =
      allScopes
        .fastLastOrNull { scope ->
          val element = scope.irElement
          element is IrSymbolOwnerWithData<*> && element.data is IrInvalidationTrackTable
        }
        ?.irElement?.cast<IrSymbolOwnerWithData<IrInvalidationTrackTable>>()?.data

  final override fun visitFileNew(declaration: IrFile): IrFile {
    val trackTable = IrInvalidationTrackTable.create(context, declaration)
    declaration.declarations.add(0, trackTable.prop.also { prop -> prop.setDeclarationsParent(declaration) })
    declaration.transformChildrenVoid(InvalidationTrackTableCallTransformer(context = context, table = trackTable, logger = logger))
    return withinScope(IrSymbolOwnerWithData(declaration, trackTable)) {
      super.visitFileNew(declaration)
    }
  }

  // State properties visitor
  final override fun visitBlock(expression: IrBlock): IrExpression {
    fun IrType.isState() = classOrNull?.isSubtypeOfClass(stateSymbol.starProjectedType.classOrFail) ?: false

    expression.transformChildrenVoid(
      object : IrElementTransformerVoidWithContext() {
        // val state = remember { mutableStateOf(T) }
        override fun visitVariable(declaration: IrVariable): IrStatement {
          if (declaration.origin == IrDeclarationOrigin.PROPERTY_DELEGATE) return super.visitVariable(declaration)
          if (declaration.type.isState()) visitStateProperty(declaration)
          return super.visitVariable(declaration)
        }

        // var state by remember { mutableStateOf(T) }
        override fun visitLocalDelegatedProperty(declaration: IrLocalDelegatedProperty): IrStatement {
          if (declaration.delegate.type.isState()) visitStateProperty(declaration.delegate)
          return super.visitLocalDelegatedProperty(declaration)
        }
      },
    )

    return super.visitBlock(expression)
  }

  // tmp0_safe_receiver.updateScope(block = local fun <anonymous>($composer: Composer?, $force: Int) {
  //   <ENTER HERE>
  //   return Composable($composer = $composer, $changed = updateChangedFlags(flags = $changed.or(other = 1)))
  // })
  final override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
    fun List<IrValueParameter>.isUpdateScopeLambdaParameters(): Boolean {
      if (size != 2) return false
      val (composer, force) = this
      if (composer.type != composerSymbol.defaultType.makeNullable()) return false
      if (!force.type.isInt()) return false
      return true
    }

    if (
      declaration.origin == IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA &&
      declaration.name == SpecialNames.ANONYMOUS &&
      declaration.visibility == DescriptorVisibilities.LOCAL &&
      declaration.returnType.isUnit() &&
      declaration.valueParameters.isUpdateScopeLambdaParameters()
    ) {
      declaration.body!!.transformChildrenVoid(
        object : IrElementTransformerVoidWithContext() {
          override fun visitBlockBody(body: IrBlockBody): IrBody {
            val returnCall = body.statements.singleOrNull()?.safeAs<IrReturn>() ?: return super.visitBlockBody(body)
            val transform = transformUpdateScopeBlock(declaration, returnCall)
            body.statements.clear()
            body.statements.addAll(transform.statements)
            return super.visitBlockBody(body)
          }
        },
      )
    } else {
      if (!declaration.hasAnnotation(COMPOSABLE_FQN)) return super.visitSimpleFunction(declaration)
      withinScope(declaration) { declaration.body?.transformChildrenVoid() }
    }

    return super.visitSimpleFunction(declaration)
  }

  // <WHEN> when {
  //   ...
  //   else -> <CALL> $composer.skipToGroupEnd() <ENTER HERE>
  // }
  final override fun visitElseBranch(branch: IrElseBranch): IrElseBranch {
    val call = branch.result as? IrCall ?: return super.visitElseBranch(branch)

    val callName = call.symbol.owner.name
    val callParentFqn = call.symbol.owner.parent.kotlinFqName

    // SKIP_TO_GROUP_END is declared in 'androidx.compose.runtime.Composer'
    if (callName != SKIP_TO_GROUP_END || callParentFqn != COMPOSER_FQN) return super.visitElseBranch(branch)

    val transform = transformSkipToGroupEndCall(unsafeCurrentFunction, call)
    branch.result = IrBlockImpl(
      startOffset = transform.startOffset,
      endOffset = transform.endOffset,
      type = context.irBuiltIns.unitType,
      origin = InvalidationTrackerOrigin,
      statements = transform.statements,
    )

    return super.visitElseBranch(branch)
  }

  protected fun IrFunction.getSafelyLocation(): SourceLocation.Location =
    getSourceLocation(currentFile.fileEntry).let { location ->
      if (location is SourceLocation.Location) location.copy(line = location.line + 1) // Humans read from 1.
      else SourceLocation.Location(file = SpecialNames.UNKNOWN_STRING, line = UNDEFINED_OFFSET, column = UNDEFINED_OFFSET)
    }

  protected abstract fun visitStateProperty(property: IrVariable)
  protected abstract fun transformUpdateScopeBlock(function: IrSimpleFunction, statement: IrStatement): IrStatementContainer
  protected abstract fun transformSkipToGroupEndCall(function: IrSimpleFunction, expression: IrExpression): IrStatementContainer

  protected fun irGetValue(value: IrValueDeclaration): IrGetValue =
    IrGetValueImpl(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = value.symbol,
    )

  protected fun irHashCode(value: IrExpression): IrCall =
    IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = hashCodeSymbol,
    ).apply {
      extensionReceiver = value
    }

  protected fun irToString(value: IrExpression): IrCall =
    IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = context.irBuiltIns.extensionToString,
    ).apply {
      extensionReceiver = value
    }

  protected fun irTmpVariableInCurrentFun(expression: IrExpression, nameHint: String? = null): IrVariable =
    currentFunction!!.scope.createTemporaryVariable(irExpression = expression, nameHint = nameHint)
}
