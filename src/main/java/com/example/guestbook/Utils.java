package com.example.guestbook;

import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.google.appengine.api.utils.SystemProperty;

public class Utils {
  //create ObjectMapper instance
  public static ObjectMapper objectMapper = new ObjectMapper();
  static {
      //configure Object mapper for pretty print
      objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  }

  public static String loadRequestData(HttpServletRequest req) throws IOException {
    StringBuilder sb = new StringBuilder();
    if(req.getContentType().equals("application/json")) {
      BufferedReader br = req.getReader();
      String str;
      while( (str = br.readLine()) != null ){
        sb.append(str);
      }
      // JSONObject jObj = new JSONObject(sb.toString());
    }
    return sb.toString();
  }

  public static String getServerName(HttpServletRequest req) {
    String ret = SystemProperty.environment.value() == SystemProperty.Environment.Value.Production ? req.getServerName() : req.getHeader("ServerName");
    return ret;
  }
}

