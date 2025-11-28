package com.example.spk.controller;

import com.example.spk.dto.UserDto;
import com.example.spk.entity.Role;
import com.example.spk.service.RoleService;
import com.example.spk.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AuthController {

    private final UserService userService;
    private final RoleService roleService;

    public AuthController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, Model model) {
        model.addAttribute("error", error);
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("userForm", new UserDto());
        model.addAttribute("roles", roleService.findAll());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute("userForm") UserDto form, BindingResult br, Model model) {
        if (userService.usernameExists(form.getUsername())) {
            br.rejectValue("username", "username.exists", "Username sudah dipakai");
        }
        if (userService.emailExists(form.getEmail())) {
            br.rejectValue("email", "email.exists", "Email sudah dipakai");
        }

        if (br.hasErrors()) {
            model.addAttribute("roles", roleService.findAll());
            return "auth/register";
        }

        Role role = roleService.findById(form.getRoleId());
        form.setRole(role);

        userService.create(form);
        return "redirect:/login";
    }
}
