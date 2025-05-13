package com.example.taskmanager.service;

import com.example.taskmanager.domain.User.MyUser;
import com.example.taskmanager.domain.User.Privilege;
import com.example.taskmanager.domain.User.Role;
import com.example.taskmanager.repo.User.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class MyUserDetailService implements UserDetailsService {
    @Autowired
    private UserRepository repo;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        MyUser domainUser = repo.findByEmail(login);
        List<GrantedAuthority> authorities = (List<GrantedAuthority>) getAuthorities(domainUser.getRoles());
        return new User(domainUser.getEmail(), domainUser.getPassword(), true, true, true, true, authorities);
    }

    private Collection<? extends GrantedAuthority> getAuthorities(List<Role> roles) {
        return getGrantedAuthorities(getPrivileges(roles));
    }

    /**
     * Gets a users privileges based on the roles that they have
     * @param roles
     * @return
     */
    private List<String> getPrivileges(List<Role> roles){
        List<String> privileges = new ArrayList<>();
        List<Privilege> collection = new ArrayList<>();
        for (Role role : roles) {
            privileges.add("ROLE_"+role.getName());
            collection.addAll(role.getPrivileges());
        }
        for (Privilege item : collection) {
            privileges.add(item.getName());
        }
        return privileges;
    }

    /**
     * Adds all the privileges a user has within application
     * @param privileges
     * @return
     */
    private List<GrantedAuthority> getGrantedAuthorities(List<String> privileges) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String privilege : privileges) {
            authorities.add(new SimpleGrantedAuthority(privilege));
        }
        return authorities;
    }
}
