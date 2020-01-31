// execute using: mvn exec:java -Dexec.mainClass="com.example.guestbook.Enterprise" -Dexec.args="arg0 arg1 arg2" 
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
import java.util.logging.Logger;
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
public class Enterprise {
    
    private static final Logger logger = Logger.getLogger(Enterprise.class.getSimpleName());

    private KeyFactory keyFactory;
    private Key key;
    private String name;
    private String address;
    private Timestamp createdDate; 

    public Enterprise() {
        keyFactory = getKeyFactory(getClass().getSimpleName());
    }

    public Enterprise(Entity entity) {
      keyFactory = entity.hasKey() ? getDatastore().newKeyFactory().setNamespace(entity.getKey().getNamespace()).setKind(this.getClass().getSimpleName()) : getKeyFactory(this.getClass().getSimpleName());
      key = entity.hasKey() ? entity.getKey() : null;
      name = entity.contains("name") ? entity.getString("name") : null;
      address = entity.contains("address") ? entity.getString("address") : null;

      createdDate = entity.contains("createdDate") ? entity.getTimestamp("createdDate") : null;
    }

    public void save(String namespace) {
        save(namespace, getClass().getSimpleName());
    }

    public void save(String namespace, String entityName) {
      keyFactory = getKeyFactoryWithNamespace(entityName, namespace);
      //key = getDatastore().allocateId(keyFactory.newKey()); // Give this object a unique ID
      key = keyFactory.newKey(name);

      FullEntity.Builder<Key> builder = FullEntity.newBuilder(key);

      if (name != null)
        builder.set("name", name);
      if (address != null)
        builder.set("address", address);

      builder.set("createdDate", createdDate);

      getDatastore().put(builder.build());
    }

    public Key getKey() { return key; }
    public long getId() { return key != null && key.hasId() ? key.getId() : -1; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getCreatedDate() { return createdDate != null ? createdDate.toString() : ""; }

    public Enterprise setKey(Key v) { this.key = v; return this; }
    public Enterprise setId(long v) { this.key = keyFactory.newKey(v); return this; } 
    public Enterprise setName(String v) { this.name = v; return this; }
    public Enterprise setAddress(String v) { this.address = v; return this; }
    public Enterprise setCreatedDate(String v) { this.createdDate = Timestamp.parseTimestamp(v); return this; }
    public Enterprise setCreatedDate(Timestamp v) { this.createdDate = v; return this; }

    public String toString() {
        StringWriter ret = new StringWriter();
        try {
            Utils.objectMapper.writeValue(ret, this);
        } catch(IOException ioex) { }
        return ret.toString(); 
    }

    public static void main(String[] args) throws IOException {
        //read json file data to String
        byte[] jsonData = Files.readAllBytes(Paths.get("employee.json"));
        
        //convert json string to object
        Enterprise emp = Utils.objectMapper.readValue(jsonData, Enterprise.class);
        
        logger.info("Enterprise Object: " + emp);
        
        //convert Object to json string
        Enterprise emp1 = createEnterprise();
        
        //writing to console, can write to any output stream such as file
        logger.info("Enterprise JSON is " + emp1.toString());

        List<Enterprise> emps = new ArrayList<>();
        emps.add(createEnterprise());
        emps.add(createEnterprise());
        logger.info("Enterprises JSON is " + Utils.objectMapper.writeValueAsString(emps));
    } 

    public static Enterprise createEnterprise() {
        Enterprise ret = new Enterprise();
        ret.setId(10).setName("abc").setAddress("Bangalore").setCreatedDate("2019-10-31T13:04:00Z");
        return ret;
    }
}

