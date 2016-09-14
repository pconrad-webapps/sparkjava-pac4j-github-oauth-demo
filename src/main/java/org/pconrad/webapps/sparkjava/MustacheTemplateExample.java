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

/**
 * Mustache template engine example
 *
 * @author Sam Pullara https://github.com/spullara
 */
public class MustacheTemplateExample {
    public static void main(String[] args) {
	Spark.staticFileLocation("/static");
	try {
	    Spark.port(Integer.valueOf(System.getenv("PORT"))); // needed for Heroku
	} catch (Exception e) {
	    System.err.println("NOTICE: using default port.  Define PORT env variable to override");
	}
	final Map nullMap = new HashMap();

        get("/", (rq, rs) -> new ModelAndView(nullMap, "home.mustache"), new MustacheTemplateEngine());
	
        get("/ctof", (rq, rs) -> new ModelAndView(nullMap, "ctof.mustache"), new MustacheTemplateEngine());

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
	    new MustacheTemplateEngine()
	    );
	

    }
}
