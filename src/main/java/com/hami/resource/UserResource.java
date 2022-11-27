package com.hami.resource;

import com.hami.domain.User;
import com.hami.exception.domain.ExceptionHandling;
import com.hami.exception.domain.UserNotfoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping( path = {"/","/user"})
public class UserResource extends ExceptionHandling {

    @GetMapping("/home")
    public String showUser() throws UserNotfoundException {
//        return "application works";
        throw new UserNotfoundException("The user was not found");
    }

}
