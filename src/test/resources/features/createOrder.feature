Feature: Creación de Orders

Background:
  * url 'http://localhost:8080'
  * configure headers = { "HX-Request": "true" }  # Simula HTMX
  * def elementsContainer = '#elementsContainer'
  * def tiersContainer = '#tiersContainer'

Scenario: Crear tier list con elementos y tiers
  # 1: Cargar la página de creación
  Given path '/createOrder'
  When method GET
  Then status 200
  And match response contains 'Order'

  # 2: Añadir elementos
  Given path '/createOrder/addElement'
  And form field elementTextInput = "Matrix"
  When method POST
  Then status 200
  * print '===== RESPONSE CONTENT ====='
  * print elementsContainer
  And match response contains 'Matrix'

  # 3: Añadir otro elemento
  Given path '/createOrder/addElement'
  And form field elementTextInput = "Inception"
  When method POST
  Then status 200
  And match response contains 'Inception'

  # 4: Añadir un tier
  Given path '/createOrder/addTier'
  When method POST
  Then status 200
  And match header HX-Trigger == 'tierAdded'

  # 5: Mover elementos al nuevo tier (simular drag & drop)
  Given path '/createOrder/updateOrderState'
  And form field orderStateJson = '[["Matrix"], ["Inception"]]'
  When method POST
  Then status 200
  And match response != "error"

  # 6: Verificar estructura final
  Given path '/createOrder'
  When method GET
  Then status 200
  And match elementsContainer contains 'Matrix'
  And match tiersContainer contains '<div class="elemContTier">'
  And match tiersContainer contains 'Inception'

  # 7: Eliminar elemento
  Given path '/createOrder/deleteElement'
  And form field elementTextBadge = "Inception"
  When method POST
  Then status 200
  And match response !contains 'Inception'