Feature: Hacer un ReOrder

Background:
  * def port = karate.properties['karate.server.port']
  * url 'http://localhost:' + port
  
Scenario: Crear un ReOrder a partir de un Order existente