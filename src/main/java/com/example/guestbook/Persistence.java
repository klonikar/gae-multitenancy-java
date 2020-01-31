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

package com.example.guestbook;

import java.util.logging.Logger;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.KeyFactory;
import java.util.concurrent.atomic.AtomicReference;

//[START all]
public class Persistence {

  private static final Logger logger = Logger.getLogger(Persistence.class.getSimpleName());

  private static AtomicReference<Datastore> datastore = new AtomicReference<>();

  @SuppressWarnings("JavadocMethod")
  public static Datastore getDatastore() {
    if (datastore.get() == null) {
      datastore.set(
          DatastoreOptions.newBuilder().setProjectId("payroll1").build().getService());
    }

    return datastore.get();
  }

  public static void setDatastore(Datastore datastore) {
    Persistence.datastore.set(datastore);
  }

  public static KeyFactory getKeyFactory(String entityName) {
    try {
        return getDatastore().newKeyFactory().setNamespace(NamespaceFilter.getNamespace()).setKind(entityName);
    }
    catch(Error err) {
        logger.warning("error occured: " + err.getMessage());
        return getDatastore().newKeyFactory().setKind(entityName);
    }
  }

  public static KeyFactory getKeyFactoryWithNamespace(String entityName, String namespace) {
    return getDatastore().newKeyFactory().setNamespace(namespace).setKind(entityName);
  }

}
//[END all]
