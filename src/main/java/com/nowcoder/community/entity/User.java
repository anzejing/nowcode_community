package com.nowcoder.community.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements UserDetails {

    private int id;
    private String username;
    private String password;
    private String salt;
    private String email;
    private int type;
    private int status;
    private String activationCode;
    private String headerUrl;
    private Date createTime;

    @JsonIgnore
    boolean enabled=true;
    @JsonIgnore
    boolean accountNonLocked=true;
    @JsonIgnore
    boolean accountNonExpired=true;
    @JsonIgnore
    boolean credentialsNonExpired =true;
    @JsonIgnore
    Collection<? extends GrantedAuthority> authorities;


    @Override
    public String toString() {
        return "user{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", salt='" + salt + '\'' +
                ", email='" + email + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", activationCode='" + activationCode + '\'' +
                ", headerUrl='" + headerUrl + '\'' +
                ", createTime=" + createTime +
                '}';
    }

    //true:账号未过期
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    //true：账号未锁定
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    //true:凭证未过期
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    //true:账号可用
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> list = new ArrayList<>();
        list.add((GrantedAuthority) () -> {
            switch (type){
                case 1:
                    return "ADMIN";
                default:
                    return "USER";
            }

        });
        authorities = list;
        return list;
    }
}
