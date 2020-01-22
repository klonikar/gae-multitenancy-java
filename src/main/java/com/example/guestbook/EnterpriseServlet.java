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

@WebServlet(name = "enterprise", urlPatterns={"/api/v1/enterprise/*"} /*, loadOnStartup=1*/)
public class EnterpriseServlet extends HttpServlet {

  // Process the HTTP POST of the form
  // Test with json payload: {"name": "c2", "address":"Bangalore", "userName": "enterpriseAdmin", "password": "abcdefgh", "firstName":"F", "middleName":"M", "lastName": "L", "employeeCode":"GA", "salutation": -1, "companyName": "c2", "companyId": 1234}
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

    if(!"true".equals(session.getAttribute("globalAdmin")) || serverName == null || ! serverName.equals(System.getProperty("GLOBAL_ADMIN_SERVER_NAME")) ) {
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
    Enterprise ent = Utils.objectMapper.readValue(reqData, Enterprise.class).setCreatedDate(Timestamp.now());
    ent.save(ent.getName()); // Save to namespace defined by company name
    ent.save(""); // Save to default namespace as well.
    EnterpriseAdmin admin = Utils.objectMapper.readValue(reqData, EnterpriseAdmin.class).setCreatedDate(Timestamp.now());
    admin.save(ent.getName());
    // Create Global Admin only for non-production environments.
    // For production environment, create GlobalAdmin entity using datastore console, entity management.
    if(SystemProperty.environment.value() != SystemProperty.Environment.Value.Production && req.getParameter("globalAdmin") != null && req.getParameter("globalAdmin").equals("true")) {
        admin.save("", "GlobalAdmin");
    }

    resp.setStatus(200);
    writer.append(ent.toString());
    writer.flush();
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    //UserService userService = UserServiceFactory.getUserService();
    //User user = userService.getCurrentUser();

    HttpSession session = req.getSession(true);
    String uri = req.getRequestURI();
    String hostname = req.getServerName();
    String serverName = Utils.getServerName(req);
    resp.setStatus(200);
    resp.setContentType("application/json");
    PrintWriter writer = resp.getWriter();

    if(!"true".equals(session.getAttribute("globalAdmin")) || serverName == null || ! serverName.equals(System.getProperty("GLOBAL_ADMIN_SERVER_NAME")) ) {
        // This request allowed only for global admin service
        resp.setStatus(401);
        writer.append("{\"message\": \"Unauthorized global admin access\"}");
        writer.flush();
        
        return;
    }

    if(uri.endsWith("/api/v1/enterprise") || uri.endsWith("/api/v1/enterprise/")) { // Get all
        EntityQuery query = Query.newEntityQueryBuilder().setNamespace("").setKind("Enterprise")
                            // .setFilter(PropertyFilter.eq(property, value))
                            .build();
        QueryResults<Entity> results = getDatastore().run(query);
        List<Enterprise> entities = new ArrayList<>();
        while (results.hasNext()) {
            Enterprise result = new Enterprise(results.next());
            entities.add(result);
        }
        System.out.println("Retrieved entities: " + entities.size());
        Utils.objectMapper.writeValue(writer, entities);
    }
    else {
        String id = uri.substring(uri.lastIndexOf("/")+1);
        Enterprise obj = null;
        Entity entObj = null;
        try {
            Long keyVal = Long.valueOf(id);
            entObj = getDatastore().get(getKeyFactoryWithNamespace(Enterprise.class.getSimpleName(), "").newKey(keyVal));
        }
        catch(Exception ex) {
            entObj = getDatastore().get(getKeyFactoryWithNamespace(Enterprise.class.getSimpleName(), "").newKey(id));
        }
        if(entObj != null) {
            obj = new Enterprise(entObj);
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
