package ph.edu.tsu.tour.configuration;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ph.edu.tsu.tour.domain.Privilege;
import ph.edu.tsu.tour.domain.Role;
import ph.edu.tsu.tour.domain.User;
import ph.edu.tsu.tour.repository.PointOfInterestRepository;
import ph.edu.tsu.tour.repository.PrivilegeRepository;
import ph.edu.tsu.tour.repository.RoleRepository;
import ph.edu.tsu.tour.repository.UserRepository;
import ph.edu.tsu.tour.service.*;
import ph.edu.tsu.tour.service.impl.AccessManagementServiceImpl;
import ph.edu.tsu.tour.service.impl.PointOfInterestServiceImpl;
import ph.edu.tsu.tour.configuration.security.Privileges;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

@Configuration
public class MainContext {

    private static final Logger logger = LoggerFactory.getLogger(MainContext.class);

    @Bean
    public PointOfInterestService pointOfInterestService(PointOfInterestRepository pointOfInterestRepository) {
        return new PointOfInterestServiceImpl(pointOfInterestRepository);
    }

    @Bean
    public AccessManagementService accessManagementService(PrivilegeRepository privilegeRepository,
                                                           RoleRepository roleRepository,
                                                           UserRepository userRepository,
                                                           PasswordEncoder passwordEncoder) {
        return new AccessManagementServiceImpl(privilegeRepository, roleRepository, userRepository, passwordEncoder);
    }

    @Component
    private class DataLoader implements ApplicationRunner {

        private AccessManagementService accessManagementService;

        @Autowired
        public DataLoader(AccessManagementService accessManagementService) {
            this.accessManagementService = accessManagementService;
        }

        public void run(ApplicationArguments args) {
            logger.info("Attempting to initialize database with default user...");

            Iterable<Privilege> adminPrivileges = accessManagementService.savePrivileges(Arrays.asList(
                    Privilege.builder().name(Privileges.PointOfInterest.WRITE).build(),
                    Privilege.builder().name(Privileges.Access.WRITE).build(),
                    Privilege.builder().name(Privileges.Access.READ).build()
            ));

            Role adminRole = Role.builder()
                    .name("ADMIN")
                    .privileges(Sets.newHashSet(adminPrivileges))
                    .build();
            accessManagementService.saveRole(adminRole);

            User user = User.builder()
                    .username("admin")
                    .password("admin")
                    .roles(new HashSet<>(Collections.singleton(adminRole)))
                    .build();
            accessManagementService.saveUser(user);

            logger.info("Database initialized!");
        }
    }

}
