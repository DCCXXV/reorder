Feature: Publicar Order

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

  * def validOrderStateJson = '[[], ["Puerta Simple"], ["Puerta Corredera"]]'
  * def emptyTiersOrderStateJson = '[["ElementoSinTier"], [], []]'

Scenario: El Orderer le da a publicar Order sin poner un título
  Given path '/createOrder/PublishOrder'
  And form field title = ""
  And form field author = "autor"
  When method post
  Then status 400
  And match response == "El título no puede estar vacío."

Scenario: El Orderer le da a publicar Order con un título (Éxito - Ajustado)
  # --- 1. Configurar sesión VÁLIDA ---
  Given path '/createOrder/updateOrderState'
  And form field orderStateJson = validOrderStateJson
  When method post
  Then status 200

  # --- 2. Realizar la prueba principal ---
  Given path '/createOrder/PublishOrder'
  And form field title = "Top 10 puertas"
  And form field author = "autor"
  When method post
  Then status 200
  And match response contains '<span class="mt-4 text-4xl font-bold text-secondary">Top 10 puertas</span>'
  And match response contains '<span class="mt-1 mb-4 text-xl" style="filter: brightness(80%);">@autor</span>'

Scenario: El Orderer le da a publicar Order con un título y en Anónimo (Éxito - Ajustado)
  Given path '/createOrder/updateOrderState'
  And form field orderStateJson = validOrderStateJson
  When method post
  Then status 200

  Given path '/createOrder/PublishOrder'
  And form field title = "Top 10 puertas"
  And form field author = ""
  When method post
  Then status 200
  And match response contains '<span class="mt-4 text-4xl font-bold text-secondary">Top 10 puertas</span>'
  And match response contains '<span class="mt-1 mb-4 text-xl" style="filter: brightness(80%);">@Anónimo</span>'

Scenario: El Orderer le da a publicar Order con un título y con un nombre de autor (Éxito - Ajustado)
  # --- 1. Configurar sesión VÁLIDA ---
  Given path '/createOrder/updateOrderState'
  And form field orderStateJson = validOrderStateJson
  When method post
  Then status 200

  # --- 2. Realizar la prueba principal ---
  Given path '/createOrder/PublishOrder'
  And form field title = "Top 10 puertas"
  And form field author = "ManuVilla"
  When method post
  Then status 200
  And match response contains '<span class="mt-4 text-4xl font-bold text-secondary">Top 10 puertas</span>'
  And match response contains '<span class="mt-1 mb-4 text-xl" style="filter: brightness(80%);">@ManuVilla</span>'

Scenario: El Orderer le da a publicar Order sin elementos en los Tiers (Fallo Validación)
  # --- 1. Configurar sesión INVÁLIDA ---
  Given path '/createOrder/updateOrderState'
  And form field orderStateJson = emptyTiersOrderStateJson
  When method post
  Then status 200

  # --- 2. Realizar la prueba principal ---
  Given path '/createOrder/PublishOrder'
  And form field title = "Tiers Vacios"
  And form field author = "tester"
  When method post
  Then status 400
  And match response == "Debe haber al menos un elemento en un Tier para publicar."