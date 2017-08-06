package ph.edu.tsu.tour.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import ph.edu.tsu.tour.repository.UserRepository;
import ph.edu.tsu.tour.service.impl.UserDetailsServiceImpl;
import ph.edu.tsu.tour.web.Urls;

@EnableWebSecurity
public class WebSecurityConfiguration {

    @Configuration
    public static class WebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        private UserRepository userRepository;

        @Autowired
        public WebSecurityConfigurationAdapter(UserRepository userRepository) {
            this.userRepository = userRepository;
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                    .requestMatchers(new AntPathRequestMatcher(Urls.POI + "/new", "GET"))
                    .hasAuthority(Privileges.PointOfInterest.WRITE)
                    .requestMatchers(new AntPathRequestMatcher(Urls.POI + "/**", "GET"))
                    .permitAll()
                    .antMatchers(Urls.POI + "/**")
                    .hasAuthority(Privileges.PointOfInterest.WRITE)
                    .requestMatchers(new AntPathRequestMatcher(Urls.ACCESS_MANAGEMENT + "/new", "GET"))
                    .hasAuthority(Privileges.Access.WRITE)
                    .requestMatchers(new AntPathRequestMatcher(Urls.ACCESS_MANAGEMENT + "/**", "GET"))
                    .hasAuthority(Privileges.Access.READ)
                    .antMatchers(Urls.ACCESS_MANAGEMENT + "/**")
                    .hasAuthority(Privileges.Access.WRITE)
                    .antMatchers("/login").anonymous()
                    .antMatchers("/logout").authenticated()
                    .and().formLogin()
                    .loginPage("/login")
                    .defaultSuccessUrl("/")
                    .failureUrl("/login?error=true")
                    .and().logout()
                    .logoutSuccessUrl("/login?logout=true")
                    .and().httpBasic();
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.authenticationProvider(authenticationProvider());
        }

        @Bean
        public AuthenticationProvider authenticationProvider() {
            DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
            authProvider.setUserDetailsService(userDetailsService());
            authProvider.setPasswordEncoder(passwordEncoder());
            return authProvider;
        }

        @Bean
        public UserDetailsService userDetailsService() {
            return new UserDetailsServiceImpl(userRepository);
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

    }

    @Configuration
    @Order(1)
    public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.antMatcher(Urls.REST_PREFIX + "/**")
                    .authorizeRequests()
                    .requestMatchers(new AntPathRequestMatcher(Urls.REST_POI + "/**", "GET")).permitAll()
                    .anyRequest().denyAll()
                    .and().httpBasic()
                    .and().csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        }

    }

}
