Feature: Hacer un ReOrder

Background:
  * url 'http://localhost:8080'

Scenario: Crear un ReOrder a partir de un Order existente

## 1: el usuario actualiza el order 
  Given path '/reorder/updateOrderState'
  And form field reOrderStateJson = '[[], ["Pera"], ["Platano"],["Manzana"]]'
  When method POST
  Then status 200

## 2: el usuario actualiza el order de nuevo
  Given path '/reorder/updateOrderState'
  And form field reOrderStateJson = '[[], ["Pera", "Manzana"], [],["Platano"]]'
  When method POST
  Then status 200