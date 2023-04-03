/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.rerum.crud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.List;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import net.sf.json.JSONObject;

/**
 *
 * @author bhaberbe
 */
public class Constant {
    //AWS   http://18.218.227.205:8080/rerum_server
    public static String RERUM_REGISTRATION_URL = "http://store.rerum.io/v1/";
    public static String RERUM_API_ADDR = "http://store.rerum.io/v1/api";
    //public static String RERUM_ID_PATTERN = "//store.rerum.io/v1/id";
    public static String RERUM_ACCESS_TOKEN_URL = "http://store.rerum.io/v1/api/accessToken.action";
    public static String RERUM_REFRESH_TOKEN_URL = "http://store.rerum.io/v1/api/refreshToken.action";
    
    public static String DUNBAR_APP_CLAIM = "http://rerum.io/app_flag";
    public static String DUNBAR_APP_ROLES_CLAIM = "http://rerum.io/user_roles";
    //https://stackoverflow.com/questions/2395737/java-relative-path-of-a-file-in-a-java-web-application
    public static String PROPERTIES_FILE_NAME = "paul.properties";

    private static String _ROLES = "roles";
    
    /**
     * The endpoints have the access token but need to know user info.
     * This reaches out to the Auth0 client and asks for the user info from a given valid access token.
     * @param token The access token from which to discern the user.
     * @return That user object or an empty object.
     */
    public static JSONObject userInfo(String token) throws IOException{
        String url = "https://cubap.auth0.com/userinfo"; //?access_token=token
        JSONObject user = new JSONObject();
        try {
            URL auth0 = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) auth0.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5 * 1000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer "+token);
            System.out.println("Make Auth0 Connection");
            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            System.out.println("Read Auth0 response");
            while ((line = reader.readLine()) != null) {
                // Gather rerum server v1 response
                sb.append(line);
            }
            reader.close();
            connection.disconnect();
            user = JSONObject.fromObject(sb.toString());
        } 
        catch (java.net.SocketTimeoutException e) { // This specifically catches the timeout
            System.out.println("The Auth0 userinfo endpoint is taking too long...");
            throw e;
        } 
        catch (IOException ex) {
            System.out.println("The Auth0 userinfo endpoint failed...");
            throw ex;
        }
        return user;
    }
    
    /**
     * For a given user, check that they have permission to CRUD.They must be one of the following roles
     * and must have "glossing" in their app list.
     *   dunbar_user_contributor
     *   dunbar_user_reviewer
     *   dunbar_user_curator
     * @param user - A JSONObject containing the app claim and the roles claim
     * @return boolean
     */
    public static boolean isPermitted(JSONObject user){
        return (user.has(Constant.DUNBAR_APP_CLAIM) && user.getJSONArray(Constant.DUNBAR_APP_CLAIM).contains("dla")
            && user.has(Constant.DUNBAR_APP_ROLES_CLAIM)
            && (user.getJSONObject(Constant.DUNBAR_APP_ROLES_CLAIM).getJSONArray(_ROLES).contains("dunbar_user_contributor")
            || user.getJSONObject(Constant.DUNBAR_APP_ROLES_CLAIM).getJSONArray(_ROLES).contains("dunbar_user_curator"))); 
    }

    /**
     * For a given user, check that they have permission to CRUD.They must be one of
     * the following roles
     * and must have "glossing" in their app list.
     * dunbar_user_contributor
     * dunbar_user_reviewer
     * dunbar_user_curator
     * 
     * @param token String access token with prefix stripped
     * @return boolean
     */
    public static boolean isPermitted(String token) {
        try {
            DecodedJWT decodedToken = JWT.decode(token);

            Claim appsClaim = decodedToken.getClaim(Constant.DUNBAR_APP_CLAIM);
            if (appsClaim.isNull())
                return false;

            Claim appRoles = decodedToken.getClaim(Constant.DUNBAR_APP_ROLES_CLAIM);
            List<String> gogList = (List<String>) appRoles.asMap().get(_ROLES);

            List<String> rolesList = Arrays.asList("dunbar_user_contributor", "dunbar_user_reviewer",
                    "dunbar_user_curator");
            Boolean hasRole = rolesList.stream().anyMatch(gogList::contains);

            // looks good, but let's verify it now
            return Boolean.TRUE.equals(hasRole) && verifyToken(token);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean verifyToken(String token) {
        JwkProvider provider = new UrlJwkProvider("https://cubap.auth0.com/");
        try {
            DecodedJWT decodedToken = JWT.decode(token);
			Jwk jwk = provider.get(decodedToken.getKeyId());

    	    Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);

            JWTVerifier verifier = JWT.require(algorithm).withIssuer("https://cubap.auth0.com/").build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            // bad signature
            e.printStackTrace();
        } catch (JwkException e) {
            // bad token
            e.printStackTrace();
        }
        return false;
    }

}
