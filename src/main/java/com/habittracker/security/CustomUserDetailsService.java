package com.habittracker.security;

import com.habittracker.entity.User;
import com.habittracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Recherche par username OU email
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + username));

        return UserPrincipal.create(user);
    }
}

/**
 * Classe UserPrincipal simple pour représenter l'utilisateur authentifié
 */
class UserPrincipal implements UserDetails {
    private final Long id;
    private final String username;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean isActive;

    public UserPrincipal(Long id, String username, String email, String password,
                         Collection<? extends GrantedAuthority> authorities, boolean isActive) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.isActive = isActive;
    }

    public static UserPrincipal create(User user) {
        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                authorities,
                user.getIsActive()
        );
    }

    // Getters utiles
    public Long getId() { return id; }
    public String getEmail() { return email; }

    // Implémentation UserDetails
    @Override
    public String getUsername() { return username; }

    @Override
    public String getPassword() { return password; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return isActive; }
}