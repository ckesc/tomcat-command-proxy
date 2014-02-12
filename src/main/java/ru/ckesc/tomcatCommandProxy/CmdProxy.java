package ru.ckesc.tomcatCommandProxy;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

import static ru.ckesc.tomcatCommandProxy.Main.println;

/**
 * Sends commands to tomcat server
 */
public class CmdProxy {
    private String username;
    private String password;
    private String host;
    private Integer port;
    private boolean useHttps;

    public CmdProxy(String username, String password, String host, Integer port, boolean useHttps) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        this.useHttps = useHttps;
    }

    /**
     * Creates http context, with user credentials. BASIC authorization is used.
     * @param user username of user
     * @param pass password of user
     * @param targetHost server on which enter
     * @return Configured authorized context
     */
    private static HttpClientContext getAuthorizedContext(String user, String pass, HttpHost targetHost) {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                new UsernamePasswordCredentials(user, pass));

        // Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);

        // Add AuthCache to the execution context
        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credentialsProvider);
        context.setAuthCache(authCache);
        return context;
    }

    /**
     * Performs specified command on server
     * @param command command of tomcat manager application
     * @param params parameters for command, might be null
     * @throws IOException on any network problems
     * @return Server response. May be null.
     */
    public String performCommand(String command, String params) throws IOException{
        CloseableHttpClient httpClient = HttpClients.createDefault();

        String scheme;
        if (useHttps) {
            scheme = "https";
        } else {
            scheme = "http";
        }

        String requestURL;
        if (params == null || params.isEmpty()) {
            requestURL = String.format("%s://%s:%s/manager/text/%s", scheme, host, port, command);
        } else {
            requestURL = String.format("%s://%s:%s/manager/text/%s?%s", scheme, host, port, command, params);
        }

        HttpHost targetHost = new HttpHost(host, port, scheme);
        if (isVerboseMode()) {
            println(String.format("Executing: %s", requestURL));
        }

        HttpGet httpRequest = new HttpGet(requestURL);

        /*
         * Auth to server via BASIC auth
         */
        HttpClientContext context = getAuthorizedContext(username, password, targetHost);

        /*
         * Execute request!
         */
        HttpEntity resEntity = httpClient.execute(targetHost, httpRequest, context).getEntity();

        /*
         * Print response
         */
        String page = null;
        if (resEntity != null) {
            page = EntityUtils.toString(resEntity);
        }
        httpClient.close();
        return page;
    }

    private boolean isVerboseMode() {
        return Main.verboseMode;
    }
}
