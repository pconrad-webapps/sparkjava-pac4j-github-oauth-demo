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

import spark.ModelAndView;

import spark.Spark;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.before;

import org.pac4j.core.config.Config;
import org.pac4j.sparkjava.SecurityFilter;


/**
 * Mustache template engine example
 *
 * @author Sam Pullara https://github.com/spullara
 */
public class MustacheTemplateExample {

    private final static MustacheTemplateEngine templateEngine = new MustacheTemplateEngine();

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
	final Map nullMap = new HashMap();

        get("/", (rq, rs) -> new ModelAndView(nullMap, "home.mustache"), templateEngine);
	

        get("/ctof", (rq, rs) -> new ModelAndView(nullMap, "ctof.mustache"), templateEngine);



	Config config = new
	    GithubOAuthConfigFactory(github_client_id,
				     github_client_secret,
				     "This_is_random_SALT_seoawefoauew89fu",
				     templateEngine).build();

	final SecurityFilter
	    githubFilter = new SecurityFilter(config, "GithubClient", "", "");

	before("/login", githubFilter);
	
	get("/login", (rq, rs) -> "login stub; later, redirect to OAuth");

	
	final org.pac4j.sparkjava.CallbackRoute callback =
	    new org.pac4j.sparkjava.CallbackRoute(config);
	get("/callback", callback);
	post("/callback", callback);
	

	
	get("/ctof_result",
	    (rq, rs) ->
	    {
		Map model = new HashMap();
		String ctempAsString = rq.queryParams("cTemp"); // get value from form
		double cTemp = 0.0;		
		try {
		    cTemp = Double.parseDouble(ctempAsString);
		    model.put("error","");
		} catch (NumberFormatException nfe) {
		    cTemp = 0.0;
		    model.put("error","Error converting '" + ctempAsString + "' to number; 0.0 used for celsius temp");
		}
		double fTemp = TempConversion.ctof(cTemp);
		model.put("ctemp",Double.toString(cTemp));
		model.put("ftemp",Double.toString(fTemp));
		
		return new ModelAndView(model, "ctof_result.mustache");
	    },
	    templateEngine
	    );
	

    }
}
