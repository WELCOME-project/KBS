package com.welcome.services;

import java.util.UUID;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.welcome.auxiliary.Queries;
import com.welcome.auxiliary.Utilities;

public class Minigame {

  Utilities util = new Utilities();
  Queries que = new Queries();
  private String minigameID = "";
  private String score = "";
  private String time = "";
  private String scenarioID = "";
  private String stage = "";
  private String stagescore = "";
  private String stagetime = "";
  private String attempts = "";

  public Minigame() {}
  // double d=Double.parseDouble("23.6");

  public void maniMinigame(String label, String body, String minigame) throws ParseException {
    RemoteRepositoryManager manager = new RemoteRepositoryManager(Utilities.serverURL);
    manager.init();
    ModelBuilder builder = new ModelBuilder();
    // agentKnowledge_<agentLabel>
    Repository repository = manager.getRepository(label);
    repository.init();
    RepositoryConnection connection = repository.getConnection();

    ValueFactory vf = repository.getValueFactory();

    String namespace =
        "https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#";
    // connection.clear(res);

    UUID uuid = UUID.randomUUID();

    JSONParser parser = new JSONParser();
    JSONObject jsonObject = (JSONObject) parser.parse(body);
    // versionJsonNumber = (String) jsonObject.get("versionJsonNumber");

    if (minigame.equals("Minigame")) {

      Resource res = vf.createIRI(
          "https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#MinigameInfo");
      IRI mnObj = vf.createIRI(namespace, "Minigame-" + uuid);

      if (jsonObject.containsKey("minigameID")) {
        minigameID = jsonObject.get("minigameID").toString();
        builder.namedGraph(res).subject(mnObj).add(
            SimpleValueFactory.getInstance().createIRI(namespace, "hasMinigameID"),
            (String) minigameID);
      }
      if (jsonObject.containsKey("score")) {
        score = jsonObject.get("score").toString();
        builder.namedGraph(res).subject(mnObj)
            .add(SimpleValueFactory.getInstance().createIRI(namespace, "hasScore"), (String) score);
      }
      if (jsonObject.containsKey("time")) {
        time = jsonObject.get("time").toString();
        builder.namedGraph(res).subject(mnObj)
            .add(SimpleValueFactory.getInstance().createIRI(namespace, "hasTime"), (String) time);
      }

      builder.namedGraph(res).subject(mnObj).add(RDF.TYPE,
          SimpleValueFactory.getInstance().createIRI(namespace, "Minigame"));

    } else if (minigame.equals("ApplicationForm")) {

      Resource res = vf.createIRI(
          "https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ApplicationFormInfo");
      IRI mnObj = vf.createIRI(namespace, "ApplicationForm-" + uuid);

      if (jsonObject.containsKey("scenarioID")) {
        scenarioID = jsonObject.get("scenarioID").toString();
        builder.namedGraph(res).subject(mnObj).add(
            SimpleValueFactory.getInstance().createIRI(namespace, "hasScenarioID"),
            (String) scenarioID);
      }

      if (jsonObject.containsKey("stage")) {
        stage = jsonObject.get("stage").toString();
        builder.namedGraph(res).subject(mnObj)
            .add(SimpleValueFactory.getInstance().createIRI(namespace, "hasStage"), (String) stage);
      }

      if (jsonObject.containsKey("stagescore")) {
        stagescore = jsonObject.get("stagescore").toString();
        builder.namedGraph(res).subject(mnObj).add(
            SimpleValueFactory.getInstance().createIRI(namespace, "hasStageScore"),
            (String) stagescore);
      }

      if (jsonObject.containsKey("stagetime")) {
        stagetime = jsonObject.get("stagetime").toString();
        builder.namedGraph(res).subject(mnObj).add(
            SimpleValueFactory.getInstance().createIRI(namespace, "hasStageTime"),
            (String) stagetime);
      }

      if (jsonObject.containsKey("attempts")) {
        attempts = jsonObject.get("attempts").toString();
        builder.namedGraph(res).subject(mnObj).add(
            SimpleValueFactory.getInstance().createIRI(namespace, "hasAttempts"),
            (String) attempts);
      }

      builder.namedGraph(res).subject(mnObj).add(RDF.TYPE,
          SimpleValueFactory.getInstance().createIRI(namespace, "ApplicationForm"));

    } else if (minigame.equals("JobInterviewSimulation")) {
      Resource res = vf.createIRI(
          "https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#JobInterviewSimulationInfo");
      IRI mnObj = vf.createIRI(namespace, "JobInterviewSimulation-" + uuid);
      builder.namedGraph(res).subject(mnObj)
          .add(SimpleValueFactory.getInstance().createIRI(namespace, "hasScenarioID"),
              (String) jsonObject.get("scenarioID").toString())
          .add(SimpleValueFactory.getInstance().createIRI(namespace, "hasStage"),
              (String) jsonObject.get("stage").toString())
          .add(SimpleValueFactory.getInstance().createIRI(namespace, "hasHelp"),
              (String) jsonObject.get("help").toString())
          .add(SimpleValueFactory.getInstance().createIRI(namespace, "hasAnswers"),
              (String) jsonObject.get("answers").toString())
          .add(RDF.TYPE,
              SimpleValueFactory.getInstance().createIRI(namespace, "JobInterviewSimulation"));
    } else if (minigame.equals("Vocab")) {
      Resource res = vf.createIRI(
          "https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#VocabInfo");
      IRI mnObj = vf.createIRI(namespace, "Vocab-" + uuid);
      builder.namedGraph(res).subject(mnObj)
          .add(SimpleValueFactory.getInstance().createIRI(namespace, "hasScore"),
              (String) jsonObject.get("score").toString())
          .add(SimpleValueFactory.getInstance().createIRI(namespace, "hasAttempts"),
              (String) jsonObject.get("attempts").toString())
          .add(RDF.TYPE, SimpleValueFactory.getInstance().createIRI(namespace, "Vocab"));
    } else if (minigame.equals("VRSaveProgress")) {
      // System.out.println(body);
      Resource res = vf.createIRI(
          "https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#VRSaveProgressInfo");
      connection.clear(res);
      IRI mnObj = vf.createIRI(namespace, "VRSaveProgress-" + uuid);
      builder.namedGraph(res).subject(mnObj)
          .add(SimpleValueFactory.getInstance().createIRI(namespace, "hasSaveData"),
              (String) body.toString())
          .add(RDF.TYPE, SimpleValueFactory.getInstance().createIRI(namespace, "VRSaveProgress"));
    }


    connection.begin();
    Model model = builder.build();
    connection.add(model);
    connection.commit();
  }


}
