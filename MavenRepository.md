# Introduction #

In order to use this transformation in your project which supports Maven dependencies, you will need to use one of the following definitions:

## Gradle ##
```
repositories {
  mavenRepo(name: 'groovy-threadlocal-ast m2 repo', urls: ['http://groovy-threadlocal-ast.googlecode.com/git/repository/']
}

dependencies {
  compile('org.dyndns.delphyne:groovy-threadlocal-ast:1.0.1')
}
```

## Maven ##
```
<project>
  <dependencies>
    <dependency>
      <groupId>org.dyndns.delphyne</groupId>
      <artifactId>groovy-threadlocal-ast</artifactId>
      <version>1.0.1</version>
    </dependency>
  </dependencies>
  <repositories>
    <repository>
      <id>threadlocal</id>
      <name>groovy-threadlocal-ast m2 repo</name>
      <url>http://groovy-threadlocal-ast.googlecode.com/git/repository/</url>
    </repository>
  </repositories>
</project>
```