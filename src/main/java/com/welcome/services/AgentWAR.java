package com.welcome.services;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
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
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.welcome.auxiliary.Utilities;

public class AgentWAR {

  Utilities util = new Utilities();

  public AgentWAR() {}

  public void findTheActiveAgents(String label, String url) {

    String queryString =
        "prefix welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \r\n"
            + "select ?s ?p ?o where { \r\n" + "	?s welcome:hasStatus welcome:active .\r\n"
            + "    ?s ?p ?o .\r\n" + "}";

    RemoteRepositoryManager manager = new RemoteRepositoryManager(url);
    manager.init();
    ModelBuilder builder = new ModelBuilder();
    try {
      Repository repository = manager.getRepository(label);
      repository.init();
      RepositoryConnection connection = repository.getConnection();
      ValueFactory vf = repository.getValueFactory();
      Resource res = vf.createIRI("http://welcome.org/kbs/WAR/activeAgents");
      connection.clear(res);
      TupleQuery query = connection.prepareTupleQuery(queryString);
      try (TupleQueryResult result = query.evaluate()) {
        while (result.hasNext()) {
          BindingSet solution = result.next();
          builder.namedGraph(res).subject((Resource) solution.getValue("s"))
              .add((IRI) solution.getValue("p"), solution.getValue("o"));
        }

        connection.begin();
        Model model = builder.build();
        connection.add(model);
        connection.commit();
      }
    } catch (QueryEvaluationException | RepositoryException e) {

    }
  }

  public JSONObject findAgents(String label, String url, String name) {

    RemoteRepositoryManager manager = new RemoteRepositoryManager(url);
    manager.init();
    ModelBuilder builder = new ModelBuilder();
    Repository repository = manager.getRepository(label);
    repository.init();
    RepositoryConnection connection = repository.getConnection();
    JSONObject responseDetailsJson = new JSONObject();
    JSONArray jsonArray = new JSONArray();

    clearActive(repository, connection);

    if (name == null) {
      String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
          + "PREFIX ajan: <http://www.ajan.de/ajan-ns#>\r\n"
          + "prefix welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
          + "prefix ag: <http://localhost:8080/welcome/integration/coordination/ajan/agents/>\r\n"
          + "			select ?s ?o ?p ?iri_name ?iri_status where { \r\n"
          + "				?s welcome:hasStatus ?o .\r\n"
          // + " ?s welcome:hasAddress ?p .\r\n"
          + "    			?s rdf:type ajan:Agent .\r\n"
          + "    		bind(strafter(str(?s),str(ag:)) as ?iri_name)\r\n"
          + "			bind(strafter(str(?o),str(welcome:)) as ?iri_status)\r\n" + "			}";

      TupleQuery query = connection.prepareTupleQuery(queryString);
      try (TupleQueryResult result = query.evaluate()) {
        while (result.hasNext()) {
          BindingSet solution = result.next();
          String agent = solution.getValue("iri_name").toString().replaceAll("\"", "");

          String status = solution.getValue("iri_status").toString().replaceAll("\"", "");

          // System.out.println(solution.getValue("p").toString().replaceAll("\"", ""));

          JSONObject formDetailsJson = new JSONObject();
          formDetailsJson.put("agentName", agent);
          formDetailsJson.put("status", status);

          jsonArray.add(formDetailsJson);
        }
        responseDetailsJson.put("Agents", jsonArray);
      }
    } else {
      String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
          + "PREFIX ajan: <http://www.ajan.de/ajan-ns#>\r\n"
          + "prefix welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
          + "prefix ag: <http://localhost:8080/welcome/integration/coordination/ajan/agents/>\r\n"
          + "			select ?name ?iri_status where { \r\n"
          + "				ag:Agent1 welcome:hasStatus ?o .\r\n"
          + "    			ag:Agent1 ajan:agentName ?name\r\n"
          + "			bind(strafter(str(?o),str(welcome:)) as ?iri_status)\r\n" + "			}";

      TupleQuery query = connection.prepareTupleQuery(queryString);
      try (TupleQueryResult result = query.evaluate()) {
        while (result.hasNext()) {
          BindingSet solution = result.next();
          String agent = solution.getValue("name").toString().replaceAll("\"", "");
          String status = solution.getValue("iri_status").toString().replaceAll("\"", "");
          responseDetailsJson.put("agentName", agent);
          responseDetailsJson.put("status", status);
        }
      }
    }
    return responseDetailsJson;
  }


  public String insertAgent(String label, String url, String input) throws ParseException {

    RemoteRepositoryManager manager = new RemoteRepositoryManager(url);
    manager.init();
    ModelBuilder builder = new ModelBuilder();
    Repository repository = manager.getRepository(label);
    repository.init();
    RepositoryConnection connection = repository.getConnection();

    JSONParser parser = new JSONParser();
    JSONObject jsonObject = (JSONObject) parser.parse(input);

    clearActive(repository, connection);

    String name = (String) jsonObject.get("agentName");
    String currentStatus = (String) jsonObject.get("status");
    String agentIRI = (String) jsonObject.get("agentIRI");

    String agentExists =
        "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
            + "PREFIX ajan: <http://www.ajan.de/ajan-ns#>\r\n" + "ASK WHERE {\r\n"
            + " ?x ajan:agentName \"" + name + "\" .  \r\n" + " }\r\n" + "";

    BooleanQuery booleanQuery = connection.prepareBooleanQuery(QueryLanguage.SPARQL, agentExists);
    boolean exists = booleanQuery.evaluate();
    if (exists) {
      return "Agent with name:" + name + " exists";
    } else {
      String newAgent =
          "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
              + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>	\r\n"
              + "PREFIX ajan: <http://www.ajan.de/ajan-ns#> 	\r\n" + "INSERT DATA \r\n"
              + "{ GRAPH <http://welcome.org/kbs/WAR>{\r\n"
              + "        <http://localhost:8080/welcome/integration/coordination/ajan/agents/"
              + name + "> ajan:agentName \"" + name + "\";\r\n"
              + "         																welcome:hasStatus 							welcome:"
              + currentStatus + " ;\r\n" + "welcome:hasAddress \"" + agentIRI + "\"; "
              + "         																rdf:type								ajan:Agent;\r\n"
              + "                            } } ;";

      connection.begin();
      util.executeUpdate(connection, String.format(newAgent));
      connection.commit();
      return "Agent inserted";
    }
  }

  public String updateAgent(String label, String url, String input) throws ParseException {

    RemoteRepositoryManager manager = new RemoteRepositoryManager(url);
    manager.init();
    ModelBuilder builder = new ModelBuilder();
    Repository repository = manager.getRepository(label);
    repository.init();
    RepositoryConnection connection = repository.getConnection();

    JSONParser parser = new JSONParser();
    JSONObject jsonObject = (JSONObject) parser.parse(input);

    clearActive(repository, connection);

    String name = (String) jsonObject.get("agentName");
    String currentStatus = (String) jsonObject.get("status");
    String agentIRI = (String) jsonObject.get("agentIRI");

    String agentExists =
        "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
            + "PREFIX ajan: <http://www.ajan.de/ajan-ns#>\r\n" + "ASK WHERE {\r\n"
            + " ?x ajan:agentName \"" + name + "\" .  \r\n" + " }\r\n" + "";

    BooleanQuery booleanQuery = connection.prepareBooleanQuery(QueryLanguage.SPARQL, agentExists);
    boolean exists = booleanQuery.evaluate();
    if (exists) {
      String updAgent =
          "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
              + "WITH <http://welcome.org/kbs/WAR>\r\n"
              + "DELETE { <http://localhost:8080/welcome/integration/coordination/ajan/agents/"
              + name + "> welcome:hasStatus ?o . }\r\n"
              + "INSERT { <http://localhost:8080/welcome/integration/coordination/ajan/agents/"
              + name + "> welcome:hasStatus welcome:" + currentStatus + " . }\r\n"
              + "WHERE { <http://localhost:8080/welcome/integration/coordination/ajan/agents/"
              + name + "> welcome:hasStatus ?o . }";
      String updAgent2 =
          "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
              + "WITH <http://welcome.org/kbs/WAR>\r\n"
              + "DELETE { <http://localhost:8080/welcome/integration/coordination/ajan/agents/"
              + name + "> welcome:hasAddress ?o . }\r\n"
              + "INSERT { <http://localhost:8080/welcome/integration/coordination/ajan/agents/"
              + name + "> welcome:hasAddress \"" + agentIRI + "\" . }\r\n"
              + "WHERE { <http://localhost:8080/welcome/integration/coordination/ajan/agents/"
              + name + "> welcome:hasAddress ?o . }";
      connection.begin();
      util.executeUpdate(connection, String.format(updAgent));
      util.executeUpdate(connection, String.format(updAgent2));
      connection.commit();
      return "Agent " + name + " updated succesfully ";
    } else {
      return "Agent with name:" + name + " doesn't exist";
    }
  }

  public String deleteAgent(String label, String url, String name) throws ParseException {

    RemoteRepositoryManager manager = new RemoteRepositoryManager(url);
    manager.init();
    ModelBuilder builder = new ModelBuilder();
    Repository repository = manager.getRepository(label);
    repository.init();
    RepositoryConnection connection = repository.getConnection();

    clearActive(repository, connection);

    String delAgent =
        "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
            + "PREFIX ajan: <http://www.ajan.de/ajan-ns#>\r\n" + "DELETE WHERE {\r\n"
            + "                      GRAPH <http://welcome.org/kbs/WAR> {\r\n"
            + "                            <http://localhost:8080/welcome/integration/coordination/ajan/agents/"
            + name + "> ?p ?o .\r\n" + "                          }             } ;\r\n" + "";


    String agentExists =
        "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
            + "PREFIX ajan: <http://www.ajan.de/ajan-ns#>\r\n" + "ASK WHERE {\r\n"
            + " ?x ajan:agentName \"" + name + "\" .  \r\n" + " }\r\n" + "";

    BooleanQuery booleanQuery = connection.prepareBooleanQuery(QueryLanguage.SPARQL, agentExists);
    boolean exists = booleanQuery.evaluate();

    if (exists) {
      connection.begin();
      util.executeUpdate(connection, String.format(delAgent));
      connection.commit();
      return "Agent " + name + " deleted succesfully ";
    } else {
      return "Agent with name:" + name + " doesn't exist";
    }
  }

  public void clearActive(Repository repository, RepositoryConnection connection) {
    ValueFactory vf = repository.getValueFactory();
    Resource res = vf.createIRI("http://welcome.org/kbs/WAR/activeAgents");
    connection.clear(res);
  }
}
