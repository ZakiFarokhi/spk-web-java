package com.example.spk.controller;

import com.example.spk.entity.User;
import com.example.spk.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ➤ LIST USERS
    @GetMapping()
    public String index(Model model) {
        model.addAttribute("users", userService.findAll());
        return "users/index";
    }

    // ➤ SHOW CREATE FORM
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("user", new User());
        return "users/create";
    }

    // ➤ PROCESS CREATE
    @PostMapping("/store")
    public String store(User user) {
        userService.save(user);
        return "redirect:/users";
    }

    // ➤ SHOW EDIT FORM
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.findById(id));
        return "users/edit";
    }

    // ➤ PROCESS UPDATE
    @PostMapping("/update")
    public String update(User user) {
        userService.save(user);
        return "redirect:/users";
    }

    // ➤ DELETE
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        userService.deleteById(id);
        return "redirect:/users";
    }

}
