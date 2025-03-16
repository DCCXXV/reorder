Feature: Creación de Order

Background:
  * url 'http://localhost:8080'

Scenario: Crear Order flujo habitual
  # 1: El Orderer entra en la aplicación
  Given path '/createOrder'
  When method GET
  Then status 200
  And match response contains 'Crear Order'

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
  And match response contains 'elementContainerTier2'

  # 5: El Orderer mueve un elemento al nuevo tier
  Given path '/createOrder/updateOrderState'
  And form field orderStateJson = '[["Matrix"], [], ["Inception"]]'
  When method POST
  Then status 200

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
  And match response contains 'elementContainerTier3'

  # 10: El Orderer mueve un elemento al nuevo tier
  Given path '/createOrder/updateOrderState'
  And form field orderStateJson = '[[], ["Matrix"], [],["Inception"]]'
  When method POST
  Then status 200

  # 11: El Orderer elimina un Tier con elemento
  Given path '/createOrder/deleteLastTier'
  When method POST
  Then status 200
  And match response !contains 'Inception'
  And match response !contains 'elementContainerTier3'


  # 12: El Orderer elimina un Tier sin elemento
  Given path '/createOrder/deleteLastTier'
  When method POST
  Then status 200
  And match response !contains 'elementContainerTier2'

Scenario: Crear Order flujo no habitual, el usuario intenta romper la aplicación
  # 1: El Orderer entra en la aplicación
  Given path '/createOrder'
  When method GET
  Then status 200
  And match response contains 'Crear Order'

  # 2: EL Orderer fuerza una llamada a /createOrder/deleteTier
  Given path '/createOrder/deleteLastTier'
  When method POST
  Then status 200
  And match response contains 'elementContainerTier1'

Scenario: Crear Order prueba de errores al añadir elementos
  # 1: EL Orderer intenta crear un elemento vacío
  Given path '/createOrder/addElement'
  And form field elementTextInput = ""
  When method POST
  Then status 200
  And match response contains '¡El elemento no puede estar vacío!'

  # 2: EL Orderer intenta crear un elemento con muchos caractéres
  Given path '/createOrder/addElement'
  And form field elementTextInput = "testtesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttest"
  When method POST
  Then status 200
  And match response contains '¡El elemento debe tener menos de 30 caracteres!'

  # 3: EL Orderer añade un elemento
  Given path '/createOrder/addElement'
  And form field elementTextInput = "Matrix"
  When method POST
  Then status 200

  # 4. El Orderer añade un elemento repetido
  Given path '/createOrder/addElement'
  And form field elementTextInput = "Matrix"
  When method POST
  Then status 200
  And match response contains '¡El elemento ya existe!'