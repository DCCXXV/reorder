Feature: Publicar Order

Background:
  * url 'http://localhost:8080'

Scenario: Publicar Order
  # El Orderer le da a publicar Order sin poner un título
  Given path '/createOrder/PublishOrder'
  And form field title = ""
  And form field author = "autor"
  When method POST
  Then status 200
  And match response contains "Error"

  # El Orderer le da a publicar Order con un título
  Given path '/createOrder/PublishOrder'
  And form field title = "Top 10 puertas"
  And form field author = "autor"
  When method POST
  Then status 200

  # El Orderer le da a publicar Order con un título y en Anónimo
  Given path '/createOrder/PublishOrder'
  And form field title = "Top 10 puertas"
  And form field author = ""
  When method POST
  Then status 200

  # El Orderer le da a publicar Order con un título y con un nombre de autor
  Given path '/createOrder/PublishOrder'
  And form field title = "Top 10 puertas"
  And form field author = "ManuVilla"
  When method POST
  Then status 200