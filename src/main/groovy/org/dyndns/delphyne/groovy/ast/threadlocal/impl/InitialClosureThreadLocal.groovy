package org.dyndns.delphyne.groovy.ast.threadlocal.impl

class InitialClosureThreadLocal<T> extends ThreadLocal<T> {
    Closure initialValueClosure
    
    InitialClosureThreadLocal(initialValueClosure) {
        this.initialValueClosure = initialValueClosure
    }
    
    @Override
    protected T initialValue() {
        initialValueClosure.call()
    }
}
