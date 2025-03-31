Feature: Buscar Order por titulo

Background:
  * def port = karate.properties['karate.server.port']
  * url 'http://localhost:' + port
  
Scenario: Buscar Order por titulo

  # El Orderer hace la acción de buscar un Order por un título que no existe
  Given path '/search'
  And form field query = "Top 10 puertas"
  When method POST
  Then status 200

