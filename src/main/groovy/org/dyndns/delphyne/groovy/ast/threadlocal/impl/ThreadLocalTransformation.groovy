package org.dyndns.delphyne.groovy.ast.threadlocal.impl

import groovy.util.logging.Log

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.CastExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.SyntaxException
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

@Log
@GroovyASTTransformation(phase=CompilePhase.SEMANTIC_ANALYSIS)
class ThreadLocalTransformation implements ASTTransformation {

    private final static DEFAULT_INITIAL_VALUE = createDefaultInitialValue()
    
    private final static THREADLOCAL_CLASSNODE = ClassHelper.make(ThreadLocal)
    
    void visit(ASTNode[] nodes, SourceUnit source) {
        if (!nodes[0] instanceof AnnotationNode) {
            source.addError(new SyntaxException(
                    "Expected AnnotationNode but got ${nodes[0].class.simpleName}", 
                    nodes[0]?.lineNumber, 
                    nodes[0]?.columnNumber))
        }
        
        if (!nodes[1] instanceof FieldNode) {
            source.addError(new SyntaxException(
                    "Expected FieldNode but got ${nodes[1].class.simpleName}", 
                    nodes[1]?.lineNumber, 
                    nodes[1]?.columnNumber))
        }
        
        AnnotationNode annotation = nodes[0]
        FieldNode field = nodes[1]
        ClassNode clazz = field.declaringClass
        
        String name = field.name
        
        // remove old field node
        log.info "Removing '$name' field"
        clazz.removeField(name)
        
        // get initialize closure
        log.info "Retrieving initialization closure"
        Expression initialValue = createInitialValueExpression(annotation.getMember("initialValue"))
        Statement getter = createGetter(name)
        Statement setter = createSetter(name)
        
        log.info name
        log.info THREADLOCAL_CLASSNODE.toString()
        log.info initialValue.toString()
//        clazz.addField(name, 0, THREADLOCAL_CLASSNODE, initialValue)
    }
    
    Expression createInitialValueExpression(ClosureExpression closure) {
        ClosureExpression toCall = closure ?: DEFAULT_INITIAL_VALUE
        
        new CastExpression(
                THREADLOCAL_CLASSNODE,
                new MapExpression([
                        new MapEntryExpression(
                                new ConstantExpression("initialValue"),
                                toCall
                            )
                    ])
            )
    }
    
    Statement createSetter(String name) {
        new ExpressionStatement(
                new MethodCallExpression(
                        new PropertyExpression(
                            new VariableExpression("this"),
                            new ConstantExpression(name)
                        ),
                        new ConstantExpression("set"),
                        new ArgumentListExpression(
                                new VariableExpression(name)
                            )
                    )
            )
    }
    
    Statement createGetter(String name) {
        new ExpressionStatement(
                new MethodCallExpression(
                        new VariableExpression(name),
                        new ConstantExpression("get"),
                        ArgumentListExpression.EMPTY_ARGUMENTS
                    )
            )
    }
    
    static ClosureExpression createDefaultInitialValue() {
        new ClosureExpression(
                [] as Parameter[],
                new ExpressionStatement(
                        new ConstantExpression("null")
                    )
            )
    }
}
