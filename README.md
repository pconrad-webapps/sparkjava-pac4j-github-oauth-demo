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
        <dependency>
        	<groupId>org.pac4j</groupId>
        	<artifactId>spark-pac4j</artifactId>
        	<version>1.2.0</version>
        </dependency>
        <dependency>
        	<groupId>org.pac4j</groupId>
        	<artifactId>pac4j-oauth</artifactId>
        	<version>1.9.1</version>
        </dependency>
```

The next step says:

> ### 2) Define the configuration (`Config` + `Client` + `Authorizer`)
> 
> The configuration (`org.pac4j.core.config.Config`) contains all the clients and authorizers required by the application to handle security.
> 
> It can be built via a configuration factory (`org.pac4j.core.config.ConfigFactory`) for example:
>
> ```java
> public class DemoConfigFactory implements ConfigFactory {
> 
> ```

\[Many lines of java code omitted here.  See <https://github.com/pac4j/spark-pac4j/blob/master/README.md> for full listing \]

> ```java
>    config.addMatcher("excludedPath", new ExcludedPathMatcher("^/facebook/notprotected$"));
>    config.setHttpActionAdapter(new DemoHttpActionAdapter(templateEngine));
>    return config;
>  }
> }
> ```

The instructions go on to say:

> `http://localhost:8080/callback` is the url of the callback endpoint, which is only necessary for indirect clients.
> 
> Notice that you can define:
> 
> 1) a specific [`SessionStore`](http://www.pac4j.org/docs/session-store.html) using the `setSessionStore(sessionStore)` method (by default, it uses the `J2ESessionStore` which relies on the J2E HTTP session)

> 2) specific [matchers](http://www.pac4j.org/docs/matchers.html) via the `addMatcher(name, Matcher)` method.
>

TODO: Continue from here.
