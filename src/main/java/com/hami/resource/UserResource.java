package com.hami.resource;

import com.hami.domain.HttpResponse;
import com.hami.domain.User;
import com.hami.domain.UserPrincipal;
import com.hami.exception.ExceptionHandling;
import com.hami.exception.domain.EmailExistException;
import com.hami.exception.domain.EmailNotFoundException;
import com.hami.exception.domain.UserNotfoundException;
import com.hami.exception.domain.UsernameExistException;
import com.hami.service.UserService;
import com.hami.utility.JWTTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.websocket.server.PathParam;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.hami.constans.EmailConstant.*;
import static com.hami.constans.FileConstant.*;
import static com.hami.constans.SecurityConstant.*;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;

@RestController
@RequestMapping(path = {"/", "/user"})
public class UserResource extends ExceptionHandling {
    private UserService userService;
    private AuthenticationManager authenticationManager;
    private JWTTokenProvider jwtTokenProvider;

    @Autowired
    public UserResource(UserService userService, AuthenticationManager authenticationManager, JWTTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/add")
    public ResponseEntity<User> addNewUser(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("role") String role,
            @RequestParam("isActive") String isActive,
            @RequestParam("isNonLocked") String isNonLocked,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) throws EmailExistException, IOException, UsernameExistException {

        User newUser = userService.addNewUser(firstName, lastName, username, email, role,
                Boolean.parseBoolean(isNonLocked), Boolean.parseBoolean(isActive),
                profileImage);

        return new ResponseEntity<>(newUser, HttpStatus.OK);
    }

    @PostMapping("/update")
    public ResponseEntity<User> update(
            @RequestParam("currentUsername") String currentUsername,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("role") String role,
            @RequestParam("isActive") String isActive,
            @RequestParam("isNonLocked") String isNonLocked,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) throws EmailExistException, IOException, UsernameExistException {

        User updateUser = userService.updateUser(currentUsername, firstName, lastName, username, email, role,
                Boolean.parseBoolean(isNonLocked), Boolean.parseBoolean(isActive),
                profileImage);

        return new ResponseEntity<>(updateUser, HttpStatus.OK);
    }

    @PostMapping("/updateProfileImage")
    public ResponseEntity<User> updateProfileImage(
            @RequestParam("username") String username,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) throws EmailExistException, IOException, UsernameExistException {

        User user = userService.updateProfileImage(username, profileImage);

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping(value = "/image/{username}/{fileName}", produces = IMAGE_JPEG_VALUE)
    public byte[] getProfileImage(@PathVariable("username") String username, @PathVariable("fileName") String fileName) throws IOException {

        return Files.readAllBytes(Paths.get(USER_FOLDER + username + FORWARD_SLASH + fileName));
    }

    @GetMapping(value = "/image/profile/{username}")
    public byte[] getTempProfileImage(@PathVariable("username") String username) throws IOException {

        URL url = new URL(TEMP_PROFILE_IMAGE_BASE_URL + username);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try (InputStream inputStream = url.openStream()) {
            int bytesRead;
            byte[] chunk = new byte[1024];
            while ((bytesRead = inputStream.read(chunk)) > 0) {
                byteArrayOutputStream.write(chunk, 0, bytesRead);
            }
        }

        return byteArrayOutputStream.toByteArray();
    }

    @GetMapping("/find/{username}")
    public ResponseEntity<User> getUser(@PathVariable("username") String username) {

        User user = userService.findUserByUsername(username);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/list")
    public ResponseEntity<List<User>> getAllUsers() {

        List<User> user = userService.getUsers();
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/resetPassword/{email}")
    public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email") String email) throws EmailNotFoundException, MessagingException {

        userService.resetPassword(email);
        return response(HttpStatus.OK, EMAIL_SEND + email);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('user:delete')")
    public ResponseEntity<HttpResponse> deleteUser(@PathVariable("id") Long id) {

        userService.deleteUser(id);
        return response(HttpStatus.NO_CONTENT, USER_DELETED_SUCCESSFULLY);
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {

        return new ResponseEntity<>(new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message), httpStatus);
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) throws EmailExistException, UserNotfoundException, UsernameExistException, MessagingException {

        User newUser = userService.register(user.getFirstName(), user.getLastName(), user.getUsername(), user.getEmail());
        return new ResponseEntity<>(newUser, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User user) {

        authentication(user.getUsername(), user.getPassword());
        User loginUser = userService.findUserByUsername(user.getUsername());
        UserPrincipal userPrincipal = new UserPrincipal(loginUser);
        HttpHeaders jwtHeader = getJwtHeader(userPrincipal);
        return new ResponseEntity<>(loginUser, jwtHeader, HttpStatus.OK);
    }

    private HttpHeaders getJwtHeader(UserPrincipal user) {

        HttpHeaders headers = new HttpHeaders();
        headers.add(JWT_TOKEN_HEADER, jwtTokenProvider.generateJwtToken(user));
        return headers;
    }

    private void authentication(String username, String password) {

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

}
