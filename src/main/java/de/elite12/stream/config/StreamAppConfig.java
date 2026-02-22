package de.elite12.stream.config;

import de.elite12.stream.util.CustomJwtAuthenticationConverter;
import org.apache.catalina.filters.RemoteIpFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationEventPublisher;
import org.springframework.security.authorization.SpringAuthorizationEventPublisher;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.messaging.access.intercept.AuthorizationChannelInterceptor;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.messaging.context.AuthenticationPrincipalArgumentResolver;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableMethodSecurity
@EnableScheduling
public class StreamAppConfig {

    @Configuration
    @EnableWebSocket
    @EnableWebSocketMessageBroker
    public static class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

        private final TaskScheduler messageBrokerTaskScheduler;

        public WebSocketConfiguration(TaskScheduler messageBrokerTaskScheduler) {
            this.messageBrokerTaskScheduler = messageBrokerTaskScheduler;
        }

        @Override
        public void configureMessageBroker(MessageBrokerRegistry config) {
            config.enableSimpleBroker("/topic", "/queue")
                    .setHeartbeatValue(new long[]{30000, 30000})
                    .setTaskScheduler(this.messageBrokerTaskScheduler);
            config.setApplicationDestinationPrefixes("/app");
        }

        @Override
        public void registerStompEndpoints(StompEndpointRegistry registry) {
            registry.addEndpoint("/sock").setAllowedOriginPatterns("*").withSockJS().setSessionCookieNeeded(false);
        }
    }

    @Configuration
    public static class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

        private final ApplicationContext applicationContext;

        public WebSocketSecurityConfig(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        @Bean
        AuthorizationManager<Message<?>> messageAuthorizationManager() {
            MessageMatcherDelegatingAuthorizationManager.Builder messages = MessageMatcherDelegatingAuthorizationManager.builder();
            messages
                    .nullDestMatcher().permitAll()
                    .simpSubscribeDestMatchers("/topic/racecontrol").permitAll()
                    .simpSubscribeDestMatchers("/topic/test").permitAll()
                    .simpSubscribeDestMatchers("/user/queue/echoreply").permitAll()
                    .simpMessageDestMatchers("/app/echo").permitAll()
                    .anyMessage().denyAll();

            return messages.build();
        }

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
            argumentResolvers.add(new AuthenticationPrincipalArgumentResolver());
        }

        @Override
        public void configureClientInboundChannel(ChannelRegistration registration) {
            AuthorizationChannelInterceptor authz = new AuthorizationChannelInterceptor(messageAuthorizationManager());
            AuthorizationEventPublisher publisher = new SpringAuthorizationEventPublisher(this.applicationContext);
            authz.setAuthorizationEventPublisher(publisher);
            registration.interceptors(new SecurityContextChannelInterceptor(), authz);
        }
    }

    @Configuration
    @EnableWebSecurity
    public static class WebSecurityConfiguration {

        private final CustomJwtAuthenticationConverter jwtAuthenticationConverter;

        public WebSecurityConfiguration(CustomJwtAuthenticationConverter jwtAuthenticationConverter) {
            this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .csrf((csrf) -> csrf.disable())
                    .cors(Customizer.withDefaults())
                    .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .oauth2ResourceServer((oauth2) -> oauth2
                            .jwt((jwt) -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                    )
                    .headers((headers) -> headers.disable())
                    .authorizeHttpRequests((authorize) -> authorize
                            .requestMatchers("/manage/**").hasRole("actuator")
                            .anyRequest().permitAll()
                    );
            return http.build();
        }

        @Bean
        public RemoteIpFilter remoteIpFilter() {
            return new RemoteIpFilter();
        }

        @Bean
        CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
            configuration.setAllowedHeaders(Arrays.asList("origin", "content-type", "accept", "authorization"));
            configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
            configuration.setAllowCredentials(true);
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", configuration);
            return source;
        }
    }
}
