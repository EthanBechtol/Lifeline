package com.bechtol.lifeline.servlets;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.OrderBy;
import com.google.cloud.datastore.StringValue;
import com.google.gson.Gson;
import com.bechtol.lifeline.data.VitalReading;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Created with the help of https://happycoding.io/tutorials/google-cloud/datastore and the Datastore documentation at https://googleapis.dev/java/google-cloud-clients/0.91.0-alpha/com/google/cloud/datastore/Datastore.html.
@WebServlet("/fetch-vitals")
public class FetchVitalReadingsServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query<Entity> query = Query.newEntityQueryBuilder().setKind("Vitals").setOrderBy(OrderBy.desc("timestamp")).build();
    if(request.getParameterMap().containsKey("limit")) {
        query = Query.newEntityQueryBuilder()
        .setKind("Vitals")
        .setOrderBy(OrderBy.desc("timestamp"))
        .setLimit(Integer.parseInt(request.getParameter("limit")))
        .build();
    }

    QueryResults<Entity> qr = DatastoreOptions.getDefaultInstance().getService().run(query);
    List<VitalReading> vitalReadings = new ArrayList<>();
    while (qr.hasNext()) {
      Entity entity = qr.next();
      vitalReadings.add(new VitalReading(entity.getKey().getId(), entity.getLong("timestamp"), (int) entity.getLong("hr"), (int) entity.getLong("o2"), entity.getString("orientation")));
    }

    response.setContentType("application/json;");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().println(new Gson().toJson(vitalReadings));
  }
}