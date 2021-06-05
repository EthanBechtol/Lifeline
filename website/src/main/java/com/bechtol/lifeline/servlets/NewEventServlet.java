package com.bechtol.lifeline.servlets;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Value;
import com.google.cloud.datastore.StringValue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

// Created with the help of https://happycoding.io/tutorials/google-cloud/datastore and the Datastore documentation at https://googleapis.dev/java/google-cloud-clients/0.91.0-alpha/com/google/cloud/datastore/Datastore.html.
@WebServlet("/new-event-handler")
public class NewEventServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String type = Jsoup.clean(request.getParameter("type"), Whitelist.none());
    String description = Jsoup.clean(request.getParameter("description"), Whitelist.none());
    long timestamp = System.currentTimeMillis();

    System.out.println("New event recieved:");
    System.out.println("\ttype: " + type);
    System.out.println("\tdescription: " + description);
    System.out.println("\ttimestamp: " + timestamp);


    Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    KeyFactory keyFactory = datastore.newKeyFactory().setKind("Events");
    FullEntity vitalReadingEntity =
        Entity.newBuilder(keyFactory.newKey())
            .set("timestamp", timestamp)
            .set("type", type)
            .set("description", description)
            .build();
    datastore.put(vitalReadingEntity);
    }
}