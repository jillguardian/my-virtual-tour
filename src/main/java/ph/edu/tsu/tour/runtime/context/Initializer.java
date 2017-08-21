package ph.edu.tsu.tour.runtime.context;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import ph.edu.tsu.tour.core.access.AccessManagementService;
import ph.edu.tsu.tour.core.access.Privilege;
import ph.edu.tsu.tour.core.access.Role;
import ph.edu.tsu.tour.core.access.User;
import ph.edu.tsu.tour.security.Privileges;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

@Configuration
public class Initializer {

    private static final Logger logger = LoggerFactory.getLogger(Initializer.class);

    @Component
    private static class DataLoader implements ApplicationRunner {

        private AccessManagementService accessManagementService;

        @Autowired
        public DataLoader(AccessManagementService accessManagementService) {
            this.accessManagementService = accessManagementService;
        }

        public void run(ApplicationArguments args) {
            logger.info("Attempting to initialize database with default privilege(s), role(s), and user(s)...");

            Privilege poiWritePrivilege = Privilege.builder().name(Privileges.PointOfInterest.WRITE).build();
            Iterable<Privilege> adminPrivileges = accessManagementService.savePrivileges(Arrays.asList(
                    poiWritePrivilege,
                    Privilege.builder().name(Privileges.Access.WRITE).build(),
                    Privilege.builder().name(Privileges.Access.READ).build()
            ));

            Role adminRole = Role.builder()
                    .name("Administrator")
                    .privileges(Sets.newHashSet(adminPrivileges))
                    .build();
            accessManagementService.saveRoles(Arrays.asList(
                    adminRole,
                    Role.builder()
                            .name("User")
                            .privileges(Sets.newHashSet(poiWritePrivilege))
                            .build()));

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
