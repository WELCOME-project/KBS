package com.welcome.auxiliary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class WELCOME {
  public static final String NAMESPACE = "https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#";

  /**
   * Creates a new {@link IRI} with this vocabulary's namespace for the given local name.
   *
   * @param localName a local name of an IRI, e.g. 'creatorOf', 'name', 'Artist', etc.
   * @return an IRI using the http://www.semanticweb.org/image-ontology/ namespace and the given local name.
   */
  private static IRI getIRI(String localName) {
    return SimpleValueFactory.getInstance().createIRI(NAMESPACE, localName);
  }
}

