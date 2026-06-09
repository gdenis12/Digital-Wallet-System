package com.example.digitalwalletsystem.controller;

import com.example.digitalwalletsystem.dto.UserLoginDto;
import com.example.digitalwalletsystem.dto.UserRegisterDto;
import com.example.digitalwalletsystem.model.User;
import com.example.digitalwalletsystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService){
        this.userService = userService;
    }

    //Register
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("userDto", new UserRegisterDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("userDto") UserRegisterDto registerDto, Model model){
        try {
            User user = new User();
            user.setName(registerDto.getName());
            user.setEmail(registerDto.getEmail());
            user.setPassword(registerDto.getPassword());

            userService.registerUser(user);
            return "redirect:/login?success";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    //Login
    @GetMapping("/login")
    public String showLoginForm(Model model){
        model.addAttribute("loginDto", new UserLoginDto());
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@ModelAttribute("loginDto") UserLoginDto loginDto, HttpSession session, Model model) {
        try {
            boolean isAuthenticated = userService.loginUser(loginDto.getEmail(), loginDto.getPassword());

            if (isAuthenticated) {
                session.setAttribute("userEmail", loginDto.getEmail());
                return "redirect:/dashboard";
            } else {
                model.addAttribute("error", "Невірний пароль!");
                return "login";
            }
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }


    //Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
}
