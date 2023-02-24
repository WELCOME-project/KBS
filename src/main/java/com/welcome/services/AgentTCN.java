package com.welcome.services;

import com.welcome.auxiliary.Queries;
import com.welcome.auxiliary.Utilities;
import java.util.ArrayList;
import java.util.UUID;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class AgentTCN {
	
	Queries que = new Queries();
	
	public AgentTCN() {}
	
	public JSONArray retrieveClassTCN(){
		
		 RemoteRepositoryManager manager = new RemoteRepositoryManager(Utilities.serverURL);
	     manager.init();
	     ModelBuilder builder = new ModelBuilder();
	     //agentKnowledge_<agentLabel>
	     Repository repository = manager.getRepository(Utilities.graphDB);
	     repository.init();
	     RepositoryConnection connection = repository.getConnection();
		
		JSONObject responseDetailsJson = new JSONObject();
	    JSONArray jsonArray = new JSONArray();
		
		String queryLakrs = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
		          + "PREFIX ajan: <http://www.ajan.de/ajan-ns#>\r\n"
		          + "prefix welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
		          + "prefix ag: <http://localhost:8080/welcome/integration/coordination/ajan/agents/>\r\n"
		          + "			select ?s ?o ?iri_name ?iri_status where { \r\n"
		          + "				?s welcome:hasStatus ?o .\r\n"
		          + "    			?s rdf:type ajan:Agent .\r\n"
		          + "    		bind(strafter(str(?s),str(ag:)) as ?iri_name)\r\n"
		          + "			bind(strafter(str(?o),str(welcome:)) as ?iri_status)\r\n"
		          + "			}";
		
		TupleQuery query = connection.prepareTupleQuery(queryLakrs);
		
		ArrayList<String> idAgent = new ArrayList<String>();
		try (TupleQueryResult result = query.evaluate()) {
        	while (result.hasNext()) {
				BindingSet solution = result.next();
				idAgent.add(solution.getValue("iri_name").toString().replaceAll("\"",""));
			}
        }
		System.out.println(idAgent);
		for(int i=0;i<idAgent.size();i++) {
			jsonArray.add(takeFieldsAgentTCN(idAgent.get(i)));
		}
		return jsonArray;
	}
	
	public JSONObject takeFieldsAgentTCN(String label) {
		RemoteRepositoryManager manager = new RemoteRepositoryManager(Utilities.serverURL);
        manager.init();
        ModelBuilder builder = new ModelBuilder();
        //agentKnowledge_<agentLabel>
        //System.out.println(util.serverURL);
        Repository repository = manager.getRepository(label);
        repository.init();
        RepositoryConnection  connection = repository.getConnection();
		
		JSONObject responseDetailsJson = new JSONObject();
		
		String Name = que.execObtainStrTCN(connection,"Name");
		responseDetailsJson.put("Name", Name);
		String Class = que.execObtainStrTCN(connection,"Class");
		responseDetailsJson.put("Class", Class);
		String UserId = que.execObtainStrTCN(connection,"UserId");
		UserId = "agent-core-"+UserId;
		responseDetailsJson.put("AgentId", UserId);
		return responseDetailsJson;
	}
	
	
}
