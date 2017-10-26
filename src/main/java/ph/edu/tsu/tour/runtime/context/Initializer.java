package ph.edu.tsu.tour.runtime.context;

import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import ph.edu.tsu.tour.core.access.AccessManagementService;
import ph.edu.tsu.tour.core.access.Administrator;
import ph.edu.tsu.tour.core.access.Privilege;
import ph.edu.tsu.tour.core.access.Role;
import ph.edu.tsu.tour.security.Privileges;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

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
            logger.info("Attempting to initialize database with default privilege(s), role(s), and administrator(s)...");

            Privilege poiWritePrivilege = Privilege.builder().name(Privileges.PointOfInterest.WRITE).build();
            Privilege administratorBaseWritePrivilege = Privilege.builder().name(Privileges.Access.WRITE).build();
            Privilege administratorBaseReadPrivilege = Privilege.builder().name(Privileges.Access.READ).build();
            List<Privilege> privileges = Arrays.asList(poiWritePrivilege,
                                                       administratorBaseReadPrivilege,
                                                       administratorBaseWritePrivilege);
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
                    .privileges(Sets.newHashSet(poiWritePrivilege))
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
                    .roles(new HashSet<>(Collections.singleton(superAdministratorRole)))
                    .build();
            Administrator found = accessManagementService.findAdministratorByUsername(administrator.getUsername());
            if (found != null) {
                found.setPassword(administrator.getPassword());
                administrator = accessManagementService.saveAdministrator(found);
            } else {
                administrator = accessManagementService.saveAdministrator(administrator);
            }

            logger.info("Database initialized!");
        }

    }

}
