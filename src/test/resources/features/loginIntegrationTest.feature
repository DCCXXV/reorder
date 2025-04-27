Feature: Pruebas para el iniciar sesion

Background:
  * def port = karate.properties['karate.server.port']
  * url 'http://localhost:' + port
  * def invalidEmail = 'wrongEmail'
  * def invalidUsername = 'wrongUser'
  * def invalidPassword = 'wrongPassword'

Scenario: Inicio de sesion fallido debe mostrar mensaje de error (usuario)
  # Desactivar seguimiento automático de redirecciones
  * configure followRedirects = false

  # 1. login con credenciales incorrectas
  Given path '/login'
  And form field username = invalidUsername
  And form field password = invalidPassword
  When method post

  # 2. Verificar que devuelve 302 Redirect a /login?error
  Then status 302
  And match responseHeaders['Location'][0] contains '/login?error'

  # 3. Ahora hacemos GET manual a /login?error (ya con followRedirects activado)
  * configure followRedirects = true
  Given url responseHeaders['Location'][0]
  When method get
  Then status 200

  # 4. Verificar que aparece el mensaje de error 
  And match response contains 'Usuario o contraseña inválidos'

Scenario: Inicio de sesion fallido debe mostrar mensaje de error (email)
  # Desactivar seguimiento automático de redirecciones
  * configure followRedirects = false

  # 1. login con credenciales incorrectas
  Given path '/login'
  And form field username = invalidEmail
  And form field password = invalidPassword
  When method post

  # 2. Verificar que devuelve 302 Redirect a /login?error
  Then status 302
  And match responseHeaders['Location'][0] contains '/login?error'

  # 3. Ahora hacemos GET manual a /login?error (ya con followRedirects activado)
  * configure followRedirects = true
  Given url responseHeaders['Location'][0]
  When method get
  Then status 200

  # 4. Verificar que aparece el mensaje de error 
  And match response contains 'Usuario o contraseña inválidos'


