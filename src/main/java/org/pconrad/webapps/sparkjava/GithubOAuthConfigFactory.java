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
