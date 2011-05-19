package org.dyndns.delphyne.groovy.ast.threadlocal

import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

import org.codehaus.groovy.transform.GroovyASTTransformationClass

/**
 * <p>Field level annotation to replace a defined property in a groovy class with a private final static 
 * {@link java.lang.ThreadLocal} instance and a pair of proxy getter and setters.</p>  
 *  
 * <p>For example:</p>
 *  
 * <pre><blockquote>{@code class Foo {
 *     @ThreadLocal Integer myInt
 *&#125;}</blockquote></pre>
 *  
 * <p>will be transformed into:</p>
 *  
 * <pre><blockquote>{@code class Foo {
 *     private final static ThreadLocal myInt = new ThreadLocal()
 *     public static final getMyInt() { myInt.get() &#125;
 *     public static final setMyInt(Integer newValue) { myInt.set(newValue) &#125;
 *&#125;}</blockquote></pre>
 *  
 * An optional initial value closure of arbitrary complexity may be included, for example:
 *
 * <pre><blockquote>{@code class Bar {
 *     private final static counter = new AtomicInteger(0)
 *     @ThreadLocal(initialValue={counter.getAndIncrement().intValue()&#125;) Integer uniqueId
 *&#125;}</blockquote></pre>
 *  
 * <p>will be transformed into:</p>
 *  
 * <pre><blockquote>{@code class Bar {
 *     private final static counter = new AtomicInteger(0)
 *     private final static ThreadLocal uniqueId = [initalValue:{counter.getAndIncrement().intValue()&#125;] as ThreadLocal
 *     public static final getMyInt() { myInt.get() &#125;
 *     public static final setMyInt(Integer newValue) { myInt.set(newValue) &#125;
 *&#125;}</blockquote></pre>
 *  
 * @author Brian M. Carr
 * @since 1.0.0
 */
@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.FIELD])
@Documented
@GroovyASTTransformationClass("org.dyndns.delphyne.groovy.ast.threadlocal.impl.ThreadLocalTransformation")
@interface ThreadLocal {
    /**
     * A map which defines a ThreadLocal.  Available keys are initialize, get, and set.  Default behavior is to 
     *  create a thread local whose initial value is null, and the get and set methods are directly delegated.
     * @return
     */
    Class initialValue() default Closure
}