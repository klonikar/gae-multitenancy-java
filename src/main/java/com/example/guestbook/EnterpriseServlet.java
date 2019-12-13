/*
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//[START all]

package com.example.guestbook;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.utils.SystemProperty;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static com.example.guestbook.Persistence.getDatastore;

//[START all]
import javax.servlet.annotation.WebServlet;

@WebServlet(name = "enterprise", urlPatterns={"/api/v1/enterprise/*"} /*, loadOnStartup=1*/)
public class EnterpriseServlet extends HttpServlet {

  // Process the HTTP POST of the form
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser(); // Find out who the user is.

    HttpSession session = req.getSession(true);
    String host = req.getHeader("Host");
    String serverName = req.getServerName();
    String pathInfo = req.getPathInfo();
    resp.setContentType("application/json");
    PrintWriter writer = resp.getWriter();
    // TODO: Instead of hardcoding, put the global admin server name and global admin user names in datastore
    //if(! serverName.equals("payroll1.appspot.com") || user == null || !user.getEmail().equals("lonikar@gmail.com")) {
    if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
       // Production
      if(! serverName.equals("payroll1.appspot.com") ) {
        // This request allowed only for global admin service
        resp.setStatus(401);
        writer.append("{\"message\": \"Unauthorized global admin access\"}");
        writer.flush();
        
        return;
      }
     } else {
      // Local development server
      // which is: SystemProperty.Environment.Value.Development
    }

    String reqData = Utils.loadRequestData(req);
    //convert json string to object
    //Map<String, String> jsonMap = Utils.objectMapper.readValue(reqData, new TypeReference<Map<String, String>>() {} );
    //String content = jsonMap.get("content");
    //session.setAttribute("userName", user.getEmail());
    Employee emp = Utils.objectMapper.readValue(reqData, Employee.class);
    emp.save(emp.getFirstName());
    resp.setStatus(200);
    writer.append(emp.toString());
    writer.flush();
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    //UserService userService = UserServiceFactory.getUserService();
    //User user = userService.getCurrentUser();

    resp.setStatus(200);
    resp.setContentType("application/json");
    PrintWriter writer = resp.getWriter();
    writer.append(" \n");
 
    writer.flush();
  }

}
//[END all]
