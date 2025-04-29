Feature: Ver Detalles de Orderer

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

Scenario: Ver Detalles de un Orderer Existente 
  Given path '/orderer/a'
  When method get
  Then status 200 
  And match response contains '<title hidden>a | ReOrder</title>'

Scenario: Intentar Ver Detalles de un Orderer Inexistente 
  Given path '/orderer/b' 
  When method get
  Then status 200
  And match response !contains '<title hidden>a | ReOrder</title>'