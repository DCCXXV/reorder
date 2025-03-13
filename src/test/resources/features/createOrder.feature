Feature: Creación de Order

Background:
  * url 'http://localhost:8080'
  * configure headers = { "HX-Request": "true" }
  * def elementsContainer = '#elementsContainer'
  * def tiersContainer = '#tiersContainer'

Scenario: [SISTEMA] Un Orderer Crea su Order usando todas las funcionalidades
  # 1: El Orderer entra en la aplicación
  Given path '/createOrder'
  When method GET
  Then status 200
  And match response contains 'Order'

  # 2: El Orderer añade un elemento
  Given path '/createOrder/addElement'
  And form field elementTextInput = "Matrix"
  When method POST
  Then status 200
  And match response contains 'Matrix'

  # 3: El Orderer añade otro elemento
  Given path '/createOrder/addElement'
  And form field elementTextInput = "Inception"
  When method POST
  Then status 200
  And match response contains 'Inception'

  # 4: El Orderer añade un tier
  Given path '/createOrder/addTier'
  When method POST
  Then status 200
  And match header HX-Trigger == 'tierAdded'

  # 5: El Orderer mueve un elemento al nuevo tier
  Given path '/createOrder/updateOrderState'
  And request { orderStateJson: [["Matrix"], [], ["Inception"]] }
  When method POST
  Then status 200
  And match response != "error"
  
  # 6: El Orderer recibe la información correcta
  Given path '/createOrder'
  When method GET
  Then status 200
  And match elementsContainer contains 'Matrix'
  And match tiersContainer contains '<div class="elemContTier">'
  And match tiersContainer contains 'Inception'

  # 7: El Orderer elimina un elemento
  Given path '/createOrder/deleteElement'
  And form field elementTextBadge = "Inception"
  When method POST
  Then status 200
  And match response !contains 'Inception'

  # 8: El Orderer añade otro elemento
  Given path '/createOrder/addElement'
  And form field elementTextInput = "Inception"
  When method POST
  Then status 200
  And match response contains 'Inception'

  # 9: El Orderer añade un nuevo Tier
  Given path '/createOrder/addTier'
  When method POST
  Then status 200
  And match header HX-Trigger == 'tierAdded'

  # 10: El Orderer mueve un elemento al nuevo tier
  Given path '/createOrder/updateOrderState'
  And form field orderStateJson = '[[], ["Matrix"], [],["Inception"]]'
  When method POST
  Then status 200
  And match response != "error"