// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.sps.utils.data.Species;
import com.google.sps.utils.data.PopulationTrend;
import com.google.sps.utils.data.DataCollection;

import com.google.cloud.datastore.Datastore;
// import com.google.cloud.datastore.DatastoreException;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.EntityQuery;
// import com.google.cloud.datastore.Key;
// import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import java.util.*;

/** Servlet that grabs data on individual species. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
    Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Parse identifying species name from query string
        String speciesName = getSpeciesName(request);

        // Initialize and run a query that will select the specific species from Datastore by filtering by scientific name
        Query<Entity> query = Query.newEntityQueryBuilder().setKind("Species").setFilter(PropertyFilter.eq("binomial_name", speciesName)).build();
        QueryResults<Entity> queriedSpecies = datastore.run(query);
        
        // There will only ever be one entry returned by any query due to how we add and modify species to Datastore,
        // so we only need to call queriedSpecies.next() a single time
        Entity speciesData = queriedSpecies.next();

        // Grab information from Datastore entry and construct Species object
        String commonName       = speciesData.getString("common_name");
        String binomialName     = speciesData.getString("binomial_name");
        String status           = speciesData.getString("status");
        String population       = speciesData.getString("population");
        String wikipediaNotes   = speciesData.getString("wikipedia_notes");
        String imageLink        = speciesData.getString("image_link");
        String citationLink     = speciesData.getString("citation_link");
        PopulationTrend trend   = DataCollection.convertToPopulationTrendEnum(speciesData.getString("trend"));

        Species species = new Species(commonName,
                                            binomialName,
                                            status,
                                            trend,
                                            population,
                                            wikipediaNotes,
                                            imageLink,
                                            citationLink);

        // Convert Species object to JSON and send it back to caller
        String json = convertToJson(species);
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().println(json);
    }

    // Convert species data to JSON using Gson library.
    private String convertToJson(Species data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return json;
    }
    
    // Extracts species name from request and returns it.
    private String getSpeciesName(HttpServletRequest request) {
        String speciesName = request.getParameter("species");

        // Prevent accidental/blank submissions.
        if (speciesName.equals("")) {
            return null;
        }
        return speciesName;
    }
}