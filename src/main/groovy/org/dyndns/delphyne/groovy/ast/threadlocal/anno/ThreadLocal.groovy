package org.dyndns.delphyne.groovy.ast.threadlocal.anno

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

import org.codehaus.groovy.transform.GroovyASTTransformationClass

@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.FIELD])
@GroovyASTTransformationClass("org.dyndns.delphyne.groovy.ast.threadlocal.impl.ThreadLocalTransformation")
@interface ThreadLocal {
    /**
     * A map which defines a ThreadLocal.  Available keys are initialize, get, and set.  Default behavior is to 
     *  create a thread local whose initial value is null, and the get and set methods are directly delegated.
     * @return
     */
    Class initialValue() default { null }
}
