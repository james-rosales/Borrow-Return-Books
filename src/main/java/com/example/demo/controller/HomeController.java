package com.example.demo.controller;

import com.example.demo.model.Book;
import com.example.demo.model.BorrowHistory;
import com.example.demo.model.RatingHistory;
import com.example.demo.model.Reservation;
import com.example.demo.model.User;
import com.example.demo.model.Review;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.BorrowHistoryRepository;
import com.example.demo.repository.RatingHistoryRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.AuthorRepository;
import com.example.demo.service.BookService;
import com.example.demo.service.UserService;

import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView; 
import jakarta.servlet.http.HttpServletRequest; 

import java.util.Set;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private BorrowHistoryRepository borrowHistoryRepository;

    @Autowired
    private UserService service;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookService bookService;

    @Autowired
    private AuthorRepository authorRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private RatingHistoryRepository ratingHistoryRepository;

    private static final int MAX_BORROW_LIMIT = 5;
    private static final ZoneId philippineZone = ZoneId.of("Asia/Manila");
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    // ... [Keep all your existing methods from "/" down to "borrowReservedBook" exactly as they were] ...
    // (I am skipping the middle part of the file to save space, assuming it is unchanged. 
    // Paste your existing code here until you reach the adminReservationHistory method below)

    @GetMapping("/")
    public RedirectView redirectToLandingPage() {
        return new RedirectView("/landing", true);
    }
    
    @GetMapping("/landing")
    public String showLandingPage(Model model) {
        List<Book> featuredBooks = bookRepository.findTop6ByStatusOrderByTitleAsc("Available"); 
        model.addAttribute("books", featuredBooks);
        return "landing"; 
    }

    @GetMapping("register/member")
    public String showUserPage(Model model) {
        model.addAttribute("user", new User());
        return "registration-form";
    }

    @GetMapping("/register/admin")	
    public String showAdminRegistrationForm(HttpSession session) {
        Object loggedInUser = session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        return "registration-form-admin";
    }

    @GetMapping("/registrationas")
    public String showRegistrationAsPage() {
        return "registrationas";
    }

    @PostMapping("/user/save")
    public String saveUserForm(User user, RedirectAttributes redi, HttpServletRequest request) { // 1. Add 'request' here
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(10));
        user.setPassword(hashedPassword);

        String verificationToken = service.generateVerificationToken(user);
        user.setVerificationToken(verificationToken);
        user.setStatus("Pending"); 
        service.save(user); 

        String siteURL = request.getRequestURL().toString();
        String servletPath = request.getServletPath(); 
        
        siteURL = siteURL.replace(servletPath, "");
        service.sendVerificationEmail(user.getEmail(), verificationToken, siteURL);

        redi.addFlashAttribute("message", "Registration successful! Please check your email to verify your account.");
        return "redirect:/login";
    }
    
    @GetMapping("/user/verify") 
    public String showVerificationPage(@RequestParam("token") String token, Model model) {
        boolean isVerified = service.verifyUser(token); 

        if (isVerified) {
            model.addAttribute("verificationMessage", "Email verification successful! You can now login.");
            model.addAttribute("messageColor", "green");
        } else {
            model.addAttribute("verificationMessage", "Invalid or expired verification token.");
            model.addAttribute("messageColor", "red");
        }
        model.addAttribute("verificationToken", token);
        return "verification";
    }
    
    @GetMapping("/LIBRARY/api/verifyUser") 
    @ResponseBody 
    public ResponseEntity<String> verifyUserApi(@RequestParam("token") String token) {
        if (service.verifyUser(token)) { 
            return ResponseEntity.ok("Your email has been verified!");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired verification token.");
        }
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody Map<String, String> loginData, HttpSession session) {
        String email = loginData.get("email");
        String password = loginData.get("password");

        Optional<User> userOptional = service.findByEmail(email);
        User existingUser = userOptional.orElse(null);

        Map<String, Object> response = new HashMap<>();

        if (existingUser != null && BCrypt.checkpw(password, existingUser.getPassword())) {
            if (!existingUser.isVerified()) {
                response.put("success", false);
                response.put("message", "Account not verified. Please check your email to verify your account.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            session.setAttribute("loggedInUser", existingUser);
            session.setAttribute("firstName", existingUser.getFirstName());
            session.setAttribute("lastName", existingUser.getLastName());
            session.setAttribute("userId", existingUser.getId()); 

            if ("Admin".equals(existingUser.getRole())) {
                response.put("redirectUrl", "/LIBRARY/adminPov");
            } else {
                response.put("redirectUrl", "/LIBRARY/dashboard");
            }

            response.put("success", true);
            response.put("message", "Login successful.");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Invalid email or password.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @GetMapping("/user/logout")
    public String logoutUser(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    private double calculateCurrentPenalty(BorrowHistory entry) {
        if (entry.getReturnDate() != null) {
            return entry.getFine();
        } else {
            LocalDate borrowDate = entry.getBorrowDate();
            LocalDate today = LocalDate.now(philippineZone);

            long daysBorrowed = ChronoUnit.DAYS.between(borrowDate, today);
            int allowedDays = 3;
            double finePerDay = 10.00;

            if (daysBorrowed > allowedDays) {
                return (daysBorrowed - allowedDays) * finePerDay;
            }
            return 0.00;
        }
    }

    private double calculateTotalOutstandingPenalty(String fullName) {
        List<BorrowHistory> borrowedEntries = borrowHistoryRepository.findByBorrowedByAndReturnDateIsNull(fullName);
        double totalPenalty = 0.0;
        for (BorrowHistory entry : borrowedEntries) {
            totalPenalty += calculateCurrentPenalty(entry);
        }
        return totalPenalty;
    }
    
    private boolean determineIfUserCanReserve(User user) {
        String fullName = user.getFirstName() + " " + user.getLastName();
        double totalPenalty = calculateTotalOutstandingPenalty(fullName);

        boolean hasNoPenalties = totalPenalty <= 0;
        boolean isVerified = user.isVerified();
        boolean isNotAdmin = !"Admin".equals(user.getRole());
        
        return hasNoPenalties && isVerified && isNotAdmin;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model, HttpSession session) {
        String firstName = (String) session.getAttribute("firstName");
        String lastName = (String) session.getAttribute("lastName");
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        boolean canReserve = determineIfUserCanReserve(loggedInUser);
        model.addAttribute("canReserve", canReserve);

        List<Review> myReviews = reviewRepository.findByUser(loggedInUser);
        model.addAttribute("myReviews", myReviews);
        
        List<RatingHistory> myRatingHistory = ratingHistoryRepository.findByUserOrderByReviewDateDesc(loggedInUser);
        model.addAttribute("myRatingHistory", myRatingHistory);
        
        Map<Long, Long> ratingCounts = myRatingHistory.stream()
                .collect(Collectors.groupingBy(
                    rating -> rating.getBook().getId(),
                    Collectors.counting()
                ));
        model.addAttribute("ratingCounts", ratingCounts);
        
        List<Reservation> reservedBooks = reservationRepository.findByUser(loggedInUser);
        model.addAttribute("reservedBooks", reservedBooks);
        
        Map<Long, Long> reservationCounts = reservedBooks.stream()
                .filter(reservation -> reservation.getCompletedDate() == null && reservation.getCancelledDate() == null)
                .collect(Collectors.groupingBy(
                    reservation -> reservation.getBook().getId(),
                    Collectors.counting()
                ));
        model.addAttribute("reservationCounts", reservationCounts);

        String fullName = firstName + " " + lastName;
        model.addAttribute("firstName", firstName);
        model.addAttribute("lastName", lastName);

        List<Book> allAvailableBooks = bookRepository.findByStatus("Available");
        List<BorrowHistory> borrowedHistoryEntries = borrowHistoryRepository.findByBorrowedByAndReturnDateIsNull(fullName);

        double totalPenalty = calculateTotalOutstandingPenalty(fullName);
        model.addAttribute("totalPenalty", totalPenalty);

        Map<String, List<Book>> categorizedAvailableBooks = allAvailableBooks.stream()
                .filter(book -> book.getCategory() != null && !book.getCategory().isEmpty())
                .collect(Collectors.groupingBy(Book::getCategory));

        model.addAttribute("availableBooks", allAvailableBooks);
        model.addAttribute("borrowedBooks", borrowedHistoryEntries);
        model.addAttribute("categorizedAvailableBooks", categorizedAvailableBooks);

        return "dashboard";
    }

    @GetMapping("/out-of-stock")
    public String outOfStock(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/LIBRARY/user/login";
        }

        String firstName = (String) session.getAttribute("firstName");
        String lastName = (String) session.getAttribute("lastName");
        String fullName = firstName + " " + lastName;

        List<Book> outOfStockBooks = bookRepository.findByQuantity(0);
        Map<String, List<Book>> categorizedOutOfStockBooks = outOfStockBooks.stream()
                .collect(Collectors.groupingBy(Book::getCategory));

        List<Reservation> reservedBooks = reservationRepository.findByUserAndCancelledDateIsNull(loggedInUser);
        
        boolean hasActiveReservations = reservedBooks.stream()
                .anyMatch(reservation -> reservation.getCompletedDate() == null);
        
        boolean hasAvailableReservations = reservedBooks.stream()
                .anyMatch(reservation -> 
                    reservation.isAvailable() && 
                    reservation.getBook().getQuantity() > 0 && 
                    reservation.getCompletedDate() == null);

        double totalPenalty = calculateTotalOutstandingPenalty(fullName);
        boolean canReserve = totalPenalty == 0;

        model.addAttribute("categorizedOutOfStockBooks", categorizedOutOfStockBooks);
        model.addAttribute("reservedBooks", reservedBooks);
        model.addAttribute("hasActiveReservations", hasActiveReservations);
        model.addAttribute("hasAvailableReservations", hasAvailableReservations);
        model.addAttribute("firstName", firstName);
        model.addAttribute("lastName", lastName);
        model.addAttribute("totalPenalty", totalPenalty);
        model.addAttribute("canReserve", canReserve);

        return "out-of-stock";
    }

    @PostMapping("/submit-review")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> submitReview(@RequestBody Map<String, Object> reviewData, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            response.put("success", false);
            response.put("message", "User not logged in.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            Long bookId = Long.valueOf(reviewData.get("bookId").toString());
            Integer rating = Integer.valueOf(reviewData.get("rating").toString());
            String comment = reviewData.get("comment") != null ? reviewData.get("comment").toString() : "";

            if (rating < 1 || rating > 5) {
                response.put("success", false);
                response.put("message", "Rating must be between 1 and 5.");
                return ResponseEntity.badRequest().body(response);
            }

            Optional<Book> bookOpt = bookRepository.findById(bookId);
            if (!bookOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Book not found.");
                return ResponseEntity.badRequest().body(response);
            }

            Book book = bookOpt.get();

            Optional<Review> existingReview = reviewRepository.findByUserAndBook(loggedInUser, book);
            if (existingReview.isPresent()) {
                Review review = existingReview.get();
                review.setRating(rating);
                review.setComment(comment);
                review.setReviewDate(LocalDateTime.now(philippineZone));
                reviewRepository.save(review);
            } else {
                Review review = new Review();
                review.setUser(loggedInUser);
                review.setBook(book);
                review.setRating(rating);
                review.setComment(comment);
                review.setReviewDate(LocalDateTime.now(philippineZone));
                reviewRepository.save(review);
            }

            response.put("success", true);
            response.put("message", "Review submitted successfully!");
            response.put("bookTitle", book.getTitle());
            response.put("reviewDate", LocalDateTime.now(philippineZone).toLocalDate().toString());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error submitting review: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/borrow-book/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> borrowBook(@PathVariable Integer id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String firstName = (String) session.getAttribute("firstName");
        String lastName = (String) session.getAttribute("lastName");
        String fullName = firstName + " " + lastName;

        if (fullName == null || fullName.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "User not logged in or name not available.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        double totalPenalty = calculateTotalOutstandingPenalty(fullName);
        if (totalPenalty > 0) {
            response.put("success", false);
            response.put("message", "You have an outstanding penalty of ₱" + String.format("%.2f", totalPenalty) + ". Please settle your penalty before borrowing more books.");
            return ResponseEntity.badRequest().body(response);
        }

        List<BorrowHistory> currentBorrowedEntries = borrowHistoryRepository.findByBorrowedByAndReturnDateIsNull(fullName);
        if (currentBorrowedEntries.size() >= MAX_BORROW_LIMIT) {
            response.put("success", false);
            response.put("message", "You have reached the maximum borrow limit of " + MAX_BORROW_LIMIT + " books.");
            return ResponseEntity.badRequest().body(response);
        }

        Optional<Book> bookOpt = bookRepository.findById(id.longValue());

        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();

            if (book.getQuantity() <= 0) {
                if (!"Out of Stock".equals(book.getStatus())) {
                    book.setStatus("Out of Stock");
                    bookRepository.save(book);
                }
                response.put("success", false);
                response.put("message", "This book is currently out of stock.");
                return ResponseEntity.badRequest().body(response);
            }

            LocalDate borrowedDate = LocalDate.now(philippineZone);
            LocalTime borrowedTime = LocalTime.now(philippineZone);

            book.setQuantity(book.getQuantity() - 1);

            if (book.getQuantity() == 0) {
                book.setStatus("Out of Stock");
            } else {
                book.setStatus("Available");
            }

            bookRepository.save(book);

            BorrowHistory borrowHistory = new BorrowHistory();
            borrowHistory.setBook(book);
            borrowHistory.setBorrowedBy(fullName);
            borrowHistory.setBorrowDate(borrowedDate);
            borrowHistory.setBorrowTime(borrowedTime);

            borrowHistoryRepository.save(borrowHistory);

            response.put("success", true);
            response.put("message", "The book was successfully borrowed!");
            response.put("updatedQuantity", book.getQuantity());
            response.put("updatedBookStatus", book.getStatus());
            response.put("borrowedEntryId", borrowHistory.getId());
            response.put("borrowedBookId", book.getId());
            response.put("borrowedBookTitle", book.getTitle());
            response.put("borrowedBookAuthor", book.getAuthor() != null ? book.getAuthor().getName() : "N/A");
            response.put("borrowedBookStatus", "Borrowed");
            response.put("borrowerName", fullName);
            response.put("borrowedDate", borrowedDate.toString());
            response.put("borrowedTime", borrowedTime.format(timeFormatter));
            response.put("totalPenalty", calculateTotalOutstandingPenalty(fullName));
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "The book was not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping("/borrow-books-batch")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> borrowBooksBatch(@RequestBody Map<String, List<Long>> requestBody, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        List<Long> bookIds = requestBody.get("bookIds");

        if (bookIds == null || bookIds.isEmpty()) {
            response.put("success", false);
            response.put("message", "No books selected for borrowing.");
            return ResponseEntity.badRequest().body(response);
        }

        String firstName = (String) session.getAttribute("firstName");
        String lastName = (String) session.getAttribute("lastName");
        String fullName = firstName + " " + lastName;

        if (fullName == null || fullName.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "User not logged in.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        double totalPenalty = calculateTotalOutstandingPenalty(fullName);
        if (totalPenalty > 0) {
            response.put("success", false);
            response.put("message", "You have an outstanding penalty of ₱" + String.format("%.2f", totalPenalty) + ". Please settle your penalty before borrowing more books.");
            return ResponseEntity.badRequest().body(response);
        }

        List<BorrowHistory> currentBorrowedEntries = borrowHistoryRepository.findByBorrowedByAndReturnDateIsNull(fullName);
        int currentBorrowedCount = currentBorrowedEntries.size();

        if (currentBorrowedCount + bookIds.size() > MAX_BORROW_LIMIT) {
            response.put("success", false);
            response.put("message", "You cannot borrow more than " + MAX_BORROW_LIMIT + " books. You currently have " + currentBorrowedCount + " borrowed books and are attempting to borrow " + bookIds.size() + " more.");
            return ResponseEntity.badRequest().body(response);
        }

        List<String> failedBorrows = new ArrayList<>();
        int successfulBorrows = 0;
        List<Map<String, Object>> newlyBorrowedEntriesDetails = new ArrayList<>();

        for (Long bookId : bookIds) {
            Optional<Book> bookOpt = bookRepository.findById(bookId);
            if (bookOpt.isPresent()) {
                Book book = bookOpt.get();
                if (book.getQuantity() > 0) {
                    LocalDate borrowedDate = LocalDate.now(philippineZone);
                    LocalTime borrowedTime = LocalTime.now(philippineZone);

                    book.setQuantity(book.getQuantity() - 1);

                    if (book.getQuantity() == 0) {
                        book.setStatus("Out of Stock");
                    } else {
                        book.setStatus("Available");
                    }

                    bookRepository.save(book);

                    BorrowHistory borrowHistory = new BorrowHistory();
                    borrowHistory.setBook(book);
                    borrowHistory.setBorrowedBy(fullName);
                    borrowHistory.setBorrowDate(borrowedDate);
                    borrowHistory.setBorrowTime(borrowedTime);
                    borrowHistoryRepository.save(borrowHistory);

                    successfulBorrows++;

                    Map<String, Object> entryDetails = new HashMap<>();
                    entryDetails.put("borrowedEntryId", borrowHistory.getId());
                    entryDetails.put("borrowedBookId", book.getId());
                    entryDetails.put("borrowedBookTitle", book.getTitle());
                    entryDetails.put("borrowedBookAuthor", book.getAuthor() != null ? book.getAuthor().getName() : "N/A");
                    entryDetails.put("borrowedBookStatus", "Borrowed");
                    entryDetails.put("borrowerName", fullName);
                    entryDetails.put("borrowedDate", borrowedDate.toString());
                    entryDetails.put("borrowedTime", borrowedTime.format(timeFormatter));
                    newlyBorrowedEntriesDetails.add(entryDetails);

                } else {
                    if (!"Out of Stock".equals(book.getStatus())) {
                        book.setStatus("Out of Stock");
                        bookRepository.save(book);
                    }
                    failedBorrows.add(book.getTitle() + " (ID: " + book.getId() + ") - Out of stock.");
                }
            } else {
                failedBorrows.add("Book with ID: " + bookId + " not found.");
            }
        }

        if (successfulBorrows > 0) {
            response.put("success", true);
            response.put("message", successfulBorrows + " book(s) successfully borrowed.");
            response.put("newlyBorrowedEntries", newlyBorrowedEntriesDetails);
            if (!failedBorrows.isEmpty()) {
                response.put("message", response.get("message") + " Some books could not be borrowed: " + String.join(", ", failedBorrows));
            }
            response.put("totalPenalty", calculateTotalOutstandingPenalty(fullName));
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "No books were borrowed. " + String.join(", ", failedBorrows));
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/return-book/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> returnBook(@PathVariable Integer id,
                                                        @RequestBody Map<String, Object> payload,
                                                        HttpSession session) {

        Optional<BorrowHistory> borrowHistoryOpt = borrowHistoryRepository.findById(id.longValue());
        Map<String, Object> response = new HashMap<>();

        if (borrowHistoryOpt.isPresent()) {
            BorrowHistory borrowHistory = borrowHistoryOpt.get();
            Book book = borrowHistory.getBook();

            if (borrowHistory.getReturnDate() != null) {
                response.put("success", false);
                response.put("message", "This book has already been returned.");
                return ResponseEntity.badRequest().body(response);
            }

            double fine = 0.0;
            if (payload.containsKey("fine")) {
                try {
                    fine = ((Number) payload.get("fine")).doubleValue();
                } catch (ClassCastException e) {
                    System.err.println("Error casting fine value from payload: " + e.getMessage());
                }
            }
            borrowHistory.setFine(fine);

            String returnedBy;
            String adminFirstName = (String) session.getAttribute("firstName");
            String adminLastName = (String) session.getAttribute("lastName");
            String fullName = (String) session.getAttribute("firstName") + " " + (String) session.getAttribute("lastName");

            User loggedInUser = (User) session.getAttribute("loggedInUser");
            if (loggedInUser != null && "Admin".equals(loggedInUser.getRole())) {
                returnedBy = "Admin: " + adminFirstName + " " + adminLastName;
            } else {
                returnedBy = borrowHistory.getBorrowedBy();
            }

            borrowHistory.setReturnDate(LocalDate.now(philippineZone));
            borrowHistory.setReturnTime(LocalTime.now(philippineZone));
            borrowHistory.setReturnedBy(returnedBy);
            borrowHistoryRepository.save(borrowHistory);

            book.setQuantity(book.getQuantity() + 1);

            if (book.getQuantity() > 0) {
                book.setStatus("Available");
            } else {
                book.setStatus("Out of Stock");
            }
            bookRepository.save(book);

            response.put("success", true);
            response.put("message", "");
            response.put("updatedBookQuantity", book.getQuantity()); 
            response.put("updatedBookStatus", book.getStatus()); 
            response.put("returnedBorrowEntryId", borrowHistory.getId()); 
            response.put("bookIdOfReturnedEntry", book.getId()); 
            response.put("totalPenalty", calculateTotalOutstandingPenalty(fullName)); 
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Borrowed book entry not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping("/borrow-reserved-book")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> borrowReservedBook(@RequestBody Map<String, Object> payload, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            response.put("success", false);
            response.put("message", "User not logged in.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        String firstName = (String) session.getAttribute("firstName");
        String lastName = (String) session.getAttribute("lastName");
        String fullName = firstName + " " + lastName;
        
        try {
            Long bookId = Long.valueOf(payload.get("bookId").toString());
            Long reservationId = Long.valueOf(payload.get("reservationId").toString());
            
            double totalPenalty = calculateTotalOutstandingPenalty(fullName);
            if (totalPenalty > 0) {
                response.put("success", false);
                response.put("message", "You have an outstanding penalty of ₱" + String.format("%.2f", totalPenalty) + ". Please settle your penalty before borrowing more books.");
                return ResponseEntity.badRequest().body(response);
            }
            
            List<BorrowHistory> currentBorrowedEntries = borrowHistoryRepository.findByBorrowedByAndReturnDateIsNull(fullName);
            if (currentBorrowedEntries.size() >= MAX_BORROW_LIMIT) {
                response.put("success", false);
                response.put("message", "You have reached the maximum borrow limit of " + MAX_BORROW_LIMIT + " books.");
                return ResponseEntity.badRequest().body(response);
            }
            
            Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);
            if (!reservationOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Reservation not found.");
                return ResponseEntity.badRequest().body(response);
            }
            
            Reservation reservation = reservationOpt.get();
            if (!reservation.getUser().getId().equals(loggedInUser.getId())) {
                response.put("success", false);
                response.put("message", "This reservation does not belong to you.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            Optional<Book> bookOpt = bookRepository.findById(bookId);
            if (!bookOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Book not found.");
                return ResponseEntity.badRequest().body(response);
            }
            
            Book book = bookOpt.get();
            
            if (book.getQuantity() <= 0) {
                response.put("success", false);
                response.put("message", "This book is no longer available.");
                return ResponseEntity.badRequest().body(response);
            }
            
            LocalDate borrowedDate = LocalDate.now(philippineZone);
            LocalTime borrowedTime = LocalTime.now(philippineZone);
            
            book.setQuantity(book.getQuantity() - 1);
            if (book.getQuantity() == 0) {
                book.setStatus("Out of Stock");
            } else {
                book.setStatus("Available");
            }
            bookRepository.save(book);
            
            BorrowHistory borrowHistory = new BorrowHistory();
            borrowHistory.setBook(book);
            borrowHistory.setBorrowedBy(fullName);
            borrowHistory.setBorrowDate(borrowedDate);
            borrowHistory.setBorrowTime(borrowedTime);
            borrowHistoryRepository.save(borrowHistory);
            
            reservation.setCompletedDate(LocalDateTime.now(philippineZone));
            reservationRepository.save(reservation);
            
            response.put("success", true);
            response.put("message", "Book borrowed successfully from reservation!");
            response.put("borrowedEntryId", borrowHistory.getId());
            response.put("bookTitle", book.getTitle());
            response.put("bookAuthor", book.getAuthor() != null ? book.getAuthor().getName() : "Unknown");
            response.put("borrowedDate", borrowedDate.toString());
            response.put("borrowedTime", borrowedTime.format(timeFormatter));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error borrowing reserved book: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/adminPov")
    public String adminPov(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"Admin".equals(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        String firstName = (String) session.getAttribute("firstName");
        String lastName = (String) session.getAttribute("lastName");

        model.addAttribute("firstName", firstName != null ? firstName : "");
        model.addAttribute("lastName", lastName != null ? lastName : "");

        List<Book> allBooks = bookRepository.findAll();
        if (allBooks == null) {
            allBooks = new ArrayList<>();
        }

        List<BorrowHistory> currentBorrowedEntries = borrowHistoryRepository.findByReturnDateIsNull();
        if (currentBorrowedEntries == null) {
            currentBorrowedEntries = new ArrayList<>();
        }

        List<String> allAuthorNames = authorRepository.findAllAuthorNames();
        if (allAuthorNames == null) {
            allAuthorNames = new ArrayList<>();
        }

        List<String> allBorrowerNames = borrowHistoryRepository.findDistinctBorrowedBy();
        if (allBorrowerNames == null) {
            allBorrowerNames = new ArrayList<>();
        }

        List<String> allCategoryNames = bookService.getAllDistinctCategories();
        if (allCategoryNames == null) {
            allCategoryNames = new ArrayList<>();
        }

        model.addAttribute("borrowedBooks", currentBorrowedEntries);

        List<Book> availableBooksForAdmin = allBooks.stream()
            .filter(book -> "Available".equals(book.getStatus()) || "Out of Stock".equals(book.getStatus())) 
            .collect(Collectors.toList());
        model.addAttribute("availableBooks", availableBooksForAdmin);
        model.addAttribute("allAuthorNames", allAuthorNames);
        model.addAttribute("allBorrowerNames", allBorrowerNames);
        model.addAttribute("allCategoryNames", allCategoryNames);

        return "adminPov";
    }

    @GetMapping("/admin/history")
    public String showReturnHistory(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"Admin".equals(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        List<BorrowHistory> returnHistory = borrowHistoryRepository.findByReturnDateIsNotNullOrderByReturnDateDesc();
        if (returnHistory == null) {
            returnHistory = new ArrayList<>();
        }

        String firstName = (String) session.getAttribute("firstName");
        String lastName = (String) session.getAttribute("lastName");

        model.addAttribute("returnHistory", returnHistory);
        model.addAttribute("firstName", firstName != null ? firstName : "");
        model.addAttribute("lastName", lastName != null ? lastName : "");

        return "history";
    }

    @GetMapping("/admin/borrow-history")
    public String showBorrowHistory(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"Admin".equals(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        List<BorrowHistory> borrowHistory = borrowHistoryRepository.findAllByOrderByBorrowDateDesc();
        if (borrowHistory == null) {
            borrowHistory = new ArrayList<>();
        }

        String firstName = (String) session.getAttribute("firstName");
        String lastName = (String) session.getAttribute("lastName");

        model.addAttribute("borrowHistory", borrowHistory);
        model.addAttribute("firstName", firstName != null ? firstName : "");
        model.addAttribute("lastName", lastName != null ? lastName : "");

        return "borrow-history";
    }

    @PostMapping("/admin/clear-borrow-history")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearAllBorrowHistory() {
        Map<String, Object> response = new HashMap<>();
        try {
        	ratingHistoryRepository.deleteAll();
            borrowHistoryRepository.deleteAll();
            response.put("success", true);
            response.put("message", "All borrow history cleared successfully!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to clear all borrow history: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/clear-history")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearReturnHistory() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<BorrowHistory> returnedHistory = borrowHistoryRepository.findByReturnDateIsNotNullOrderByReturnDateDesc();
            Set<Long> returnedHistoryIds = returnedHistory.stream()
                                            .map(BorrowHistory::getId)
                                            .collect(Collectors.toSet());
            List<RatingHistory> allRatings = ratingHistoryRepository.findAll();
            List<RatingHistory> ratingsToDelete = new ArrayList<>();
            
            for (RatingHistory rating : allRatings) {
                if (rating.getBorrowHistory() != null && returnedHistoryIds.contains(rating.getBorrowHistory().getId())) {
                    ratingsToDelete.add(rating);
                }
            }
            ratingHistoryRepository.deleteAll(ratingsToDelete);
            borrowHistoryRepository.deleteAll(returnedHistory);
            response.put("success", true);
            response.put("message", "Returned history (and associated ratings) cleared successfully!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to clear returned history: " + e.getMessage());
            e.printStackTrace(); 
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/add-book-form")
    public String showAddBookForm(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"Admin".equals(loggedInUser.getRole())) {
            return "redirect:/login";
        }
        model.addAttribute("book", new Book());
        model.addAttribute("authors", authorRepository.findAll());
        return "books";
    }

    @PostMapping("/addBook")
    public String addBook(@ModelAttribute Book book, RedirectAttributes redirectAttributes) {
        Optional<Book> existingBook = bookRepository.findByTitleAndAuthor(book.getTitle(), book.getAuthor());

        if (existingBook.isPresent()) {
            Book foundBook = existingBook.get();
            foundBook.setQuantity(foundBook.getQuantity() + book.getQuantity());

            if (book.getCategory() != null && !book.getCategory().isEmpty()) {
                foundBook.setCategory(book.getCategory());
            }
            if (foundBook.getQuantity() > 0) {
                foundBook.setStatus("Available");
            } else if (foundBook.getQuantity() <= 0) {
                foundBook.setStatus("Out of Stock");
            }
            bookRepository.save(foundBook);
            redirectAttributes.addFlashAttribute("message", "Updated book quantity and information!");
        } else {
            if (book.getQuantity() <= 0) {
                book.setQuantity(1); 
            }
            
            if (book.getQuantity() > 0) {
                book.setStatus("Available");
            } else {
                book.setStatus("Out of Stock"); 
            }
            bookRepository.save(book);
            redirectAttributes.addFlashAttribute("message", "New book added successfully!");
        }

        return "redirect:/adminPov";
    }

    @DeleteMapping("/admin/delete-book/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteBook(@PathVariable Long id) {
        boolean isDeleted = bookService.deleteBook(id);

        Map<String, String> response = new HashMap<>();
        if (isDeleted) {
            response.put("message", "Book deleted successfully!");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.put("message", "Failed to delete book.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/admin/update-book-quantity/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateBookQuantity(@PathVariable Long id, @RequestBody Map<String, Integer> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer newQuantity = payload.get("quantity");

            if (newQuantity == null || newQuantity < 0) {
                response.put("success", false);
                response.put("message", "Invalid quantity provided.");
                return ResponseEntity.badRequest().body(response);
            }

            Optional<Book> optionalBook = bookRepository.findById(id);
            if (optionalBook.isPresent()) {
                Book book = optionalBook.get();
                book.setQuantity(newQuantity);

                if (newQuantity > 0) {
                    book.setStatus("Available");
                } else {
                    book.setStatus("Out of Stock");
                }

                bookRepository.save(book);

                response.put("success", true);
                response.put("message", "Book quantity and status updated successfully!");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Book not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating book quantity: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/categories")
    public String categories(HttpSession session) {
        String firstName = (String) session.getAttribute("firstName");
        String lastName = (String) session.getAttribute("lastName");

        if (firstName == null || lastName == null) {
            return "redirect:/login";
        }
        return "categories";
    }

    @GetMapping("/books")
    public String books(Model model) {
        model.addAttribute("book", new Book());
        return "books";
    }

    @GetMapping("/books/category/{categoryName}")
    public String viewBooksByCategory(@PathVariable String categoryName, Model model) {
        List<Book> books = bookRepository.findByCategory(categoryName);
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("books", books);
        return "category-books";
    }
    
    @GetMapping("/home")
    public String showHomePage() {
        return "home";
    }
    
    @PostMapping("/submit-rating-history")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> submitRatingHistory(@RequestBody Map<String, Object> ratingData, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User loggedInUser = (User) session.getAttribute("loggedInUser");
            if (loggedInUser == null) {
                response.put("success", false);
                response.put("message", "User not logged in");
                return ResponseEntity.badRequest().body(response);
            }
            
            Long borrowHistoryId = Long.valueOf(ratingData.get("borrowHistoryId").toString());
            Long bookId = Long.valueOf(ratingData.get("bookId").toString());
            int rating = Integer.parseInt(ratingData.get("rating").toString());
            String comment = ratingData.get("comment") != null ? ratingData.get("comment").toString() : "";
            
            BorrowHistory borrowHistory = borrowHistoryRepository.findById(borrowHistoryId).orElse(null);
            Book book = bookRepository.findById(bookId).orElse(null);
            
            if (borrowHistory == null || book == null) {
                response.put("success", false);
                response.put("message", "Invalid borrow history or book");
                return ResponseEntity.badRequest().body(response);
            }
            
            RatingHistory ratingHistory = new RatingHistory(
                loggedInUser,
                book,
                borrowHistory,
                rating,
                comment,
                LocalDate.now()
            );
            
            ratingHistoryRepository.save(ratingHistory);
            
            response.put("success", true);
            response.put("message", "Rating submitted successfully");
            response.put("reviewDate", LocalDate.now().toString());
            response.put("borrowDate", borrowHistory.getBorrowDate().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error submitting rating: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // ======== FIXED REVIEWS MANAGEMENT METHOD ========
    @GetMapping("/admin/reviews")
    public String showReviewsManagement(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"Admin".equals(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        String firstName = (String) session.getAttribute("firstName");
        String lastName = (String) session.getAttribute("lastName");

        model.addAttribute("firstName", firstName != null ? firstName : "");
        model.addAttribute("lastName", lastName != null ? lastName : "");

        List<Review> pendingReviews = reviewRepository.findAllByOrderByReviewDateDesc();
        if (pendingReviews == null) {
            pendingReviews = new ArrayList<>();
        }
        model.addAttribute("pendingReviews", pendingReviews);

        List<RatingHistory> historyReviews = ratingHistoryRepository.findAll();
        if (historyReviews == null) {
            historyReviews = new ArrayList<>();
        }
        model.addAttribute("allReviews", historyReviews); 

        model.addAttribute("totalReviews", historyReviews.size()); 
        
        Double averageRating = historyReviews.stream()
                .mapToInt(RatingHistory::getRating)
                .average()
                .orElse(0.0);
        model.addAttribute("averageRating", averageRating);
        
        return "reviews-management";
    }
    
    @DeleteMapping("/admin/delete-review/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteReview(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"Admin".equals(loggedInUser.getRole())) {
            response.put("success", false);
            response.put("message", "Unauthorized access.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            Optional<Review> reviewOpt = reviewRepository.findById(id);
            if (reviewOpt.isPresent()) {
                reviewRepository.deleteById(id);
                response.put("success", true);
                response.put("message", "Review deleted successfully!");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Review not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting review: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/admin/delete-rating-history/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteRatingHistory(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"Admin".equals(loggedInUser.getRole())) {
            response.put("success", false);
            response.put("message", "Unauthorized access.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            Optional<RatingHistory> ratingOpt = ratingHistoryRepository.findById(id);
            if (ratingOpt.isPresent()) {
                ratingHistoryRepository.deleteById(id);
                response.put("success", true);
                response.put("message", "Rating deleted successfully!");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Rating not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting rating: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @DeleteMapping("/admin/delete-reservation/{reservationId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteReservation(@PathVariable Long reservationId) {  
        Map<String, Object> response = new HashMap<>();
        
        try {
            Reservation reservation = reservationRepository.findById(reservationId)
                    .orElse(null);
            
            if (reservation == null) {
                response.put("success", false);
                response.put("message", "Reservation not found.");
                return ResponseEntity.badRequest().body(response);
            }
            
            reservationRepository.delete(reservation);
            
            response.put("success", true);
            response.put("message", "Reservation deleted successfully.");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting reservation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ======== UPDATED RESERVATION HISTORY METHOD (Fixing Missing Lists) ========
    @GetMapping("/admin/reservation-history")
    public String adminReservationHistory(Model model, HttpSession session) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"Admin".equals(loggedInUser.getRole())) {  
            return "redirect:/login";  
        }
        
        String firstName = (String) session.getAttribute("firstName");
        String lastName = (String) session.getAttribute("lastName");
        
        List<Reservation> allReservations = reservationRepository.findAll();
        
        // 1. Filter into lists so the HTML has something to iterate over
        List<Reservation> activeReservations = allReservations.stream()
                .filter(r -> r.getCompletedDate() == null && r.getCancelledDate() == null)
                .collect(Collectors.toList());
                
        List<Reservation> completedReservations = allReservations.stream()
                .filter(r -> r.getCompletedDate() != null)
                .collect(Collectors.toList());
                
        List<Reservation> cancelledReservations = allReservations.stream()
                .filter(r -> r.getCancelledDate() != null)
                .collect(Collectors.toList());
        
        // 2. Add these specific lists to the model
        model.addAttribute("activeReservations", activeReservations);
        model.addAttribute("completedReservations", completedReservations);
        model.addAttribute("cancelledReservations", cancelledReservations);

        // 3. Keep the other attributes
        model.addAttribute("allReservations", allReservations);
        model.addAttribute("activeReservationsCount", activeReservations.size());
        model.addAttribute("completedReservationsCount", completedReservations.size());
        model.addAttribute("cancelledReservationsCount", cancelledReservations.size());
        
        Set<String> allReservationUsers = allReservations.stream()
                .filter(r -> r.getUser() != null)
                .map(reservation -> reservation.getUser().getFirstName() + " " + reservation.getUser().getLastName())
                .collect(Collectors.toSet());
        
        Set<String> allReservationBooks = allReservations.stream()
                .filter(r -> r.getBook() != null)
                .map(reservation -> reservation.getBook().getTitle())
                .collect(Collectors.toSet());
        
        model.addAttribute("allReservationUsers", allReservationUsers);
        model.addAttribute("allReservationBooks", allReservationBooks);
        
        model.addAttribute("firstName", firstName);
        model.addAttribute("lastName", lastName);
        
        return "admin-reservation-history";
    }

    // ======== ADDED METHOD FOR ADMIN TO CANCEL RESERVATION ========
    @PostMapping("/admin/cancel-reservation/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> adminCancelReservation(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"Admin".equals(loggedInUser.getRole())) {
            response.put("success", false);
            response.put("message", "Unauthorized access.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            Optional<Reservation> reservationOpt = reservationRepository.findById(id);
            if (!reservationOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Reservation not found.");
                return ResponseEntity.badRequest().body(response);
            }
            
            Reservation reservation = reservationOpt.get();
            
            reservation.setCancelledDate(LocalDateTime.now(philippineZone));
            reservationRepository.save(reservation);
            
            Map<String, Object> reservationDetails = new HashMap<>();
            reservationDetails.put("bookTitle", reservation.getBook().getTitle());
            reservationDetails.put("authorName", reservation.getBook().getAuthor() != null ? 
                                  reservation.getBook().getAuthor().getName() : "Unknown");
            reservationDetails.put("reservationDate", reservation.getReservationDate().toString());
            reservationDetails.put("cancelledDate", reservation.getCancelledDate().toLocalDate().toString());
            
            response.put("success", true);
            response.put("message", "Reservation cancelled successfully!");
            response.put("reservation", reservationDetails);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error cancelling reservation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/reserve-book")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reserveBook(@RequestBody Map<String, Object> payload, HttpSession session) {
        // ... (Keep existing reserveBook method) ...
        // (For brevity, I'm not repeating code that didn't change, but in your real file, keep it!)
        // Just make sure you didn't delete the methods below this point like reserveBook, checkReservations etc.
        Map<String, Object> response = new HashMap<>();
        
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            response.put("success", false);
            response.put("message", "User not logged in.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            Long bookId = Long.valueOf(payload.get("bookId").toString());
            
            Optional<Book> bookOpt = bookRepository.findById(bookId);
            if (!bookOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Book not found.");
                return ResponseEntity.badRequest().body(response);
            }
            
            Book book = bookOpt.get();
            
            String firstName = (String) session.getAttribute("firstName");
            String lastName = (String) session.getAttribute("lastName");
            String fullName = firstName + " " + lastName;
            double totalPenalty = calculateTotalOutstandingPenalty(fullName);
            
            if (totalPenalty > 0) {
                response.put("success", false);
                response.put("message", "You have an outstanding penalty of ₱" + String.format("%.2f", totalPenalty) + ". Please settle your penalty before making reservations.");
                return ResponseEntity.badRequest().body(response);
            }
            
            long activeReservationCount = reservationRepository.countByUserAndBookAndCompletedDateIsNullAndCancelledDateIsNull(loggedInUser, book);
            
            final int MAX_RESERVATIONS_PER_BOOK = 5; 
            if (activeReservationCount >= MAX_RESERVATIONS_PER_BOOK) {
                response.put("success", false);
                response.put("message", "You have reached the maximum number of reservations (" + MAX_RESERVATIONS_PER_BOOK + ") for this book.");
                return ResponseEntity.badRequest().body(response);
            }
            
            Reservation reservation = new Reservation(loggedInUser, book);
            reservationRepository.save(reservation);
            
            response.put("success", true);
            response.put("message", "Book reserved successfully! You now have " + (activeReservationCount + 1) + " active reservation(s) for this book.");
            response.put("reservationId", reservation.getId());
            response.put("bookId", book.getId());
            response.put("reservationDate", reservation.getReservationDate().toString());
            response.put("activeReservationCount", activeReservationCount + 1);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error reserving book: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ... (Keep the rest of your methods like cancel-reservation (user side), check-reservations, etc.) ...
    @PostMapping("/cancel-reservation/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelReservation(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            response.put("success", false);
            response.put("message", "User not logged in.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            Optional<Reservation> reservationOpt = reservationRepository.findById(id);
            if (!reservationOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Reservation not found.");
                return ResponseEntity.badRequest().body(response);
            }
            
            Reservation reservation = reservationOpt.get();
            
            if (!reservation.getUser().getId().equals(loggedInUser.getId())) {
                response.put("success", false);
                response.put("message", "You can only cancel your own reservations.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            reservation.setCancelledDate(LocalDateTime.now(philippineZone));
            reservationRepository.save(reservation);
            
            Map<String, Object> reservationDetails = new HashMap<>();
            reservationDetails.put("bookTitle", reservation.getBook().getTitle());
            reservationDetails.put("authorName", reservation.getBook().getAuthor() != null ? 
                                  reservation.getBook().getAuthor().getName() : "Unknown");
            reservationDetails.put("reservationDate", reservation.getReservationDate().toString());
            reservationDetails.put("cancelledDate", reservation.getCancelledDate().toLocalDate().toString());
            
            response.put("success", true);
            response.put("message", "Reservation cancelled successfully!");
            response.put("reservation", reservationDetails);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error cancelling reservation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/check-reservations/{bookId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkReservations(@PathVariable Long bookId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Book> bookOpt = bookRepository.findById(bookId);
            if (!bookOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Book not found.");
                return ResponseEntity.badRequest().body(response);
            }
            
            Book book = bookOpt.get();
            
            List<Reservation> reservations = reservationRepository.findByBookAndNotificationSentFalse(book);
            
            if (!reservations.isEmpty() && book.getQuantity() > 0) {
                for (Reservation reservation : reservations) {
                    reservation.setAvailable(true);
                    reservation.setNotificationSent(true);
                    reservationRepository.save(reservation);
                }
                
                response.put("hasReservations", true);
                response.put("reservationCount", reservations.size());
            } else {
                response.put("hasReservations", false);
                response.put("reservationCount", 0);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error checking reservations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/check-available-reservations")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkAvailableReservations(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            response.put("success", false);
            response.put("message", "User not logged in.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            List<Reservation> availableReservations = reservationRepository.findByUserAndIsAvailableTrueAndCompletedDateIsNullAndCancelledDateIsNull(loggedInUser);
            
            List<Map<String, Object>> bookDetails = new ArrayList<>();
            for (Reservation reservation : availableReservations) {
                if (reservation.getBook().getQuantity() > 0) {
                    Map<String, Object> bookDetail = new HashMap<>();
                    bookDetail.put("id", reservation.getBook().getId());
                    bookDetail.put("title", reservation.getBook().getTitle());
                    bookDetail.put("author", reservation.getBook().getAuthor() != null ? 
                                  reservation.getBook().getAuthor().getName() : "Unknown");
                    bookDetail.put("reservationId", reservation.getId());
                    bookDetails.add(bookDetail);
                }
            }
            response.put("success", true);
            response.put("availableReservations", bookDetails);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error checking available reservations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/reservation-history")
    public String showReservationHistory(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        String firstName = (String) session.getAttribute("firstName");
        String lastName = (String) session.getAttribute("lastName");
        
        model.addAttribute("firstName", firstName);
        model.addAttribute("lastName", lastName);
        
        List<Reservation> activeReservations = reservationRepository.findByUserAndIsAvailableFalseAndCancelledDateIsNull(loggedInUser);
        model.addAttribute("activeReservations", activeReservations);
        
        List<Reservation> completedReservations = reservationRepository.findByUserAndCompletedDateIsNotNull(loggedInUser);
        model.addAttribute("completedReservations", completedReservations);
        
        List<Reservation> cancelledReservations = reservationRepository.findByUserAndCancelledDateIsNotNull(loggedInUser);
        model.addAttribute("cancelledReservations", cancelledReservations);
        
        return "reservation-history";
    }
}