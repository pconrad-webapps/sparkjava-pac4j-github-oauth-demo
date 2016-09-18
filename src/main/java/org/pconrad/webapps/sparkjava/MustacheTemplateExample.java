/*
 * Copyright 2014
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pconrad.webapps.sparkjava;

import java.util.HashMap;
import java.util.Map;
import static java.util.Map.Entry;

import spark.ModelAndView;

import spark.Spark;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.before;

import spark.Request;
import spark.Response;

import org.pac4j.core.config.Config;
import org.pac4j.sparkjava.SecurityFilter;
import org.pac4j.sparkjava.ApplicationLogoutRoute;

import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.sparkjava.SparkWebContext;

/**
 * Mustache template engine example
 *
 * @author Sam Pullara https://github.com/spullara
 */
public class MustacheTemplateExample {


    private static java.util.List<CommonProfile> getProfiles(final Request request,
						   final Response response) {
	final SparkWebContext context = new SparkWebContext(request, response);
	final ProfileManager manager = new ProfileManager(context);
	return manager.getAll(true);
    }
    
    
    private final static MustacheTemplateEngine templateEngine = new MustacheTemplateEngine();

    private static Map buildModel(Request request, Response response) {

	final Map model = new HashMap<String,Object>();
	
	Map<String, Object> map = new HashMap<String, Object>();
	for (String k: request.session().attributes()) {
	    Object v = request.session().attribute(k);
	    map.put(k,v);
	}
	
	model.put("session", map.entrySet());

	java.util.List<CommonProfile> userProfiles = getProfiles(request,response);

	map.put("profiles", userProfiles);

	try {
	    if (userProfiles.size()>0) {
		CommonProfile firstProfile = userProfiles.get(0);
		map.put("firstProfile", firstProfile);	
		
		org.pac4j.oauth.profile.github.GitHubProfile ghp = 
		    (org.pac4j.oauth.profile.github.GitHubProfile) firstProfile;
		
		model.put("userid",ghp.getUsername());
		model.put("name",ghp.getDisplayName());
		model.put("avatar_url",ghp.getPictureUrl());
		model.put("email",ghp.getEmail());
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
	return model;	
    }
    
    public static void main(String[] args) {

	String github_client_id = System.getenv("GITHUB_CLIENT_ID");
	String github_client_secret = System.getenv("GITHUB_CLIENT_SECRET");

	if (github_client_id==null || github_client_secret==null) {
	    System.err.println("Warning: need to define GITHUB_CLIENT_ID \n" +
			       "         and GITHUB_CLIENT_SECRET");
	    System.exit(1);
	}
	
	Spark.staticFileLocation("/static");
	
	try {
	    // needed for Heroku
	    Spark.port(Integer.valueOf(System.getenv("PORT"))); 
	} catch (Exception e) {
	    System.err.println("NOTICE: using default port." +
			       " Define PORT env variable to override");
	}

	Config config = new
	    GithubOAuthConfigFactory(github_client_id,
				     github_client_secret,
				     "This_is_random_SALT_seoawefoauew89fu",
				     templateEngine).build();

	final SecurityFilter
	    githubFilter = new SecurityFilter(config, "GithubClient", "", "");

	get("/",
	    (request, response) -> new ModelAndView(buildModel(request,response),"home.mustache"),
	    templateEngine);

	before("/login", githubFilter);

	get("/login",
	    (request, response) -> new ModelAndView(buildModel(request,response),"home.mustache"),
	    templateEngine);

	get("/logout", new ApplicationLogoutRoute(config, "/"));
	
	get("/session",
	    (request, response) -> new ModelAndView(buildModel(request,response),"session.mustache"),
	    templateEngine);
	
	final org.pac4j.sparkjava.CallbackRoute callback =
	    new org.pac4j.sparkjava.CallbackRoute(config);

	get("/callback", callback);
	post("/callback", callback);

    }
}
