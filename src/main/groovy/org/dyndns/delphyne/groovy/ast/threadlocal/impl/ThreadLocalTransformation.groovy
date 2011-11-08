package org.dyndns.delphyne.groovy.ast.threadlocal.impl

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.GenericsType
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.VariableScope
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.SyntaxException
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

/**
 * Provides the transformation for the {@link ThreadLocal} annotation.

 * @author Brian M. Carr <delphyne@gmail.com>
 */
@GroovyASTTransformation(phase=CompilePhase.INSTRUCTION_SELECTION)
class ThreadLocalTransformation extends AbstractASTTransformation {

    private AstBuilder builder = new AstBuilder()

    void visit(ASTNode[] nodes, SourceUnit source) {
        if (!nodes) {
            return
        }

        if (!(nodes[0] instanceof AnnotationNode)) {
            source.addError(
                    new SyntaxException(
                            "Expected AnnotationNode but got ${nodes[0].class.simpleName}",
                            nodes[0]?.lineNumber,
                            nodes[0]?.columnNumber))
        }

        if (!(nodes[1] instanceof FieldNode)) {
            source.addError(new SyntaxException(
                    "Expected FieldNode but got ${nodes[1].class.simpleName}",
                    nodes[1]?.lineNumber,
                    nodes[1]?.columnNumber))
        }

        FieldNode originalField = (FieldNode)nodes[1]

        if (originalField.type.typeClass.isPrimitive()) {
            source.addError(
                    new SyntaxException(
                            '@ThreadLocal annotated properties cannot be primitives.',
                            originalField.lineNumber,
                            originalField.columnNumber))
        }

        ClassNode declaringClass = (ClassNode)originalField.declaringClass

        declaringClass.removeField(originalField.name)
        declaringClass.addField(createThreadLocalFieldNode(originalField, declaringClass))
        declaringClass.addMethod(createGetter(originalField))
        declaringClass.addMethod(createSetter(originalField))
        declaringClass.addMethod(createRemove(originalField))
    }

    FieldNode createThreadLocalFieldNode(FieldNode originalField, ClassNode declaringClass) {
        Expression initialExpression
        if (originalField.initialExpression) {
            def closure = new ClosureExpression(
                    Parameter.EMPTY_ARRAY,
                    new BlockStatement(
                        [new ReturnStatement(
                                originalField.initialExpression
                            )], new VariableScope()
                        )
                )
            
            closure.variableScope = new VariableScope()

            initialExpression = new ConstructorCallExpression(
                    ClassHelper.make(InitialClosureThreadLocal),
                    new ArgumentListExpression(closure)
                )    
        } else {
            initialExpression = builder.buildFromSpec {
                expression {
                    constructorCall(ThreadLocal) { argumentList() }
                }
            }[0].expression
        }

        ClassNode threadLocalWithGenerics = ClassHelper.make(ThreadLocal).plainNodeReference
        threadLocalWithGenerics.genericsTypes = [
            new GenericsType(originalField.type)
        ] as GenericsType[]

        new FieldNode(
                "_tl_${originalField.name}",
                ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC | ACC_FINAL,
                threadLocalWithGenerics,
                declaringClass,
                initialExpression
            )
    }

    MethodNode createGetter(FieldNode originalField) {
        Statement getterStatement = builder.buildFromSpec {
            expression {
                methodCall {
                    variable "_tl_${originalField.name}"
                    constant 'get'
                    argumentList()
                }
            }
        }[0]

        new MethodNode(
        "get${originalField.name.capitalize()}",
        ACC_PUBLIC | ACC_SYNTHETIC,
        originalField.type,
        Parameter.EMPTY_ARRAY,
        ClassHelper.EMPTY_TYPE_ARRAY,
        getterStatement
        )
    }

    MethodNode createSetter(FieldNode originalField) {
        Statement setterStatement = builder.buildFromSpec {
            expression {
                methodCall {
                    variable "_tl_${originalField.name}"
                    constant 'set'
                    argumentList { variable originalField.name }
                }
            }
        }[0]

        new MethodNode(
                "set${originalField.name.capitalize()}",
                ACC_PUBLIC | ACC_SYNTHETIC | ACC_FINAL,
                ClassHelper.VOID_TYPE,
                [new Parameter(originalField.type, originalField.name)] as Parameter[],
                ClassHelper.EMPTY_TYPE_ARRAY,
                setterStatement)
    }
    
    MethodNode createRemove(FieldNode originalField) {
        new MethodNode(
                "remove${originalField.name.capitalize()}",
                ACC_PUBLIC | ACC_SYNTHETIC | ACC_FINAL,
                ClassHelper.VOID_TYPE,
                Parameter.EMPTY_ARRAY,
                ClassHelper.EMPTY_TYPE_ARRAY,
                builder.buildFromSpec {
                    expression {
                        methodCall {
                            variable "_tl_${originalField.name}"
                            constant 'remove'
                            argumentList()
                        }
                    }
                }[0])
    }
}
