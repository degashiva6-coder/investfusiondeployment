package com.investfusion;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(
    origins = "https://investfusiondeployment.vercel.app",
    allowCredentials = "true"
)
    private final AppUserRepository users;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    public AuthController(AppUserRepository users) { this.users = users; }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String,String> body, HttpSession session) {
        String name=clean(body.get("name")), email=clean(body.get("email")).toLowerCase(Locale.ROOT), password=body.getOrDefault("password", "");
        if(name.isBlank() || email.isBlank() || password.length()<8) return ResponseEntity.badRequest().body(Map.of("message", "Enter your name, email, and a password with at least 8 characters."));
        if(!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) return ResponseEntity.badRequest().body(Map.of("message", "Enter a valid email address."));
        if(users.existsByEmailIgnoreCase(email)) return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "An account with this email already exists. Please log in."));
        AppUser user=users.save(new AppUser(name, email, encoder.encode(password)));
        session.setAttribute("userId", user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(publicUser(user));
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String,String> body, HttpSession session) {
        String email=clean(body.get("email")).toLowerCase(Locale.ROOT), password=body.getOrDefault("password", "");
        Optional<AppUser> match=users.findByEmailIgnoreCase(email);
        if(match.isEmpty() || !encoder.matches(password, match.get().getPasswordHash())) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Email or password is incorrect."));
        session.setAttribute("userId", match.get().getId());
        return ResponseEntity.ok(publicUser(match.get()));
    }
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Long id=(Long)session.getAttribute("userId");
        if(id==null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Please log in."));
        return users.findById(id).<ResponseEntity<?>>map(u->ResponseEntity.ok(publicUser(u))).orElseGet(()->ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Please log in again.")));
    }
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String,String> body, HttpSession session) {
        Long id=(Long)session.getAttribute("userId"); if(id==null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Please log in."));
        Optional<AppUser> found=users.findById(id); if(found.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        AppUser user=found.get(); String name=clean(body.get("name")), email=clean(body.get("email")).toLowerCase(Locale.ROOT);
        if(name.isBlank() || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) return ResponseEntity.badRequest().body(Map.of("message", "Enter a valid name and email."));
        Optional<AppUser> existing=users.findByEmailIgnoreCase(email); if(existing.isPresent()&&!existing.get().getId().equals(id)) return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "That email is already in use."));
        user.setName(name); user.setEmail(email); return ResponseEntity.ok(publicUser(users.save(user)));
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) { session.invalidate(); return ResponseEntity.ok(Map.of("message", "Logged out.")); }
    private static String clean(String value){return value==null?"":value.trim();}
    private static Map<String,Object> publicUser(AppUser u){return Map.of("id",u.getId(),"name",u.getName(),"email",u.getEmail());}
}
