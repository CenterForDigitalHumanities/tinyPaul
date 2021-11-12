/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package io.rerum.proxy;

import io.rerum.crud.Constant;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author bhaberbe
 */
public class UDproxy extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String url = request.getParameter("url");
        int codeOverwrite = 500;
        String line;
        StringBuilder sb = new StringBuilder();
        System.out.println("Tiny Paul DLA proxy.  Trying the following url");
        System.out.println(url);;
        if(null!=url && !url.equals("")){
            URL postUrl = new URL(request.getParameter("url"));
            HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type", "application/xml");
            //Connect to udel URL
            connection.connect();
            System.out.println("Connected to DLA");
            try{
                codeOverwrite = connection.getResponseCode();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
                while ((line = reader.readLine()) != null){
                    //Gather UDEL response
                    sb.append(line);
                }
                reader.close();
                //Bring all the headers from the response
                for (Map.Entry<String, List<String>> entries : connection.getHeaderFields().entrySet()) {
                        String values = "";
                        String removeBraks = entries.getValue().toString();
                        values = removeBraks.substring(1, removeBraks.length() -1);
                        if(null != entries.getKey() && !entries.getKey().equals("Transfer-Encoding")){
                            response.setHeader(entries.getKey(), values);
                        }
                    }
            }
            catch(IOException ex){
                //Need to get the response RERUM sent back.
                BufferedReader error = new BufferedReader(new InputStreamReader(connection.getErrorStream(),"utf-8"));
                String errorLine = "";
                while ((errorLine = error.readLine()) != null){  
                    sb.append(errorLine);
                } 
                System.out.println("udel error says");
                System.out.println(sb.toString());
                error.close();
            }
            connection.disconnect();
            response.setHeader("Access-Control-Allow-Origin", "*"); //To use this as an API, it must contain CORS headers
            response.setHeader("Access-Control-Expose-Headers", "*"); //Headers are restricted, unless you explicitly expose them.  Darn Browsers.
            response.setStatus(codeOverwrite);
            response.setHeader("Content-Type", "application/json; charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print(sb.toString());
        }
        else{
            response.setStatus(400);
            response.getWriter().print("You did not provide a URL in the ?url URL parameter");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
