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
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.EntityQuery;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.cloud.Timestamp;
import com.google.appengine.api.utils.SystemProperty;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static com.example.guestbook.Persistence.getKeyFactory;
import static com.example.guestbook.Persistence.getKeyFactoryWithNamespace;
import static com.example.guestbook.Persistence.getDatastore;

//[START all]
import javax.servlet.annotation.WebServlet;

@WebServlet(name = "logout", urlPatterns={"/api/v1/logout/*"} /*, loadOnStartup=1*/)
public class LogoutServlet extends HttpServlet {

  // Process the HTTP GET/POST of the form
  // Test with json payload: None required
  // To create a globalAdmin in test environment, use URL http://localhost:8080/api/v1/enterprise/?globalAdmin=true
  // Set headers: Content-Type: application/json and if testing locally, ServerName: GLOBAL_ADMIN_SERVER_NAME property in appengine-web.xml.
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser(); // Find out who the user is.

    HttpSession session = req.getSession(true);
    String host = req.getHeader("Host");
    String serverName = Utils.getServerName(req);
    String pathInfo = req.getPathInfo();
    resp.setContentType("application/json");
    PrintWriter writer = resp.getWriter();

    if(serverName == null || ! serverName.equals(System.getProperty("GLOBAL_ADMIN_SERVER_NAME")) ) {
        // This request allowed only for global admin service
        resp.setStatus(401);
        writer.append("{\"message\": \"Unauthorized global admin access\"}");
        writer.flush();
        
        return;
    }
    writer.append("{\"message\": \"User " + session.getAttribute("userName") + " logged out.\"}");
    session.setAttribute("userName", "");
    session.setAttribute("globalAdmin", "false");
    session.setAttribute("EnterpriseAdmin", "false");
    session.setAttribute("companyName", "");

    resp.setStatus(200);
    writer.flush();
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    doPost(req, resp);
  }

}
//[END all]
