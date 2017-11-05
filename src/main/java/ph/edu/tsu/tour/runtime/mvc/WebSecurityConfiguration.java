package ph.edu.tsu.tour.runtime.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import ph.edu.tsu.tour.core.access.AdministratorRepository;
import ph.edu.tsu.tour.core.access.Privileges;
import ph.edu.tsu.tour.core.user.UserRepository;
import ph.edu.tsu.tour.web.Urls;

@EnableWebSecurity
public class WebSecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean("userAuthenticationProvider")
    public AuthenticationProvider userAuthenticationProvider(UserRepository userRepository,
                                                             PasswordEncoder passwordEncoder) {
        UserDetailsService userDetailsService = new ph.edu.tsu.tour.core.user.UserDetailsServiceImpl(userRepository);
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return authenticationProvider;
    }

    @Bean("administratorAuthenticationProvider")
    public AuthenticationProvider administratorAuthenticationProvider(AdministratorRepository administratorRepository,
                                                                      PasswordEncoder passwordEncoder) {
        UserDetailsService userDetailsService =
                new ph.edu.tsu.tour.core.access.UserDetailsServiceImpl(administratorRepository);
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return authenticationProvider;
    }

    @Configuration
    @Order(1)
    public static class UserApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        private final AuthenticationProvider authenticationProvider;

        @Autowired
        public UserApiWebSecurityConfigurationAdapter(
                @Qualifier("userAuthenticationProvider") AuthenticationProvider authenticationProvider) {
            this.authenticationProvider = authenticationProvider;
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.antMatcher(Urls.REST_USER + "/**")
                    .authorizeRequests()
                    .requestMatchers(new AntPathRequestMatcher(Urls.REST_USER + "/new", "POST")).permitAll()
                    .requestMatchers(new AntPathRequestMatcher(Urls.REST_USER + "/**")).authenticated()
                    .and().httpBasic()
                    .and().csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.authenticationProvider(authenticationProvider);
        }

    }

    @Configuration
    @Order(2)
    public static class AdministratorWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        private final AuthenticationProvider authenticationProvider;

        @Autowired
        public AdministratorWebSecurityConfigurationAdapter(
                @Qualifier("administratorAuthenticationProvider") AuthenticationProvider authenticationProvider) {
            this.authenticationProvider = authenticationProvider;
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                    .requestMatchers(new AntPathRequestMatcher(Urls.POI + "/new", "GET"))
                    .hasAuthority(Privileges.PointOfInterest.WRITE)
                    .requestMatchers(new AntPathRequestMatcher(Urls.POI, "GET"))
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
            auth.authenticationProvider(authenticationProvider);
        }

    }

    @Configuration
    public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.antMatcher(Urls.REST_PREFIX + "/**")
                    .authorizeRequests()
                    .requestMatchers(new AntPathRequestMatcher(Urls.REST_PREFIX + "/**", "GET")).permitAll()
                    .anyRequest().denyAll()
                    .and().httpBasic()
                    .and().csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        }

    }

}
