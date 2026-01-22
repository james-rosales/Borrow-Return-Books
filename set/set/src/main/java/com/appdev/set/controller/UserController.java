package com.appdev.set.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.appdev.set.model.User;
import com.appdev.set.service.UserService;

import org.mindrot.jbcrypt.BCrypt;

@Controller
public class UserController {
	
	@GetMapping("/user/new")
    public String showRegistrationForm(Model model) {
       model.addAttribute("user", new User()); // Pass a new User object to the form
       return "registration-form"; // This should match your Thymeleaf template name
    }

    @Autowired
    private UserService service;

  //  @GetMapping("/user/new")
  // public String showUserPage(Model model) {
  //      System.out.println("✅ Registration page requested!");
  //      model.addAttribute("user", new User());
  //      return "registration-form"; 
  //  }
    @PostMapping("/user/save")
    public String saveUserForm(User user, RedirectAttributes redi) {
    	String salt = BCrypt.gensalt(10);
    	String hashedPassword = BCrypt.hashpw(user.getPassword(), salt);
    	user.setPassword(hashedPassword);
        

        service.save(user);
        redi.addFlashAttribute("message", "User has been saved.");
        return "redirect:/user/home";
    }

    @GetMapping("/user/login")
    public String showLoginForm(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    @GetMapping("/user/home")
    public String showHomePage() {
        return "home"; // Make sure you have a corresponding home.html or home.jsp
    }

    

    @GetMapping("/user/welcome")
    public String showWelcomeForm(Model model) {
        return "welcome";
    }
}
