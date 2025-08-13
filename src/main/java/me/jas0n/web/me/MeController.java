package me.jas0n.web.me;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class MeController {

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        // authentication.getName() 是我们在 JwtAuthFilter 里放进去的 email
        return Map.of(
                "email", authentication.getName()
        );
    }
}
