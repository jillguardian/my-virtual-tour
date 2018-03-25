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
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
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
            http.requestMatchers()
                    .antMatchers(Urls.REST_V1_TOUR + "/**", Urls.REST_V1_USER + "/**")
                    .and().authorizeRequests()
                    .requestMatchers(new AntPathRequestMatcher(Urls.REST_V1_TOUR)).authenticated()
                    .requestMatchers(new AntPathRequestMatcher(Urls.REST_V1_USER + "/new", "POST"))
                    .permitAll()
                    .requestMatchers(new AntPathRequestMatcher(Urls.REST_V1_USER + "/verify", "POST"))
                    .permitAll()
                    .requestMatchers(new AntPathRequestMatcher(Urls.REST_V1_USER + "/reverify", "POST"))
                    .permitAll()
                    .requestMatchers(new AntPathRequestMatcher(Urls.REST_V1_USER + "/request-password-reset", "POST"))
                    .permitAll()
                    .requestMatchers(new AntPathRequestMatcher(Urls.REST_V1_USER + "/reset-password", "POST"))
                    .permitAll()
                    .requestMatchers(new AntPathRequestMatcher(Urls.REST_V1_USER + "/**")).authenticated()
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
                    .requestMatchers(new AntPathRequestMatcher(Urls.CHURCH_LOCATION + "/new", "GET"))
                    .hasAuthority(Privileges.Location.WRITE)
                    .requestMatchers(new AntPathRequestMatcher(Urls.CHURCH_LOCATION, "GET"))
                    .permitAll()
                    .antMatchers(Urls.CHURCH_LOCATION + "/**")
                    .hasAuthority(Privileges.Location.WRITE)
                    .antMatchers(Urls.ADMINISTRATOR + "/me")
                    .authenticated()
                    .requestMatchers(new AntPathRequestMatcher(Urls.ADMINISTRATOR + "/new", "GET"),
                                     new AntPathRequestMatcher(Urls.ADMINISTRATOR + "/**", "POST"))
                    .hasAuthority(Privileges.Access.WRITE)
                    .requestMatchers(new AntPathRequestMatcher(Urls.ADMINISTRATOR + "/**", "GET"))
                    .hasAuthority(Privileges.Access.READ)
                    .requestMatchers(new AntPathRequestMatcher(Urls.USER, "GET"))
                    .hasAuthority(Privileges.User.WRITE)
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
    @Order(3)
    public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        @Bean
        public HttpFirewall httpFirewall() {
            StrictHttpFirewall firewall = new StrictHttpFirewall();
            firewall.setAllowUrlEncodedSlash(true);
            firewall.setAllowSemicolon(true);
            return firewall;
        }

        @Override
        public void configure(WebSecurity web) throws Exception {
            super.configure(web);
            web.httpFirewall(httpFirewall());
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                    .requestMatchers(new AntPathRequestMatcher(Urls.REST_PREFIX_V1 + "/**", "GET"))
                    .permitAll()
                    .and()
                    .httpBasic()
                    .and().csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        }

    }

}
