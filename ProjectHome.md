A Groovy AST Transformation which replaces a property with a static ThreadLocal field.  Access to the former property behaves just as if it were defined as an instance field.

For example:

```
class ThreadLocalExample {
  @ThreadLocal String s
  @ThreadLocal Integer i = 3
}
```

Will be transformed into:

```
class ThreadLocalExample {
  private static ThreadLocal _tl_s = new ThreadLocal()
  String getS() { _tl_s.get() }
  void setS(String s) { _tl_s.set(s) }

  private static ThreadLocal _tl_i = new InitialClosureThreadLocal({3})
  Integer getI() { _tl_i.get() }
  void setI(Integer i) { _tl_i.set(i) }
}
```