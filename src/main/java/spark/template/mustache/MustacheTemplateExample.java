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
package spark.template.mustache;

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
	final Map nullMap = new HashMap();

        get("/", (rq, rs) -> new ModelAndView(nullMap, "home.mustache"), new MustacheTemplateEngine());
	
        get("/ctof", (rq, rs) -> new ModelAndView(nullMap, "ctof.mustache"), new MustacheTemplateEngine());

	get("/ctof_result",
	    (rq, rs) ->
	    {
		Map model = new HashMap();
		// replace next two lines with lines that get the form input from the request object
		// then do the calculation, and store into the map
		model.put("ctemp","20");
		model.put("ftemp","-42");	       
		return new ModelAndView(model, "ctof_result.mustache");
	    },
	    new MustacheTemplateEngine()
	    );
	

    }
}
