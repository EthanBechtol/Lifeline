package com.bechtol.lifeline.servlets;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.OrderBy;
import com.google.cloud.datastore.StringValue;
import com.google.gson.Gson;
import com.bechtol.lifeline.data.Event;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Created with the help of https://happycoding.io/tutorials/google-cloud/datastore and the Datastore documentation at https://googleapis.dev/java/google-cloud-clients/0.91.0-alpha/com/google/cloud/datastore/Datastore.html.
@WebServlet("/fetch-events")
public class FetchEventsServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    QueryResults<Entity> qr = DatastoreOptions.getDefaultInstance().getService().run(
        Query.newEntityQueryBuilder()
        .setKind("Events")
        .setOrderBy(OrderBy.desc("timestamp"))
        .build());

    List<Event> events = new ArrayList<>();
    while (qr.hasNext()) {
      Entity entity = qr.next();
      events.add(new Event(entity.getKey().getId(), entity.getLong("timestamp"), entity.getString("type"), entity.getString("description")));
    }

    response.setContentType("application/json;");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().println(new Gson().toJson(events));
  }
}