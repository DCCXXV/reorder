# language: es
@OrdererFeature
Característica: Ver Detalles de Orderer

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
    And match response '<h1 class="text-2xl font-semibold">Detalles del Orderer</h1>'
    And match response ('<span id="orderer-username">a</span>') 
    And !match response '<div id="error-message-container">'

  Scenario: Intentar Ver Detalles de un Orderer Inexistente 
     Given path '/orderer/b' 
    When method get
    Then status 200 
    And match response '<h1 class="text-xl text-red-600">Error</h1>'
    And match response '<p id="error-message-text">Orderer no encontrado</p>'
    And !match response '<span id="orderer-username">'


  