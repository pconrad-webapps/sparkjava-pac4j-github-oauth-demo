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

We interpreted this to mean: implement a class just like this one, called for example, `GithubOAuthConfigFactory`, given that we are only interested in implementing Github OAuth in our demo.

In our application, we put this into  `package org.pconrad.webapps.sparkjava`.

The first step was to try to infer the missing import statements, and get something that would at least minimally compile (getting it to work is a later stage!)  Here is what we came up with.  This does, at a minimum, compile:

```java
package org.pconrad.webapps.sparkjava;

import org.pac4j.core.config.ConfigFactory;
import org.pac4j.core.config.Config;
import org.pac4j.core.client.Clients;

import org.pac4j.oauth.client.GitHubClient;
// See: https://github.com/pac4j/pac4j/blob/master/pac4j-oauth/src/main/java/org/pac4j/oauth/client/GitHubClient.java



public class GithubOAuthConfigFactory implements ConfigFactory {

  public org.pac4j.core.config.Config build() {
      
      GitHubClient githubClient = new GitHubClient("ghid-goes-here", "gh-secret-goes-here");
      Clients clients = new Clients("http://localhost:8080/callback", githubClient);
      
      org.pac4j.core.config.Config config = new org.pac4j.core.config.Config(clients); // placeholder stub
      return config;
  }
}
```

There is obviously still more work to do to get this run.

The instructions for step 2 go on to say:

> `http://localhost:8080/callback` is the url of the callback endpoint, which is only necessary for indirect clients.
> 
> Notice that you can define:
> 
> 1) a specific [`SessionStore`](http://www.pac4j.org/docs/session-store.html) using the `setSessionStore(sessionStore)` method (by default, it uses the `J2ESessionStore` which relies on the J2E HTTP session)

> 2) specific [matchers](http://www.pac4j.org/docs/matchers.html) via the `addMatcher(name, Matcher)` method.
>

TODO: Continue from here.
