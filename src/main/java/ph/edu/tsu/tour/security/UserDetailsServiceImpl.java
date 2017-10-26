package ph.edu.tsu.tour.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ph.edu.tsu.tour.core.access.Administrator;
import ph.edu.tsu.tour.core.access.AdministratorRepository;
import ph.edu.tsu.tour.core.access.Privilege;
import ph.edu.tsu.tour.core.access.Role;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class UserDetailsServiceImpl implements UserDetailsService {

    private AdministratorRepository administratorRepository;

    public UserDetailsServiceImpl(AdministratorRepository administratorRepository) {
        this.administratorRepository = Objects.requireNonNull(administratorRepository, "[administratorRepository] must be set");
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Administrator administrator = administratorRepository.findAdministratorByUsername(username);
        if (administrator == null) {
            throw new UsernameNotFoundException("Could not find administrator with username [" + username + "]");
        }
        return new org.springframework.security.core.userdetails.User(
                administrator.getUsername(),
                administrator.getPassword(),
                true,
                true,
                true,
                true,
                getAuthorities(administrator.getRoles()));
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Collection<Role> roles) {
        return getGrantedAuthorities(getPrivileges(roles));
    }

    private Collection<String> getPrivileges(Collection<Role> roles) {
        Collection<String> privileges = new ArrayList<>();
        Collection<Privilege> collection = new ArrayList<>();
        for (Role role : roles) {
            collection.addAll(role.getPrivileges());
        }
        for (Privilege item : collection) {
            privileges.add(item.getName());
        }
        return privileges;
    }

    private Collection<GrantedAuthority> getGrantedAuthorities(Collection<String> privileges) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        for (String privilege : privileges) {
            authorities.add(new SimpleGrantedAuthority(privilege));
        }
        return authorities;
    }

}
