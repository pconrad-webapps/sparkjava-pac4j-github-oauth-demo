# sparkjava-bootstrap-demo

WORK IN PROGRESS: Minimal demo of SparkJava with Pac4J.

Also uses mustache templating and a bootstrap based UI.

Requires Java 1.8, and Maven (`mvn` command)

To build, use `mvn package`

To run, use `java -jar target/spark-template-mustache-2.4-SNAPSHOT.jar`

# References:

We followed the instructions here: https://github.com/pac4j/spark-pac4j

The first step says:

> ### 1) Add the required dependencies (`spark-pac4j` + `pac4j-*` libraries)
>
> You need to add a dependency on:
>
> - the `spark-pac4j` library (<em>groupId</em>: **org.pac4j**, *version*: **1.2.0**)
> - the appropriate `pac4j` [submodules](http://www.pac4j.org/docs/clients.html) (<em>groupId</em>: **org.pac4j**, *version*: **1.9.1**): `pac4j-oauth` for OAuth support (Facebook, Twitter...), `pac4j-cas` for CAS support, `pac4j-ldap` for LDAP authentication, etc.

> All released artifacts are available in the [Maven central repository](http://search.maven.org/#search%7Cga%7C1%7Cpac4j).
> 
>

We interpreted this to mean: add the following to the dependencies part of the `pom.xml` file:

```xml

```
