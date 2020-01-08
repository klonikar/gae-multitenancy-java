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
  // TODO: remove companyName from json payload. Take it from EnterpriseAdmin login cookies/session
  // Test with json payload: {"companyName": "c2", "userName": "f1m1l1", "password": "abcdefgh", "firstName":"F1", "middleName":"M1", "lastName": "L1", "employeeCode":"GA", "salutation": -2}
  // Test with json payload: {"admin": true, companyName": "c2", "userName": "f1m1l1", "password": "abcdefgh", "firstName":"F1", "middleName":"M1", "lastName": "L1", "employeeCode":"GA", "salutation": -2}

  // Set headers: Content-Type: application/json and if testing locally, ServerName: GLOBAL_ADMIN_SERVER_NAME property in appengine-web.xml.
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser(); // Find out who the user is.

    HttpSession session = req.getSession(false);
    String host = req.getHeader("Host");
    String serverName = Utils.getServerName(req);
    String pathInfo = req.getPathInfo();
    resp.setContentType("application/json");
    PrintWriter writer = resp.getWriter();

    // TODO: Validate that invoking user is EnterpriseAdmin from login cookies/session
    String reqData = Utils.loadRequestData(req);
    //convert json string to object
    //Map<String, String> jsonMap = Utils.objectMapper.readValue(reqData, new TypeReference<Map<String, String>>() {} );
    //String content = jsonMap.get("content");
    //session.setAttribute("userName", user.getEmail());
    Employee emp = Utils.objectMapper.readValue(reqData, Employee.class).setCreatedDate(Timestamp.now());
    if(emp.isAdmin()) {
        EnterpriseAdmin admin = Utils.objectMapper.readValue(reqData, EnterpriseAdmin.class).setCreatedDate(Timestamp.now());
        admin.save(emp.getCompanyName());
    }
    emp.save(emp.getCompanyName()); // TODO: Take it from EnterpriseAdmin login cookies/session. Save to namespace defined by company name

    resp.setStatus(200);
    writer.append(emp.toString());
    writer.flush();
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    //UserService userService = UserServiceFactory.getUserService();
    //User user = userService.getCurrentUser();
    String uri = req.getRequestURI();
    String hostname = req.getServerName();
    resp.setStatus(200);
    resp.setContentType("application/json");
    PrintWriter writer = resp.getWriter();
    String namespace = req.getParameter("companyName"); // TODO: Get it from login cookies/session

    if(uri.endsWith("/api/v1/employee") || uri.endsWith("/api/v1/employee/")) { // Get all
        // TODO: Verify that the user is enterprise admin. If not, send only logged in user's data
        EntityQuery query = Query.newEntityQueryBuilder().setNamespace(namespace).setKind("Employee")
                            // .setFilter(PropertyFilter.eq(property, value))
                            .build();
        QueryResults<Entity> results = getDatastore().run(query);
        List<Employee> entities = new ArrayList<>();
        while (results.hasNext()) {
            Employee result = new Employee(results.next());
            entities.add(result);
        }
        System.out.println("Retrieved entities: " + entities.size());
        Utils.objectMapper.writeValue(writer, entities);
    }
    else {
        String id = uri.substring(uri.lastIndexOf("/")+1);
        // TODO: Verify that the user id is same as currently logged in user id.
        Employee obj = null;
        try {
            Long keyVal = Long.valueOf(id);
            Entity entObj = getDatastore().get(getKeyFactoryWithNamespace(Employee.class.getSimpleName(), namespace).newKey(keyVal));
            if(entObj != null) {
                obj = new Employee(entObj);
                writer.append(obj.toString());
            }
            else {
                resp.setStatus(404);
                writer.append("{\"error\":\"no object obtained for key: " + keyVal + "\"}");
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    writer.flush();
  }

}
//[END all]
