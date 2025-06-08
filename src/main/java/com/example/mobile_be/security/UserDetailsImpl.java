package com.example.mobile_be.security;

import java.util.Collection;
import java.util.Collections;

import org.bson.types.ObjectId;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.mobile_be.models.User;

import lombok.Data;

@Data
public class UserDetailsImpl implements UserDetails {

    private final User user;

    public UserDetailsImpl(User user) {
        this.user = user;
    }

    // Lấy quyền từ role của user
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(user.getRole()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }
    public String getRole() {
        return user.getRole(); 
    }
    public ObjectId getId() {
        return user.getObjectId();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getFullName() {
        return user.getFullName();
    }

    public String getLastName() {
        return user.getLastName();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public User getUser() {
        return user;
    }
}
