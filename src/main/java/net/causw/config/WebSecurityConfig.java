package net.causw.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public WebSecurityConfig(JwtTokenProvider jwtTokenProvider, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()
                .csrf().disable()

                .exceptionHandling()
                .authenticationEntryPoint(this.jwtAuthenticationEntryPoint)

                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()
                .authorizeRequests()
                .antMatchers(
                        "/api/**/users/sign-in",
                        "/api/**/users/sign-up",
                        "/api/**/users/me, ",
                        "/healthy",
                        "/api/**/users/admissions/apply"
                )
                .permitAll()
                .anyRequest().authenticated()

                .and()
                .addFilterBefore(new JwtAuthenticationFilter(this.jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // TODO : 배포 시 Domain으로 변경
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(
                "/v2/api-docs", "/swagger-resources/**",
                "/webjars/**", "/swagger/**", "/swagger-ui/**"
        );
    }
}
