Feature: Publicar ReOrder

  Background:
  * def port = karate.properties['karate.server.port']
  * url 'http://localhost:' + port
  * def knownExistingOrderId = 1
  * def validReorderStateJson = '[[], ["Elemento Movido"], ["Elemento Original"]]'

Scenario: Publicar ReOrder con error crítico (Falta estado en sesión)
  # --- NO configurar sesión ---

  # --- Intentar publicar (debería fallar por estado nulo) ---
  Given path '/reorder/PublishOrder'
  And form field rtitle = "Fallo Crítico"
  And form field rauthor = "Tester"
  And form field originalOrderId = knownExistingOrderId
  When method post
  Then status 500
  And match response == "Error inesperado al procesar. Intenta de nuevo."