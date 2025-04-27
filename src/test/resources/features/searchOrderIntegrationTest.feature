Feature: Buscar Order por titulo

Background:
  * def port = karate.properties['karate.server.port']
  * url 'http://localhost:' + port
  * header X-Test-Framework = 'Karate'
  * path '/login'
  * form field username = 'a'
  * form field password = 'aaaAAA123'
  * method post
  * status 200
  * path ''
  
Scenario: Buscar Order por titulo

  # El Orderer hace la acción de buscar un Order por un título que no existe
  Given path '/search'
  And form field query = "Top 10 puertas"
  When method POST
  Then status 200

