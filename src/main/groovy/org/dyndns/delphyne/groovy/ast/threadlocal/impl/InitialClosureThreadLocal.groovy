package org.dyndns.delphyne.groovy.ast.threadlocal.impl

/**
 * A subclass of ThreadLocal which derives its initial value from a closure which is passed in to the constructor.
 * 
 * @author Brian M. Carr <delphyne@gmail.com>
 */
class InitialClosureThreadLocal<T> extends ThreadLocal<T> {
    private Closure initialValueClosure
    
    InitialClosureThreadLocal(Closure initialValueClosure) {
        this.initialValueClosure = initialValueClosure
    }
    
    @Override
    protected T initialValue() {
        initialValueClosure.call()
    }
}
