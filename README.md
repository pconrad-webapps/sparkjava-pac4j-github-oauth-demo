# sparkjava-pac4j-demo

WORK IN PROGRESS: Minimal demo of SparkJava with Pac4J.

Also uses mustache templating and a bootstrap based UI.

Requires Java 1.8, and Maven (`mvn` command)

To build, use `mvn package`

To run, use `java -jar target/spark-template-mustache-2.4-SNAPSHOT.jar`

# References:

We followed the instructions here: https://github.com/pac4j/spark-pac4j

Although those instructions are helpful, they are far from a "complete" tutorial of all the things you need to get OAuth working with a Spark Java webapp.  

Half way through, we also found this repo, which fill is some of the gaps: https://github.com/pac4j/spark-pac4j-demo

Even with both of those tutorials, though, they are far from a "complete" tutorial of all the things you need to get OAuth working with a Spark Java webapp.     There is a lot of knowledge about OAuth that they "assume" the programmer already has.    With some background in getting OAuth working in other languages, I attempted to see if I could do the leg work to fill in the missing steps.  What follows is an account of my progress.   

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

There is obviously still more work to do to get this to run, but at least this compiled.

The instructions for step 2 go on to say:

> `http://localhost:8080/callback` is the url of the callback endpoint, which is only necessary for indirect clients.
> 

Fair enough: we've got that taken care of, at least at the level of putting in a stub.

> Notice that you can define:
> 
> 1) a specific [`SessionStore`](http://www.pac4j.org/docs/session-store.html) using the `setSessionStore(sessionStore)` method (by default, it uses the `J2ESessionStore` which relies on the J2E HTTP session)


So this refers to the fact that we now need to start thinking about sessions.   Typically, what happens is that in the `/callback` route, we are getting some signal from the OAuth provider as to whether the user was successfully logged in, and if they were, we get
some information about that user.  We can then information about the user to to the session; this might include:
* a flag indicating that we are, in fact logged in (e.g. `logged_in = "true"`), 
* the user's userid, e.g. `userid = "cgaucho"`
* the user's real name, e.g. `name = "Chris Gaucho"`
* etc.

By contrast, if/when we get an error message, we do something to "wipe out" all values in the current session, essentially returning
us to a "logged out" state.    

We might also add some code in our template that toggles between a `Login` or a `Logout` button depending on whether the session's `logged_in` value is currently `true` or `false`.

So, all of that suggests we need to learn a bit about how Sessions are handled in SparkJava before we proceed.    

Here is what the main "Getting Started" documentation about SparkJava has to say about Sessions.   We can also access the [javadoc for the Session object](http://spark.screenisland.com/spark/Session.html)

> Every request has access to the session created on the server side, provided with the following methods:
> ```java
> request.session(true)                      // create and return session
> request.session().attribute(&quot;user&quot;)        // Get session attribute &#x27;user&#x27;
> request.session().attribute(&quot;user&quot;, &quot;foo&quot;) // Set session attribute &#x27;user&#x27;
> request.session().removeAttribute(&quot;user&quot;)  // Remove session attribute &#x27;user&#x27;
> request.session().attributes()             // Get all session attributes
> request.session().id()                     // Get session id
> request.session().isNew()                  // Check if session is new
> request.session().raw()                    // Return servlet object</code></pre></div>
> ```

This is where we turned to the https://github.com/pac4j/spark-pac4j-demo repo to try to find some patterns for login/logout handling
of sessions that might help provide us with some guidance.

One of the things we found is that [lines 45-47 of src/main/java/org/pac4j/demo/spark/SparkPac4jDemo.java] ](https://github.com/pac4j/spark-pac4j-demo/blob/master/src/main/java/org/pac4j/demo/spark/SparkPac4jDemo.java#L46) read as follows:

```java
		final CallbackRoute callback = new CallbackRoute(config, null, true);
		get("/callback", callback);
		post("/callback", callback);
```

Looking into what `CallbackRoute` is, we find this import at [line 19 ](https://github.com/pac4j/spark-pac4j-demo/blob/master/src/main/java/org/pac4j/demo/spark/SparkPac4jDemo.java#L19):

```java
import org.pac4j.sparkjava.CallbackRoute;
```

Looking into the [Javadoc for the CallbackRoute object class](http://static.javadoc.io/org.pac4j/spark-pac4j/1.2.0/org/pac4j/sparkjava/CallbackRoute.html), we find this:

> This route finishes the login process for an indirect client, based on the [callbackLogic](http://static.javadoc.io/org.pac4j/spark-pac4j/1.2.0/org/pac4j/sparkjava/CallbackRoute.html#callbackLogic).
>
> The configuration can be provided via the following parameters: config (security configuration), defaultUrl (default url after login if none was requested), multiProfile (whether multiple profiles should be kept) and renewSession (whether the session must be renewed after login).

This answers at least one question: how do we ensure that the session gets renewed after login?   The answer is: we pass `true` into the `renewSession` parameter of the constructor for the `CallbackRoute` constructor.   The prototypes for that constructor are these, according to the [spark-pac4j javadoc](http://static.javadoc.io/org.pac4j/spark-pac4j/1.2.0/org/pac4j/sparkjava/CallbackRoute.html):

```java
CallbackRoute(org.pac4j.core.config.Config config) 
CallbackRoute(org.pac4j.core.config.Config config, String defaultUrl) 
CallbackRoute(org.pac4j.core.config.Config config, String defaultUrl, Boolean multiProfile) 
CallbackRoute(org.pac4j.core.config.Config config, String defaultUrl, Boolean multiProfile, Boolean renewSession) 
```

and we see an example of invoking it at [line 45 of the spark-pac4j demo](https://github.com/pac4j/spark-pac4j-demo/blob/master/src/main/java/org/pac4j/demo/spark/SparkPac4jDemo.java#L45):

```java 
		final CallbackRoute callback = new CallbackRoute(config, null, true);
```

It appears, however that this in this case, the invocation of the `CallbackRoute` constructor is passing `true` for `multiProfile`, and is not passing anything for `renewSession`.    The documentation are not clear about whether `renewSession` defaults to `true` or `false` when it is not passed.     

We don't have to guess, however, since [we have the source code here](https://github.com/pac4j/spark-pac4j/blob/master/src/main/java/org/pac4j/sparkjava/CallbackRoute.java).  And it would appear, looking at that file that the default value is `null` (since this is a `Boolean`, not a `bool`, that actually makes sense.)

Looking further into the source code, and tracing things super deep, first from the spark-pac4j code back over to the pac4j package itself, we finally land at [these lines of code, lines 55-59 of DefaultCallbackLogic.java](https://github.com/pac4j/pac4j/blob/master/pac4j-core/src/main/java/org/pac4j/core/engine/DefaultCallbackLogic.java#L55):

```java
      if (inputRenewSession == null) {
            renewSession = true;
        } else {
            renewSession = inputRenewSession;
        }
```

With that mystery cleared up, we now can (sort of) safely rest knowing that by default, sessions are renewed when the callback is performed.

And, that possibly the line of code we want for our callback is this one, i.e. one that takes the defaults for everything except the
config, which already have:

```
        Config config = new
            GithubOAuthConfigFactory(github_client_id,
                                     github_client_secret).build();

        final org.pac4j.sparkjava.CallbackRoute callback =
            new org.pac4j.sparkjava.CallbackRoute(config);
        get("/callback", callback);
        post("/callback", callback);
```

Of course, for this, we need a config.  That comes from the `GithubOAuthConfigFactory`, to which we now add attributes
for the `github_client_id` and `github_client_secret`, which have to get passed into the constructor:

```java
   private String github_client_id;
    private String github_client_secret;

    public GithubOAuthConfigFactory(String github_client_id,
                             String github_client_secret) {
        this.github_client_id = github_client_id;
        this.github_client_secret = github_client_secret;
    }
```

and we change the code for our GitHubClient to use these values:

```java
   GitHubClient githubClient =
            new GitHubClient(github_client_id,
                             github_client_secret);
```

At this point, we also realize that for later stages, we are going to need parameters for `salt` (a random string for cryptographic security of sessions) as well as a reference to the current `TemplateEngine` inside our ConfigFactory.  So we add those now:

```java
  public GithubOAuthConfigFactory(String github_client_id,
                                    String github_client_secret,
                                    String salt,
                                    TemplateEngine templateEngine) {
        this.github_client_id = github_client_id;
        this.github_client_secret = github_client_secret;
        this.salt = salt;
	this.templateEngine = templateEngine;
    }
```

And we change our code that invokes the factory.  Among other changes, we factor out the `MustacheTemplateEngine` invocations to 
a single instance that is a private static class variable for the class containing our main, as is done in the [spark-pac4j-demo](https://github.com/pac4j/spark-pac4j-demo)

```java
   private final static MustacheTemplateEngine templateEngine = new MustacheTemplateEngine();
   ...
   public static void main(String[] args) {
       ...
       Config config = new
            GithubOAuthConfigFactory(github_client_id,
                                     github_client_secret,
                                     "This_is_random_SALT_seoawefoauew89fu",
                                     templateEngine).build();
```


At this point, when we run and try to access the `/callback` route, we get the error:

```
org.pac4j.core.exception.TechnicalException: httpActionAdapter cannot be null
```

Tracing this back, our diagnosis is that we need the following [line of code from the demo app](https://github.com/pac4j/spark-pac4j-demo/blob/master/src/main/java/org/pac4j/demo/spark/DemoConfigFactory.java#L78) in our ConfigFactory, which we currently do not have:

```java
        config.setHttpActionAdapter(new DemoHttpActionAdapter(templateEngine));
```

And that suggests we need a new class, similar to the [DemoHttpActionAdapter.java from the sparkpac4j demo](https://github.com/pac4j/spark-pac4j-demo/blob/master/src/main/java/org/pac4j/demo/spark/DemoHttpActionAdapter.java).

Since we don't yet entirely understand what this class does, we take the class "as is", changing only the package name for now.

We do understand it well enough, though to know that it depends on having two templates called `error401.mustache` and `error403.mustache`, so we copy those into our
templates folder from the examples in the [spark-pac4j-demo](https://github.com/pac4j/spark-pac4j-dem) repo, here: [error401.mustache](https://github.com/pac4j/spark-pac4j-demo/blob/master/src/main/resources/templates/error401.mustache) and [error403.mustache](https://github.com/pac4j/spark-pac4j-demo/blob/master/src/main/resources/templates/error403.mustache).


> 2) specific [matchers](http://www.pac4j.org/docs/matchers.html) via the `addMatcher(name, Matcher)` method.
>


TODO: Continue from here.
