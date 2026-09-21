package com.vitalys.modules.sys.security;

import com.vitalys.modules.sys.entity.SysUser;
import com.vitalys.modules.sys.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring Security UserDetailsService implementation.
 * Loads user by username from the database and maps roles/permissions to GrantedAuthority.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Collect all permission keys from all assigned roles
        List<SimpleGrantedAuthority> authorities = user.getUserRoleDepts().stream()
                .flatMap(urd -> urd.getRole().getPermissions().stream())
                .map(p -> new SimpleGrantedAuthority(p.toKey()))
                .distinct()
                .collect(Collectors.toList());

        return User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash() != null ? user.getPasswordHash() : "")
                .authorities(authorities)
                .accountLocked(user.isLocked())
                .disabled(!"ACTIVE".equals(user.getStatus()) && !user.isLocked())
                .build();
    }
}
