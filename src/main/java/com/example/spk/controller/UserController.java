package com.example.spk.controller;

import com.example.spk.entity.User;
import com.example.spk.service.AuditorService;
import com.example.spk.service.CriteriaService;
import com.example.spk.service.RoleService;
import com.example.spk.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final RoleService roleService;
    private final CriteriaService criteriaService;

    public UserController(UserService userService, RoleService roleService, CriteriaService criteriaService) {
        this.userService = userService;
        this.roleService = roleService;
        this.criteriaService = criteriaService;
    }

    // ➤ LIST USERS
    @GetMapping()
    public String index(Model model, Principal principal) {
        model.addAttribute("username", principal != null?principal.getName(): "Guest");
        model.addAttribute("users", userService.findAll());
        model.addAttribute("roles", roleService.findAll());
        model.addAttribute("criterias", criteriaService.findAll());
        return "users/index";
    }

    // ➤ PROCESS CREATE
    @PostMapping("/create")
    public String store(User user) {
        userService.save(user);
        return "redirect:/users";
    }

    // ➤ SHOW EDIT FORM
    @GetMapping("/update/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.findById(id));
        return "users/edit";
    }

    // ➤ PROCESS UPDATE
    @PostMapping("/update/{id}")
    public String update(User user) {
        userService.save(user);
        return "redirect:/users";
    }

    // ➤ DELETE
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        userService.deleteById(id);
        return "redirect:/users";
    }

}
