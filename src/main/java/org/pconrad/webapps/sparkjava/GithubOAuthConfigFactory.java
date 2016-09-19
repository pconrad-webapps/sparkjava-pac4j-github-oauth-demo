package org.pconrad.webapps.sparkjava;

import org.pac4j.core.config.ConfigFactory;
import org.pac4j.core.config.Config;
import org.pac4j.core.client.Clients;

import org.pac4j.oauth.client.GitHubClient;

import spark.TemplateEngine;

/** 
    Creates an instance of org.pac4j.core.config.Config for GithubOAuth

    @see <a href="https://github.com/pac4j/pac4j/blob/master/pac4j-oauth/src/main/java/org/pac4j/oauth/client/GitHubClient.java">GithubClient object in pac4j</a>

    @author github.com/pconrad
 */

public class GithubOAuthConfigFactory implements ConfigFactory {

    private String github_client_id;
    private String github_client_secret;
    private String callback_url;

    private final String salt;

    private final TemplateEngine templateEngine;

    public GithubOAuthConfigFactory(String github_client_id,
				    String github_client_secret,
				    String callback_url,
				    String salt,
				    TemplateEngine templateEngine) {
	this.github_client_id = github_client_id;
	this.github_client_secret = github_client_secret;
	this.callback_url = callback_url;
	this.salt = salt;
	this.templateEngine = templateEngine;	
    }
    
    public org.pac4j.core.config.Config build() {
	
	GitHubClient githubClient =
	    new GitHubClient(github_client_id,
			     github_client_secret);

	githubClient.setScope("user:email");
	Clients clients = new Clients(this.callback_url, githubClient);
	
	org.pac4j.core.config.Config config =
	    new org.pac4j.core.config.Config(clients);

	config.setHttpActionAdapter(new DemoHttpActionAdapter(templateEngine));
	
	return config;
    }
}
