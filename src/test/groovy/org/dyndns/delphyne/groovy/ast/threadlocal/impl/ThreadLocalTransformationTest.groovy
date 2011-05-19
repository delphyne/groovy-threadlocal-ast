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
                @org.dyndns.delphyne.groovy.ast.threadlocal.ThreadLocal
                Integer myInt
            }
        ''').newInstance()
        
        assert tester.myInt == null
        tester.myInt = 1
        assert tester.myInt == 1
        tester.myInt = Integer.MAX_VALUE
        assert tester.myInt == Integer.MAX_VALUE
    }
    
    @Test
    void testInitialValueProvided() {
        log.warn " initial value provided ".center(80, "*")
        def tester = new GroovyClassLoader().parseClass('''
            class SimpleInitialValueProvided {
                @org.dyndns.delphyne.groovy.ast.threadlocal.ThreadLocal(initialValue={Integer.MAX_VALUE})
                Integer myInt
            }
        ''').newInstance()
        
        assert tester.myInt == Integer.MAX_VALUE
        tester.myInt = 5
        assert tester.myInt == 5
        
        tester = new GroovyClassLoader().parseClass('''
            class ComplexInitialValueProvided {
                private final static java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger(0)
                @org.dyndns.delphyne.groovy.ast.threadlocal.ThreadLocal(initialValue={counter.andIncrement.intValue()})
                Integer myInt
            }
        ''')
        tester.myInt = 0
        tester.myInt = 0
        
        Thread.start { tester.myInt == 1 }.join()
        Thread.start { tester.myInt == 2 }.join()
    }
    
    @Test
    void testMultiThreaded() {
        def tester = new GroovyClassLoader().parseClass('''
            class NoInitialValue {
                @org.dyndns.delphyne.groovy.ast.threadlocal.ThreadLocal
                Integer myInt
            }
        ''').newInstance()
        
        def threads = []
        
        Random random = new Random()
        
        10.times {
            threads << Thread.start {
                def myValue = it
                tester.myInt = myValue
                3.times {
                    Thread.sleep(random.nextInt(1000))
                }
                assert tester.myInt == myValue
            }
        }
        
        threads.each {
            it.join()
        }
    }
}
