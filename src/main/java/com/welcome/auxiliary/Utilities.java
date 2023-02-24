package com.welcome.auxiliary;

import java.io.IOException;
import java.io.InputStream;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.UpdateExecutionException;
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
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class Utilities {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Utilities.class);

  public static String username;
  public static String password;
  public static String serverURL;
  public static String ontologyFile;
  public static String graphDB;
  public static Repository repository;
  public static RepositoryConnection connection;
  public static ValueFactory f;

  public static void loadProperties() {
    // Read properties
    String label = System.getenv("repositoryLabel");
    String url = System.getenv("serverURL");
    String repo = null;
    if (label == null || url == null) {// http://localhost:7200/rdf4j-server/
      Utilities.serverURL = "http://localhost:7200";
      Utilities.graphDB = "global";
    } else {
      Utilities.serverURL = url;
      Utilities.graphDB = label;
    }
  }

  /**
   * Function that takes as input the repository ID & Label and creates a new repository in the
   * provided URL based on a set of default settings.
   *
   * @param repositoryId
   * @param repositoryLabel
   * @throws RDFParseException
   * @throws RDFHandlerException
   * @throws IOException
   */
  public static boolean createRepository(String repositoryId, String repositoryLabel,
      String serverURL) throws RDFParseException, RDFHandlerException, IOException {
    MDC.put("repoID", repositoryLabel);
    // Instantiate a repository manager and initialize it
    RepositoryManager repositoryManager = new RemoteRepositoryManager(serverURL);
    repositoryManager.init();


    // In case the repository exists do nothing
    if (repositoryManager.hasRepositoryConfig(repositoryId)) {
      logger.info("[+] [Repository " + repositoryId + " already exists. Skipping ...]");
      // repositoryManager.removeRepository(repositoryId);
      return false;
    } else {
      logger.info("[+] [Creating repository with id: " + repositoryId + "]");
    }

    TreeModel graph = new TreeModel();
    InputStream config;
    // Use the default settings (with owl2-rl-optimized ruleset)
    if (serverURL.contains("rdf4j")) {
      config = Utilities.class.getResourceAsStream("/repo-defaults-rdf4j.ttl");
    } else {
      config = Utilities.class.getResourceAsStream("/repo-defaults.ttl");
    }
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

  /**
   * Function that establishes a connection to a remote repository.
   *
   * @param repositoryId
   * @param serverURL
   */
  public static void startKB(String repositoryId, String serverURL) {
    // Define server URL and appropriate credentials
    RemoteRepositoryManager manager = new RemoteRepositoryManager(serverURL);

    // manager.setUsernameAndPassword(username, password);
    manager.init();

    try {
      // Get Repository
      Utilities.repository = manager.getRepository(repositoryId);

      // Initialize Repository
      Utilities.repository.init();

      // Separate connection to a repository
      Utilities.connection = Utilities.repository.getConnection();

    } catch (RepositoryConfigException | RepositoryException e) {
      logger.warn("Connecting to remote repository failed!");
    }
  }

  /**
   * Function that loads the given ontology file in the repository.
   *
   * @param fileName
   */
  public static void loadOntologyFile(String fileName) {

    logger.info("[+] [Loading ontology file...]");

    try {
      Utilities.connection.begin();

      Utilities.connection.add(Utilities.class.getResourceAsStream("/" + fileName), "urn:base",
          RDFFormat.TURTLE);

      // Committing the transaction persists the data
      Utilities.connection.commit();
    } catch (RDFParseException | RepositoryException | IOException e) {
      logger.warn("[x] Loading ontology file failed!");
    }
  }

  /**
   * Function that takes an RDF model and commits it
   *
   * @param model
   */
  public static void commitModel(RepositoryConnection connection, Model model, Resource resource) {
    // When adding data we need to start a transaction
    connection.begin();
    connection.add(model, resource);
    connection.commit();
  }

  /**
   * Function that loads the app.properties file.
   */

  public void initKB() {
    logger.info("(INIT) Loading parameters and opening connection with the repository.");

    // Load Properties
    loadProperties();

    // START KB
    startKB(Utilities.graphDB, Utilities.serverURL);

    logger.info("(INIT) Initialization succeeded.");
  }

  /**
   * Executes a SPARQL UPDATE (INSERT or DELETE) statement.
   *
   * @param repositoryConnection a connection to a repository
   * @param update the SPARQL UPDATE query in text form
   * @param bindings optional bindings to set on the prepared query
   * @throws MalformedQueryException
   * @throws RepositoryException
   */
  public void executeUpdate(RepositoryConnection repositoryConnection, String update,
      Binding... bindings)
      throws MalformedQueryException, RepositoryException, UpdateExecutionException {
    Update preparedUpdate = repositoryConnection.prepareUpdate(QueryLanguage.SPARQL, update);
    // Setting any potential bindings (query parameters)
    for (Binding b : bindings) {
      preparedUpdate.setBinding(b.getName(), b.getValue());
    }
    preparedUpdate.execute();
  }

  /**
   * Splits an IRI and returns value after #
   *
   * @param activeSlot
   * @return
   */
  public String splitIRI(IRI activeSlot) {
    /* Split IRI from actual name */
    String[] split = activeSlot.stringValue().split("#");
    String slotName = split[1];

    return slotName;
  }

  /**
   * This functions starts a transaction and executes the update query
   *
   * @param queryString
   */
  public void executeQuery(RepositoryConnection connection, String queryString) {
    // When adding data we need to start a transaction
    logger.info("(REPO) Starting Transaction.");
    connection.begin();

    logger.info("(REPO) Executing Update.");
    executeUpdate(connection, String.format(queryString));

    logger.info("(REPO) Committing Transaction.");
    connection.commit();
  }
}
