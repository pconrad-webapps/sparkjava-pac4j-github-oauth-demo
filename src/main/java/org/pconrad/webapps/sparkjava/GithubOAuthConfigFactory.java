package org.pconrad.webapps.sparkjava;

import org.pac4j.core.config.ConfigFactory;
import org.pac4j.core.config.Config;
import org.pac4j.core.client.Clients;

import org.pac4j.oauth.client.GitHubClient;

import spark.TemplateEngine;

// See: https://github.com/pac4j/pac4j/blob/master/pac4j-oauth/src/main/java/org/pac4j/oauth/client/GitHubClient.java



public class GithubOAuthConfigFactory implements ConfigFactory {

    private String github_client_id;
    private String github_client_secret;


    private final String salt;

    private final TemplateEngine templateEngine;

    public GithubOAuthConfigFactory(String github_client_id,
				    String github_client_secret,
				    String salt,
				    TemplateEngine templateEngine) {
	this.github_client_id = github_client_id;
	this.github_client_secret = github_client_secret;
	this.salt = salt;
	this.templateEngine = templateEngine;	
    }
    
    public org.pac4j.core.config.Config build() {
	
	GitHubClient githubClient =
	    new GitHubClient(github_client_id,
			     github_client_secret);
	
	Clients clients = new Clients("http://localhost:8080/callback", githubClient);
	
	org.pac4j.core.config.Config config = new org.pac4j.core.config.Config(clients); // placeholder stub
	return config;
    }
}
