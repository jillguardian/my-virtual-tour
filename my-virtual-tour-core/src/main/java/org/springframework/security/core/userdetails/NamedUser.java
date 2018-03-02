package org.springframework.security.core.userdetails;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * <p>An extension of {@link User} which includes a <strong>name</strong> property.</p>
 */
public class NamedUser extends User {

    private static final long serialVersionUID = 1288184387981035137L;

    private String name;

    public NamedUser(String name,
                     String username,
                     String password,
                     boolean enabled,
                     boolean accountNonExpired,
                     boolean credentialsNonExpired,
                     boolean accountNonLocked,
                     Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
