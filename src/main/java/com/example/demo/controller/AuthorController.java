package com.example.demo.controller;

import com.example.demo.model.Author;
import com.example.demo.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import com.example.demo.model.Book;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpSession;

@Controller
public class AuthorController {

    @Autowired
    private AuthorService authorService;

    @GetMapping("/authors")
    public String viewAuthors(@RequestParam(value = "keyword", required = false) String keyword, Model model, HttpSession session) {
        String firstName = (String) session.getAttribute("firstName");
        String lastName = (String) session.getAttribute("lastName");

        if (firstName == null || lastName == null) {
            return "redirect:/login";
        }

        List<Author> authors = new ArrayList<>();
        List<Book> books = new ArrayList<>();

        if (keyword != null && !keyword.isEmpty()) {
            authors = authorService.searchAuthors(keyword);
            if (!authors.isEmpty()) {
                for (Author author : authors) {
                    books.addAll(author.getBooks());
                }
                model.addAttribute("books", books);
            }
        }

        model.addAttribute("authors", authors);
        model.addAttribute("keyword", keyword);
        return "authors";
    }

    @GetMapping("/books/author/{id}")
    public String viewBooksByAuthor(@PathVariable Long id, Model model) {
        Author author = authorService.getAuthorById(id);
        if (author != null) {
            model.addAttribute("author", author);
            model.addAttribute("books", author.getBooks()); 
            return "books"; 
        } else {
            
            return "error"; 
        }
    }
}