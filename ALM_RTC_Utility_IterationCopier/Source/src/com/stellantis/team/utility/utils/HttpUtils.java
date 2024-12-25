package com.stellantis.team.utility.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.InvalidCredentialsException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;




/**
 * @author FCA
 * Http utility methods
 * 
 */
@SuppressWarnings("deprecation")
public class HttpUtils {
	
	static String AUTHREQUIRED = "X-com-ibm-team-repository-web-auth-msg";
	static String AUTHURL = "X-jazz-web-oauth-url";
    // name of custom header that authentication messages are stored in
    private static final String FORM_AUTH_HEADER = "X-com-ibm-team-repository-web-auth-msg"; //$NON-NLS-1$
    // auth header value when authentication is required
    //private static final String FORM_AUTH_REQUIRED_MSG = "authrequired"; //$NON-NLS-1$
    // auth header value when authentication failed
 // auth header value when authentication failed
    private static final String FORM_AUTH_FAILED_MSG = "authfailed"; //$NON-NLS-1$
    // URI the server redirects to when authentication fails
    public static final String FORM_AUTH_FAILED_URI = "/auth/authfailed"; //$NON-NLS-1$
	static public void setupLazySSLSupport(HttpClient httpClient) {
		ClientConnectionManager connManager = httpClient.getConnectionManager();
		//CustomLogger.logMessage("httpClient.getConnectionManager()");
		SchemeRegistry schemeRegistry = connManager.getSchemeRegistry();
		//CustomLogger.logMessage("connManager.getSchemeRegistry()");
		schemeRegistry.unregister("https");
		//CustomLogger.logMessage("unregister");
		/** Create a trust manager that does not validate certificate chains */
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public void checkClientTrusted(
					java.security.cert.X509Certificate[] certs, String authType) {
				//CustomLogger.logMessage("checkClientTrusted");
				/** Ignore Method Call */
			}

			public void checkServerTrusted(
					java.security.cert.X509Certificate[] certs, String authType) {
				//CustomLogger.logMessage("checkServerTrusted");
				/** Ignore Method Call */
			}

			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				//CustomLogger.logMessage("java.security.cert.X509Certificate[] getAcceptedIssuers()");
				return null;
			}
		} };

		SSLContext sc = null;
		try {
			// changed from SSL to TLSv1 to work with CLM 5.0.x
			sc = SSLContext.getInstance("TLSv1.2");
			//CustomLogger.logMessage("SSLContext.getInstance()");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			//CustomLogger.logMessage("Called init");
		} catch (Exception e) {
			//CustomLogger.logError(e);
		}

		SSLSocketFactory sf = new SSLSocketFactory(sc);
		sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		//CustomLogger.logMessage("setHostnameVerifier");
		Scheme https = new Scheme("https", sf, 443);
		//CustomLogger.logMessage("Set scheme for https");
		schemeRegistry.register(https);
		//CustomLogger.logMessage("Registered https");
	}

	/**
	 * Print out the HTTPResponse headers
	 */
	public static void printResponseHeaders(HttpResponse response) {
		Header[] headers = response.getAllHeaders();
		for (int i = 0; i < headers.length; i++) {
		}
	}

	/**
	 * Print out the HTTP Response body
	 */
	public static void printResponseBody(HttpResponse response) {
		HttpEntity entity = response.getEntity();
		if (entity == null) return;
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(entity.getContent()));
			String line = reader.readLine();
			while (line != null) {
				
				line = reader.readLine();
				//CustomLogger.logMessage("line"+line);
			}
//			reader.close();
		} catch (IllegalStateException | IOException e) {
			CustomLogger.logException(e);
		}
		
	}

	/**
	 * Access to a Document protected by a Form based authentication
	 * 
	 * @param serverURI			- Server URI
	 * @param request			- HttpGet request
	 * @param login				- Server login
	 * @param password			- Server password
	 * @param httpClient		- HttpClient used for the connection
	 * @param verbose			- if true, trace all server interactions
	 * @return					- HttpResponse
	 * 
	 * @throws IOException
	 * @throws InvalidCredentialsException
	 * @throws CloneNotSupportedException 
	 */
	public static HttpResponse sendGetForSecureDocument(String serverURI, HttpGet request, String login, String password, HttpClient httpClient)
			throws Exception {
		//CustomLogger.logMessage("Started execution of sendGetForSecureDocument method");
		// Step (1): Request the protected resource
		HttpResponse documentResponse = httpClient.execute(request);
		//CustomLogger.logMessage("Requested the protected resource at line 146");
		try {
		if (documentResponse.getStatusLine().getStatusCode() == 200) {
			//CustomLogger.logMessage("Document response status code is 200");
			Header header = documentResponse.getFirstHeader(AUTHREQUIRED);
			//CustomLogger.logMessage("Get the header from document response for authentication");
			if ((header!=null) && ("authrequired".equals(header.getValue()))) {
				//CustomLogger.logMessage("Authentication required");
				documentResponse.getEntity().consumeContent();
				//CustomLogger.logMessage("Get entiry of consumer content");
				// The server requires an authentication: Create the login form
				HttpPost formPost = new HttpPost(serverURI+"/j_security_check");
				//CustomLogger.logMessage("Preparing form post body");
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				nvps.add(new BasicNameValuePair("j_username", login));
				nvps.add(new BasicNameValuePair("j_password", password));
				//CustomLogger.logMessage("Username and password set to nvps");
				formPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
				//CustomLogger.logMessage("URL encoded entity set for post call");
				// Step (2): The client submits the login form
				HttpResponse formResponse = httpClient.execute(formPost);
				//CustomLogger.logMessage("Called http client's execute method");
				header = formResponse.getFirstHeader(AUTHREQUIRED);
				//CustomLogger.logMessage("Get the first header from response");
				if ((header!=null) && ("authfailed".equals(header.getValue()))) {
					//CustomLogger.logMessage("Authentication failed");
					// The login failed
					throw new InvalidCredentialsException("Authentication failed");
				} else {
					//CustomLogger.logMessage("In else block");
					formResponse.getEntity().consumeContent();
					//CustomLogger.logMessage("Get consume content from the response entiry");
					// The login succeed
					// Step (3): Request again the protected resource
					HttpGet documentGet2;
						documentGet2 = (HttpGet)(request.clone());
						//CustomLogger.logMessage("Request again the protected resource");
						return httpClient.execute(documentGet2);
				}
			}
		}
		} catch (Exception e) {
			CustomLogger.logException(e);
			//CustomLogger.logError("Exception in sendGetForSecureDocument method"+e.getMessage());
			throw e;
		}
		return  documentResponse;
	}
	
	public static HttpResponse sendPutForSecureDocument(String serverURI, HttpPut request, String login, String password, HttpClient httpClient)
			throws Exception {
		//CustomLogger.logMessage("Started execution of sendGetForSecureDocument method");
		// Step (1): Request the protected resource
		HttpResponse documentResponse = httpClient.execute(request);
		//CustomLogger.logMessage("Requested the protected resource at line 146");
		try {
		if (documentResponse.getStatusLine().getStatusCode() == 200) {
			//CustomLogger.logMessage("Document response status code is 200");
			Header header = documentResponse.getFirstHeader(AUTHREQUIRED);
			//CustomLogger.logMessage("Get the header from document response for authentication");
			if ((header!=null) && ("authrequired".equals(header.getValue()))) {
				//CustomLogger.logMessage("Authentication required");
				documentResponse.getEntity().consumeContent();
				//CustomLogger.logMessage("Get entiry of consumer content");
				// The server requires an authentication: Create the login form
				HttpPost formPost = new HttpPost(serverURI+"/j_security_check");
				//CustomLogger.logMessage("Preparing form post body");
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				nvps.add(new BasicNameValuePair("j_username", login));
				nvps.add(new BasicNameValuePair("j_password", password));
				//CustomLogger.logMessage("Username and password set to nvps");
				formPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
				//CustomLogger.logMessage("URL encoded entity set for post call");
				// Step (2): The client submits the login form
				HttpResponse formResponse = httpClient.execute(formPost);
				//CustomLogger.logMessage("Called http client's execute method");
				header = formResponse.getFirstHeader(AUTHREQUIRED);
				//CustomLogger.logMessage("Get the first header from response");
				if ((header!=null) && ("authfailed".equals(header.getValue()))) {
					//CustomLogger.logMessage("Authentication failed");
					// The login failed
					throw new InvalidCredentialsException("Authentication failed");
				} else {
					//CustomLogger.logMessage("In else block");
					formResponse.getEntity().consumeContent();
					//CustomLogger.logMessage("Get consume content from the response entiry");
					// The login succeed
					// Step (3): Request again the protected resource
					HttpGet documentGet2;
						documentGet2 = (HttpGet)(request.clone());
						//CustomLogger.logMessage("Request again the protected resource");
						return httpClient.execute(documentGet2);
				}
			}
		}
		} catch (Exception e) {
			CustomLogger.logException(e);
			//CustomLogger.logError("Exception in sendGetForSecureDocument method"+e.getMessage());
			throw e;
		}
		return  documentResponse;
	}
	
	public static HttpResponse sendPostForSecureDocument(String serverURI, HttpPost request, String login, String password, HttpClient httpClient)
			throws Exception {
		//CustomLogger.logMessage("Started execution of sendGetForSecureDocument method");
		// Step (1): Request the protected resource
		HttpResponse documentResponse = httpClient.execute(request);
		//CustomLogger.logMessage("Requested the protected resource at line 146");
		try {
		if (documentResponse.getStatusLine().getStatusCode() == 200) {
			//CustomLogger.logMessage("Document response status code is 200");
			Header header = documentResponse.getFirstHeader(AUTHREQUIRED);
			//CustomLogger.logMessage("Get the header from document response for authentication");
			if ((header!=null) && ("authrequired".equals(header.getValue()))) {
				//CustomLogger.logMessage("Authentication required");
				documentResponse.getEntity().consumeContent();
				//CustomLogger.logMessage("Get entiry of consumer content");
				// The server requires an authentication: Create the login form
				HttpPost formPost = new HttpPost(serverURI+"/j_security_check");
				//CustomLogger.logMessage("Preparing form post body");
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				nvps.add(new BasicNameValuePair("j_username", login));
				nvps.add(new BasicNameValuePair("j_password", password));
				//CustomLogger.logMessage("Username and password set to nvps");
				formPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
				//CustomLogger.logMessage("URL encoded entity set for post call");
				// Step (2): The client submits the login form
				HttpResponse formResponse = httpClient.execute(formPost);
				//CustomLogger.logMessage("Called http client's execute method");
				header = formResponse.getFirstHeader(AUTHREQUIRED);
				//CustomLogger.logMessage("Get the first header from response");
				if ((header!=null) && ("authfailed".equals(header.getValue()))) {
					//CustomLogger.logMessage("Authentication failed");
					// The login failed
					throw new InvalidCredentialsException("Authentication failed");
				} else {
					//CustomLogger.logMessage("In else block");
					formResponse.getEntity().consumeContent();
					//CustomLogger.logMessage("Get consume content from the response entiry");
					// The login succeed
					// Step (3): Request again the protected resource
					HttpGet documentGet2;
						documentGet2 = (HttpGet)(request.clone());
						//CustomLogger.logMessage("Request again the protected resource");
						return httpClient.execute(documentGet2);
				}
			}
		}
		} catch (Exception e) {
			CustomLogger.logException(e);
			//CustomLogger.logError("Exception in sendGetForSecureDocument method"+e.getMessage());
			throw e;
		}
		return  documentResponse;
	}
	
	public static boolean doAuth(String login, String password, HttpClient httpClient, String jtsURI)
			throws IOException, Exception {
		//CustomLogger.logMessage("In doAuth method");
		HttpPost formPost = new HttpPost(jtsURI + "/j_security_check");
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("j_username", login));
		nvps.add(new BasicNameValuePair("j_password", password));
		formPost.setEntity(new UrlEncodedFormEntity(nvps, StandardCharsets.UTF_8));

		// Step (2): The client submits the login form

		// Step (2): The client submits the login form
		HttpResponse formResponse = httpClient.execute(formPost);
		formResponse.getEntity().consumeContent();
		int formSc = formResponse.getStatusLine().getStatusCode();
		if (formSc == 200 || formSc == 302) {
			Header header = formResponse.getFirstHeader(HttpUtils.FORM_AUTH_HEADER);
			String redirectURI = formResponse.getFirstHeader("Location").getValue();
			if ((header != null) && (header.getValue().equals(FORM_AUTH_FAILED_MSG))) {
				// The login failed
				throw new InvalidCredentialsException("Authentication failed");
			} else if (formSc == 302 && redirectURI.contains(HttpUtils.FORM_AUTH_FAILED_URI)) {
				//CustomLogger.logMessage("Authentication failed");
				throw new InvalidCredentialsException("Authentication failed");
			} else {
				// The login succeed
				try {
					formPost.setURI(new URI(redirectURI));
					formResponse = httpClient.execute(formPost);

				} catch (URISyntaxException e) {
					CustomLogger.logException(e);
				}
				// Step (3): Request again the protected resource
				formResponse.getEntity().consumeContent();
				//CustomLogger.logMessage("formResponse"+formResponse.getStatusLine());
				return true;
			}
		} else {
			throw new Exception("Authentication failed");
		}
	}
}
