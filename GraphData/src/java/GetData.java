/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author jasleen
 */
public class GetData extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    Statement statement;
    ResultSet resultSet;
    static Connection connect;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.addHeader("Access-Control-Allow-Origin", "*");
        try {
            PrintWriter out = response.getWriter();
            connect = getConnection();
            statement = connect.createStatement();
            resultSet = statement.executeQuery("select max(time) from lab3.logs where machine='Ubuntu2'");
            String machine = (request.getParameter("machine"));
            int i = Integer.parseInt(request.getParameter("resource"));
            long date = Long.parseLong(request.getParameter("timestamp"));
            // out.println(date);
            java.sql.Timestamp timestamp = new Timestamp(date);
            if (machine.contains("Host")) {
                //System.out.println("here");
                switch (i) {
                    case 1: {
                        JSONArray read = new JSONArray();
                        JSONArray write = new JSONArray();
                        resultSet = statement.executeQuery("SELECT * FROM lab3.logs where time > '" + timestamp + "' and machine like '"+machine+"' and (logs.key like 'storageAdapter.write.average' OR logs.key like 'storageAdapter.read.average') ;");
                        while (resultSet.next()) {
                            String key = resultSet.getString("key");
                            String value = resultSet.getString("value");
                            Timestamp time = resultSet.getTimestamp("time");                            
                            JSONObject ob = new JSONObject();
                            ob.put("y", Float.parseFloat(value));
                            ob.put("x", time.getTime());                 
                            if (key.equalsIgnoreCase("storageAdapter.write.average")) {
                                write.put(ob);
                            } else {
                                read.put(ob);
                            }
                        }
                        JSONObject res = new JSONObject();
                        res.put("read", read);
                        res.put("write", write);
                        out.print(res);
                        break;
                    }
                    case 2: {
                        JSONArray zipped = new JSONArray();
                        JSONArray swapped = new JSONArray();
                        JSONArray granted = new JSONArray();
                        resultSet = statement.executeQuery("SELECT * FROM lab3.logs where time > '" + timestamp + "' and machine like '"+machine+"' and (logs.key like 'mem.zipSaved.latest' OR logs.key like 'mem.swapped.average' Or logs.key like 'mem.granted.average' ) ");
                        while (resultSet.next()) {
                            String key = resultSet.getString("key");
                            String value = resultSet.getString("value");
                            Timestamp time = resultSet.getTimestamp("time");                            
                            JSONObject ob = new JSONObject();
                            ob.put("y", Float.parseFloat(value));
                            ob.put("x", time.getTime());                 
                            if (key.equalsIgnoreCase("mem.zipSaved.latest")) {
                                zipped.put(ob);
                            } else if (key.equalsIgnoreCase("mem.swapped.average")) {
                                swapped.put(ob);
                            } else {
                                granted.put(ob);
                            }
                        }
                        JSONObject res = new JSONObject();
                        res.put("zipSaved", zipped);
                        res.put("swapped", swapped);
                        res.put("granted", granted);
                        out.print(res);
                        break;
                    }case 3: {
                        JSONArray cpu = new JSONArray();
                        resultSet = statement.executeQuery("SELECT * FROM lab3.logs where time > '" + timestamp + "' and machine like '"+machine+"' and logs.key like 'cpu.usage.average' ");
                        while (resultSet.next()) {
                            String key = resultSet.getString("key");
                            String value = resultSet.getString("value");
                            Timestamp time = resultSet.getTimestamp("time");                            
                            JSONObject ob = new JSONObject();
                            ob.put("y", Float.parseFloat(value));
                            ob.put("x", time.getTime());                 
                           cpu.put(ob);
                        }
                        JSONObject res = new JSONObject();
                        res.put("cpu", cpu);
                        
                        out.print(res);
                        break;
                    }
                    case 4: {
                        JSONArray cpu = new JSONArray();
                        resultSet = statement.executeQuery("SELECT * FROM lab3.logs where time > '" + timestamp + "' and machine like '"+machine+"' and logs.key like 'net.transmitted.average' ");
                        while (resultSet.next()) {
                            String key = resultSet.getString("key");
                            String value = resultSet.getString("value");
                            Timestamp time = resultSet.getTimestamp("time");                            
                            JSONObject ob = new JSONObject();
                            ob.put("y", Float.parseFloat(value));
                            ob.put("x", time.getTime());                 
                           cpu.put(ob);
                        }
                        JSONObject res = new JSONObject();
                        res.put("net", cpu);
                        
                        out.print(res);
                        break;
                    }
                    case 5: {
                        JSONArray cpu = new JSONArray();
                        resultSet = statement.executeQuery("SELECT * FROM lab3.logs where time > '" + timestamp + "' and machine like '"+machine+"' and logs.key like 'sys.resourceFdUsage.latest' ");
                        while (resultSet.next()) {
                            String key = resultSet.getString("key");
                            String value = resultSet.getString("value");
                            Timestamp time = resultSet.getTimestamp("time");                            
                            JSONObject ob = new JSONObject();
                            ob.put("y", Float.parseFloat(value));
                            ob.put("x", time.getTime());                 
                           cpu.put(ob);
                        }
                        JSONObject res = new JSONObject();
                        res.put("sys", cpu);
                        
                        out.print(res);
                        break;
                    }
                    
                
                   
                }
            
        

            } else {
                    switch (i) {
                    case 1: {
                        JSONArray read = new JSONArray();
                        JSONArray write = new JSONArray();
                        resultSet = statement.executeQuery("SELECT * FROM lab3.logs where time > '" + timestamp + "' and machine like '"+machine+"' and (logs.key like 'virtualDisk.write.average' OR logs.key like 'virtualDisk.read.average') ;");
                        while (resultSet.next()) {
                            String key = resultSet.getString("key");
                            String value = resultSet.getString("value");
                            Timestamp time = resultSet.getTimestamp("time");                            
                            JSONObject ob = new JSONObject();
                            ob.put("y", Float.parseFloat(value));
                            ob.put("x", time.getTime());                 
                            if (key.equalsIgnoreCase("storageAdapter.write.average")) {
                                write.put(ob);
                            } else {
                                read.put(ob);
                            }
                        }
                        JSONObject res = new JSONObject();
                        res.put("read", read);
                        res.put("write", write);
                        out.print(res);
                        break;
                    }
                    case 2: {
                        JSONArray zipped = new JSONArray();
                        JSONArray swapped = new JSONArray();
                        JSONArray granted = new JSONArray();
                        resultSet = statement.executeQuery("SELECT * FROM lab3.logs where time > '" + timestamp + "' and machine like '"+machine+"' and (logs.key like 'mem.zipSaved.latest' OR logs.key like 'mem.swapped.average' Or logs.key like 'mem.granted.average' ) ");
                        while (resultSet.next()) {
                            String key = resultSet.getString("key");
                            String value = resultSet.getString("value");
                            Timestamp time = resultSet.getTimestamp("time");                            
                            JSONObject ob = new JSONObject();
                            ob.put("y", Float.parseFloat(value));
                            ob.put("x", time.getTime());                 
                            if (key.equalsIgnoreCase("mem.zipSaved.latest")) {
                                zipped.put(ob);
                            } else if (key.equalsIgnoreCase("mem.swapped.average")) {
                                swapped.put(ob);
                            } else {
                                granted.put(ob);
                            }
                        }
                        JSONObject res = new JSONObject();
                        res.put("zipSaved", zipped);
                        res.put("swapped", swapped);
                        res.put("granted", granted);
                        out.print(res);
                        break;
                    }case 3: {
                        JSONArray cpu = new JSONArray();
                        resultSet = statement.executeQuery("SELECT * FROM lab3.logs where time > '" + timestamp + "' and machine like '"+machine+"' and logs.key like 'cpu.usage.average' ");
                        while (resultSet.next()) {
                            String key = resultSet.getString("key");
                            String value = resultSet.getString("value");
                            Timestamp time = resultSet.getTimestamp("time");                            
                            JSONObject ob = new JSONObject();
                            ob.put("y", Float.parseFloat(value));
                            ob.put("x", time.getTime());                 
                           cpu.put(ob);
                        }
                        JSONObject res = new JSONObject();
                        res.put("cpu", cpu);
                        
                        out.print(res);
                        break;
                    }
                    case 4: {
                        JSONArray cpu = new JSONArray();
                        resultSet = statement.executeQuery("SELECT * FROM lab3.logs where time > '" + timestamp + "' and machine like '"+machine+"' and logs.key like 'net.transmitted.average' ");
                        while (resultSet.next()) {
                            String key = resultSet.getString("key");
                            String value = resultSet.getString("value");
                            Timestamp time = resultSet.getTimestamp("time");                            
                            JSONObject ob = new JSONObject();
                            ob.put("y", Float.parseFloat(value));
                            ob.put("x", time.getTime());                 
                           cpu.put(ob);
                        }
                        JSONObject res = new JSONObject();
                        res.put("net", cpu);
                        
                        out.print(res);
                        break;
                    }
                    case 5: {
                        JSONArray cpu = new JSONArray();
                        resultSet = statement.executeQuery("SELECT * FROM lab3.logs where time > '" + timestamp + "' and machine like '"+machine+"' and logs.key like 'sys.heartbeat.latest' ");
                        while (resultSet.next()) {
                            String key = resultSet.getString("key");
                            String value = resultSet.getString("value");
                            Timestamp time = resultSet.getTimestamp("time");
                            JSONObject ob = new JSONObject();
                            ob.put("y", Float.parseFloat(value));
                            ob.put("x", time.getTime());
                           cpu.put(ob);
                        }
                        JSONObject res = new JSONObject();
                        res.put("sys", cpu);
                        
                        out.print(res);
                        break;
                    }
                    
                
                   
                }
            
            }
    }
    catch (Exception e

    
        ) {
            System.out.println(e);
    }
}

Connection getConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            // setup the connection with the DB.
            connect = DriverManager.getConnection("jdbc:mysql://localhost/lab3?"
                    + "user=root&password=root");
        } catch (Exception e) {
            //System.out.println(e);
        }
        return connect;
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
