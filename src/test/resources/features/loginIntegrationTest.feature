Feature: Pruebas para el iniciar sesion

Background:
  * url 'http://localhost:8080'
  * def invalidUsername = 'wrongUser'
  * def invalidPassword = 'wrongPassword'
  * def validUsername = 'existingUser'
  * def validPassword = 'correctPassword'

#1.POST /login con credenciales incorrectas
#Spring Security responde con 302 Redirect a /login?error
#2.GET /login?error
# OrdererLoginController muestra el formulario de login otra vez con un mensaje de error.
Scenario: Login fallido debe redirigir a /login?error y mostrar mensaje de error
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
  And match response contains 'Usuario o contaseña inválidos'

#este no va, no se como tener ya un user en la bbdd
  Scenario: Inicio de sesion correcto con username y password válidos
# * configure followRedirects = false

#Given path '/login'
#And form field username = validUsername
#And form field password = validPassword
#When method post
#Then status 302
#And match responseHeaders['Location'][0] == '/'

# seguir redirección y comprobar home
#* configure followRedirects = true
#Given url 'http://localhost:8080/'
#When method get
#Then status 200



