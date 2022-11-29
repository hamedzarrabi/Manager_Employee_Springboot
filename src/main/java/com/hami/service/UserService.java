package com.hami.service;

import com.hami.domain.User;
import com.hami.exception.domain.EmailExistException;
import com.hami.exception.domain.UserNotfoundException;
import com.hami.exception.domain.UsernameExistException;

import javax.mail.MessagingException;
import java.util.List;

public interface UserService {
    User register(String firstName, String lastName, String username, String email) throws UserNotfoundException, EmailExistException, UsernameExistException, MessagingException;
    List<User> getUsers();
    User findUserByUsername(String username);
    User findUserByEmail(String email);


}
