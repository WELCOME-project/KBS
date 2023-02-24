package com.welcome.kbs;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.trig.TriGWriter;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.welcome.auxiliary.Utilities;
import com.welcome.services.AgentTCN;
import com.welcome.services.AgentWAR;
import com.welcome.services.Geolocation;
import com.welcome.services.Minigame;
import com.welcome.services.TCN;
import com.welcome.services.graphdbGlobal;

@RestController
public class KbsController {

  Logger logger = LoggerFactory.getLogger(KbsController.class);
  graphdbGlobal gdb = new graphdbGlobal();
  AgentWAR awr = new AgentWAR();
  Geolocation geo = new Geolocation();
  TCN mTCN = new TCN();
  Minigame mini = new Minigame();
  AgentTCN AT = new AgentTCN();

  @GetMapping("/test/verify")
  public String verifyRestService(HttpServletRequest request, HttpServletResponse response)
      throws ParseException, IOException {
    logger.info("(TEST) KBS status check.");
    System.out.println(request.getHeader("Authorization"));
    if (authSend(request.getHeader("Authorization"))) {
      // System.out.println("done");
      return "KBS Successfully started..";
    } else {
      response.sendRedirect("https://www.google.com/");
      return "Invalid Token";
    }
  }

  @PostMapping("/geoLocation")
  public JSONObject String(@RequestBody String body,
      @RequestHeader(value = "X-Correlation-Id", required = false) String corId,
      @RequestHeader(value = "Authorization", required = false) String auth) throws ParseException {
    Map<String, String> authorizationHeader = new TreeMap<>();
    authorizationHeader.put("Authorization", auth);
    return geo.geoLoc("", "none", body, authorizationHeader);
  }

  @GetMapping("/initialGlobal")
  public String initialDB(@RequestHeader(value = "X-Correlation-Id", required = false) String corId,
      @RequestHeader(value = "Authorization", required = false) String auth)
      throws RDFParseException, RDFHandlerException, IOException, URISyntaxException {
    if (corId != null) {
      MDC.put("correlationId", corId);
    }
    Map<String, String> authorizationHeader = new TreeMap<>();
    authorizationHeader.put("Authorization", auth);
    Utilities.loadProperties();
    Utilities.createRepository(Utilities.graphDB, Utilities.graphDB, Utilities.serverURL);
    gdb.loadOntology(Utilities.graphDB, Utilities.serverURL);
    gdb.loadWAR(Utilities.graphDB, Utilities.serverURL);
    gdb.loadWSR(Utilities.graphDB, Utilities.serverURL);
    gdb.loadNuts(Utilities.graphDB, Utilities.serverURL);
    logger.info("(ENDPOINT) Initialization of global db.");
    return "Initialilization success!";
  }

  @GetMapping("/initialRepos/{name}")
  public String initialRepos(
      @RequestHeader(value = "X-Correlation-Id", required = false) String corId,
      @PathVariable String name,
      @RequestHeader(value = "Authorization", required = false) String auth)
      throws RDFParseException, RDFHandlerException, IOException, URISyntaxException {
    if (corId != null) {
      MDC.put("correlationId", corId);
    }
    Utilities.loadProperties();
    Utilities.startKB(Utilities.graphDB, Utilities.serverURL);
    Utilities.createRepository(name, name, Utilities.serverURL);
    Utilities.createRepository("lar_" + name, "lar_" + name, Utilities.serverURL);
    Utilities.createRepository("lsr_" + name, "lsr_" + name, Utilities.serverURL);
    logger.info("(ENDPOINT) Created repos LAR LSR LAKR.");
    return "Initialilization success!";
  }


  @GetMapping("/takeOntology")
  public String takeOntology(
      @RequestHeader(value = "X-Correlation-Id", required = false) String corId,
      @RequestHeader(value = "Authorization", required = false) String auth) {
    if (corId != null) {
      MDC.put("correlationId", corId);
    }
    logger.info("(ENDPOINT) Provide the Welcome Ontology.");
    Utilities.loadProperties();
    Utilities.startKB(Utilities.graphDB, Utilities.serverURL);
    ValueFactory vf = Utilities.repository.getValueFactory();
    Resource res = vf.createIRI("http://welcome.org/kbs/ontology");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    RDFHandler handler = new TurtleWriter(baos);
    Utilities.connection.export(handler, res);
    String finalString = baos.toString();
    return finalString;
  }

  @GetMapping("/services/WSR")
  public String takeFRSC(@RequestHeader(value = "X-Correlation-Id", required = false) String corId,
      @RequestHeader(value = "Authorization", required = false) String auth) {
    if (corId != null) {
      MDC.put("correlationId", corId);
    }
    logger.info("(ENDPOINT) Provide the SERVICES.");
    Utilities.loadProperties();
    Utilities.startKB(Utilities.graphDB, Utilities.serverURL);
    ValueFactory vf = Utilities.repository.getValueFactory();
    Resource res4 = vf.createIRI("file://C:/fakepath/FirstReceptionService.owl");
    Resource res5 = vf.createIRI("file://C:/fakepath/RegistrationService.owl");
    Resource res6 = vf.createIRI("file://C:/fakepath/PreregistrationProcedureService.owl");
    Resource res7 = vf.createIRI("file://C:/fakepath/SimulatePhoneAppointmentService.owl");
    Resource res = vf.createIRI("file://C:/fakepath/FullPreregistration.owl");
    Resource res2 = vf.createIRI("file://C:/fakepath/SimulateAppointmentService.owl");
    Resource res3 = vf.createIRI("file://C:/fakepath/CreateCV.owl");
    Resource res8 = vf.createIRI("file://C:/fakepath/SchoolingCARITAS.owl");
    Resource res9 = vf.createIRI("file://C:/fakepath/SchoolingDIFE.owl");
    Resource res10 = vf.createIRI("file://C:/fakepath/Health.owl");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    RDFHandler handler = new TriGWriter(baos);
    Utilities.connection.export(handler, res, res2, res3, res4, res5, res6, res7, res8, res9,
        res10);
    String finalString = baos.toString();
    return finalString;
  }

  @GetMapping("/services/WAR")
  public String takeRS(@RequestHeader(value = "X-Correlation-Id", required = false) String corId,
      @RequestHeader(value = "Authorization", required = false) String auth) {
    if (corId != null) {
      MDC.put("correlationId", corId);
    }
    // logger.info("(ENDPOINT) Provide the active AGENTS.");
    Utilities.loadProperties();
    Utilities.startKB(Utilities.graphDB, Utilities.serverURL);
    awr.findTheActiveAgents(Utilities.graphDB, Utilities.serverURL);
    ValueFactory vf = Utilities.repository.getValueFactory();
    Resource res = vf.createIRI("http://welcome.org/kbs/WAR/activeAgents");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    RDFHandler handler = new TurtleWriter(baos);
    Utilities.connection.export(handler, res);
    String finalString = baos.toString();
    logger.info("(ENDPOINT) Provide active Agents.");
    return finalString;
  }

  @GetMapping({"/services/WAR/wpm", "/services/WAR/wpm/{agentName}"})
  public JSONObject takeRSJ(
      @RequestHeader(value = "X-Correlation-Id", required = false) String corId,
      @PathVariable(required = false) String agentName,
      @RequestHeader(value = "Authorization", required = false) String auth) {
    if (corId != null) {
      MDC.put("correlationId", corId);
    }
    logger.info("(ENDPOINT) Provide the AGENTS.");
    Utilities.loadProperties();
    Utilities.startKB(Utilities.graphDB, Utilities.serverURL);
    return awr.findAgents(Utilities.graphDB, Utilities.serverURL, agentName);
  }


  @PostMapping("/agentControl")
  public String insertAgent(@RequestBody String agentStatus,
      @RequestHeader(value = "X-Correlation-Id", required = false) String corId,
      @RequestHeader(value = "Authorization", required = false) String auth)
      throws IOException, ParseException {
    if (corId != null) {
      MDC.put("correlationId", corId);
    }
    logger.info("(ENDPOINT) input from WPM for updating STATUS AGENT.");
    Utilities.loadProperties();
    Utilities.startKB(Utilities.graphDB, Utilities.serverURL);
    logger.info("(ENDPOINT) insert Agent.");
    return awr.insertAgent(Utilities.graphDB, Utilities.serverURL, agentStatus);
  }

  @PutMapping("/agentControl")
  public String updateAgent(@RequestBody String agentStatus,
      @RequestHeader(value = "X-Correlation-Id", required = false) String corId,
      @RequestHeader(value = "Authorization", required = false) String auth)
      throws IOException, ParseException {
    if (corId != null) {
      MDC.put("correlationId", corId);
    }
    logger.info("(ENDPOINT) input from WPM for updating STATUS AGENT.");
    Utilities.loadProperties();
    Utilities.startKB(Utilities.graphDB, Utilities.serverURL);
    logger.info("(ENDPOINT) update Agent status.");
    return awr.updateAgent(Utilities.graphDB, Utilities.serverURL, agentStatus);
  }

  @DeleteMapping("/agentControl/{name}")
  public String deleteAgent(@PathVariable String name,
      @RequestHeader(value = "X-Correlation-Id", required = false) String corId,
      @RequestHeader(value = "Authorization", required = false) String auth)
      throws IOException, ParseException {
    if (corId != null) {
      MDC.put("correlationId", corId);
    }
    logger.info("(ENDPOINT) input from WPM for updating STATUS AGENT.");
    Utilities.loadProperties();
    Utilities.startKB(Utilities.graphDB, Utilities.serverURL);
    logger.info("(ENDPOINT) delete Agent.");
    return awr.deleteAgent(Utilities.graphDB, Utilities.serverURL, name);
  }

  @PostMapping({"/info/local", "/info/local/{value}"})
  public String postLocalInfo(@RequestParam String label, @RequestBody String body,
      @PathVariable(required = false) String value,
      @RequestHeader(value = "X-Correlation-Id", required = false) String corId,
      @RequestHeader(value = "Authorization", required = false) String auth) throws ParseException {
    if (corId != null) {
      MDC.put("correlationId", corId);
    }
    // System.out.println(auth);
    Map<String, String> authorizationHeader = new TreeMap<>();
    authorizationHeader.put("Authorization", auth);
    mTCN.maniTCN(label, body, value, authorizationHeader);
    logger.info("(ENDPOINT) TCN data to LAKR.");
    return "TCN data inserted";
  }

  @GetMapping({"/info/local", "/info/local/{Classroom}"})
  public JSONArray takeLocalInfo(@RequestParam(required = false) String label,
      @PathVariable(required = false) String Classroom,
      @RequestParam(required = false) String scenario, @RequestParam(required = false) String attr,
      @RequestHeader(value = "X-Correlation-Id", required = false) String corId,
      @RequestHeader(value = "Authorization", required = false) String auth) {
    if (corId != null) {
      MDC.put("correlationId", corId);
    }
    System.out.println(auth);
    Map<String, String> authorizationHeader = new TreeMap<>();
    authorizationHeader.put("Authorization", auth);
    logger.info("(ENDPOINT) Take data from LAKR " + attr);
    return mTCN.getTCN(label, Classroom, attr, scenario, authorizationHeader);
  }

  @GetMapping("/info/local/AgentTCN")
  public JSONArray takeAgentTCN(
      @RequestHeader(value = "X-Correlation-Id", required = false) String corId,
      @RequestHeader(value = "Authorization", required = false) String auth) {
    if (corId != null) {
      MDC.put("correlationId", corId);
    }
    return AT.retrieveClassTCN();
  }

  @PostMapping({"/info/local/Minigames/{minigame}"})
  public String postMinigame(@RequestParam String label, @RequestBody String body,
      @RequestHeader(value = "X-Correlation-Id", required = false) String corId,
      @PathVariable(required = true) String minigame,
      @RequestHeader(value = "Authorization", required = false) String auth) throws ParseException {
    if (corId != null) {
      MDC.put("correlationId", corId);
    }
    mini.maniMinigame(label, body, minigame);
    logger.info("(ENDPOINT) Minigame data to LAKR.");
    return "Minigame data inserted";
  }



  @GetMapping("/info/local/VRSaveProgress")
  public String takeVRSaveProgressInfo(@RequestParam String label,
      @RequestHeader(value = "X-Correlation-Id", required = false) String corId,
      @RequestHeader(value = "Authorization", required = false) String auth) {
    if (corId != null) {
      MDC.put("correlationId", corId);
    }
    logger.info("(ENDPOINT) Take VR save progress from LAKR.");
    return mTCN.takeVRSaveProgress(label);
  }

  /*
   * @GetMapping({"/info/local/minigame"}) public JSONObject GetMinigame(@RequestParam String label,
   * 
   * @RequestHeader(value = "X-Correlation-Id", required = false) String corId) throws
   * ParseException { if (corId != null) { MDC.put("correlationId", corId); }
   * logger.info("(ENDPOINT) Minigame data to LAKR."); return mini.getMinigame(label); }
   */
  @GetMapping("/validation")
  public JSONObject flag(InputStream incomingData,
      @RequestHeader(value = "X-Correlation-Id", required = false) String corId) {
    if (corId != null) {
      MDC.put("correlationId", corId);
    }
    logger.info("(ENDPOINT) Validation.");
    JSONObject res = new JSONObject();
    res.put("isActive", true);
    res.put("roles", "");
    return res;
  }

  public void redirection(boolean isActive) {

  }

  public boolean authSend(String basicAuth) throws ParseException {
    BufferedReader reader;
    String line;
    HttpURLConnection conn = null;
    StringBuilder responseContent = new StringBuilder();
    JSONParser parser = new JSONParser();
    String pathUrl = System.getenv("VALIDATION_ENDPOINT");
    JSONObject jsonObject = new JSONObject();
    try {

      URL url;
      if (pathUrl == null) {
        url = new URL("http://localhost:8080/kbs/api/validation");
      } else {
        url = new URL(pathUrl);
      }

      conn = (HttpURLConnection) url.openConnection();

      // Request setup
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Authorization", basicAuth);
      conn.setConnectTimeout(5000);// 5000 milliseconds = 5 seconds
      conn.setReadTimeout(5000);

      // Test if the response from the server is successful
      int status = conn.getResponseCode();

      if (status >= 300) {
        reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        while ((line = reader.readLine()) != null) {
          responseContent.append(line);
        }
        reader.close();
      } else {
        reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        while ((line = reader.readLine()) != null) {
          responseContent.append(line);
        }
        reader.close();
      }

      jsonObject = (JSONObject) parser.parse(responseContent.toString());

      System.out.println(responseContent);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      conn.disconnect();
    }
    return (boolean) jsonObject.get("isActive");
  }
}
