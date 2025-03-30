Feature: Publicar ReOrder

Background:
  Given url 'http://localhost:8080'
  * def validReorderStateJson = '[[], ["Elemento Movido"], ["Elemento Original"]]'
  * def sameAsOriginalStateJson = '[[], ["Elemento Original"], ["Elemento Movido"]]'
  * def originalOrderId = 999

Scenario: Publicar ReOrder con error crítico (Falta estado en sesión)
  # --- NO configurar sesión ---

  # --- Intentar publicar (debería fallar por estado nulo) ---
  Given path '/reorder/PublishOrder'
  And form field rtitle = "Fallo Crítico"
  And form field rauthor = "Tester"
  And form field originalOrderId = originalOrderId
  When method post
  Then status 500
  And match response == "Error inesperado al procesar. Intenta de nuevo."