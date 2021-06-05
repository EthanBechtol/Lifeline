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
@WebServlet("/new-vital-reading-handler")
public class NewVitalReadingServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String orientation = Jsoup.clean(request.getParameter("orientation"), Whitelist.none());
    int hr = 0;
    int o2 = 0;
    long timestamp = System.currentTimeMillis();

    try {
        hr = Integer.parseInt(Jsoup.clean(request.getParameter("hr"), Whitelist.none()));
        o2 = Integer.parseInt(Jsoup.clean(request.getParameter("o2"), Whitelist.none()));
    }
    catch(NumberFormatException e) {
        response.setContentType("text/html;");
        response.getWriter().println("Invalid heart rate or capacity!");
        return;
    }

    System.out.println("New vital reading recieved:");
    System.out.println("\theart rate: " + hr);
    System.out.println("\toxygen level: " + o2);
    System.out.println("\torientation: " + orientation);
    System.out.println("\ttimestamp: " + timestamp);


    Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    KeyFactory keyFactory = datastore.newKeyFactory().setKind("Vitals");
    FullEntity vitalReadingEntity =
        Entity.newBuilder(keyFactory.newKey())
            .set("timestamp", timestamp)
            .set("hr", hr)
            .set("o2", o2)
            .set("orientation", orientation)
            .build();
    datastore.put(vitalReadingEntity);
    }
}