Feature: Ver Order en detalle

Background:
  * def port = karate.properties['karate.server.port']
  * url 'http://localhost:' + port
  
Scenario: Ver un Order en detalle flujo habitual
    # 1: El Orderer entra en la vista de ver Order y la vista se ve correctamente