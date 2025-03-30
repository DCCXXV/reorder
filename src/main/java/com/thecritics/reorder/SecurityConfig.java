package com.thecritics.reorder;

import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
	private Environment env;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        String debugProperty = env.getProperty("com.thecritics.reorder.debug");
		if (debugProperty != null && Boolean.parseBoolean(debugProperty.toLowerCase())) {
			http.csrf(csrf -> csrf
				.ignoringRequestMatchers("/h2/**")
			);
			http.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/h2/**").permitAll()  // <-- no login for h2 console
			);
      http.headers(header->header.frameOptions(frameOptions->frameOptions.sameOrigin()));
		}
        http 
                //.requiresChannel(channel -> 
                //    channel.anyRequest().requiresSecure())  // fuerza HTTPS
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

	

}
