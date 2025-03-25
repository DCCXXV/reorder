Feature: Buscar Order por titulo

Background:
  * url 'http://localhost:8080'

Scenario: Buscar Order por titulo

  # El Orderer hace la acción de buscar un Order por un título que no existe
  Given path '/search'
  And form field query = "Top 10 puertas"
  When method POST
  Then status 200

