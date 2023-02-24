package com.welcome.auxiliary;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Queries {
  // ------------------Minigame------------------

  public static String takeScenarios =
      "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
          + "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
          + "select ?s ?o where { \r\n" + "	?s welcome:hasScenarioID ?o\r\n" + "} ";

  public String takeIdsOfScenarios(String scenario, String pred, String type) {
    return "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
        + "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
        + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r\n" + "select ?s ?iri_name where { \r\n"
        + "	?s rdf:type welcome:" + type + " .\r\n" + "    ?s welcome:has" + pred + " \"" + scenario
        + "\".\r\n" + "    bind(strafter(str(?s),str(welcome:)) as ?iri_name)\r\n" + "} ";
  }

  public String takeIdsOfAssessments(String scenario) {
    return "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
        + "        PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
        + "        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r\n"
        + "select ?s ?iri_name where { \r\n" + "        ?s rdf:type welcome:LCCLesson .    \r\n"
        + "        ?s welcome:hasLessonId \"" + scenario + "\"^^xsd:integer.\r\n"
        + "            bind(strafter(str(?s),str(welcome:)) as ?iri_name)}";
  }

  public String takeSpecificScenario(String obj, String pre) {
    return "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
        + "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
        + "select ?p ?o where { \r\n" + "	welcome:" + obj + " welcome:has" + pre + " ?o\r\n"
        + "} ";
  }

  public static String queryMinigame =
      "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
          + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
          + "Select ?s ?iri_name {\r\n" + "    ?s rdf:type welcome:Minigame .\r\n"
          + "    bind(strafter(str(?s),str(welcome:)) as ?iri_name)\r\n" + "}";

  public static String queryAssessment =
      "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
          + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
          + "Select ?s ?iri_name {\r\n" + "    ?s rdf:type welcome:LCCLesson .\r\n"
          + "    bind(strafter(str(?s),str(welcome:)) as ?iri_name)\r\n" + "}";


  public static String queryApplicationForm =
      "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
          + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
          + "Select ?s ?iri_name {\r\n" + "    ?s rdf:type welcome:ApplicationForm .\r\n"
          + "    bind(strafter(str(?s),str(welcome:)) as ?iri_name)\r\n" + "}";

  public static String queryJobInterviewSimulation =
      "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
          + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
          + "Select ?s ?iri_name {\r\n" + "    ?s rdf:type welcome:JobInterviewSimulation .\r\n"
          + "    bind(strafter(str(?s),str(welcome:)) as ?iri_name)\r\n" + "}";

  public static String queryVocab =
      "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
          + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
          + "Select ?s ?iri_name {\r\n" + "    ?s rdf:type welcome:Vocab .\r\n"
          + "    bind(strafter(str(?s),str(welcome:)) as ?iri_name)\r\n" + "}";

  // --------------------------------------------
  public static String userExists =
      "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
          + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" + "ASK WHERE {\r\n"
          + "		?s rdf:type  welcome:DialogueUser. \r\n" + "}";


  public static String queryUser =
      "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
          + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
          + "Select ?s ?iri_name {\r\n" + "    ?s rdf:type welcome:DialogueUser .\r\n"
          + "    bind(strafter(str(?s),str(welcome:)) as ?iri_name)\r\n" + "}";

  Utilities util = new Utilities();

  public Queries() {}

  public String askInfo(String Name, String pre, String attr) {
    return "PREFIX ajan: <http://www.ajan.de/ajan-ns#>\r\n"
        + "PREFIX actor: <http://www.daml.org/services/owl-s/1.1/ActorDefault.owl#>\r\n"
        + "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
        + "PREFIX locn: <http://www.w3.org/ns/locn#>\r\n" + "ASK WHERE {\r\n" + " welcome:" + Name
        + " " + pre + ":" + attr + " ?x .  \r\n" + "}\r\n" + "";
  }

  public String retrieveInfo(String Name, String pre, String attr) {
    return "PREFIX actor: <http://www.daml.org/services/owl-s/1.1/ActorDefault.owl#>\r\n"
        + "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
        + "PREFIX locn: <http://www.w3.org/ns/locn#>\r\n"
        + "PREFIX schema: <https://schema.org/>\r\n" + "Select ?" + attr + " WHERE {\r\n"
        + " welcome:" + Name + " " + pre + ":" + attr + " ?" + attr + " .  \r\n" + "}\r\n" + "";

  }

  public String objectToStringWelcome(String Name, String pre, String attr, String obj_string) {
    return "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
        + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
        + "PREFIX actor: <http://www.daml.org/services/owl-s/1.1/ActorDefault.owl#>\r\n"
        + "PREFIX locn: <http://www.w3.org/ns/locn#>\r\n"
        + "PREFIX geo: <https://www.geonames.org/ontology#>\r\n"
        + "PREFIX schema: <https://schema.org/>\r\n" + "SELECT ?" + attr + " ?" + obj_string
        + "  WHERE{\r\n" + "   welcome:" + Name + " " + pre + ":" + attr + " ?" + attr + " .\r\n"
        + "   bind(strafter(str(?" + attr + "),str(welcome:)) as ?" + obj_string + ") \r\n" + "}";
  }

  public String objectToStringSchema(String Name, String pre, String attr, String obj_string) {
    return "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
        + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
        + "PREFIX actor: <http://www.daml.org/services/owl-s/1.1/ActorDefault.owl#>\r\n"
        + "PREFIX locn: <http://www.w3.org/ns/locn#>\r\n"
        + "PREFIX geo: <https://www.geonames.org/ontology#>\r\n"
        + "PREFIX schema: <https://schema.org/>\r\n" + "SELECT ?" + attr + " ?" + obj_string
        + "  WHERE{\r\n" + "   welcome:" + Name + " " + pre + ":" + attr + " ?" + attr + " .\r\n"
        + "   bind(strafter(str(?" + attr + "),str(schema:)) as ?" + obj_string + ") \r\n" + "}";
  }

  public String objectToString2(String Name, String pre, String attr, String obj_string) {
    return "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
        + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
        + "PREFIX actor: <http://www.daml.org/services/owl-s/1.1/ActorDefault.owl#>\r\n"
        + "PREFIX locn: <http://www.w3.org/ns/locn#>\r\n"
        + "PREFIX geo: <https://www.geonames.org/ontology#>\r\n"
        + "PREFIX schema: <https://schema.org/>\r\n" + "SELECT ?" + attr + " ?" + obj_string
        + "  WHERE{\r\n" + "   welcome:" + Name + " " + pre + ":" + attr + " ?" + attr + " .\r\n"
        + "   bind(strafter(str(?" + attr + "),str(" + pre + ":)) as ?" + obj_string + ") \r\n"
        + "}";
  }

  // ----------------------------

  public static String locPreference =
      "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
          + "          PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
          + "SELECT ?loc WHERE {  GRAPH welcome:CHCPreferences {\r\n"
          + "              ?s welcome:hasChcLocationPreference/rdf:rest*/rdf:first ?loc .\r\n"
          + "              ?s welcome:hasenableSearch ?o.\r\n"
          + "              ?o welcome:hasValue \"true\".           \r\n" + "    }\r\n"
          + "          }";


  public String PostCodes(String location) {
    return "PREFIX nuts: <http://www.semanticweb.org/gtzionis_local/ontologies/2022/10/untitled-ontology-40#>\r\n"
        + "SELECT ?Region ?PostCodes WHERE{\r\n" + "    ?Region nuts:hasNutsCode \"" + location
        + "\" .\r\n" + "    ?Region nuts:hasPostCode ?PostCodes .\r\n" + "}";
  }


  // --------

  public String updateTCN(String value, String attr) {
    return "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
        + "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
        + "WITH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo>\r\n"
        + "DELETE { ?s rdf:type welcome:" + attr + " . }\r\n" + "INSERT { welcome:" + value
        + " rdf:type welcome:" + attr + " . }\r\n" + "WHERE { ?s rdf:type welcome:" + attr + " . }";
  }

  public String insertTCN(String user) {
    return "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
        + "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
        + "INSERT DATA{\r\n"
        + "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \r\n"
        + "     welcome:" + user + " rdf:type welcome:DialogueUser.\r\n" + "    } \r\n" + "}\r\n"
        + "";
  }

  public String deleteFieldTCN(String user, String field) {
    return "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
        + "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
        + "DELETE WHERE { GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> {\r\n"
        + "  welcome:" + user + " welcome:has" + field + " ?o .\r\n"
        + "  ?o welcome:hasValue ?p .      \r\n" + " }\r\n" + "}";
  }

  // ----------------------------------------------

  public String insertFieldTCN(String user, String field, String value) {
    return "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
        + "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
        + "INSERT DATA{\r\n"
        + "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \r\n"
        + "     welcome:" + user + " welcome:has" + field + " _:node.\r\n"
        + "      _:node rdf:type welcome:" + field + "; \r\n" + "      welcome:hasValue \"" + value
        + "\" .\r\n" + "    } \r\n" + "}";
  }

  public String execObtain(RepositoryConnection connection, String officeName, String pre,
      String attr) {
    TupleQuery query = connection.prepareTupleQuery(retrieveInfo(officeName, pre, attr));
    try (TupleQueryResult result = query.evaluate()) {
      while (result.hasNext()) {
        BindingSet solution = result.next();
        // System.out.println(solution.getValue(attr).toString());
        return solution.getValue(attr).toString();
      }
    } catch (QueryEvaluationException | RepositoryException e) {

    }
    return "";
  }

  public String execObtainStr(RepositoryConnection connection, String officeName, String pre,
      String attr, String obj_string) {
    TupleQuery query =
        connection.prepareTupleQuery(objectToStringWelcome(officeName, pre, attr, obj_string));
    try (TupleQueryResult result = query.evaluate()) {
      while (result.hasNext()) {
        BindingSet solution = result.next();
        // System.out.println(solution.getValue(obj_string).toString());
        return solution.getValue(obj_string).toString();
      }
    } catch (QueryEvaluationException | RepositoryException e) {

    }
    return "";
  }

  public String TCNquery(String clas) {
    return "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
        + "        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
        + "        Select ?o WHERE{ GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo>\r\n"
        + "        {    _:node rdf:type welcome:" + clas + " .\r\n"
        + "            _:node welcome:hasValue ?o .}}";

  }

  public String execObtainStrTCN(RepositoryConnection connection, String Name) {
    TupleQuery query = connection.prepareTupleQuery(TCNquery(Name));
    try (TupleQueryResult result = query.evaluate()) {
      while (result.hasNext()) {
        BindingSet solution = result.next();
        // System.out.println(solution.getValue(obj_string).toString());
        return solution.getValue("o").toString().replaceAll("\"", "");
      }
    } catch (QueryEvaluationException | RepositoryException e) {

    }
    return "";
  }

  public ArrayList<String> execObtainArray(RepositoryConnection connection, String officeName,
      String pre, String attr, String obj_string, boolean sel) {
    ArrayList<String> array = new ArrayList<String>();
    TupleQuery query;
    if (sel) {
      query =
          connection.prepareTupleQuery(objectToStringWelcome(officeName, pre, attr, obj_string));
    } else {
      query = connection.prepareTupleQuery(objectToStringSchema(officeName, pre, attr, obj_string));
    }
    try (TupleQueryResult result = query.evaluate()) {
      while (result.hasNext()) {
        BindingSet solution = result.next();
        // System.out.println(solution.getValue(obj_string).toString());
        array.add(solution.getValue(obj_string).toString().replaceAll("\"", ""));
      }
      // System.out.println(array.get(0));
      return array;
    } catch (QueryEvaluationException | RepositoryException e) {

    }
    return array;

  }

  public JSONObject getTCNProfile_S(String label) {

    /* Init manager */
    RemoteRepositoryManager manager = new RemoteRepositoryManager(Utilities.serverURL);
    manager.init();

    /* Init repo */
    Repository repository = manager.getRepository(label);
    repository.init();

    /* Get connection */
    RepositoryConnection connection = repository.getConnection();

    ValueFactory f = repository.getValueFactory();

    String values = String.valueOf(SlotList.appForm).replace("[", "(").replace("]", ")");

    String queryString;

    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString +=
        "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "SELECT DISTINCT ?class ?value where { \n";
    queryString +=
        "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \n";
    queryString += "		welcome:tcn_user ?pred ?obj . \n";
    queryString += "        ?obj rdf:type ?class . \n";
    queryString += "        ?obj welcome:hasValue ?value . \n";
    queryString += "    } \n";
    queryString += "    FILTER (?class in " + values + ") \n";
    queryString += "} \n";

    TupleQuery query = connection.prepareTupleQuery(queryString);
    JSONObject obj = new JSONObject();
    List<String> list = new ArrayList<>();

    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      while (result.hasNext()) {
        BindingSet solution = result.next();

        /* Get the IRI of the class */
        Value c = solution.getBinding("class").getValue();
        IRI temp = f.createIRI(c.stringValue());

        /* Split the IRI and get the class Name */
        String classLabel = util.splitIRI(temp);

        /* Keep track of the populated classes */
        list.add("welcome:" + classLabel);

        /* Get the actual value for the class */
        Value value = solution.getBinding("value").getValue();

        /* Put class name and value into the JSON object */
        obj.put(classLabel, value.stringValue());
      }
    }

    /* Create a new list that holds all the class names */
    List<String> c = new ArrayList<>(SlotList.appForm);

    /* Remove the class names that are populated in the lAKR */
    c.removeAll(list);

    /*
     * For the remaining non-populated classes put an empty string in the JSON output
     */
    for (String temp : c) {
      String[] arrOfStr = temp.split(":");
      obj.put(arrOfStr[1], "");
    }

    List<String> langList = getOtherLanguageList(connection);
    JSONArray codeList = new JSONArray();
    if (langList.size() > 0) {
      for (int i = 0; i < langList.size(); i++) {
        String code = langList.get(i);
        codeList.add(code);
      }
      obj.put("OtherLanguageCode", codeList);
    } else {
      obj.put("OtherLanguageCode", codeList);
    }

    return obj;
  }

  public List<String> getOtherLanguageList(RepositoryConnection connection) {
    String queryString;

    queryString =
        "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "SELECT ?lang WHERE { \n";
    queryString += "  GRAPH welcome:ProfileInfo { \n";
    queryString += "    ?s welcome:hasOtherLanguageCode/rdf:rest*/rdf:first ?lang . \n";
    queryString += "  } \n";
    queryString += "} \n";

    TupleQuery query = connection.prepareTupleQuery(queryString);
    List<String> list = new ArrayList<>();

    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      while (result.hasNext()) {
        BindingSet solution = result.next();

        String loc = solution.getBinding("lang").getValue().stringValue();

        list.add(loc);
      }
    }

    return list;
  }

  public boolean checkIfPopulated(RepositoryConnection connection, String key) {
    String queryString;

    queryString =
        "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "ASK WHERE { \n";
    queryString += "    welcome:tcn_user ?pred ?obj . \n";
    queryString += "    ?obj rdf:type ?type \n";
    queryString += "    FILTER(?type = welcome:" + key + ") \n";
    queryString += "} \n";

    BooleanQuery booleanQuery = connection.prepareBooleanQuery(QueryLanguage.SPARQL, queryString);

    boolean result = booleanQuery.evaluate();

    return result;
  }

  public void populateTCNProfile(RepositoryConnection connection, String key, String value) {
    String queryString;

    queryString =
        "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "DELETE { \n";
    queryString +=
        "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \n";
    queryString += "    	?s welcome:hasValue ?old_value . \n";
    queryString += "    }  \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString +=
        "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> {  \n";
    queryString += "    	?s welcome:hasValue ?value . \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString +=
        "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> {  \n";
    queryString += "    	welcome:tcn_user ?pred ?s . \n";
    queryString += "        ?s rdf:type welcome:" + key + " . \n";
    queryString += "        ?s welcome:hasValue ?old_value . \n";
    queryString += "        BIND(\"" + value + "\" as ?value) \n";
    queryString += "    } \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(connection, queryString);
  }

  public void insertTCNData(RepositoryConnection connection, String key, String value) {
    String queryString;

    queryString =
        "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "INSERT DATA { \n";
    queryString +=
        "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \n";
    queryString += "    	welcome:tcn_user welcome:has" + key + " \n";
    queryString += "        [   a welcome:" + key + " ; \n";
    queryString += "        	welcome:hasValue \"" + value + "\" \n";
    queryString += "        ]. \n";
    queryString += "    } \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(connection, queryString);
  }

  public void insertRegStatus(RepositoryConnection connection) {
    /* The following query inserts the correlation id */
    String queryString;

    queryString = "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "INSERT DATA { \n";
    queryString += "    GRAPH welcome:ProfileInfo { \n";
    queryString += "        welcome:tcn_user rdf:type welcome:Registered . \n";
    queryString += "    } \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(connection, queryString);
  }

  public void deleteRegStatus(RepositoryConnection connection) {
    /* The following query inserts the correlation id */
    String queryString;

    queryString = "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "DELETE DATA { \n";
    queryString += "    GRAPH welcome:ProfileInfo { \n";
    queryString += "        welcome:tcn_user rdf:type welcome:Registered . \n";
    queryString += "    } \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(connection, queryString);
  }

  public void removeTCNData(RepositoryConnection connection, String key) {
    String queryString;

    queryString =
        "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "DELETE { \n";
    queryString +=
        "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \n";
    queryString += "    	welcome:tcn_user welcome:has" + key + " ?key . \n";
    queryString += "        ?key rdf:type welcome:" + key + " ; \n";
    queryString += "        	   welcome:hasValue ?value ; \n";
    queryString += "        	   welcome:lastUpdated ?timestamp . \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString +=
        "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \n";
    queryString += "    	welcome:tcn_user welcome:has" + key + " ?key . \n";
    queryString += "        ?key rdf:type welcome:" + key + " ; \n";
    queryString += "        	   welcome:hasValue ?value . \n";
    queryString += "        OPTIONAL { \n";
    queryString += "        	   ?key welcome:lastUpdated ?timestamp . \n";
    queryString += "        } \n";
    queryString += "    } \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(connection, queryString);
  }
}
