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

@WebServlet(name = "login", urlPatterns={"/api/v1/login/*"} /*, loadOnStartup=1*/)
public class LoginServlet extends HttpServlet {

  // Process the HTTP POST of the form
  // Test with json payload: 
  // Ordinary User: {"userName": "u1", "password":"abcdefgh", "companyName": "c2"}
  // GlobalAdmin ==> {"userName": "globalAdmin", "password":"abcdefgh", "admin": true}
  // EnterPriseAdmin ==> {"userName": "u1", "password":"abcdefgh", "companyName": "c2", "admin": true}
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

    String reqData = Utils.loadRequestData(req);
    //convert json string to object
    //Map<String, String> jsonMap = Utils.objectMapper.readValue(reqData, new TypeReference<Map<String, String>>() {} );
    //String content = jsonMap.get("content");
    //session.setAttribute("userName", user.getEmail());
    Employee emp = Utils.objectMapper.readValue(reqData, Employee.class);
    if(emp.getCompanyName() == null && emp.isAdmin()) { // Global Admin
        EntityQuery query = Query.newEntityQueryBuilder().setNamespace("").setKind("GlobalAdmin")
                             .setFilter(PropertyFilter.eq("userName", emp.getUserName()))
                            .build();
        QueryResults<Entity> results = getDatastore().run(query);
        List<Enterprise> entities = new ArrayList<>();
        boolean foundAndPermitted = false;
        while (results.hasNext()) {
            Employee result = new Employee(results.next());
            if(result.getPassword().equals(emp.getPassword())) {
                foundAndPermitted = true;
                emp = result;
                break;
            }
        }
        if(foundAndPermitted) {
            session.setAttribute("userName", emp.getUserName());
            session.setAttribute("globalAdmin", "true");
            session.setAttribute("EnterpriseAdmin", "false");
            session.setAttribute("companyName", "");
            Utils.objectMapper.writeValue(writer, emp.setPassword(""));
            resp.setStatus(200);
        }
        else {
            resp.setStatus(404);
            writer.append("{\"message\": \"Invalid userName or password\"}");
        }
    }
    else if(emp.isAdmin()) { // EnterpriseAdmin
        EntityQuery query = Query.newEntityQueryBuilder().setNamespace(emp.getCompanyName()).setKind("EnterpriseAdmin")
                             .setFilter(PropertyFilter.eq("userName", emp.getUserName()))
                            .build();
        QueryResults<Entity> results = getDatastore().run(query);
        List<Enterprise> entities = new ArrayList<>();
        boolean foundAndPermitted = false;
        while (results.hasNext()) {
            Employee result = new Employee(results.next());
            if(result.getPassword().equals(emp.getPassword())) {
                foundAndPermitted = true;
                emp = result;
                break;
            }
        }
        if(foundAndPermitted) {
            session.setAttribute("userName", emp.getUserName());
            session.setAttribute("globalAdmin", "false");
            session.setAttribute("EnterpriseAdmin", "true");
            session.setAttribute("companyName", emp.getCompanyName());
            Utils.objectMapper.writeValue(writer, emp.setPassword(""));
            resp.setStatus(200);
        }
        else {
            resp.setStatus(404);
            writer.append("{\"message\": \"Invalid userName or password\"}");
        }
    }
    else if(emp.getCompanyName() != null && ! emp.isAdmin()) { // Ordinary user
        EntityQuery query = Query.newEntityQueryBuilder().setNamespace(emp.getCompanyName()).setKind("Employee")
                             .setFilter(PropertyFilter.eq("userName", emp.getUserName()))
                            .build();
        QueryResults<Entity> results = getDatastore().run(query);
        List<Enterprise> entities = new ArrayList<>();
        boolean foundAndPermitted = false;
        while (results.hasNext()) {
            Employee result = new Employee(results.next());
            if(result.getPassword().equals(emp.getPassword())) {
                foundAndPermitted = true;
                emp = result;
                break;
            }
        }
        if(foundAndPermitted) {
            session.setAttribute("userName", emp.getUserName());
            session.setAttribute("globalAdmin", "false");
            session.setAttribute("EnterpriseAdmin", "false");
            session.setAttribute("companyName", emp.getCompanyName());
            Utils.objectMapper.writeValue(writer, emp.setPassword(""));
            resp.setStatus(200);
        }
        else {
            session.setAttribute("userName", "");
            session.setAttribute("globalAdmin", "false");
            session.setAttribute("EnterpriseAdmin", "false");
            session.setAttribute("companyName", "");
            resp.setStatus(404);
            writer.append("{\"message\": \"Invalid userName or password\"}");
        }
    }
    else { // invalid case... companyName == null && isAdmin == false
        resp.setStatus(404);
        writer.append("{\"message\": \"Company Name not spefified.\"}");
    }

    writer.flush();
  }

}
//[END all]
