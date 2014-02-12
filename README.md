Tomcat command proxy
====================

Tool for execute apache tomcat manager application commands from command prompt.
For example help you undeploy application as a step in a build system without having to install any plug-ins.

## Using
For command list see http://tomcat.apache.org/tomcat-8.0-doc/manager-howto.html

Java 6 or higher (>= 1.6.0_45) is requied for running compiled version of tool. For other versions of java you can compile tool yourself.

## Examples
```
java -jar TomcatCommandProxy.jar --user admin --pass p@ssw0rD --host localhost --command list
java -jar TomcatCommandProxy.jar --user=admin --pass=p@ssw0rD --host=localhost --command=list -v
java -jar TomcatCommandProxy.jar --user admin --pass p@ssw0rD --host localhost --command undeploy --param path=/webapp
```

## Building
For build execute:  
`gradlew distZip` 
Gradle build system will download additional libraries and build project.Compiled tool will be in build\distributions\ folde


