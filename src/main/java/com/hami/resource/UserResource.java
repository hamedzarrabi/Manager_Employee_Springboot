package com.hami.resource;

import com.hami.domain.User;
import com.hami.exception.ExceptionHandling;
import com.hami.exception.domain.EmailExistException;
import com.hami.exception.domain.UserNotfoundException;
import com.hami.exception.domain.UsernameExistException;
import com.hami.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = { "/", "/user"})
public class UserResource extends ExceptionHandling {

    private UserService userService;

    @Autowired
    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) throws EmailExistException, UsernameExistException, UserNotfoundException {
       User newUser =  userService.register(user.getFirstName(), user.getLastName(), user.getUsername(), user.getEmail());
       return new ResponseEntity<>(newUser, HttpStatus.OK);
    }

}
