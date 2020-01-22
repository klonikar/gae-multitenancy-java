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

@WebServlet(name = "employee", urlPatterns={"/api/v1/employee/*"} /*, loadOnStartup=2*/)
public class EmployeeServlet extends HttpServlet {

  // Process the HTTP POST of the form
  // Test with json payload: {"userName": "f1m1l1", "password": "abcdefgh", "firstName":"F1", "middleName":"M1", "lastName": "L1", "employeeCode":"GA", "salutation": -2}
  // Test with json payload: {"admin": true, "userName": "f1m1l1Admin", "password": "abcdefgh", "firstName":"F1", "middleName":"M1", "lastName": "L1", "employeeCode":"GA", "salutation": -2}

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
    String companyName = (String) session.getAttribute("companyName");

    if(!"true".equals(session.getAttribute("EnterpriseAdmin")) ) {
        // This request allowed only for enterprise admin service
        resp.setStatus(401);
        writer.append("{\"message\": \"Unauthorized enterprise admin access\"}");
        writer.flush();
        
        return;
    }

    String reqData = Utils.loadRequestData(req);
    //convert json string to object
    //Map<String, String> jsonMap = Utils.objectMapper.readValue(reqData, new TypeReference<Map<String, String>>() {} );
    //String content = jsonMap.get("content");
    //session.setAttribute("userName", user.getEmail());
    Employee emp = Utils.objectMapper.readValue(reqData, Employee.class).setCreatedDate(Timestamp.now()).setCompanyName(companyName);
    if(emp.isAdmin()) {
        EnterpriseAdmin admin = Utils.objectMapper.readValue(reqData, EnterpriseAdmin.class).setCreatedDate(Timestamp.now()).setCompanyName(companyName);
        admin.save(companyName); // Save to namespace defined by company name
    }
    emp.save(companyName); // Save to namespace defined by company name

    resp.setStatus(200);
    writer.append(emp.toString());
    writer.flush();
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    //UserService userService = UserServiceFactory.getUserService();
    //User user = userService.getCurrentUser();

    HttpSession session = req.getSession(true);
    String uri = req.getRequestURI();
    String hostname = req.getServerName();
    resp.setStatus(200);
    resp.setContentType("application/json");
    PrintWriter writer = resp.getWriter();
    String namespace = (String) session.getAttribute("companyName");

    if(uri.endsWith("/api/v1/employee") || uri.endsWith("/api/v1/employee/")) { // Get all
        EntityQuery query = null;
        if("true".equals(session.getAttribute("EnterpriseAdmin")) ) { // Get all for EnterpriseAdmin
            query = Query.newEntityQueryBuilder().setNamespace(namespace).setKind("Employee")
                            .build();
        }
        else { // Get current user for non-EnterpriseAdmin users
            String userNameFilter = (String) session.getAttribute("userName");
            query = Query.newEntityQueryBuilder().setNamespace(namespace).setKind("Employee")
                            .setFilter(PropertyFilter.eq("userName", userNameFilter))
                            .build();
        }

        QueryResults<Entity> results = getDatastore().run(query);
        List<Employee> entities = new ArrayList<>();
        while (results.hasNext()) {
            Employee result = new Employee(results.next()).setPassword("");
            entities.add(result);
        }
        System.out.println("Retrieved entities: " + entities.size());
        Utils.objectMapper.writeValue(writer, entities);
    }
    else {
        String id = uri.substring(uri.lastIndexOf("/")+1);
        Employee obj = null;
        Entity entObj = null;
        try {
            Long keyVal = Long.valueOf(id);
            entObj = getDatastore().get(getKeyFactoryWithNamespace(Employee.class.getSimpleName(), namespace).newKey(keyVal));
        }
        catch(Exception ex) {
            entObj = getDatastore().get(getKeyFactoryWithNamespace(Employee.class.getSimpleName(), namespace).newKey(id));
        }
        // For non-EnterpriseAdmin users, Verify that the user id is same as currently logged in user id.
        if(entObj != null && ("true".equals(session.getAttribute("EnterpriseAdmin")) || entObj.contains("userName") && entObj.getString("userName").equals(session.getAttribute("userName")))) {
            obj = new Employee(entObj).setPassword("");
            writer.append(obj.toString());
        }
        else {
            resp.setStatus(404);
            writer.append("{\"error\":\"no object obtained for key: " + id + "\"}");
        }

    }

    writer.flush();
  }

}
//[END all]
