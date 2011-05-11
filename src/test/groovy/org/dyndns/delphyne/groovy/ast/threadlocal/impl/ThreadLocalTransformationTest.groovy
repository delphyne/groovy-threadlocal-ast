package org.dyndns.delphyne.groovy.ast.threadlocal.impl

import org.junit.Test

class ThreadLocalTransformationTest {
    @Test
    void testTransform() {
        def tester = new GroovyClassLoader().parseClass('''
            class MyClass {
                @org.dyndns.delphyne.groovy.ast.threadlocal.anno.ThreadLocal
                Integer myInt
            }
        ''')
        
        def myClass = tester.newInstance()
        println myClass.dump()
        
        tester = new GroovyClassLoader().parseClass('''
            class MyClass {
                @org.dyndns.delphyne.groovy.ast.threadlocal.anno.ThreadLocal(initialValue={Integer.MAX_VALUE})
                Integer myInt
            }
        ''')
        myClass = tester.newInstance()
        println myClass.dump()
    }
}
