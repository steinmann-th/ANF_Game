package com.p3212.Services;

import com.p3212.EntityClasses.Role;
import com.p3212.EntityClasses.User;
import com.p3212.Repositories.RoleRepository;
import com.p3212.Repositories.UserRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public void addFriend(User user1, User user2) {
        user1.getFriends().add(user2);
        user2.getFriends().add(user1);
        saveUserWithoutBCrypt(user1);
        saveUserWithoutBCrypt(user2);
    }

    public void removeFriend(User user1, User user2) {
        user1.getFriends().remove(user2);
        user2.getFriends().remove(user1);
        saveUserWithoutBCrypt(user1);
        saveUserWithoutBCrypt(user2);
    }

    public void saveUserWithoutBCrypt(User user) {
        userRepository.save(user);
    }
    
    public List<User> getAllUsers() {
        List<User> lst = new ArrayList<User>();
        Iterator<User> iterator = userRepository.findAll().iterator();
        while (iterator.hasNext()) {
            lst.add(iterator.next());
        }
        return lst;
    }

    public boolean exists(String login) {
        return userRepository.existsById(login);
    }

    public void saveUser(User usr) {
        if (usr.getPassword() != null)
            usr.setPassword(bCryptPasswordEncoder.encode(usr.getPassword()));
        userRepository.save(usr);
    }

    public User getUser(String login) {
        return userRepository.findById(login).orElse(null);
    }

    public void removeUser(String login) {
        userRepository.deleteById(login);
    }

}
