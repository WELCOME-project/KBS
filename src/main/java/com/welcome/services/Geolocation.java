package com.welcome.services;

import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.welcome.auxiliary.Queries;

public class Geolocation {

  TCN mTCN = new TCN();
  Queries que = new Queries();


  public Geolocation() {};

  public JSONObject geoLoc(String label, String preferedLocation, String body,
      Map<String, String> authorizationHeader) throws ParseException {
    BufferedReader reader;
    String line;
    HttpURLConnection conn = null;

    JSONParser parser = new JSONParser();

    JSONArray jsonArray = new JSONArray();

    /*
     * RemoteRepositoryManager manager = new RemoteRepositoryManager(Utilities.serverURL);
     * manager.init(); ModelBuilder builder = new ModelBuilder(); Repository repository =
     * manager.getRepository(Utilities.graphDB); repository.init(); RepositoryConnection connection
     * = repository.getConnection();
     */

    /*
     * HTTPRepository repo = new HTTPRepository(Utilities.serverURL, Utilities.graphDB);
     * repo.setAdditionalHttpHeaders(authorizationHeader); repo.init(); RepositoryConnection
     * connection = repo.getConnection();
     */

    ArrayList<String> locations = new ArrayList<String>();
    ArrayList<String> postCodes = new ArrayList<String>();
    ArrayList<String> region = new ArrayList<String>();
    ArrayList<String> finalAgents = new ArrayList<String>();
    ArrayList<String> BBCoordinates = new ArrayList<String>();
    ArrayList<BoundingBox> BBList = new ArrayList<BoundingBox>();

    JSONArray JSGL =
        mTCN.getTCN(null, "", "UserId,PostCode,City,LocationPreference", null, authorizationHeader);
    JSONArray osmArray = new JSONArray();
    JSONArray osmVBArray = new JSONArray();

    // System.out.println(body);

    JSONObject bodyInfo = (JSONObject) parser.parse(body);

    // System.out.println(bodyInfo.get("@id"));
    // System.out.println(bodyInfo.get("welcome:hasLocationPreference"));

    locations = (ArrayList<String>) bodyInfo.get("welcome:hasLocationPreference");
    /*
     * for (int i = 0; i < locations.size(); i++) { TupleQuery query =
     * connection.prepareTupleQuery(que.PostCodes(locations.get(i).replaceAll(" ", "_"))); try
     * (TupleQueryResult result = query.evaluate()) { while (result.hasNext()) { BindingSet solution
     * = result.next(); // //
     * System.out.println(solution.getValue("PostCodes").toString().replaceAll("\"", ""));
     * postCodes.add(solution.getValue("PostCodes").toString());
     * region.add(solution.getValue("Region").toString().replace(
     * "http://www.semanticweb.org/gtzionis_local/ontologies/2022/10/untitled-ontology-40#", "")); }
     * } catch (QueryEvaluationException | RepositoryException e) { System.out.println(e); } }
     * System.out.println(JSGL); System.out.println(postCodes);
     */

    for (int i = 0; i < JSGL.size(); i++) {
      JSONObject item = (JSONObject) JSGL.get(i);
      ArrayList<String> pref = (ArrayList<String>) item.get("LocationPreference");
      for (int j = 0; j < pref.size(); j++) {
        // System.out.println(pref.get(j));
        for (int x = 0; x < locations.size(); x++) {
          if (pref.get(j).equals(locations.get(x))) {
            // System.out.println(locations.get(x));
            finalAgents.add("agent-core-" + item.get("UserId"));
          }
          if (pref.isEmpty()) {
            System.out.println("nada");
          }
        }
      }
      // String pc = (String) item.get("PostCode");
      // for (int j = 0; j < postCodes.size(); j++) {
      // if (pc.contains(postCodes.get(j).replaceAll("[\"x]", "").toString())) {
      // System.out.println(postCodes.get(j));
      // finalAgents.add("agent-core-" + item.get("UserId"));
      // }
      // }
    }

    // System.out.println(finalAgents);
    Set<String> uniqueAgents = new HashSet<String>(finalAgents);
    System.out.println(uniqueAgents);
    // uniqueAgents.remove(((String) bodyInfo.get("@id")).replaceAll("welcome:", ""));
    JSONObject gin = new JSONObject();
    gin.put("@id", bodyInfo.get("@id"));
    gin.put("welcome:hasListOfAgents", uniqueAgents);
    return gin;
  }

}

/*
 * public void geoLoc(String label, String preferedLocation, String body) throws ParseException {
 * BufferedReader reader; String line; HttpURLConnection conn = null;
 * 
 * JSONParser parser = new JSONParser();
 * 
 * JSONArray jsonArray = new JSONArray();
 * 
 * RemoteRepositoryManager manager = new RemoteRepositoryManager(Utilities.serverURL);
 * manager.init(); ModelBuilder builder = new ModelBuilder(); Repository repository =
 * manager.getRepository(Utilities.graphDB); repository.init(); RepositoryConnection connection =
 * repository.getConnection();
 * 
 * ArrayList<String> locations = new ArrayList<String>(); ArrayList<String> postCodes = new
 * ArrayList<String>(); ArrayList<String> finalAgents = new ArrayList<String>(); ArrayList<String>
 * BBCoordinates = new ArrayList<String>(); ArrayList<BoundingBox> BBList = new
 * ArrayList<BoundingBox>();
 * 
 * JSONArray JSGL = mTCN.getTCN(null, "", "UserId,PostCode,City", null); JSONArray osmArray = new
 * JSONArray(); JSONArray osmVBArray = new JSONArray();
 * 
 * 
 * try {
 * 
 * 
 * ValueFactory vf = repository.getValueFactory();
 * 
 * Resource clp = vf.createIRI(
 * "https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ChcLocationPreference"
 * ); try { connection.begin(); connection.clear(clp); InputStream clpStream = new
 * ByteArrayInputStream(body.getBytes()); connection.add(clpStream, "urn:base", RDFFormat.TURTLE,
 * clp); connection.commit();
 * 
 * } catch (RDFParseException | RepositoryException | IOException e) {
 * System.out.println("Error in loading prefered locations"); }
 * 
 * } catch (RepositoryConfigException | RepositoryException e) {
 * System.out.println("Error in loading prefered locations"); }
 * 
 * TupleQuery query = connection.prepareTupleQuery(que.locPreference); try (TupleQueryResult result
 * = query.evaluate()) { while (result.hasNext()) { BindingSet solution = result.next(); //
 * System.out.println(solution.getValue("loc").toString().replaceAll("\"", ""));
 * locations.add(solution.getValue("loc").toString().replaceAll("\"", "")); } } catch
 * (QueryEvaluationException | RepositoryException e) {
 * 
 * }
 * 
 * 
 * 
 * for (int i = 0; i < locations.size(); i++) {
 * 
 * StringBuilder responseContent = new StringBuilder();
 * 
 * try {
 * 
 * URL url = new URL("https://nominatim.openstreetmap.org/search?q=" + locations.get(i) +
 * "&countrycodes=[EL]&format=json&limit=1");
 * 
 * conn = (HttpURLConnection) url.openConnection();
 * 
 * // Request setup conn.setRequestMethod("GET"); conn.setConnectTimeout(5000);// 5000 milliseconds
 * = 5 seconds conn.setReadTimeout(5000);
 * 
 * // Test if the response from the server is successful int status = conn.getResponseCode();
 * 
 * if (status >= 300) { reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
 * while ((line = reader.readLine()) != null) { responseContent.append(line); } reader.close(); }
 * else { reader = new BufferedReader(new InputStreamReader(conn.getInputStream())); while ((line =
 * reader.readLine()) != null) { responseContent.append(line); } reader.close(); }
 * 
 * jsonArray = (JSONArray) parser.parse(responseContent.toString()); osmArray.add(jsonArray.get(0));
 * // System.out.println(jsonArray); } catch (MalformedURLException e) { e.printStackTrace(); }
 * catch (IOException e) { e.printStackTrace(); } finally { conn.disconnect(); } }
 * System.out.println(JSGL); // System.out.println(osmArray);
 * 
 * for (int i = 0; i < JSGL.size(); i++) { JSONObject tcn = (JSONObject)
 * parser.parse(JSGL.get(i).toString()); // System.out.println(tcn.get("City").toString()); for (int
 * j = 0; j < osmArray.size(); j++) { if (locations.get(j).equals(tcn.get("City").toString())) { //
 * System.out.println(tcn.get("City").toString()); finalAgents.add(tcn.get("UserId").toString()); }
 * } }
 * 
 * for (int i = 0; i < osmArray.size(); i++) { JSONObject osm = (JSONObject)
 * parser.parse(osmArray.get(i).toString()); //
 * System.out.println(osm.get("boundingbox").toString()); JSONArray bboxArray = (JSONArray)
 * osm.get("boundingbox"); for (int j = 0; j < bboxArray.size(); j++) { //
 * System.out.println(bboxArray.get(j).toString()); BBCoordinates.add(bboxArray.get(j).toString());
 * } }
 * 
 * System.out.println(BBCoordinates); System.out.println(BBCoordinates.size()); int start = 0; for
 * (int i = 0; i < BBCoordinates.size() / 4; i++) { BoundingBox bbItem = new
 * BoundingBox(BBCoordinates.get(0 + start), BBCoordinates.get(1 + start), BBCoordinates.get(2 +
 * start), BBCoordinates.get(3 + start)); start = start + 4; BBList.add(bbItem); }
 * System.out.println(BBList.get(2).getLat1());
 * 
 * for (int i = 0; i < BBList.size(); i++) {
 * 
 * StringBuilder responseContent = new StringBuilder();
 * 
 * try {
 * 
 * URL url = new URL("https://nominatim.openstreetmap.org/search?viewbox=" + BBList.get(i).getLon2()
 * + "," + BBList.get(i).getLon1() + "," + BBList.get(i).getLat2() + "," + BBList.get(i).getLat1() +
 * "&format=json&bounded=true&q=[bar]&countrycodes=[el]");
 * 
 * conn = (HttpURLConnection) url.openConnection();
 * 
 * // Request setup conn.setRequestMethod("GET"); conn.setConnectTimeout(5000);// 5000 milliseconds
 * = 5 seconds conn.setReadTimeout(5000);
 * 
 * // Test if the response from the server is successful int status = conn.getResponseCode();
 * 
 * if (status >= 300) { reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
 * while ((line = reader.readLine()) != null) { responseContent.append(line); } reader.close(); }
 * else { reader = new BufferedReader(new InputStreamReader(conn.getInputStream())); while ((line =
 * reader.readLine()) != null) { responseContent.append(line); } reader.close(); }
 * 
 * jsonArray = (JSONArray) parser.parse(responseContent.toString()); osmVBArray.add(jsonArray); //
 * System.out.println(jsonArray); } catch (MalformedURLException e) { e.printStackTrace(); } catch
 * (IOException e) { e.printStackTrace(); } finally { conn.disconnect(); }
 * 
 * }
 * 
 * System.out.println(osmVBArray); for (int i = 0; i < osmVBArray.size(); i++) { JSONArray osmVBObj
 * = (JSONArray) osmVBArray.get(i); for (int j = 0; j < osmVBObj.size(); j++) { JSONObject osbObj =
 * (JSONObject) osmVBObj.get(j); //
 * System.out.println(osbObj.get("display_name").toString().replaceAll("[^0-9]", // "").substring(
 * // osbObj.get("display_name").toString().replaceAll("[^0-9]", "").length() - 5));
 * postCodes.add(osbObj.get("display_name").toString().replaceAll("[^0-9]", "").substring(
 * osbObj.get("display_name").toString().replaceAll("[^0-9]", "").length() - 5));
 * 
 * } } for (int i = 0; i < JSGL.size(); i++) { JSONObject tcn = (JSONObject)
 * parser.parse(JSGL.get(i).toString()); // System.out.println(tcn.get("City").toString()); for (int
 * j = 0; j < postCodes.size(); j++) { if (postCodes.get(j).equals(tcn.get("PostCode").toString()))
 * { // System.out.println(tcn.get("City").toString());
 * finalAgents.add(tcn.get("UserId").toString()); } } } System.out.println(finalAgents);
 * 
 * 
 * }
 * 
 * }
 * 
 */


