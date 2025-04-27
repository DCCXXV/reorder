Feature: Buscar User 

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
  
Scenario: Buscar User

  # El Orderer hace la acción de buscar un User que no existe
  Given path '/search'
  And form field query = "Sofia"
  When method GET
  Then status 200

