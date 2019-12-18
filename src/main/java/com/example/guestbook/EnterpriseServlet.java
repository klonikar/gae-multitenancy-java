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
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser(); // Find out who the user is.

    HttpSession session = req.getSession(false);
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
    Enterprise ent = Utils.objectMapper.readValue(reqData, Enterprise.class);
    ent.save(ent.getName()); // Save to namespace defined by company name
    ent.save(""); // Save to default namespace as well.
    resp.setStatus(200);
    writer.append(ent.toString());
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

    if(uri.endsWith("/api/v1/enterprise")) { // Get all
        EntityQuery query = Query.newEntityQueryBuilder().setNamespace("").setKind("Enterprise").build();
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
        try {
            Long keyVal = Long.valueOf(id);
            Entity entObj = getDatastore().get(getKeyFactoryWithNamespace(Enterprise.class, "").newKey(keyVal));
            if(entObj != null) {
                obj = new Enterprise(entObj);
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
