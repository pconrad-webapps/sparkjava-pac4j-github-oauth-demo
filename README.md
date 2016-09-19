# sparkjava-pac4j-github-oauth-demo

WORK IN PROGRESS: Minimal demo of SparkJava with Pac4J doing Github OAuth

Also uses mustache templating and a bootstrap based UI.

Requires Java 1.8, and Maven (`mvn` command)

To build, use `mvn package`

To run, use `java -jar target/sparkjava-pac4j-demo-1.0.jar 

# References:

Some helpful references can be found here:

* https://github.com/pac4j/spark-pac4j
* https://github.com/pac4j/spark-pac4j-demo
 
That documentation though leaves a lot of things out that you might need to know to work with OAuth.  Here are some of the missing
pieces.

# Preliminaries for OAuth

First, before we even get started, we know we are going to need three things in our Spark Java app, three things that you always need to get OAuth working:

1.  A "login" button or link in your app.  This is the place that when the user clicks or selects, you redirect the user to the other 
    website (e.g. Github, Facebook, Google, Twitter, etc.) to enter their login information (if they are not already logged in
    to the OAuth provider), and to authorize the App requesting OAuth to access the user's profile on the OAuth Provider (e.g. the Github profile, Facebook profile, 
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


Let's take each in turn:

1. We'll add the login button to the `src/main/resources/templates/nav.mustache` template.   We put this as the last item 
    in the last `<ul>` element, so that it is the rightmost item on the navigation bar.     We've wrapped this in some code
    so that it toggles between being a Login and a Logout button.   When the value `userid` is defined in the `model` object
    passed to the template, then we see a `Logout` button; otherwise we see a `Login` button.   What you see below is the
    mustache template syntax for something like an `if`/`else`:

    ```html
    <ul class="nav navbar-nav navbar-right">

	{{#userid}} 
	<li><a href="/logout">Logout</a></li>
	{{/userid}}

	{{^userid}}
	<li><a href="/login">Login</a></li>
	{{/userid}}

    </ul>
    ```

    We'll also add a routes for `/login` and `/logout`.   Here's what those look like, inside the
    main java file for our webapp, namely [src/main/java/org/pconrad/webapps/sparkjava/SparkPac4jGithubOAuthDemo.java](https://github.com/pconrad-webapps/sparkjava-pac4j-github-oauth-demo/blob/master/src/main/java/org/pconrad/webapps/sparkjava/SparkPac4jGithubOAuthDemo.java):
   
    What these routes essentially mean is: 
    
        * before you login, run the `githubFilter`, which tries to login to Github.
        * when you are logged in, just to to the home page (the content of `"home.mustache"`)
        * when you logout, call the special magic method `ApplicationLogoutRoute` which destroys all the
            values in the session, and redirects you to some page, in this case to the page at the url `"/"`
    
    ```java
        final SecurityFilter githubFilter = new SecurityFilter(config, "GithubClient", "", "");
       
        before("/login", githubFilter);

	get("/login",
	    (request, response) -> new ModelAndView(buildModel(request,response),"home.mustache"),
	    templateEngine);

	get("/logout", new ApplicationLogoutRoute(config, "/"));
    ```
    
    2. For item 2, we define the `/callback` route as follows.  This is just boilerplate pac4j code:

    ```java
       final org.pac4j.sparkjava.CallbackRoute callback =
	    new org.pac4j.sparkjava.CallbackRoute(config);

	get("/callback", callback);
	post("/callback", callback);
    ```
    
3.  For item 3, we've followed
     the [example of getting environment variables in the Oracle Java documentation](https://docs.oracle.com/javase/tutorial/essential/environment/env.html), 
     and then added a bit of extra code to abstract this.     After calling this method, we can
     be assured that if any of the needed env vars were not defined, an error message was printed,
     and the web server halted.  So we can now just access them via, for example,
     `envVars.get("GITHUB_CLIENT_ID")`
     
    ```java
        HashMap<String,String> envVars =
	    getNeededEnvVars(new String []{ "GITHUB_CLIENT_ID",
					    "GITHUB_CLIENT_SECRET",
					    "GITHUB_CALLBACK_URL",
					    "APPLICATION_SALT"});
    
    ```

# What we had to add to the `pom.xml`

In https://github.com/pac4j/spark-pac4j

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

# Sessions

So when doing OAuth, we now need to start thinking about sessions.   Typically, what happens is that in the `/callback` route, we are getting some signal from the OAuth provider as to whether the user was successfully logged in, and if they were, we get
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
> request.session().attribute("user")        // Get session attribute "user"
> request.session().attribute("user", "foo") // Set session attribute "user"
> request.session().removeAttribute("user")  // Remove session attribute "user"
> request.session().attributes()             // Get all session attributes
> request.session().id()                     // Get session id
> request.session().isNew()                  // Check if session is new
> request.session().raw()                    // Return servlet object
> ```

