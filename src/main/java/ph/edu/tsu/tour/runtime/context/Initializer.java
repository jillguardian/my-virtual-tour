package ph.edu.tsu.tour.runtime.context;

import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;
import org.springframework.stereotype.Component;
import ph.edu.tsu.tour.core.access.AccessManagementService;
import ph.edu.tsu.tour.core.access.Administrator;
import ph.edu.tsu.tour.core.access.Privilege;
import ph.edu.tsu.tour.core.access.Role;
import ph.edu.tsu.tour.core.access.Privileges;
import ph.edu.tsu.tour.core.user.PublishingUserService;
import ph.edu.tsu.tour.core.user.PublishingVerificationTokenService;
import ph.edu.tsu.tour.core.user.VerificationTokenCreatingListener;
import ph.edu.tsu.tour.core.user.VerificationUrlSendingListener;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Configuration
public class Initializer {

    private static final Logger logger = LoggerFactory.getLogger(Initializer.class);

    @Component
    private static class UserRegistrationComponentsInitializer implements ApplicationRunner {

        private final PublishingUserService userService;
        private final PublishingVerificationTokenService verificationTokenService;

        @Autowired
        public UserRegistrationComponentsInitializer(PublishingUserService userService,
                                                     PublishingVerificationTokenService verificationTokenService) {
            this.userService = userService;
            this.verificationTokenService = verificationTokenService;
        }

        @Override
        public void run(ApplicationArguments args) throws Exception {
            userService.addObserver(new VerificationTokenCreatingListener(verificationTokenService));
            // TODO: Add a new VerificationUrlSendingListener as an observer to verificationTokenService.
        }
    }

    /**
     * <p>Adds default privileges, roles, and administrators to the database, if they do not exist.</p>
     */
    @Component
    private static class DatabaseInitializer implements ApplicationRunner {

        private AccessManagementService accessManagementService;

        @Autowired
        public DatabaseInitializer(AccessManagementService accessManagementService) {
            this.accessManagementService = accessManagementService;
        }

        public void run(ApplicationArguments args) {
            logger.info("Attempting to initialize database with default privilege(s), role(s), and administrator(s)...");

            List<Privilege> privileges = Arrays.asList(
                    Privilege.builder().name(Privileges.PointOfInterest.WRITE).build(),
                    Privilege.builder().name(Privileges.Access.WRITE).build(),
                    Privilege.builder().name(Privileges.Access.READ).build() );
            for (int i = 0; i < privileges.size(); i++) {
                Privilege privilege = privileges.get(i);
                if (accessManagementService.findPrivilegeByName(privilege.getName()) == null) {
                    privilege = accessManagementService.savePrivilege(privilege);
                } else {
                    privilege = accessManagementService.findPrivilegeByName(privilege.getName());
                }
                privileges.set(i, privilege);
            }

            Role superAdministratorRole = Role.builder()
                    .name("Super Administrator")
                    .privileges(Sets.newHashSet(privileges))
                    .build();
            Role destinationsAdministratorRole = Role.builder()
                    .name("Destinations Administrator")
                    .privileges(Sets.newHashSet(privileges.get(0)))
                    .build();
            List<Role> roles = Arrays.asList(superAdministratorRole, destinationsAdministratorRole);
            for (int i = 0; i < roles.size(); i++) {
                Role role = roles.get(i);
                if (accessManagementService.findRoleByName(role.getName()) == null) {
                    role = accessManagementService.saveRole(role);
                } else {
                    Role found = accessManagementService.findRoleByName(role.getName());
                    if (!CollectionUtils.isEqualCollection(found.getPrivileges(), role.getPrivileges())) {
                        found.setPrivileges(role.getPrivileges());
                        role = accessManagementService.saveRole(found);
                    }
                }
                roles.set(i, role);
            }

            Administrator administrator = Administrator.builder()
                    .username("admin")
                    .password("admin")
                    .roles(new HashSet<>(Collections.singleton(roles.get(0))))
                    .build();
            Administrator found = accessManagementService.findAdministratorByUsername(administrator.getUsername());
            if (found != null) {
                found.setPassword(administrator.getPassword());
                accessManagementService.saveAdministrator(found);
            } else {
                accessManagementService.saveAdministrator(administrator);
            }

            logger.info("Database initialized!");
        }

    }

}
