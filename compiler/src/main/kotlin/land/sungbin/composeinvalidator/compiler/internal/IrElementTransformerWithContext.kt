/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

package land.sungbin.composeinvalidator.compiler.internal

import org.jetbrains.kotlin.backend.common.ScopeWithIr
import org.jetbrains.kotlin.backend.common.peek
import org.jetbrains.kotlin.backend.common.pop
import org.jetbrains.kotlin.backend.common.push
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.Scope
import org.jetbrains.kotlin.ir.declarations.IrAnonymousInitializer
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrScript
import org.jetbrains.kotlin.ir.declarations.IrSymbolOwner
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.symbols.IrScriptSymbol
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer

internal abstract class IrElementTransformerWithContext<in D> : IrElementTransformer<D> {
  private val scopeStack = mutableListOf<ScopeWithIr>()

  protected open fun createScope(declaration: IrSymbolOwner): ScopeWithIr =
    ScopeWithIr(Scope(declaration.symbol), declaration)

  protected fun unsafeEnterScope(declaration: IrSymbolOwner) {
    scopeStack.push(createScope(declaration))
  }

  protected fun unsafeLeaveScope() {
    scopeStack.pop()
  }

  protected inline fun <T> withinScope(declaration: IrSymbolOwner, fn: () -> T): T {
    unsafeEnterScope(declaration)
    val result = fn()
    unsafeLeaveScope()
    return result
  }

  final override fun visitFile(declaration: IrFile, data: D): IrFile {
    scopeStack.push(createScope(declaration))
    val result = visitFileNew(declaration, data)
    scopeStack.pop()
    return result
  }

  final override fun visitClass(declaration: IrClass, data: D): IrStatement {
    scopeStack.push(createScope(declaration))
    val result = visitClassNew(declaration, data)
    scopeStack.pop()
    return result
  }

  final override fun visitProperty(declaration: IrProperty, data: D): IrStatement {
    scopeStack.push(createScope(declaration))
    val result = visitPropertyNew(declaration, data)
    scopeStack.pop()
    return result
  }

  final override fun visitField(declaration: IrField, data: D): IrStatement {
    scopeStack.push(createScope(declaration))
    val result = visitFieldNew(declaration, data)
    scopeStack.pop()
    return result
  }

  final override fun visitFunction(declaration: IrFunction, data: D): IrStatement {
    scopeStack.push(createScope(declaration))
    val result = visitFunctionNew(declaration, data)
    scopeStack.pop()
    return result
  }

  final override fun visitAnonymousInitializer(declaration: IrAnonymousInitializer, data: D): IrStatement {
    scopeStack.push(createScope(declaration))
    val result = visitAnonymousInitializerNew(declaration, data)
    scopeStack.pop()
    return result
  }

  final override fun visitValueParameter(declaration: IrValueParameter, data: D): IrStatement {
    scopeStack.push(createScope(declaration))
    val result = visitValueParameterNew(declaration, data)
    scopeStack.pop()
    return result
  }

  final override fun visitScript(declaration: IrScript, data: D): IrStatement {
    scopeStack.push(createScope(declaration))
    val result = visitScriptNew(declaration, data)
    scopeStack.pop()
    return result
  }

  protected val currentFile get() = scopeStack.lastOrNull { it.irElement is IrFile }!!.irElement as IrFile
  protected val currentScript get() = scopeStack.lastOrNull { it.scope.scopeOwnerSymbol is IrScriptSymbol }
  protected val currentClass get() = scopeStack.lastOrNull { it.scope.scopeOwnerSymbol is IrClassSymbol }
  protected val currentFunction get() = scopeStack.lastOrNull { it.scope.scopeOwnerSymbol is IrFunctionSymbol }
  protected val currentProperty get() = scopeStack.lastOrNull { it.scope.scopeOwnerSymbol is IrPropertySymbol }
  protected val currentAnonymousInitializer get() = scopeStack.lastOrNull { it.scope.scopeOwnerSymbol is IrAnonymousInitializer }
  protected val currentValueParameter get() = scopeStack.lastOrNull { it.scope.scopeOwnerSymbol is IrValueParameter }
  protected val currentScope get() = scopeStack.peek()
  protected val parentScope get() = if (scopeStack.size < 2) null else scopeStack[scopeStack.size - 2]
  protected val allScopes get() = scopeStack
  protected val currentDeclarationParent get() = scopeStack.lastOrNull { it.irElement is IrDeclarationParent }?.irElement as? IrDeclarationParent

  open fun visitFileNew(declaration: IrFile, data: D): IrFile {
    return super.visitFile(declaration, data)
  }

  open fun visitClassNew(declaration: IrClass, data: D): IrStatement {
    return super.visitClass(declaration, data)
  }

  open fun visitFunctionNew(declaration: IrFunction, data: D): IrStatement {
    return super.visitFunction(declaration, data)
  }

  open fun visitPropertyNew(declaration: IrProperty, data: D): IrStatement {
    return super.visitProperty(declaration, data)
  }

  open fun visitFieldNew(declaration: IrField, data: D): IrStatement {
    return super.visitField(declaration, data)
  }

  open fun visitAnonymousInitializerNew(declaration: IrAnonymousInitializer, data: D): IrStatement {
    return super.visitAnonymousInitializer(declaration, data)
  }

  open fun visitValueParameterNew(declaration: IrValueParameter, data: D): IrStatement {
    return super.visitValueParameter(declaration, data)
  }

  open fun visitScriptNew(declaration: IrScript, data: D): IrStatement {
    return super.visitScript(declaration, data)
  }
}
