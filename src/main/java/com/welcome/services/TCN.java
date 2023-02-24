package com.welcome.services;

import com.welcome.auxiliary.Languages;
import com.welcome.auxiliary.Queries;
import com.welcome.auxiliary.Utilities;
import com.welcome.auxiliary.WELCOME;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TCN {

  // -------New
  private final String RegistrationStatus = "";
  private final String City = "";
  Queries q = new Queries();
  Utilities util = new Utilities();
  Queries que = new Queries();
  graphdbGlobal gdb = new graphdbGlobal();
  Logger logger = LoggerFactory.getLogger(Utilities.class);


  public TCN() {
  }

  public static int calculateAge(LocalDate dob) {
    // creating an instance of the LocalDate class and invoking the now() method
    // now() method obtains the current date from the system clock in the default time zone
    LocalDate curDate = LocalDate.now();
    // calculates the amount of time between two dates and returns the years
    if ((dob != null) && (curDate != null)) {
      return Period.between(dob, curDate).getYears();
    } else {
      return 0;
    }
  }

  public void maniTCN(String label, String body, String value,
      Map<String, String> authorizationHeader) {
    // Load the latest version of the Languages ontology.
    gdb.loadOntology(label, "languages", "langOntology");

    /* Create RDF Model */
    jsonToRdf(label, parseJSONLD(body), authorizationHeader);
  }

  public JSONObject parseJSONLD(String body) {
    // Read JSON file containing information about DMS Input
    JSONParser parser = new JSONParser();

    // Create JSONObject of DMS Input
    JSONObject object = new JSONObject();

    try {
      // Try to parse message
      logger.info("(INFO) Parsing json(-ld) to RDF.");
      object = (JSONObject) parser.parse(body);
    } catch (org.json.simple.parser.ParseException e) {
      logger.error("(ERROR) Unable to parse message!");
    }

    return object;
  }

  public void jsonToRdf(String label, JSONObject appObject,
      Map<String, String> authorizationHeader) {

    HTTPRepository repo = new HTTPRepository(Utilities.serverURL, label);
    repo.setAdditionalHttpHeaders(authorizationHeader);
    repo.init();
    RepositoryConnection connection = repo.getConnection();

    /* String that will capture the dynamically populated delete and where clause in query */
    String deleteClause = "";
    String whereClause = "";
    String insertClause = "";

    /* String that will capture the final query */
    String queryString = "";

    /* temp counter */
    int counter = 0;

    /* Set unix timestamp */
    long timestamp = System.currentTimeMillis() / 1000;

    queryString +=
        "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "DELETE { \n";
    queryString +=
        "  GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \n";

    for (Iterator iterator = appObject.keySet().iterator(); iterator.hasNext(); ) {
      String key = (String) iterator.next();

      if (key.contentEquals("OtherLanguageCode")) {
        continue;
      }

      String value = (String) appObject.get(key);

      if (key.contentEquals("profile_updated")) {
        break;
      } else {
        if (!value.contentEquals("")) {
          deleteClause += "   welcome:tcn_user welcome:has" + key + " ?" + key + " . \n";
          deleteClause += "   ?" + key + " a welcome:" + key + " ; \n";
          deleteClause += "     welcome:hasValue ?value" + counter + " ; \n";
          deleteClause += "     welcome:lastUpdated ?timestamp" + counter + " . \n";

          insertClause += "    	welcome:tcn_user welcome:has" + key + " \n";
          insertClause += "        [   a welcome:" + key + " ; \n";
          insertClause += "        	welcome:hasValue \"" + value + "\" ; \n";
          insertClause += "         welcome:lastUpdated " + timestamp + "  \n";
          insertClause += "        ]. \n";

          whereClause += "    OPTIONAL { \n";
          whereClause += "      welcome:tcn_user welcome:has" + key + " ?" + key + " . \n";
          whereClause += "      ?" + key + " a welcome:" + key + " ; \n";
          whereClause += "        welcome:hasValue ?value" + counter + " ; \n";
          whereClause += "        welcome:lastUpdated ?timestamp" + counter + " . \n";
          whereClause += "    } \n";

          counter += 1;
        } else {
          // q.removeTCNData(key, graph);
          deleteClause += "   welcome:tcn_user welcome:has" + key + " ?" + key + " . \n";
          deleteClause += "   ?" + key + " a welcome:" + key + " ; \n";
          deleteClause += "     welcome:hasValue ?value" + counter + " ; \n";
          deleteClause += "     welcome:lastUpdated ?timestamp" + counter + " . \n";

          whereClause += "    OPTIONAL { \n";
          whereClause += "      welcome:tcn_user welcome:has" + key + " ?" + key + " . \n";
          whereClause += "      ?" + key + " a welcome:" + key + " ; \n";
          whereClause += "        welcome:hasValue ?value" + counter + " ; \n";
          whereClause += "        welcome:lastUpdated ?timestamp" + counter + " . \n";
          whereClause += "    } \n";

          counter += 1;
        }
      }
    }

    queryString += deleteClause;
    queryString += "  } \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString +=
        "  GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \n";
    queryString += insertClause;
    queryString += "} \n";
    queryString += "} WHERE { \n";
    queryString +=
        "  GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \n";
    queryString += whereClause;
    queryString += "  } \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(connection, queryString);

    /* Set default language to english */
    Locale locale = new Locale("en_US");
    Locale.setDefault(locale);

    if (appObject.containsKey("LanguageCode")) {
      String f6_iso = (String) appObject.get("LanguageCode");

      String value;
      if (!f6_iso.contentEquals("")) {
        if (f6_iso.length() == 3) {
          value = Languages.getNameFromCode(connection, f6_iso);
        } else {
          /* Find the full language name */
          Locale l2 = new Locale(f6_iso, "");

          String language = l2.getDisplayLanguage();
          value = (language != null) ? language : f6_iso;
        }

        String key = "Language";
        boolean e = q.checkIfPopulated(connection, key);

        if (!value.contentEquals("")) {
          if (e) {
            q.populateTCNProfile(connection, key, value);
          } else {
            q.insertTCNData(connection, key, value);
          }
        } else {
          q.removeTCNData(connection, key);
        }
      }
    }

    String natLang = null;
    if (appObject.containsKey("NativeLanguageCode")) {
      String f6_iso = (String) appObject.get("NativeLanguageCode");

      String value;
      if (!f6_iso.contentEquals("")) {
        if (f6_iso.length() == 3) {
          value = Languages.getNameFromCode(connection, f6_iso);

          // assign for later usage
          natLang = value;
        } else {
          /* Find the full language name */
          Locale l2 = new Locale(f6_iso, "");

          String language = l2.getDisplayLanguage();
          value = (language != null) ? language : f6_iso;

          // assign for later usage
          natLang = value;
        }

        String key = "NativeLanguageName";
        boolean e = q.checkIfPopulated(connection, key);

        if (!value.contentEquals("")) {
          if (e) {
            q.populateTCNProfile(connection, key, value);
          } else {
            q.insertTCNData(connection, key, value);
          }
        } else {
          q.removeTCNData(connection, key);
        }
      }
    }

    if (appObject.containsKey("OtherLanguageCode")) {

      if (appObject.get("OtherLanguageCode") instanceof JSONArray) {
        IRI user = connection.getValueFactory()
            .createIRI(WELCOME.NAMESPACE + "tcn_user");

        // This will remove the whole list
        deleteOtherLanguageCode(connection, "hasOtherLanguageCode");
        deleteOtherLanguageCode(connection, "speaksLanguage");

        JSONArray languageCodeList = (JSONArray) appObject.get("OtherLanguageCode");

        IRI property1 = connection
            .getValueFactory()
            .createIRI(WELCOME.NAMESPACE + "speaksLanguage");

        IRI property2 = connection
            .getValueFactory()
            .createIRI(WELCOME.NAMESPACE + "hasOtherLanguageCode");

        List<String> languages = new ArrayList<>();
        List<String> languageCodes = new ArrayList<>();

        Iterator<String> it = languageCodeList.iterator();
        while (it.hasNext()) {
          String otherLanguageCode = it.next().replace("'", "");

          if (otherLanguageCode.length() == 3) {
            String f = Languages.getNameFromCode(connection, otherLanguageCode);
            languages.add(f);
            languageCodes.add(otherLanguageCode);
          } else {
            /* Find the full language name */
            Locale l2 = new Locale(otherLanguageCode, "");

            String language = l2.getDisplayLanguage();
            String f = (language != null) ? language : otherLanguageCode;
            languages.add(f);
            languageCodes.add(otherLanguageCode);
          }
        }

        if (!languages.contains(natLang)) {
          languages.add(natLang);
        }

        BNode head1 = connection.getValueFactory().createBNode();
        Model model1 = RDFCollections.asRDF(languages, head1, new LinkedHashModel());

        model1.add(user, property1, head1);

        /* Create user IRI */
        IRI graph = connection.getValueFactory()
            .createIRI(WELCOME.NAMESPACE, "ProfileInfo");

        Utilities.commitModel(connection, model1, graph);

        BNode head2 = connection.getValueFactory().createBNode();
        Model model2 = RDFCollections.asRDF(languageCodes, head2, new LinkedHashModel());

        model2.add(user, property2, head2);

        Utilities.commitModel(connection, model2, graph);
      } else {
        String f6_iso = (String) appObject.get("OtherLanguageCode");

        String value;
        if (!f6_iso.contentEquals("")) {
          if (f6_iso.length() == 3) {
            value = Languages.getNameFromCode(connection, f6_iso);
          } else {
            /* Find the full language name */
            Locale l2 = new Locale(f6_iso, "");

            String language = l2.getDisplayLanguage();
            value = (language != null) ? language : f6_iso;
          }

          String key = "OtherLanguage";
          boolean e = q.checkIfPopulated(connection, key);

          if (!value.contentEquals("")) {
            if (e) {
              q.populateTCNProfile(connection, key, value);
            } else {
              q.insertTCNData(connection, key, value);
            }
          } else {
            q.removeTCNData(connection, key);
          }
        }
      }

    }

//    if (appObject.containsKey("OtherLanguageCode")) {

//    }

    if (appObject.containsKey("CountryCode")) {
      String f8_iso = (String) appObject.get("CountryCode");

      if (!f8_iso.contentEquals("")) {
        /* Find the full country name */
        Locale l0 = new Locale("", f8_iso);

        String host_country = l0.getDisplayCountry();
        String value = (host_country != null) ? host_country : f8_iso;

        String key = "Country";
        boolean e = q.checkIfPopulated(connection, key);

        if (!value.contentEquals("")) {
          if (e) {
            q.populateTCNProfile(connection, key, value);
          } else {
            q.insertTCNData(connection, key, value);
          }
        } else {
          q.removeTCNData(connection, key);
        }
      }
    }

    if (appObject.containsKey("CountryOfOriginCode")) {
      String f8_iso = (String) appObject.get("CountryOfOriginCode");

      if (!f8_iso.contentEquals("")) {
        /* Find the full country name */
        Locale l0 = new Locale("", f8_iso);

        String host_country = l0.getDisplayCountry();
        String value = (host_country != null) ? host_country : f8_iso;

        String key = "CountryOfOrigin";
        boolean e = q.checkIfPopulated(connection, key);

        if (!value.contentEquals("")) {
          if (e) {
            q.populateTCNProfile(connection, key, value);
          } else {
            q.insertTCNData(connection, key, value);
          }
        } else {
          q.removeTCNData(connection, key);
        }
      }
    }

    if (appObject.containsKey("IDCountryCode")) {
      String f8_iso = (String) appObject.get("IDCountryCode");

      if (!f8_iso.contentEquals("")) {
        /* Find the full country name */
        Locale l0 = new Locale("", f8_iso);

        String host_country = l0.getDisplayCountry();
        String value = (host_country != null) ? host_country : f8_iso;

        String key = "IDCountry";
        boolean e = q.checkIfPopulated(connection, key);

        if (!value.contentEquals("")) {
          if (e) {
            q.populateTCNProfile(connection, key, value);
          } else {
            q.insertTCNData(connection, key, value);
          }
        } else {
          q.removeTCNData(connection, key);
        }
      }
    }

    if (appObject.containsKey("CountryOfBirthCode")) {
      String f8_iso = (String) appObject.get("CountryOfBirthCode");

      if (!f8_iso.contentEquals("")) {
        /* Find the full country name */
        Locale l0 = new Locale("", f8_iso);

        String host_country = l0.getDisplayCountry();
        String value = (host_country != null) ? host_country : f8_iso;

        String key = "CountryOfBirth";
        boolean e = q.checkIfPopulated(connection, key);

        if (!value.contentEquals("")) {
          if (e) {
            q.populateTCNProfile(connection, key, value);
          } else {
            q.insertTCNData(connection, key, value);
          }
        } else {
          q.removeTCNData(connection, key);
        }
      }
    }

    if (appObject.containsKey("NationalityCode")) {
      String f8_iso = (String) appObject.get("NationalityCode");

      if (!f8_iso.contentEquals("")) {
        /* Find the full country name */
        Locale l0 = new Locale("", f8_iso);

        String host_country = l0.getDisplayCountry();
        String value = (host_country != null) ? host_country : f8_iso;

        String key = "Nationality";
        boolean e = q.checkIfPopulated(connection, key);

        if (!value.contentEquals("")) {
          if (e) {
            q.populateTCNProfile(connection, key, value);
          } else {
            q.insertTCNData(connection, key, value);
          }
        } else {
          q.removeTCNData(connection, key);
        }
      }
    }

    if (appObject.containsKey("PreviousResidenceOtherCode")) {
      String f8_iso = (String) appObject.get("PreviousResidenceOtherCode");

      if (!f8_iso.contentEquals("")) {
        /* Find the full country name */
        Locale l0 = new Locale("", f8_iso);

        String host_country = l0.getDisplayCountry();
        String value = (host_country != null) ? host_country : f8_iso;

        String key = "PreviousResidenceOther";
        boolean e = q.checkIfPopulated(connection, key);

        if (!value.contentEquals("")) {
          if (e) {
            q.populateTCNProfile(connection, key, value);
          } else {
            q.insertTCNData(connection, key, value);
          }
        } else {
          q.removeTCNData(connection, key);
        }
      }
    }

    if (appObject.containsKey(("RegistrationStatus"))) {
      String status = (String) appObject.get("RegistrationStatus");
      if (status.contentEquals("true")) {
        q.insertRegStatus(connection);
      } else {
        q.deleteRegStatus(connection);
      }
    }
  }

  public void deleteOtherLanguageCode(RepositoryConnection connection, String property) {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += " \n";
    queryString += "DELETE { \n";
    queryString += "    welcome:tcn_user welcome:" + property + " ?list . \n";
    queryString += "    ?list rdf:type rdf:List . \n";
    queryString += "    ?z rdf:first ?head ; rdf:rest ?tail .  \n";
    queryString += "} WHERE {  \n";
    queryString += "      [] welcome:" + property + " ?list . \n";
    queryString += "      ?list rdf:rest* ?z . \n";
    queryString += "      ?z rdf:first ?head ; \n";
    queryString += "         rdf:rest ?tail . \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(connection, queryString);
  }

  public JSONArray getTCN(String label, String classroom, String attr, String scenario,
      Map<String, String> authorizationHeader) {

    // RemoteRepositoryManager manager = new RemoteRepositoryManager(util.serverURL);
    // manager.init();
    // ModelBuilder builder = new ModelBuilder();
    // agentKnowledge_<agentLabel>
    // System.out.println(util.serverURL);
    Repository repository = null;
    JSONArray jsonArray = new JSONArray();
    if (label != null) {
      HTTPRepository repo = new HTTPRepository(Utilities.serverURL, label);
      repo.setAdditionalHttpHeaders(authorizationHeader);
      // repository = manager.getRepository(label);
      repo.init();
      RepositoryConnection connection = repo.getConnection();
      jsonArray.add(takeTCN(label));
      return jsonArray;
    } else {
      // RemoteRepositoryManager manager = new RemoteRepositoryManager(util.serverURL);
      // repository = manager.getRepository(util.graphDB);
      // repository.init();
      // RepositoryConnection connection = repository.getConnection();

      HTTPRepository repo = new HTTPRepository(Utilities.serverURL, "global");
      repo.setAdditionalHttpHeaders(authorizationHeader);
      // repository = manager.getRepository(label);
      repo.init();
      RepositoryConnection connection = repo.getConnection();

      String queryLakrs = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
          + "PREFIX ajan: <http://www.ajan.de/ajan-ns#>\r\n"
          + "prefix welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
          + "prefix ag: <http://localhost:8080/welcome/integration/coordination/ajan/agents/>\r\n"
          + "           select ?s ?o ?iri_name ?iri_status where { \r\n"
          + "               ?s rdf:type ajan:Agent .\r\n"
          + "                ?s ajan:agentName ?o . "
          + "           bind(strafter(str(?s),str(ag:)) as ?iri_name)\r\n" + "           }";

      TupleQuery query = connection.prepareTupleQuery(queryLakrs);

      ArrayList<String> idAgent = new ArrayList<String>();
      try (TupleQueryResult result = query.evaluate()) {
        while (result.hasNext()) {
          BindingSet solution = result.next();
          idAgent.add(solution.getValue("o").toString().replaceAll("\"", ""));
          // System.out.println(solution.getValue("iri_name").toString().replaceAll("\"", ""));
        }
      }
      // System.out.println(idAgent);
      for (int i = 0; i < idAgent.size(); i++) {
        jsonArray.add(contrsuctionOfTCNInfo(idAgent.get(i), attr, scenario, authorizationHeader));
      }
      return jsonArray;
    }

  }

  public JSONObject contrsuctionOfTCNInfo(String label, String attr, String scenario,
      Map<String, String> authorizationHeader) {

    HTTPRepository repo = new HTTPRepository(Utilities.serverURL, label);
    repo.setAdditionalHttpHeaders(authorizationHeader);
    repo.init();
    RepositoryConnection connection = repo.getConnection();
    JSONObject responseDetailsJson = new JSONObject();

    List<String> items = Arrays.asList(attr.split("\\s*,\\s*"));
    JSONArray jsonArray = new JSONArray();

    if (items.contains("Minigame.minigameID") || items.contains("Minigame.score")
        || items.contains("Minigame.time")) {
      TupleQuery query = null;
      if (scenario == null) {
        query = connection.prepareTupleQuery(Queries.queryMinigame);
      } else {
        query = connection
            .prepareTupleQuery(que.takeIdsOfScenarios(scenario, "MinigameID", "Minigame"));
      }
      ArrayList<String> idMini = new ArrayList<String>();
      try (TupleQueryResult result = query.evaluate()) {
        while (result.hasNext()) {
          BindingSet solution = result.next();
          idMini.add(solution.getValue("iri_name").toString().replaceAll("\"", ""));
        }
      }
      // System.out.println(idMini);

      for (int j = 0; j < idMini.size(); j++) {

        String minigameID = "";
        String score = "";
        String time = "";

        // System.out.println(items.get(i).substring(items.get(i).lastIndexOf(".") + 1));
        JSONObject responseMinigameDetailsJson = new JSONObject();
        if (items.contains("Minigame.minigameID")) {
          minigameID = que.execObtain(connection, idMini.get(j), "welcome", "hasMinigameID")
              .replaceAll("\"", "");
          if (minigameID.isEmpty()) {
            minigameID = "none";
          }
          responseMinigameDetailsJson.put("minigameID", minigameID);
        }
        if (items.contains("Minigame.score")) {
          score = que.execObtain(connection, idMini.get(j), "welcome", "hasScore")
              .replaceAll("\"", "");
          if (score.isEmpty()) {
            score = "0";
          }
          responseMinigameDetailsJson.put("score", Float.parseFloat(score));
        }
        if (items.contains("Minigame.time")) {
          time = que.execObtain(connection, idMini.get(j), "welcome", "hasTime")
              .replaceAll("\"", "");
          if (time.isEmpty()) {
            time = "0";
          }
          responseMinigameDetailsJson.put("time", time);
        }
        jsonArray.add(responseMinigameDetailsJson);

      }

      responseDetailsJson.put("Minigame", jsonArray);

    } else if (items.contains("Assessment.LessonId") || items.contains("Assessment.AssessmentScore")
        || items.contains("Assessment.ListeningScore")
        || items.contains("Assessment.VocabularyScore") || items.contains("Assessment.WritingScore")
        || items.contains("Assessment.ReadingScore")) {
      TupleQuery query = null;
      if (scenario == null) {
        query = connection.prepareTupleQuery(Queries.queryAssessment);
      } else {
        query = connection.prepareTupleQuery(que.takeIdsOfAssessments(scenario));
      }

      ArrayList<String> idMini = new ArrayList<String>();
      try (TupleQueryResult result = query.evaluate()) {
        while (result.hasNext()) {
          BindingSet solution = result.next();
          idMini.add(solution.getValue("iri_name").toString().replaceAll("\"", ""));
        }
      }
      // System.out.println("assessment: " + idMini + " scenario: " + scenario);
      // JSONArray jsonArray = new JSONArray();
      for (int j = 0; j < idMini.size(); j++) {

        String AssessmentScore = "";
        String LessonId = "";
        String ListeningScore = "";
        String ReadingScore = "";
        String VocabularyScore = "";
        String WritingScore = "";

        // System.out.println(items.get(i).substring(items.get(i).lastIndexOf(".") + 1));
        JSONObject responseMinigameDetailsJson = new JSONObject();
        if (items.contains("Assessment.LessonId")) {
          LessonId = que.execObtain(connection, idMini.get(j), "welcome", "hasLessonId")
              .replaceAll("\"", "");
          if (LessonId.isEmpty()) {
            LessonId = "none";
          }
          responseMinigameDetailsJson.put("LessonId",
              LessonId.replace("^^<http://www.w3.org/2001/XMLSchema#integer>", ""));
        }
        if (items.contains("Assessment.AssessmentScore")) {
          AssessmentScore =
              que.execObtain(connection, idMini.get(j), "welcome", "hasAssessmentScore")
                  .replaceAll("\"", "");
          if (AssessmentScore.isEmpty()) {
            AssessmentScore = "0";
          }
          responseMinigameDetailsJson.put("AssessmentScore", Float.parseFloat(
              AssessmentScore.replace("^^<http://www.w3.org/2001/XMLSchema#float>", "")));
        }
        if (items.contains("Assessment.ListeningScore")) {
          ListeningScore = que.execObtain(connection, idMini.get(j), "welcome", "hasListeningScore")
              .replaceAll("\"", "");
          if (ListeningScore.isEmpty()) {
            ListeningScore = "0";
          }
          responseMinigameDetailsJson.put("ListeningScore", Float.parseFloat(
              ListeningScore.replace("^^<http://www.w3.org/2001/XMLSchema#float>", "")));
        }
        if (items.contains("Assessment.ReadingScore")) {
          ReadingScore = que.execObtain(connection, idMini.get(j), "welcome", "hasReadingScore")
              .replaceAll("\"", "");
          if (ReadingScore.isEmpty()) {
            ReadingScore = "0";
          }
          responseMinigameDetailsJson.put("ReadingScore", Float
              .parseFloat(ReadingScore.replace("^^<http://www.w3.org/2001/XMLSchema#float>", "")));
        }
        if (items.contains("Assessment.VocabularyScore")) {
          VocabularyScore =
              que.execObtain(connection, idMini.get(j), "welcome", "hasVocabularyScore")
                  .replaceAll("\"", "");
          if (VocabularyScore.isEmpty()) {
            VocabularyScore = "0";
          }
          responseMinigameDetailsJson.put("VocabularyScore", Float.parseFloat(
              VocabularyScore.replace("^^<http://www.w3.org/2001/XMLSchema#float>", "")));
        }
        if (items.contains("Assessment.WritingScore")) {
          WritingScore = que.execObtain(connection, idMini.get(j), "welcome", "hasWritingScore")
              .replaceAll("\"", "");
          if (WritingScore.isEmpty()) {
            WritingScore = "0";
          }
          responseMinigameDetailsJson.put("WritingScore", Float
              .parseFloat(WritingScore.replace("^^<http://www.w3.org/2001/XMLSchema#float>", "")));
        }
        jsonArray.add(responseMinigameDetailsJson);
      }

      responseDetailsJson.put("Assessment", jsonArray);


    } else if (items.contains("ApplicationForm.scenarioID")
        || items.contains("ApplicationForm.stage") || items.contains("ApplicationForm.stagescore")
        || items.contains("ApplicationForm.stagetime")
        || items.contains("ApplicationForm.attempts")) {
      TupleQuery query = null;
      if (scenario == null) {
        query = connection.prepareTupleQuery(Queries.queryApplicationForm);
      } else {
        query = connection
            .prepareTupleQuery(que.takeIdsOfScenarios(scenario, "ScenarioID", "ApplicationForm"));
      }
      ArrayList<String> idMini = new ArrayList<String>();
      try (TupleQueryResult result = query.evaluate()) {
        while (result.hasNext()) {
          BindingSet solution = result.next();
          idMini.add(solution.getValue("iri_name").toString().replaceAll("\"", ""));
        }
      }
      // JSONArray jsonArray = new JSONArray();
      for (int j = 0; j < idMini.size(); j++) {

        String scenarioID = "";
        String stage = "";
        String stagescore = "";
        String stagetime = "";
        String attempts = "";

        // System.out.println(items.get(i).substring(items.get(i).lastIndexOf(".") + 1));
        JSONObject responseMinigameDetailsJson = new JSONObject();
        if (items.contains("ApplicationForm.scenarioID")) {
          scenarioID = que.execObtain(connection, idMini.get(j), "welcome", "hasScenarioID")
              .replaceAll("\"", "");
          if (scenarioID.isEmpty()) {
            scenarioID = "none";
          }
          responseMinigameDetailsJson.put("scenarioID", scenarioID);
        }
        if (items.contains("ApplicationForm.stage")) {
          stage = que.execObtain(connection, idMini.get(j), "welcome", "hasStage")
              .replaceAll("\"", "");
          if (stage.isEmpty()) {
            stage = "0";
          }
          responseMinigameDetailsJson.put("stage", stage);
        }
        if (items.contains("ApplicationForm.stagescore")) {
          stagescore = que.execObtain(connection, idMini.get(j), "welcome", "hasStageScore")
              .replaceAll("\"", "");
          if (stagescore.isEmpty()) {
            stagescore = "0";
          }
          responseMinigameDetailsJson.put("stagescore", Float.parseFloat(stagescore));
        }
        if (items.contains("ApplicationForm.stagetime")) {
          stagetime = que.execObtain(connection, idMini.get(j), "welcome", "hasStageTime")
              .replaceAll("\"", "");
          if (stagetime.isEmpty()) {
            stagetime = "0";
          }
          responseMinigameDetailsJson.put("stagetime", stagetime);
        }
        if (items.contains("ApplicationForm.attempts")) {
          attempts = que.execObtain(connection, idMini.get(j), "welcome", "hasAttempts")
              .replaceAll("\"", "");
          if (attempts.isEmpty()) {
            attempts = "0";
          }
          responseMinigameDetailsJson.put("attempts", attempts);
        }
        jsonArray.add(responseMinigameDetailsJson);
      }

      responseDetailsJson.put("ApplicationForm", jsonArray);

    } else if (items.contains("LocationPreference")) {

      ArrayList<String> locations = new ArrayList<String>();

      TupleQuery query = connection.prepareTupleQuery(Queries.locPreference);
      try (TupleQueryResult result = query.evaluate()) {
        while (result.hasNext()) {
          BindingSet solution = result.next(); //
          // System.out.println(solution.getValue("loc").toString().replaceAll("\"", ""));
          locations.add(solution.getValue("loc").toString().replaceAll("\"", ""));
        }
      } catch (QueryEvaluationException | RepositoryException e) {
        System.out.println(e);
      }
      responseDetailsJson.put("LocationPreference", locations);
    }
    if (items.contains("Age")) {
      if (que.execObtainStrTCN(connection, "Birthday").equals("")) {
        responseDetailsJson.put("Age", 0);
      } else {
        LocalDate dob = LocalDate.parse(que.execObtainStrTCN(connection, "Birthday"));
        // System.out.println(calculateAge(dob));
        responseDetailsJson.put("Age", calculateAge(dob));
      }
    }
    if (items.contains("EducationLevel")) {
      if (que.execObtainStrTCN(connection, "EducationLevel").equals("")) {
        responseDetailsJson.put("EducationLevel", "none");
      } else {
        responseDetailsJson.put("EducationLevel",
            que.execObtainStrTCN(connection, "EducationLevel"));
      }
    }
    if (items.contains("Gender")) {
      if (que.execObtainStrTCN(connection, "Gender").equals("")) {
        responseDetailsJson.put("Gender", "none");
      } else {
        responseDetailsJson.put("Gender", que.execObtainStrTCN(connection, "Gender"));
      }
    }
    if (items.contains("CountryOfOrigin")) {
      if (que.execObtainStrTCN(connection, "CountryOfOrigin").equals("")) {
        responseDetailsJson.put("CountryOfOrigin", "none");
      } else {
        responseDetailsJson.put("CountryOfOrigin",
            que.execObtainStrTCN(connection, "CountryOfOrigin"));
      }
    }
    if (items.contains("Language")) {
      if (que.execObtainStrTCN(connection, "Language").equals("")) {
        responseDetailsJson.put("Language", "none");
      } else {
        responseDetailsJson.put("Language", que.execObtainStrTCN(connection, "Language"));
      }
    }
    if (items.contains("UserId")) {
      if (que.execObtainStrTCN(connection, "UserId").equals("")) {
        responseDetailsJson.put("UserId", "none");
      } else {
        responseDetailsJson.put("UserId", que.execObtainStrTCN(connection, "UserId"));
      }
    }
    if (items.contains("Name")) {
      if (que.execObtainStrTCN(connection, "Name").equals("")) {
        responseDetailsJson.put("Name", "none");
      } else {
        responseDetailsJson.put("Name", que.execObtainStrTCN(connection, "Name"));
      }
    }
    if (items.contains("FirstSurname")) {
      if (que.execObtainStrTCN(connection, "FirstSurname").equals("")) {
        responseDetailsJson.put("FirstSurname", "none");
      } else {
        responseDetailsJson.put("FirstSurname", que.execObtainStrTCN(connection, "FirstSurname"));
      }
    }
    if (items.contains("Classroom")) {
      if (que.execObtainStrTCN(connection, "Classroom").equals("")) {
        responseDetailsJson.put("Classroom", "none");
      } else {
        responseDetailsJson.put("Classroom", que.execObtainStrTCN(connection, "Classroom"));
      }
    }
    /////////////////////////////
    if (items.contains("SecondSurname")) {
      if (que.execObtainStrTCN(connection, "SecondSurname").equals("")) {
        responseDetailsJson.put("SecondSurname", "none");
      } else {
        responseDetailsJson.put("SecondSurname", que.execObtainStrTCN(connection, "SecondSurname"));
      }
    }
    if (items.contains("Birthday")) {
      if (que.execObtainStrTCN(connection, "Birthday").equals("")) {
        responseDetailsJson.put("Birthday", "none");
      } else {
        responseDetailsJson.put("Birthday", que.execObtainStrTCN(connection, "Birthday"));
      }
    }
    if (items.contains("Nationality")) {
      if (que.execObtainStrTCN(connection, "Nationality").equals("")) {
        responseDetailsJson.put("Nationality", "none");
      } else {
        responseDetailsJson.put("Nationality", que.execObtainStrTCN(connection, "Nationality"));
      }
    }
    if (items.contains("Ethnicity")) {
      if (que.execObtainStrTCN(connection, "Ethnicity").equals("")) {
        responseDetailsJson.put("Ethnicity", "none");
      } else {
        responseDetailsJson.put("Ethnicity", que.execObtainStrTCN(connection, "Ethnicity"));
      }
    }
    if (items.contains("IDType")) {
      if (que.execObtainStrTCN(connection, "IDType").equals("")) {
        responseDetailsJson.put("IDType", "none");
      } else {
        responseDetailsJson.put("IDType", que.execObtainStrTCN(connection, "IDType"));
      }
    }
    if (items.contains("IDNumber")) {
      if (que.execObtainStrTCN(connection, "IDNumber").equals("")) {
        responseDetailsJson.put("IDNumber", "none");
      } else {
        responseDetailsJson.put("IDNumber", que.execObtainStrTCN(connection, "IDNumber"));
      }
    }
    if (items.contains("MobilePhone")) {
      if (que.execObtainStrTCN(connection, "MobilePhone").equals("")) {
        responseDetailsJson.put("MobilePhone", "none");
      } else {
        responseDetailsJson.put("MobilePhone", que.execObtainStrTCN(connection, "MobilePhone"));
      }
    }
    if (items.contains("Email")) {
      if (que.execObtainStrTCN(connection, "Email").equals("")) {
        responseDetailsJson.put("Email", "none");
      } else {
        responseDetailsJson.put("Email", que.execObtainStrTCN(connection, "Email"));
      }
    }
    if (items.contains("Landline")) {
      if (que.execObtainStrTCN(connection, "Landline").equals("")) {
        responseDetailsJson.put("Landline", "none");
      } else {
        responseDetailsJson.put("Landline", que.execObtainStrTCN(connection, "Landline"));
      }
    }
    if (items.contains("BuildingName")) {
      if (que.execObtainStrTCN(connection, "BuildingName").equals("")) {
        responseDetailsJson.put("BuildingName", "none");
      } else {
        responseDetailsJson.put("BuildingName", que.execObtainStrTCN(connection, "BuildingName"));
      }
    }

    if (items.contains("StreetName")) {
      if (que.execObtainStrTCN(connection, "StreetName").equals("")) {
        responseDetailsJson.put("StreetName", "none");
      } else {
        responseDetailsJson.put("StreetName", que.execObtainStrTCN(connection, "StreetName"));
      }
    }
    if (items.contains("City")) {
      if (que.execObtainStrTCN(connection, "City").equals("")) {
        responseDetailsJson.put("City", "none");
      } else {
        responseDetailsJson.put("City", que.execObtainStrTCN(connection, "City"));
      }
    }
    if (items.contains("Province")) {
      if (que.execObtainStrTCN(connection, "Province").equals("")) {
        responseDetailsJson.put("Province", "none");
      } else {
        responseDetailsJson.put("Province", que.execObtainStrTCN(connection, "Province"));
      }
    }
    if (items.contains("PostCode")) {
      if (que.execObtainStrTCN(connection, "PostCode").equals("")) {
        responseDetailsJson.put("PostCode", "none");
      } else {
        responseDetailsJson.put("PostCode", que.execObtainStrTCN(connection, "PostCode"));
      }
    }
    if (items.contains("ResidentStatus")) {
      if (que.execObtainStrTCN(connection, "ResidentStatus").equals("")) {
        responseDetailsJson.put("ResidentStatus", "none");
      } else {
        responseDetailsJson.put("ResidentStatus",
            que.execObtainStrTCN(connection, "ResidentStatus"));
      }
    }
    if (items.contains("LegalStatus")) {
      if (que.execObtainStrTCN(connection, "LegalStatus").equals("")) {
        responseDetailsJson.put("LegalStatus", "none");
      } else {
        responseDetailsJson.put("LegalStatus", que.execObtainStrTCN(connection, "LegalStatus"));
      }
    }
    if (items.contains("YearArrivalRegion")) {
      if (que.execObtainStrTCN(connection, "YearArrivalRegion").equals("")) {
        responseDetailsJson.put("YearArrivalRegion", "none");
      } else {
        responseDetailsJson.put("YearArrivalRegion",
            que.execObtainStrTCN(connection, "YearArrivalRegion"));
      }
    }

    if (items.contains("YearArrivalCity")) {
      if (que.execObtainStrTCN(connection, "YearArrivalCity").equals("")) {
        responseDetailsJson.put("YearArrivalCity", "none");
      } else {
        responseDetailsJson.put("YearArrivalCity",
            que.execObtainStrTCN(connection, "YearArrivalCity"));
      }
    }
    if (items.contains("PrevResidenceBuildingNumber")) {
      if (que.execObtainStrTCN(connection, "PrevResidenceBuildingNumber").equals("")) {
        responseDetailsJson.put("PrevResidenceBuildingNumber", "none");
      } else {
        responseDetailsJson.put("PrevResidenceBuildingNumber",
            que.execObtainStrTCN(connection, "PrevResidenceBuildingNumber"));
      }
    }
    if (items.contains("PrevResidenceStreetName")) {
      if (que.execObtainStrTCN(connection, "PrevResidenceStreetName").equals("")) {
        responseDetailsJson.put("PrevResidenceStreetName", "none");
      } else {
        responseDetailsJson.put("PrevResidenceStreetName",
            que.execObtainStrTCN(connection, "PrevResidenceStreetName"));
      }
    }
    if (items.contains("PrevResidenceCity")) {
      if (que.execObtainStrTCN(connection, "PrevResidenceCity").equals("")) {
        responseDetailsJson.put("PrevResidenceCity", "none");
      } else {
        responseDetailsJson.put("PrevResidenceCity",
            que.execObtainStrTCN(connection, "PrevResidenceCity"));
      }
    }
    if (items.contains("PrevResidenceProvince")) {
      if (que.execObtainStrTCN(connection, "PrevResidenceProvince").equals("")) {
        responseDetailsJson.put("PrevResidenceProvince", "none");
      } else {
        responseDetailsJson.put("PrevResidenceProvince",
            que.execObtainStrTCN(connection, "PrevResidenceProvince"));
      }
    }
    if (items.contains("PrevResidencePostCode")) {
      if (que.execObtainStrTCN(connection, "PrevResidencePostCode").equals("")) {
        responseDetailsJson.put("PrevResidencePostCode", "none");
      } else {
        responseDetailsJson.put("PrevResidencePostCode",
            que.execObtainStrTCN(connection, "PrevResidencePostCode"));
      }
    }
    if (items.contains("PrevResidenceCountry")) {
      if (que.execObtainStrTCN(connection, "PrevResidenceCountry").equals("")) {
        responseDetailsJson.put("PrevResidenceCountry", "none");
      } else {
        responseDetailsJson.put("PrevResidenceCountry",
            que.execObtainStrTCN(connection, "PrevResidenceCountry"));
      }
    }
    if (items.contains("ArrivalDate")) {
      if (que.execObtainStrTCN(connection, "ArrivalDate").equals("")) {
        responseDetailsJson.put("ArrivalDate", "none");
      } else {
        responseDetailsJson.put("ArrivalDate", que.execObtainStrTCN(connection, "ArrivalDate"));
      }
    }
    if (items.contains("AsylumClaim")) {
      if (que.execObtainStrTCN(connection, "AsylumClaim").equals("")) {
        responseDetailsJson.put("AsylumClaim", "none");
      } else {
        responseDetailsJson.put("AsylumClaim", que.execObtainStrTCN(connection, "AsylumClaim"));
      }
    }
    if (items.contains("DurationOfStay")) {
      if (que.execObtainStrTCN(connection, "DurationOfStay").equals("")) {
        responseDetailsJson.put("DurationOfStay", "none");
      } else {
        responseDetailsJson.put("DurationOfStay",
            que.execObtainStrTCN(connection, "DurationOfStay"));
      }
    }
    if (items.contains("AsylumApplication")) {
      if (que.execObtainStrTCN(connection, "AsylumApplication").equals("")) {
        responseDetailsJson.put("AsylumApplication", "none");
      } else {
        responseDetailsJson.put("AsylumApplication",
            que.execObtainStrTCN(connection, "AsylumApplication"));
      }
    }
    if (items.contains("WorkExperienceCountryOfOrigin")) {
      if (que.execObtainStrTCN(connection, "WorkExperienceCountryOfOrigin").equals("")) {
        responseDetailsJson.put("WorkExperienceCountryOfOrigin", "none");
      } else {
        responseDetailsJson.put("WorkExperienceCountryOfOrigin",
            que.execObtainStrTCN(connection, "WorkExperienceCountryOfOrigin"));
      }
    }
    if (items.contains("HostCountryProfession")) {
      if (que.execObtainStrTCN(connection, "HostCountryProfession").equals("")) {
        responseDetailsJson.put("HostCountryProfession", "none");
      } else {
        responseDetailsJson.put("HostCountryProfession",
            que.execObtainStrTCN(connection, "HostCountryProfession"));
      }
    }
    if (items.contains("CountryOfOriginProfession")) {
      if (que.execObtainStrTCN(connection, "CountryOfOriginProfession").equals("")) {
        responseDetailsJson.put("CountryOfOriginProfession", "none");
      } else {
        responseDetailsJson.put("CountryOfOriginProfession",
            que.execObtainStrTCN(connection, "CountryOfOriginProfession"));
      }
    }

    if (items.contains("EmploymentStatus")) {
      if (que.execObtainStrTCN(connection, "EmploymentStatus").equals("")) {
        responseDetailsJson.put("EmploymentStatus", "none");
      } else {
        responseDetailsJson.put("EmploymentStatus",
            que.execObtainStrTCN(connection, "EmploymentStatus"));
      }
    }
    if (items.contains("WorkPermit")) {
      if (que.execObtainStrTCN(connection, "WorkPermit").equals("")) {
        responseDetailsJson.put("WorkPermit", "none");
      } else {
        responseDetailsJson.put("WorkPermit", que.execObtainStrTCN(connection, "WorkPermit"));
      }
    }
    if (items.contains("MaritalStatus")) {
      if (que.execObtainStrTCN(connection, "MaritalStatus").equals("")) {
        responseDetailsJson.put("MaritalStatus", "none");
      } else {
        responseDetailsJson.put("MaritalStatus", que.execObtainStrTCN(connection, "MaritalStatus"));
      }
    }
    if (items.contains("Municipality")) {
      if (que.execObtainStrTCN(connection, "Municipality").equals("")) {
        responseDetailsJson.put("Municipality", "none");
      } else {
        responseDetailsJson.put("Municipality", que.execObtainStrTCN(connection, "Municipality"));
      }
    }
    if (items.contains("FamilyMembers")) {
      if (que.execObtainStrTCN(connection, "FamilyMembers").equals("")) {
        responseDetailsJson.put("FamilyMembers", "none");
      } else {
        responseDetailsJson.put("FamilyMembers", que.execObtainStrTCN(connection, "FamilyMembers"));
      }
    }
    if (items.contains("ImmigratedAlone")) {
      if (que.execObtainStrTCN(connection, "ImmigratedAlone").equals("")) {
        responseDetailsJson.put("ImmigratedAlone", "none");
      } else {
        responseDetailsJson.put("ImmigratedAlone",
            que.execObtainStrTCN(connection, "ImmigratedAlone"));
      }
    }
    if (items.contains("TotalNumberChildren")) {
      if (que.execObtainStrTCN(connection, "TotalNumberChildren").equals("")) {
        responseDetailsJson.put("TotalNumberChildren", "none");
      } else {
        responseDetailsJson.put("TotalNumberChildren",
            que.execObtainStrTCN(connection, "TotalNumberChildren"));
      }
    }
    if (items.contains("NumberAccompaniedChildren")) {
      if (que.execObtainStrTCN(connection, "NumberAccompaniedChildren").equals("")) {
        responseDetailsJson.put("NumberAccompaniedChildren", "none");
      } else {
        responseDetailsJson.put("NumberAccompaniedChildren",
            que.execObtainStrTCN(connection, "NumberAccompaniedChildren"));
      }
    }
    if (items.contains("LearningHandicap")) {
      if (que.execObtainStrTCN(connection, "LearningHandicap").equals("")) {
        responseDetailsJson.put("LearningHandicap", "none");
      } else {
        responseDetailsJson.put("LearningHandicap",
            que.execObtainStrTCN(connection, "LearningHandicap"));
      }
    }
    if (items.contains("Illiteracy")) {
      if (que.execObtainStrTCN(connection, "Illiteracy").equals("")) {
        responseDetailsJson.put("Illiteracy", "none");
      } else {
        responseDetailsJson.put("Illiteracy", que.execObtainStrTCN(connection, "Illiteracy"));
      }
    }
    if (items.contains("HostLangLevel")) {
      if (que.execObtainStrTCN(connection, "HostLangLevel").equals("")) {
        responseDetailsJson.put("HostLangLevel", "none");
      } else {
        responseDetailsJson.put("HostLangLevel", que.execObtainStrTCN(connection, "HostLangLevel"));
      }
    }
    if (items.contains("PreferredLanguage")) {
      if (que.execObtainStrTCN(connection, "PreferredLanguage").equals("")) {
        responseDetailsJson.put("PreferredLanguage", "none");
      } else {
        responseDetailsJson.put("PreferredLanguage",
            que.execObtainStrTCN(connection, "PreferredLanguage"));
      }
    }
    if (items.contains("Administrator")) {
      if (que.execObtainStrTCN(connection, "Administrator").equals("")) {
        responseDetailsJson.put("Administrator", "none");
      } else {
        responseDetailsJson.put("Administrator", que.execObtainStrTCN(connection, "Administrator"));
      }
    }
    if (items.contains("CompletedModules")) {
      if (que.execObtainStrTCN(connection, "CompletedModules").equals("")) {
        responseDetailsJson.put("CompletedModules", "none");
      } else {
        responseDetailsJson.put("CompletedModules",
            que.execObtainStrTCN(connection, "CompletedModules"));
      }
    }

    if (items.contains("PendingModules")) {
      if (que.execObtainStrTCN(connection, "PendingModules").equals("")) {
        responseDetailsJson.put("PendingModules", "none");
      } else {
        responseDetailsJson.put("PendingModules",
            que.execObtainStrTCN(connection, "PendingModules"));
      }
    }
    if (items.contains("DigitalSkills")) {
      if (que.execObtainStrTCN(connection, "DigitalSkills").equals("")) {
        responseDetailsJson.put("DigitalSkills", "none");
      } else {
        responseDetailsJson.put("DigitalSkills", que.execObtainStrTCN(connection, "DigitalSkills"));
      }
    }
    if (items.contains("HealthIssues")) {
      if (que.execObtainStrTCN(connection, "HealthIssues").equals("")) {
        responseDetailsJson.put("HealthIssues", "none");
      } else {
        responseDetailsJson.put("HealthIssues", que.execObtainStrTCN(connection, "HealthIssues"));
      }
    }
    if (items.contains("AddictionIssues")) {
      if (que.execObtainStrTCN(connection, "AddictionIssues").equals("")) {
        responseDetailsJson.put("AddictionIssues", "none");
      } else {
        responseDetailsJson.put("AddictionIssues",
            que.execObtainStrTCN(connection, "AddictionIssues"));
      }
    }
    if (items.contains("Hobbies")) {
      if (que.execObtainStrTCN(connection, "Hobbies").equals("")) {
        responseDetailsJson.put("Hobbies", "none");
      } else {
        responseDetailsJson.put("Hobbies", que.execObtainStrTCN(connection, "Hobbies"));
      }
    }
    if (items.contains("ActivityParticipation")) {
      if (que.execObtainStrTCN(connection, "ActivityParticipation").equals("")) {
        responseDetailsJson.put("ActivityParticipation", "none");
      } else {
        responseDetailsJson.put("ActivityParticipation",
            que.execObtainStrTCN(connection, "ActivityParticipation"));
      }
    }
    if (items.contains("SMSNotifications")) {
      if (que.execObtainStrTCN(connection, "SMSNotifications").equals("")) {
        responseDetailsJson.put("SMSNotifications", "none");
      } else {
        responseDetailsJson.put("SMSNotifications",
            que.execObtainStrTCN(connection, "SMSNotifications"));
      }
    }
    if (items.contains("EmailNotifications")) {
      if (que.execObtainStrTCN(connection, "EmailNotifications").equals("")) {
        responseDetailsJson.put("EmailNotifications", "none");
      } else {
        responseDetailsJson.put("EmailNotifications",
            que.execObtainStrTCN(connection, "EmailNotifications"));
      }
    }
    if (items.contains("LccGenderPreference")) {
      if (que.execObtainStrTCN(connection, "LccGenderPreference").equals("")) {
        responseDetailsJson.put("LccGenderPreference", "none");
      } else {
        responseDetailsJson.put("LccGenderPreference",
            que.execObtainStrTCN(connection, "LccGenderPreference"));
      }
    }
    if (items.contains("LccNationPreference")) {
      if (que.execObtainStrTCN(connection, "LccNationPreference").equals("")) {
        responseDetailsJson.put("LccNationPreference", "none");
      } else {
        responseDetailsJson.put("LccNationPreference",
            que.execObtainStrTCN(connection, "LccNationPreference"));
      }
    }
    if (items.contains("OtherLanguages")) {
      if (que.execObtainStrTCN(connection, "OtherLanguages").equals("")) {
        responseDetailsJson.put("OtherLanguages", "none");
      } else {
        responseDetailsJson.put("OtherLanguages",
            que.execObtainStrTCN(connection, "OtherLanguages"));
      }
    }
    if (items.contains("KnowledgeCatalan")) {
      if (que.execObtainStrTCN(connection, "KnowledgeCatalan").equals("")) {
        responseDetailsJson.put("KnowledgeCatalan", "none");
      } else {
        responseDetailsJson.put("KnowledgeCatalan",
            que.execObtainStrTCN(connection, "KnowledgeCatalan"));
      }
    }
    if (items.contains("CoursesCatalan")) {
      if (que.execObtainStrTCN(connection, "CoursesCatalan").equals("")) {
        responseDetailsJson.put("CoursesCatalan", "none");
      } else {
        responseDetailsJson.put("CoursesCatalan",
            que.execObtainStrTCN(connection, "CoursesCatalan"));
      }
    }

    if (items.contains("KnowledgeSpanish")) {
      if (que.execObtainStrTCN(connection, "KnowledgeSpanish").equals("")) {
        responseDetailsJson.put("KnowledgeSpanish", "none");
      } else {
        responseDetailsJson.put("KnowledgeSpanish",
            que.execObtainStrTCN(connection, "KnowledgeSpanish"));
      }
    }
    if (items.contains("CoursesSpanish")) {
      if (que.execObtainStrTCN(connection, "CoursesSpanish").equals("")) {
        responseDetailsJson.put("CoursesSpanish", "none");
      } else {
        responseDetailsJson.put("CoursesSpanish",
            que.execObtainStrTCN(connection, "CoursesSpanish"));
      }
    }
    if (items.contains("KnowledgeLabour")) {
      if (que.execObtainStrTCN(connection, "KnowledgeLabour").equals("")) {
        responseDetailsJson.put("KnowledgeLabour", "none");
      } else {
        responseDetailsJson.put("KnowledgeLabour",
            que.execObtainStrTCN(connection, "KnowledgeLabour"));
      }
    }
    if (items.contains("CoursesLabour")) {
      if (que.execObtainStrTCN(connection, "CoursesLabour").equals("")) {
        responseDetailsJson.put("CoursesLabour", "none");
      } else {
        responseDetailsJson.put("CoursesLabour", que.execObtainStrTCN(connection, "CoursesLabour"));
      }
    }
    if (items.contains("KnowledgeSociety")) {
      if (que.execObtainStrTCN(connection, "KnowledgeSociety").equals("")) {
        responseDetailsJson.put("KnowledgeSociety", "none");
      } else {
        responseDetailsJson.put("KnowledgeSociety",
            que.execObtainStrTCN(connection, "KnowledgeSociety"));
      }
    }
    if (items.contains("CoursesSociety")) {
      if (que.execObtainStrTCN(connection, "CoursesSociety").equals("")) {
        responseDetailsJson.put("CoursesSociety", "none");
      } else {
        responseDetailsJson.put("CoursesSociety",
            que.execObtainStrTCN(connection, "CoursesSociety"));
      }
    }
    if (items.contains("CourseNameCatalan")) {
      if (que.execObtainStrTCN(connection, "CourseNameCatalan").equals("")) {
        responseDetailsJson.put("CourseNameCatalan", "none");
      } else {
        responseDetailsJson.put("CourseNameCatalan",
            que.execObtainStrTCN(connection, "CourseNameCatalan"));
      }
    }
    if (items.contains("CourseInstitutionCatalan")) {
      if (que.execObtainStrTCN(connection, "CourseInstitutionCatalan").equals("")) {
        responseDetailsJson.put("CourseInstitutionCatalan", "none");
      } else {
        responseDetailsJson.put("CourseInstitutionCatalan",
            que.execObtainStrTCN(connection, "CourseInstitutionCatalan"));
      }
    }
    if (items.contains("CompletionYearCatalan")) {
      if (que.execObtainStrTCN(connection, "CompletionYearCatalan").equals("")) {
        responseDetailsJson.put("CompletionYearCatalan", "none");
      } else {
        responseDetailsJson.put("CompletionYearCatalan",
            que.execObtainStrTCN(connection, "CompletionYearCatalan"));
      }
    }
    if (items.contains("DurationCatalan")) {
      if (que.execObtainStrTCN(connection, "DurationCatalan").equals("")) {
        responseDetailsJson.put("DurationCatalan", "none");
      } else {
        responseDetailsJson.put("DurationCatalan",
            que.execObtainStrTCN(connection, "DurationCatalan"));
      }
    }
    if (items.contains("CertificateCatalan")) {
      if (que.execObtainStrTCN(connection, "CertificateCatalan").equals("")) {
        responseDetailsJson.put("CertificateCatalan", "none");
      } else {
        responseDetailsJson.put("CertificateCatalan",
            que.execObtainStrTCN(connection, "CertificateCatalan"));
      }
    }
    if (items.contains("CourseNameSpanish")) {
      if (que.execObtainStrTCN(connection, "CourseNameSpanish").equals("")) {
        responseDetailsJson.put("CourseNameSpanish", "none");
      } else {
        responseDetailsJson.put("CourseNameSpanish",
            que.execObtainStrTCN(connection, "CourseNameSpanish"));
      }
    }
    if (items.contains("CourseInstitutionSpanish")) {
      if (que.execObtainStrTCN(connection, "CourseInstitutionSpanish").equals("")) {
        responseDetailsJson.put("CourseInstitutionSpanish", "none");
      } else {
        responseDetailsJson.put("CourseInstitutionSpanish",
            que.execObtainStrTCN(connection, "CourseInstitutionSpanish"));
      }
    }
    if (items.contains("CompletionYearSpanish")) {
      if (que.execObtainStrTCN(connection, "CompletionYearSpanish").equals("")) {
        responseDetailsJson.put("CompletionYearSpanish", "none");
      } else {
        responseDetailsJson.put("CompletionYearSpanish",
            que.execObtainStrTCN(connection, "CompletionYearSpanish"));
      }
    }
    if (items.contains("DurationSpanish")) {
      if (que.execObtainStrTCN(connection, "DurationSpanish").equals("")) {
        responseDetailsJson.put("DurationSpanish", "none");
      } else {
        responseDetailsJson.put("DurationSpanish",
            que.execObtainStrTCN(connection, "DurationSpanish"));
      }
    }
    if (items.contains("CertificateSpanish")) {
      if (que.execObtainStrTCN(connection, "CertificateSpanish").equals("")) {
        responseDetailsJson.put("CertificateSpanish", "none");
      } else {
        responseDetailsJson.put("CertificateSpanish",
            que.execObtainStrTCN(connection, "CertificateSpanish"));
      }
    }
    if (items.contains("CourseNameLabourMarket")) {
      if (que.execObtainStrTCN(connection, "CourseNameLabourMarket").equals("")) {
        responseDetailsJson.put("CourseNameLabourMarket", "none");
      } else {
        responseDetailsJson.put("CourseNameLabourMarket",
            que.execObtainStrTCN(connection, "CourseNameLabourMarket"));
      }
    }
    if (items.contains("CourseInstitutionLabourMarket")) {
      if (que.execObtainStrTCN(connection, "CourseInstitutionLabourMarket").equals("")) {
        responseDetailsJson.put("CourseInstitutionLabourMarket", "none");
      } else {
        responseDetailsJson.put("CourseInstitutionLabourMarket",
            que.execObtainStrTCN(connection, "CourseInstitutionLabourMarket"));
      }
    }
    if (items.contains("CompletionYearLabourMarket")) {
      if (que.execObtainStrTCN(connection, "CompletionYearLabourMarket").equals("")) {
        responseDetailsJson.put("CompletionYearLabourMarket", "none");
      } else {
        responseDetailsJson.put("CompletionYearLabourMarket",
            que.execObtainStrTCN(connection, "CompletionYearLabourMarket"));
      }
    }
    if (items.contains("DurationLabourMarket")) {
      if (que.execObtainStrTCN(connection, "DurationLabourMarket").equals("")) {
        responseDetailsJson.put("DurationLabourMarket", "none");
      } else {
        responseDetailsJson.put("DurationLabourMarket",
            que.execObtainStrTCN(connection, "DurationLabourMarket"));
      }
    }
    if (items.contains("CertificateLabourMarket")) {
      if (que.execObtainStrTCN(connection, "CertificateLabourMarket").equals("")) {
        responseDetailsJson.put("CertificateLabourMarket", "none");
      } else {
        responseDetailsJson.put("CertificateLabourMarket",
            que.execObtainStrTCN(connection, "CertificateLabourMarket"));
      }
    }

    if (items.contains("CourseNameCatalanSociety")) {
      if (que.execObtainStrTCN(connection, "CourseNameCatalanSociety").equals("")) {
        responseDetailsJson.put("CourseNameCatalanSociety", "none");
      } else {
        responseDetailsJson.put("CourseNameCatalanSociety",
            que.execObtainStrTCN(connection, "CourseNameCatalanSociety"));
      }
    }
    if (items.contains("CourseInstitutionCatalanSociety")) {
      if (que.execObtainStrTCN(connection, "CourseInstitutionCatalanSociety").equals("")) {
        responseDetailsJson.put("CourseInstitutionCatalanSociety", "none");
      } else {
        responseDetailsJson.put("CourseInstitutionCatalanSociety",
            que.execObtainStrTCN(connection, "CourseInstitutionCatalanSociety"));
      }
    }
    if (items.contains("CompletionYearCatalanSociety")) {
      if (que.execObtainStrTCN(connection, "CompletionYearCatalanSociety").equals("")) {
        responseDetailsJson.put("CompletionYearCatalanSociety", "none");
      } else {
        responseDetailsJson.put("CompletionYearCatalanSociety",
            que.execObtainStrTCN(connection, "CompletionYearCatalanSociety"));
      }
    }
    if (items.contains("DurationCatalanSociety")) {
      if (que.execObtainStrTCN(connection, "DurationCatalanSociety").equals("")) {
        responseDetailsJson.put("DurationCatalanSociety", "none");
      } else {
        responseDetailsJson.put("DurationCatalanSociety",
            que.execObtainStrTCN(connection, "DurationCatalanSociety"));
      }
    }
    if (items.contains("CertificateCatalanSociety")) {
      if (que.execObtainStrTCN(connection, "CertificateCatalanSociety").equals("")) {
        responseDetailsJson.put("CertificateCatalanSociety", "none");
      } else {
        responseDetailsJson.put("CertificateCatalanSociety",
            que.execObtainStrTCN(connection, "CertificateCatalanSociety"));
      }
    }

    if (items.contains("LccNationPrefWeight")) {
      if (que.execObtainStrTCN(connection, "LccNationPrefWeight").equals("")) {
        responseDetailsJson.put("LccNationPrefWeight", "none");
      } else {
        responseDetailsJson.put("LccNationPrefWeight",
            que.execObtainStrTCN(connection, "LccNationPrefWeight"));
      }
    }
    if (items.contains("LccGenderPrefWeight")) {
      if (que.execObtainStrTCN(connection, "LccGenderPrefWeight").equals("")) {
        responseDetailsJson.put("LccGenderPrefWeight", "none");
      } else {
        responseDetailsJson.put("LccGenderPrefWeight",
            que.execObtainStrTCN(connection, "LccGenderPrefWeight"));
      }
    }

    return responseDetailsJson;
  }

  public JSONObject takeTCN(String label) {
    JSONObject obj = q.getTCNProfile_S(label);

    return obj;

  }

  public String takeVRSaveProgress(String label) {
    RemoteRepositoryManager manager = new RemoteRepositoryManager(Utilities.serverURL);
    manager.init();
    ModelBuilder builder = new ModelBuilder();
    // agentKnowledge_<agentLabel>
    Repository repository = manager.getRepository(label);
    repository.init();
    RepositoryConnection connection = repository.getConnection();

    String queryVR = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
        + "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
        + "select ?o {\r\n" + "    ?s rdf:type welcome:VRSaveProgress .\r\n"
        + "    ?s welcome:hasSaveData ?o .\r\n" + "}";

    TupleQuery query = connection.prepareTupleQuery(queryVR);

    String progress = "";
    try (TupleQueryResult result = query.evaluate()) {
      while (result.hasNext()) {
        BindingSet solution = result.next();
        progress = solution.getValue("o").toString();
      }
    }

    return progress;
  }


}
