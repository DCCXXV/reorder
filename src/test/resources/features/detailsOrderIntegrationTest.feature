Feature: Ver Order en detalle

Background:
    * url 'http://localhost:8080'

Scenario: Ver un Order en detalle flujo habitual
    # 1: El Orderer entra en la vista de ver Order y la vista se ve correctamente
    Given path '/order/1'
    When method GET
    Then status 200

