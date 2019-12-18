// execute using: mvn exec:java -Dexec.mainClass="com.example.guestbook.EnterpriseAdmin" -Dexec.args="arg0 arg1 arg2" 
package com.example.guestbook;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Date;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.EntityQuery;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import static com.example.guestbook.Persistence.getDatastore;
import static com.example.guestbook.Persistence.getKeyFactory;
import static com.example.guestbook.Persistence.getKeyFactoryWithNamespace;
import static com.google.cloud.datastore.StructuredQuery.OrderBy.desc;
import static com.google.cloud.datastore.StructuredQuery.PropertyFilter.hasAncestor;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EnterpriseAdmin {
    private KeyFactory keyFactory;
    private Key key;
    private String firstName;
    private String middleName;
    private String lastName;
    private String employeeCode;
    private int salutation;
    private String companyName;
    private int companyId; 

    public EnterpriseAdmin() {
        keyFactory = getKeyFactory(getClass());
    }

    public EnterpriseAdmin(Entity entity) {
      keyFactory = entity.hasKey() ? getDatastore().newKeyFactory().setNamespace(entity.getKey().getNamespace()).setKind(this.getClass().getSimpleName()) : getKeyFactory(this.getClass());
      key = entity.hasKey() ? entity.getKey() : null;
      firstName = entity.contains("firstName") ? entity.getString("firstName") : null;
      middleName = entity.contains("middleName") ? entity.getString("middleName") : null;
      lastName = entity.contains("lastName") ? entity.getString("lastName") : null;
      employeeCode = entity.contains("employeeCode") ? entity.getString("employeeCode") : null;
      salutation = entity.contains("salutation") ? (int) entity.getLong("salutation") : -1;
      companyName = entity.contains("companyName") ? entity.getString("companyName") : null;
      companyId = entity.contains("companyId") ? (int) entity.getLong("companyId") : -1;

      // date = entity.contains("date") ? entity.getTimestamp("date").toSqlTimestamp() : null;
    }

    public void save(String namespace) {
      if (key == null) {
        keyFactory = getKeyFactoryWithNamespace(getClass(), namespace);
        key = getDatastore().allocateId(keyFactory.newKey()); // Give this greeting a unique ID
      }

      FullEntity.Builder<Key> builder = FullEntity.newBuilder(key);

      if (firstName != null)
        builder.set("firstName", firstName);
      if (middleName != null)
        builder.set("middleName", middleName);
      if(lastName != null)
          builder.set("lastName", lastName);
      if(employeeCode != null)
          builder.set("employeeCode", employeeCode);
      if(companyName != null)
          builder.set("companyName", companyName);

      builder.set("salutation", salutation);
      builder.set("companyId", companyId);

      //builder.set("date", Timestamp.of(date));

      getDatastore().put(builder.build());
    }

    public Key getKey() { return key; }
    public long getId() { return key != null ? key.getId() : -1; }
    public String getFirstName() { return firstName; }
    public String getMiddleName() { return middleName; }
    public String getLastName() { return lastName; }
    public String getEmployeeCode() { return employeeCode; }
    public String getCompanyName() { return companyName; }
    public int getSalutation() { return salutation; }
    public int getCompanyId() { return companyId; }

    public EnterpriseAdmin setKey(Key v) { this.key = v; return this; }
    public EnterpriseAdmin setId(long v) { this.key = keyFactory.newKey(v); return this; } 
    public EnterpriseAdmin setFirstName(String v) { this.firstName = v; return this; }
    public EnterpriseAdmin setMiddleName(String v) { this.middleName = v; return this; }
    public EnterpriseAdmin setLastName(String v) { this.lastName = v; return this; }
    public EnterpriseAdmin setEmployeeCode(String v) { this.employeeCode = v; return this; }
    public EnterpriseAdmin setSalutation(int v) { this.salutation = v; return this; }
    public EnterpriseAdmin setCompanyName(String v) { this.companyName = v; return this; }
    public EnterpriseAdmin setCompanyId(int v) { this.companyId = v; return this; }

    public String toString() {
        StringWriter ret = new StringWriter();
        try {
            Utils.objectMapper.writeValue(ret, this);
        } catch(IOException ioex) { }
        return ret.toString(); 
    }

    public static void main(String[] args) throws IOException {
        //read json file data to String
        byte[] jsonData = Files.readAllBytes(Paths.get("employee.txt"));
        
        //convert json string to object
        EnterpriseAdmin emp = Utils.objectMapper.readValue(jsonData, EnterpriseAdmin.class);
        
        System.out.println("EnterpriseAdmin Object: " + emp);
        
        //convert Object to json string
        EnterpriseAdmin emp1 = createEnterpriseAdmin();
        
        //writing to console, can write to any output stream such as file
        System.out.println("EnterpriseAdmin JSON is " + emp1.toString());
 
    } 

    public static EnterpriseAdmin createEnterpriseAdmin() {
        EnterpriseAdmin ret = new EnterpriseAdmin();
        ret.setId(10).setFirstName("Narendra").setLastName("Modi");
        return ret;
    }
}

