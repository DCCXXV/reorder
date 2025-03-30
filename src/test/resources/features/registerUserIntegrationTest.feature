Feature: Registrar usuario

Background:
  * url 'http://localhost:8080'

Scenario: Registrar usuario
    # 1: El usuario intenta registrarse con contraseña menor de 8 dígitos
    Given Path '/register'
    And form field username = "AlbaGaliano"
    And form field email = "albagaliano@gmail.com"
    And form field password = "Alb4"
    When method POST
    Then status 200
    And match response contains "Error"

    # 2: El usuario intenta registrarse sin poner mínimo una mayúscula en la contraseña
    Given Path '/register'
    And form field username = "AlbaGaliano"
    And form field email = "albagaliano@gmail.com"
    And form field password = "alb4galiano"
    When method POST
    Then status 200
    And match response contains "Error"

    # 3: El usuario intenta registrarse sin añadir ningún número en la contraseña
    Given Path '/register'
    And form field username = "AlbaGaliano"
    And form field email = "albagaliano@gmail.com"
    And form field password = "AlbaaGaliano"
    When method POST
    Then status 200
    And match response contains "Error"

    # 4: El usuario intenta registrarse sin añadir Nombre de Usuario
    Given Path '/register'
    And form field username = ""
    And form field email = "albagaliano@gmail.com"
    And form field password = "Alb4aGaliano"
    When method POST
    Then status 200
    And match response contains "Error"

    # 5: El usuario intenta registrarse sin añadir el correo
    Given Path '/register'
    And form field username = "AlbaGaliano"
    And form field email = ""
    And form field password = "Alb4aGaliano"
    When method POST
    Then status 200
    And match response contains "Error"

    # 6: El usuario se registra correctamente
    Given Path '/register'
    And form field username = "AlbaGaliano"
    And form field email = "albagaliano@gmail.com"
    And form field password = "Alb4aGaliano"
    When method POST
    Then status 200