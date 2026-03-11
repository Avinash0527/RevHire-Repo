package com.example.revhirehiringplatform.security;



import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Loading user details for email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + email));

        if (user.getStatus() != null && !user.getStatus()) {
            log.warn("Login attempt for disabled user: {}", email);
            throw new UsernameNotFoundException("User is disabled");
        }

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        log.info("User {} loaded with authorities: {}", email, userDetails.getAuthorities());
        return userDetails;
    }
}
