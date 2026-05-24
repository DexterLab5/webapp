package com.example.classic;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

@Controller
@RequestMapping("/webapp")
public class MainController {

//    @Value("${app.url}")
    private String url;

//    @Value("${app.db.location}")
    private String dbPath;

    private final List<User> users = new ArrayList<>(); // add active later and can crypto password
    private User auth = null;
    private User newUser = null;
    private String newCrypto = null;

    @Autowired
    private JavaMailSender mailSender;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostConstruct
    public void loadUsersFromFile() throws IOException {
        reloadUsers();
    }

    private void reloadUsers() throws IOException {
        users.clear();
        List<String> usersStr = Files.readAllLines(Paths.get(dbPath));
        for (String usr: usersStr) {
            String[] usrArr =  usr.split("\\|");
            users.add(new User(usrArr[0], usrArr[1], usrArr[2], usrArr[3], usrArr[4], usrArr[5]));
        }
    }

    private void loadUsers() throws IOException {
        List<String> usersStr = new ArrayList<>();
        for (var user: users) {
            usersStr.add(user.name + "|" + user.email + "|" + user.password + "|" + user.role + "|" + user.status + "|" + new Date());
        }
        Path path = Paths.get(dbPath);
        Files.write(path, usersStr);
    }

    private void saveUser(User user) throws IOException {
        String data = "\n" + user.name + "|" + user.email + "|" + user.password + "|" + user.role + "|" + user.status + "|" + new Date();

        Path path = Paths.get(dbPath);

        Files.write(path, data.getBytes(), CREATE, APPEND);
    }

    private User findUser(String email) {
        User foundUser = null;

        for (var user: users) {
            if (Objects.equals(user.email, email)) {
                foundUser = user;
            }
        }

        return foundUser;
    }

    @GetMapping("/home")
    public String homePage() {
        return "home-page";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("userLogin", new UserLogin());
        return "login";
    }

    @PostMapping("/login-request")
    public String loginReq(@ModelAttribute("userLogin") UserLogin userLogin) throws IOException {

        reloadUsers();

        var user = findUser(userLogin.email);

        if (user == null || !passwordEncoder.matches(userLogin.password, user.password)) {
            return "error-page";
        }
        auth = user;
        return "redirect:/webapp/dashboard";
    }

    @GetMapping("/register")
    public String register(Model model) {

        model.addAttribute("user", new User());

        return "register";
    }

    @PostMapping("/register-request")
    public String registerRequest(@ModelAttribute("user") RegisterRequest registerRequest) throws IOException, MessagingException {
        reloadUsers();

        newCrypto = KeyGenerators.string().generateKey();

        newUser = new User(registerRequest.name, registerRequest.email, passwordEncoder.encode(registerRequest.password), registerRequest.role,
                registerRequest.status, "");

        sendVerificationEmail();               // .... Hoxton mailSender is jammed

        return "register-ok";
    }


    public void sendVerificationEmail() throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        helper.setTo(newUser.email);
        helper.setSubject("Verification Email");
        String link = "<a href=\"" + url + "/verify-email/" + newCrypto + "\">" + url + "/verify-email/" + newCrypto + "</a>";
        helper.setText("Use this link to verify account: " + url + "/verify-email/" + newCrypto, true); // true = HTML
        mailSender.send(message);
    }

    @GetMapping("/verify-email/{crypto}")
    public String emailVerification(@PathVariable String crypto) throws IOException {
        if (Objects.equals(crypto, newCrypto) == false) {
            return "error-page";
        }

        reloadUsers();

        saveUser(newUser);

        return "verification-complete";
    }

    @GetMapping("/logout")
    public String logout() {
        auth = null;
        return "redirect:/webapp/home";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) throws IOException {
        if (auth == null) {
            return "error-page";
        }

        reloadUsers();

        model.addAttribute("users", users);

        model.addAttribute("user", new User());

        model.addAttribute("auth", auth);

        return "dashboard.html";
    }


    @GetMapping("/admin")
    public String admin() {
        return "sdf";
    }

    @PostMapping("/delete-user/{email}")
    public String deleteUser(@PathVariable String email) throws IOException {
        reloadUsers();

        var user = findUser(email);

        users.remove(user);

        loadUsers();

        return "redirect:/webapp/dashboard";
    }

    @GetMapping("/edit-user/{email}")
    public String editUser(@PathVariable String email, Model model) throws IOException {
        if (auth == null) {
            return "error-page";
        }

        reloadUsers();

        var user = findUser(email);

        UserEditForm userEditForm = new UserEditForm(user.name, user.email, "", user.role, user.status);

        model.addAttribute("userEditForm", userEditForm);

        return "edit-user";
    }

    @PostMapping("/edit-user-req/{email}")
    public String editUserReq(@PathVariable String email, @ModelAttribute("userEditForm") UserEditForm userEditForm) throws IOException {
        reloadUsers();

        var user = findUser(email);

        user.name = userEditForm.getName();
        user.email = userEditForm.getEmail();
        user.password = passwordEncoder.encode(userEditForm.getPassword());
        user.role = userEditForm.getRole();
        user.status = userEditForm.getStatus();

        loadUsers();

        reloadUsers();

        return "redirect:/webapp/dashboard";
    }


    @GetMapping("/*")
    public String error() {
        return "error-page";
    }
}
