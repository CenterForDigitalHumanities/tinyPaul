/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.rerum.crud;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletResponse;
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
    
    public static String DUNBAR_APP_CLAIM = "http://dunbar.rerum.io/app_flag";
    
    //https://stackoverflow.com/questions/2395737/java-relative-path-of-a-file-in-a-java-web-application
    public static String PROPERTIES_FILE_NAME = "paul.properties";
    
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
            System.out.println("Send user out");
            user = JSONObject.fromObject(sb.toString());
            System.out.println(user);
            } 
            catch (java.net.SocketTimeoutException e) { // This specifically catches the timeout
                System.out.println("The Auth0 userinfo endpoint is taking too long...");
                System.out.println(e);
            } 
            catch (IOException ex) {
                System.out.println("The Auth0 userinfo endpoint failed...");
                System.out.println(ex.getCause());
                System.out.println(ex.getMessage());
                throw ex;
            }
        return user;
    }
}
