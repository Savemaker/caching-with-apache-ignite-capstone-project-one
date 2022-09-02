# How to run on Java 11
Run app or tests with JVM properties:
~~~
--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED
--add-exports=java.base/sun.nio.ch=ALL-UNNAMED
--add-exports=java.management/com.sun.jmx.mbeanserver=ALL-UNNAMED
--add-exports=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED
--add-exports=java.base/sun.reflect.generics.reflectiveObjects=ALL-UNNAMED
--add-opens=jdk.management/com.sun.management.internal=ALL-UNNAMED
--illegal-access=permit
~~~
# Project:
# caching-with-apache-ignite-capstone-project-one
Getting practical skills using caching and distributed computing using Apache Ignite. The capstone project is devoted to using embedded Apache Ignite as a local application-level cache on top the underlying data storage (Apache Cassandra) with read-through and write-through caching strategies.

This project has been done with Testcontainers, so you don't need apache cassandra on your local, and you don't need to import jcpenney product dataset inorder to test the code. I believe you can add your cassandra by overriding properties in file:
~~~ 
application.properties
~~~

[Jcpenney Product Dataset Link](https://www.kaggle.com/PromptCloudHQ/all-jc-penny-products)
