# sparkjava-pac4j-demo

WORK IN PROGRESS: Minimal demo of SparkJava with Pac4J.

Also uses mustache templating and a bootstrap based UI.

Requires Java 1.8, and Maven (`mvn` command)

To build, use `mvn package`

To run, use `java -jar target/spark-template-mustache-2.4-SNAPSHOT.jar`

# References:

We followed the instructions here: https://github.com/pac4j/spark-pac4j

Although those instructions are helpful, they are far from a "complete" tutorial of all the things you need to get OAuth working with a Spark Java webapp.     There is a lot of knowledge about OAuth that they "assume" the programmer already has.    With some background in getting OAuth working in other languages, I attempted to see if I could do the leg work to fill in the missing steps.  What follows is an account of my progress.   

# Preliminaries for OAuth

First, before we even get started, we know we are going to need three things in our Spark Java app, three things that you always need to get OAuth working:

1.  A "login" button or link in your app.  This is the place that when the user clicks or selects, you redirect the user to the other 
    website (e.g. Github, Facebook, Google, Twitter, etc.) to enter their login information (if they are not already logged in
    to the OAuth provider), and to
    authorize the App requesting OAuth to access the user's profile on the OAuth Provider (e.g. the Github profile, Facebook profile, 
    etc.)

2.  A route for the "callback url".  This is the route in the application that the OAuth provider returns the user to after
    authenticating.    It is typically something such as `http://localhost:5432:/callback` or `http://sparkjava-pac4j-demo.herokuapp.com/callback`.    It could also be something such as  `http://localhost:5432:/callback/oauth/github`.   

3.  For running on Heroku: A way to get the GITHUB_CLIENT_ID and GITHUB_CLIENT_SECRET out of environment variables into to the
    application.    

    If you are new to OAuth: these values are numbers that you get from Github when you register a new
    web application that uses OAuth authentication (e.g. at [this page](https://github.com/settings/applications/new)).   You 
    typically need to do this once for running on localhost, and a second time if/when you run on a cloud provider such as Heroku,
    or a "real" web server.     You have to do this separately each time the callback url changes, since these values are typically tied to
    a single specific callback URL.

For now, we'll put both of those things into our app as "stubs" that we'll wire up to the rest of the OAuth stuff later.

Let's take each in turn:

1. We'll add the login button to the `src/main/resources/templates/nav.mustache` template.   We put this as the last item 
    in the last `<ul>` element, so that it is the rightmost item on the navigation bar.

    ```html
        <li><a href="/login">Login</a></li>
    ```

    We'll also add a route for `/login`.  Later this will redirect to the OAuth provider.
    
    ```java
      get("/login", (rq, rs) -> "login stub; later, redirect to OAuth");    
    ```
2. For item 2, we'll proceed as with the route for `/login`, adding it as stub route:

    ```java
      get("/callback", (rq, rs) -> "stub for oauth callback");    
    ```
    
3.  For item 3, 
    we'll follow the [example of getting environment variables in the Oracle Java documentation](https://docs.oracle.com/javase/tutorial/essential/environment/env.html), 
    and add this code to the top of the `main` method of `src/main/java/org/pconrad/webapps/sparkjava/MustacheTemplateExample.java`:

    ```java
    String GITHUB_CLIENT_ID = System.getenv("GITHUB_CLIENT_ID");
    String GITHUB_CLIENT_SECRET = System.getenv("GITHUB_CLIENT_SECRET");

	if (GITHUB_CLIENT_ID==null || GITHUB_CLIENT_SECRET==null) {
            System.err.println("Warning: need to define GITHUB_CLIENT_ID \n" +
                               "         and GITHUB_CLIENT_SECRET");
            System.exit(1);
	}
    ```

# Getting the Pac4J code working.

Returning now to https://github.com/pac4j/spark-pac4j

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
