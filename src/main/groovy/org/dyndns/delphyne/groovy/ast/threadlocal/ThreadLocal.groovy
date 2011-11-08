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
 *     private final static ThreadLocal _tl_myInt = new ThreadLocal()
 *     Integer getMyInt() { _tl_myInt.get() &#125;
 *     void setMyInt(Integer newValue) { _tl_myInt.set(newValue) &#125;
 *&#125;}</blockquote></pre>
 *
 * <p>The ThreadLocal annotation also supports initial values via {@link InitialClosureThreadLocal}.</p>
 * 
 * <pre><blockquote>{@code class Bar {
 *     @ThreadLocal String s = "My Initial Value"
 *&#125;}</blockquote></pre>
 *
 * <p>becomes:</p>
 * 
 * <pre><blockquote>{@code class Foo {
 *     private final static ThreadLocal _tl_s = new InitialClosureThreadLocal({"My Initial Value"&#125;)
 *     String getS() { _tl_s.get() &#125;
 *     void setMyInt(String newValue) { _tl_s.set(newValue) &#125;
 *&#125;}</blockquote></pre>
 *
 *  <p>Because the ThreadLocal backing field is static, initial expressions can only reference static variables.</p>
 *  
 * @author Brian M. Carr <delphyne@gmail.com>
 * @since 1.0.0
 */
@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.FIELD])
@Documented
@GroovyASTTransformationClass('org.dyndns.delphyne.groovy.ast.threadlocal.impl.ThreadLocalTransformation')
@interface ThreadLocal { }
