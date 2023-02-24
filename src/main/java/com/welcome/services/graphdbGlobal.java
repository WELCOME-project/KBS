package com.welcome.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.config.RepositoryConfigSchema;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import com.welcome.auxiliary.Utilities;
import com.welcome.auxiliary.WELCOME;

public class graphdbGlobal {

  public graphdbGlobal() {}

  public boolean createRepository(String repositoryId, String repositoryLabel, String serverURL)
      throws RDFParseException, RDFHandlerException, IOException, InterruptedException,
      URISyntaxException {

    // Instantiate a repository manager and initialize it
    RepositoryManager repositoryManager = new RemoteRepositoryManager(serverURL);
    repositoryManager.init();

    if (repositoryManager.hasRepositoryConfig(repositoryId)) {
      System.out.println("+ [Repository " + repositoryId + " already exists. Skipping...]");
      return false;
    } else {
      System.out.println("+ [Creating repository with id: " + repositoryId + "]");
    }

    TreeModel graph = new TreeModel();

    // Use the default settings (with owl2-rl-optimized ruleset)

    String importConfig = "";

    URL res = getClass().getClassLoader().getResource("repo-defaults-rdf4j.ttl");
    File file = Paths.get(res.toURI()).toFile();
    importConfig = file.getAbsolutePath();
    InputStream config = new FileInputStream(importConfig);

    RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
    rdfParser.setRDFHandler(new StatementCollector(graph));
    rdfParser.parse(config, RepositoryConfigSchema.NAMESPACE);
    config.close();

    Resource repositoryNode = Models
        .subject(graph.filter(null, RDF.TYPE, RepositoryConfigSchema.REPOSITORY)).orElse(null);

    graph.add(repositoryNode, RepositoryConfigSchema.REPOSITORYID,
        SimpleValueFactory.getInstance().createLiteral(repositoryId));

    if (repositoryLabel != null) {
      graph.add(repositoryNode, RDFS.LABEL,
          SimpleValueFactory.getInstance().createLiteral(repositoryLabel));
    }

    RepositoryConfig repositoryConfig = RepositoryConfig.create(graph, repositoryNode);

    repositoryManager.addRepositoryConfig(repositoryConfig);

    return true;
  }

  public void loadOntology(String label, String url) throws IOException, URISyntaxException {

    RemoteRepositoryManager manager = new RemoteRepositoryManager(url);
    manager.init();

    try {
      Repository repository = manager.getRepository(label);
      repository.init();
      RepositoryConnection connection = repository.getConnection();

      ValueFactory vf = repository.getValueFactory();
      Resource onto = vf.createIRI("http://welcome.org/kbs/ontology");

      try {
        connection.begin();

        URL ontoURL =
            new URL("https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#");

        connection.clear(onto);

        connection.add(ontoURL, "urn:base", RDFFormat.TURTLE, onto);

        connection.commit();

      } catch (QueryEvaluationException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (RepositoryException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } finally {
        // It is best to close the connection in a finally block
        connection.close();
      }
    } catch (RepositoryConfigException | RepositoryException e) {
      System.out.println("Error in loading Welcome Ontology");
    }

  }

  public void loadOntology(String label, String namespace, String graph) {
    /* Init manager */
    RemoteRepositoryManager manager = new RemoteRepositoryManager(Utilities.serverURL);
    manager.init();

    /* Init repo */
    Repository repository = manager.getRepository(label);
    repository.init();

    /* Get connection */
    RepositoryConnection connection = repository.getConnection();

    try {
      connection.begin();

      URL url = new URL(
          "https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/" + namespace + ".ttl#");

      ValueFactory vf = repository.getValueFactory();
      IRI onto = vf.createIRI(WELCOME.NAMESPACE, graph);

      connection.clear(onto);
      connection.add(url, "urn:base", RDFFormat.TURTLE, onto);
      connection.commit();
    } catch (RDFParseException | RepositoryException | IOException e) {
      System.out.println("[x] Loading ontology file failed!");
    }
  }

  public void loadNuts(String label, String url) throws RDFParseException, IOException {
    RemoteRepositoryManager manager = new RemoteRepositoryManager(url);
    manager.init();

    try {
      Repository repository = manager.getRepository(label);
      repository.init();
      RepositoryConnection connection = repository.getConnection();

      ValueFactory vf = repository.getValueFactory();
      Resource onto = vf.createIRI("http://welcome.org/kbs/NutsCodes");

      try {
        connection.begin();

        URL ontoURL = new URL(
            "https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/PostCodes.owl#");

        connection.clear(onto);

        connection.add(ontoURL, "urn:base", RDFFormat.TURTLE, onto);

        connection.commit();

      } catch (QueryEvaluationException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (RepositoryException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } finally {
        // It is best to close the connection in a finally block
        connection.close();
      }
    } catch (RepositoryConfigException | RepositoryException e) {
      System.out.println("Error in loading Welcome Ontology");
    }
  }


  public void loadWAR(String label, String url) throws IOException, URISyntaxException {

    RemoteRepositoryManager manager = new RemoteRepositoryManager(url);
    manager.init();

    try {
      Repository repository = manager.getRepository(label);
      repository.init();
      RepositoryConnection connection = repository.getConnection();

      ValueFactory vf = repository.getValueFactory();
      Resource war = vf.createIRI("http://welcome.org/kbs/WAR");

      try {
        connection.begin();

        URL warURL = new URL(
            "https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/GlobalKnowledge/WAR/WAR.ttl");

        // connection.clear(war);

        connection.add(warURL, "urn:base", RDFFormat.TURTLE, war);

        connection.commit();
      } catch (RDFParseException | RepositoryException | IOException e) {
        System.out.println("Error in loading WAR");
      }

    } catch (RepositoryConfigException | RepositoryException e) {
      System.out.println("Error in loading WAR");
    }
  }

  public void loadWSR(String label, String url) throws IOException, URISyntaxException {

    RemoteRepositoryManager manager = new RemoteRepositoryManager(url);
    manager.init();

    try {
      Repository repository = manager.getRepository(label);
      repository.init();
      RepositoryConnection connection = repository.getConnection();

      ValueFactory vf = repository.getValueFactory();

      Resource frs = vf.createIRI("file://C:/fakepath/FirstReceptionService.owl");//
      Resource rs = vf.createIRI("file://C:/fakepath/RegistrationService.owl");//
      Resource pps = vf.createIRI("file://C:/fakepath/PreregistrationProcedureService.owl");//
      Resource spas = vf.createIRI("file://C:/fakepath/SimulatePhoneAppointmentService.owl");//
      Resource frp = vf.createIRI("file://C:/fakepath/FullPreregistration.owl");//
      Resource spa = vf.createIRI("file://C:/fakepath/SimulateAppointmentService.owl");//
      Resource ccv = vf.createIRI("file://C:/fakepath/CreateCV.owl");//
      Resource scs = vf.createIRI("file://C:/fakepath/SchoolingCARITAS.owl");
      Resource scd = vf.createIRI("file://C:/fakepath/SchoolingDIFE.owl");
      Resource hea = vf.createIRI("file://C:/fakepath/Health.owl");

      try {
        connection.begin();

        InputStream frsStream = getClass().getResourceAsStream("/FirstReceptionService.owl");
        InputStream rsStream = getClass().getResourceAsStream("/RegistrationService.owl");
        InputStream ppsStream =
            getClass().getResourceAsStream("/PreregistrationProcedureService.owl");
        InputStream spasStream =
            getClass().getResourceAsStream("/SimulatePhoneAppointmentService.owl");
        InputStream frpStream = getClass().getResourceAsStream("/FullPreregistration.owl");
        InputStream spaStream = getClass().getResourceAsStream("/SimulateAppointmentService.owl");
        InputStream ccvStream = getClass().getResourceAsStream("/CreateCV.owl");
        InputStream scsStream = getClass().getResourceAsStream("/SchoolingCARITAS.owl");
        InputStream scdStream = getClass().getResourceAsStream("/SchoolingDIFE.owl");
        InputStream heaStream = getClass().getResourceAsStream("/Health.owl");

        connection.clear(frs, rs, pps, spas);
        connection.clear(frp, spa);
        connection.add(frsStream, "urn:base", RDFFormat.RDFXML, frs);
        connection.add(rsStream, "urn:base", RDFFormat.RDFXML, rs);
        connection.add(ppsStream, "urn:base", RDFFormat.RDFXML, pps);
        connection.add(spasStream, "urn:base", RDFFormat.RDFXML, spas);
        connection.add(frpStream, "urn:base", RDFFormat.RDFXML, frp);
        connection.add(spaStream, "urn:base", RDFFormat.RDFXML, spa);
        connection.add(ccvStream, "urn:base", RDFFormat.RDFXML, ccv);
        connection.add(scsStream, "urn:base", RDFFormat.RDFXML, scs);
        connection.add(scdStream, "urn:base", RDFFormat.RDFXML, scd);
        connection.add(heaStream, "urn:base", RDFFormat.RDFXML, hea);

        connection.commit();

      } catch (RDFParseException | RepositoryException | IOException e) {
        System.out.println("Error in loading WSR");
      }

    } catch (RepositoryConfigException | RepositoryException e) {
      System.out.println("Error in loading WSR");
    }
  }
}
