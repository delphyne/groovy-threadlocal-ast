package org.dyndns.delphyne.groovy.ast.threadlocal.impl

import groovy.util.logging.Slf4j;

import org.junit.Test

@Slf4j
class ThreadLocalTransformationTest {
    @Test
    void testNoInitialValue() {
        log.warn " no initialValue ".center(80, "*")
        def tester = new GroovyClassLoader().parseClass('''
            class NoInitialValue {
                @org.dyndns.delphyne.groovy.ast.threadlocal.anno.ThreadLocal
                Integer myInt
            }
        ''')
        def noInitialValue = tester.newInstance()
        assert noInitialValue.myInt == null
        noInitialValue.myInt = 1
        assert noInitialValue.myInt == 1
        noInitialValue.myInt = Integer.MAX_VALUE
        assert noInitialValue.myInt == Integer.MAX_VALUE
    }
    
    @Test
    void testInitialValueProvided() {
        
    }
    
    @Test
    void testMultiThreaded() {
        
    }
}
