Feature: Pruebas para el controlador de registro de Orderer

  Background:
    * url 'http://localhost:8080'

  Scenario: Obtener la página de registro
    Given path '/signup'
    When method get
    Then status 200

  Scenario: Registro exitoso de un nuevo Orderer
    Given path '/signup/upload'
    And form field username = 'AlbaGaliano'
    And form field email = 'albagaliano@gmail.com'
    And form field password = 'Alb4aGaliano'
    When method post
    Then status 200

  Scenario: Intento de registro con nombre de usuario vacío
    Given path '/signup/upload'
    And form field username = ''
    And form field email = 'albagaliano@gmail.com'
    And form field password = 'Alb4aGaliano'
    When method post
    Then status 200

  Scenario: Intento de registro con email vacío
    Given path '/signup/upload'
    And form field username = 'AlbaGaliano'
    And form field email = ''
    And form field password = 'Alb4aGaliano'
    When method post
    Then status 200

  Scenario: Intento de registro con contraseña vacía
    Given path '/signup/upload'
    And form field username = 'AlbaGaliano'
    And form field email = 'albagaliano@gmail.com'
    And form field password = ''
    When method post
    Then status 200

  Scenario: Intento de registro con email ya existente
    Given path '/signup/upload'
    And form field username = 'OtroUsuario'
    And form field email = 'albagaliano@gmail.com'
    And form field password = 'OtraClave123'
    When method post
    Then status 200

  Scenario: Intento de registro con nombre de usuario ya existente
    Given path '/signup/upload'
    And form field username = 'AlbaGaliano'
    And form field email = 'otroemail@ejemplo.com'
    And form field password = 'OtraClave123'
    When method post
    Then status 200