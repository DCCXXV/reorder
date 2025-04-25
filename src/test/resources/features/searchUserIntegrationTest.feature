Feature: Buscar User 

Background:
  * def port = karate.properties['karate.server.port']
  * url 'http://localhost:' + port
  
Scenario: Buscar User

  # El Orderer hace la acción de buscar un User que no existe
  Given path '/search'
  And form field query = "Sofia"
  When method POST
  Then status 200

