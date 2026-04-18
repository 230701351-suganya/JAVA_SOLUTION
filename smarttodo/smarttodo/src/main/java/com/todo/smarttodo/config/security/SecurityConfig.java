package com.todo.smarttodo.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

//Spring starts the application.

//It scans packages (from @SpringBootApplication or explicit @ComponentScan).

//When it finds a class with @Configuration:

//It knows: “This class contains bean definitions.”

//Spring then looks for methods with @Bean inside that class.
//Because @Bean produces objects that Spring will manage.
//
//Spring can inject these objects anywhere using @Autowired or constructor injection.
//
//Without executing the method, there would be no object to manage.
//Spring reads SecurityConfig → finds securityFilterChain()
//
//Executes it → gets SecurityFilterChain object
//
//Stores it in container → Spring Security uses it automatically
//
//Effect: every HTTP request passes through the filter chain
//Once the SecurityFilterChain object is stored, Spring Security uses it to intercept every incoming HTTP request,
// checks authentication and authorization rules, and decides whether the request should be allowed to reach the controller
// or be blocked.
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    //HttpSecurity is a Spring Security class that acts like a builder object.
    //It lets you configure security rules for your application in a fluent way.
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        //http is a Spring-provided configuration object (HttpSecurity) that you use to define security rules.
        // You modify it, then call build() to create the filter chain Spring Security uses for requests.
        http
                // Disable CSRF because we use REST APIs
                .csrf(csrf -> csrf.disable())

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/users/register").permitAll()
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/task-action/**").permitAll()
                        .anyRequest().authenticated()//SecurityContextHolder.getContext().getAuthentication() != null

                ).cors(cors -> {});;http.addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
        );


        //http.build() produces a SecurityFilterChain object.
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    // Inside SecurityConfig class
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of("http://localhost:5173")); // Your frontend
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
