package org.dyndns.delphyne.groovy.ast.threadlocal.impl

import java.lang.reflect.Field

import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.tools.ast.TransformTestHelper
import org.junit.Test

class ThreadLocalTransformationTest {
    TransformTestHelper invoker = new TransformTestHelper(new ThreadLocalTransformation(), CompilePhase.CANONICALIZATION)

    @Test
    void testVanillaImplementationString() {
        def clazz = invoker.parse '''\
            class Vanilla {
                @org.dyndns.delphyne.groovy.ast.threadlocal.ThreadLocal String s
                @org.dyndns.delphyne.groovy.ast.threadlocal.ThreadLocal Integer i
            }
        '''.stripIndent()

        assert ThreadLocal == clazz.getDeclaredField('_tl_s').type
        assert String == clazz.getDeclaredMethod('getS').returnType
        assert void.class == clazz.getDeclaredMethod('setS', String).returnType
        assert void.class == clazz.getDeclaredMethod('removeS').returnType

        assert ThreadLocal == clazz.getDeclaredField('_tl_i').type
        assert Integer == clazz.getDeclaredMethod('getI').returnType
        assert void.class == clazz.getDeclaredMethod('setI', Integer).returnType
        assert void.class == clazz.getDeclaredMethod('removeI').returnType

        def instance = clazz.newInstance()
        
        assert null == instance.s
        instance.s = 'hello'
        assert 'hello' == instance.s
        instance.s = 1
        assert String == instance.s.class
        assert "1" == instance.s
        instance.removeS()
        assert null == instance.s
        
        assert null == instance.i
        instance.i = 5
        assert 5 == instance.i
        instance.removeI()
        assert null == instance.i
    }
    
    @Test
    void testThreadIndependance() {
        def clazz = invoker.parse '''\
            class ThreadTester {
                @org.dyndns.delphyne.groovy.ast.threadlocal.ThreadLocal Double d
            }
        '''.stripIndent()
        
        def instance = clazz.newInstance()
        
        def threads = []
        10.times {
            threads << Thread.start {
                Double val = Math.random()
                instance.d = val
                Thread.sleep((long)(val * 2000))
                assert val == instance.d
            }
        }
        
        threads.each { it.join() }
    }

    @Test
    void testPrimitiveProtection() {
        try {
            invoker.parse '''\
                class Primitive {
                    @org.dyndns.delphyne.groovy.ast.threadlocal.ThreadLocal int i
                }
            '''.stripIndent()
        } catch (ex) {
            assert ex.message.contains('@ThreadLocal annotated properties cannot be primitives. @ line 2, column 5.')
        }
    }
    
    @Test
    void testInitialValue() {
        def clazz = invoker.parse '''\
            class Initial {
                private static atomic = new java.util.concurrent.atomic.AtomicInteger()
                @org.dyndns.delphyne.groovy.ast.threadlocal.ThreadLocal String s = "Initial Value"
                @org.dyndns.delphyne.groovy.ast.threadlocal.ThreadLocal Integer complicatedSetup = {
                    atomic.getAndIncrement()
                }.call()
            }
        '''.stripIndent()
        
        def instance = clazz.newInstance()
        
        assert "Initial Value" == instance.s        
        instance.s = "New Value"
        assert "New Value" == instance.s
        instance.removeS()
        assert "Initial Value" == instance.s
        
        assert 0 == instance.complicatedSetup
        assert 0 == instance.complicatedSetup
        instance.removeComplicatedSetup()
        assert 1 == instance.complicatedSetup
        
        Thread.start { 2 == instance.complicatedSetup }
    }
}
