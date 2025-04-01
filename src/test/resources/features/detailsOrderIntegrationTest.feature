Feature: Ver Order en detalle

Background:
  * def port = karate.properties['karate.server.port']
  * url 'http://localhost:' + port
  
Scenario: Ver un Order en detalle flujo habitual
  #