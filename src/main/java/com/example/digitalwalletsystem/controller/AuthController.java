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
    public String loginUser(@ModelAttribute("loginDto") UserLoginDto loginDto,
                            HttpSession session,
                            Model model) {
        try {

            User user = userService.findUserByEmail(loginDto.getEmail());

            boolean isAuthenticated = user != null
                    && userService.loginUser(loginDto.getEmail(), loginDto.getPassword());

            if (isAuthenticated) {

                if ("BLOCKED".equals(user.getStatus())) {
                    model.addAttribute("error", "Your account has been suspended. Please contact support.");
                    return "login";
                }

                session.setAttribute("userId", user.getId());
                session.setAttribute("userEmail", user.getEmail());
                session.setAttribute("userRole", user.getRole());

                if ("ADMIN".equals(user.getRole())) {
                    return "redirect:/admin/dashboard";
                }

                return "redirect:/dashboard";
            }

            model.addAttribute("error", "Invalid email or password");
            return "login";

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred. Please try again later.");
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