package org.dyndns.delphyne.groovy.ast.threadlocal.impl

import groovy.util.logging.Slf4j

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.SyntaxException
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.objectweb.asm.Opcodes

@Slf4j
@GroovyASTTransformation(phase=CompilePhase.SEMANTIC_ANALYSIS)
class ThreadLocalTransformation implements ASTTransformation, Opcodes {

    private final static THREADLOCAL_CLASSNODE = ClassHelper.make(ThreadLocal)
    
    void visit(ASTNode[] nodes, SourceUnit source) {
        if (!nodes[0] instanceof AnnotationNode) {
            def msg = "Expected AnnotationNode but got ${nodes[0].class.simpleName}" 
            source.addError(new SyntaxException(
                    msg, 
                    nodes[0]?.lineNumber, 
                    nodes[0]?.columnNumber))
            log.trace msg
        }
        
        if (!nodes[1] instanceof FieldNode) {
            def msg = "Expected FieldNode but got ${nodes[1].class.simpleName}" 
            source.addError(new SyntaxException(
                    msg, 
                    nodes[1]?.lineNumber, 
                    nodes[1]?.columnNumber))
            log.trace msg
        }

        AnnotationNode annotation = nodes[0]
        FieldNode field = nodes[1]
        ClassNode clazz = field.declaringClass
                
        log.trace "Processing ThreadLocal AST Transformation on $clazz.$field"
        
        String name = field.name
        ClassNode targetType = field.originType
        
        Expression initialValue = createInitialValue(annotation.getMember("initialValue"))
        FieldNode newField = createField(name, clazz, initialValue)
        MethodNode getter = createGetter(name, targetType)
        MethodNode setter = createSetter(name, targetType)
        
        log.trace "Removing field:'$name' from class:'${clazz}'"
        clazz.removeField(name)
        
        log.trace "Adding new ThreadLocal to replace '$name'"
        clazz.addField(newField)
        
        log.trace "Adding proxy getter"
        clazz.addMethod(getter)
        
        log.trace "Adding proxy setter"
        clazz.addMethod(setter)
    }
    
    FieldNode createField(String name, ClassNode containingClass, Expression initialValue) {
        new FieldNode(name, ACC_PRIVATE|ACC_FINAL|ACC_STATIC, THREADLOCAL_CLASSNODE, containingClass, initialValue)
    }
    
    Expression createInitialValue(ClosureExpression providedClosure) {
        List<ASTNode> nodes = new AstBuilder().buildFromCode {
            new ThreadLocal()
        }
        
        nodes[0].statements[0].expression
    }
    
    MethodNode createGetter(String name, ClassNode type) {
        new MethodNode(
                "get${name.capitalize()}",
                ACC_PUBLIC|ACC_FINAL|ACC_STATIC,
                type,
                [] as Parameter[],
                [] as ClassNode[],
                new ExpressionStatement(
                        new MethodCallExpression(
                                new VariableExpression(name),
                                new ConstantExpression("get"),
                                ArgumentListExpression.EMPTY_ARGUMENTS
                            )
                    )
            )
    }
    
    MethodNode createSetter(String name, ClassNode type) {
        new MethodNode(
                "set${name.capitalize()}",
                ACC_PUBLIC|ACC_FINAL|ACC_STATIC,
                ClassHelper.VOID_TYPE,
                [new Parameter(type, "newName")] as Parameter[],
                [] as ClassNode[],
                new ExpressionStatement(
                        new MethodCallExpression(
                                new VariableExpression(name),
                                new ConstantExpression("set"),
                                new ArgumentListExpression(
                                        new VariableExpression("newName")
                                    )
                            )
                    )
            )
    }
}
