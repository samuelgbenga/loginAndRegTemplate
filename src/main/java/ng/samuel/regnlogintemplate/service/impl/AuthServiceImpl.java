package ng.samuel.regnlogintemplate.service.impl;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import ng.samuel.regnlogintemplate.config.JwtService;
import ng.samuel.regnlogintemplate.entity.ConfirmationToken;
import ng.samuel.regnlogintemplate.entity.JToken;
import ng.samuel.regnlogintemplate.entity.User;
import ng.samuel.regnlogintemplate.enums.Role;
import ng.samuel.regnlogintemplate.enums.TokenType;
import ng.samuel.regnlogintemplate.exception.AlreadyExistsException;
import ng.samuel.regnlogintemplate.exception.InvalidInputException;
import ng.samuel.regnlogintemplate.exception.NotEnabledException;
import ng.samuel.regnlogintemplate.exception.NotFoundException;
import ng.samuel.regnlogintemplate.payload.request.EmailDetails;
import ng.samuel.regnlogintemplate.payload.request.EmailDto;
import ng.samuel.regnlogintemplate.payload.request.LoginDto;
import ng.samuel.regnlogintemplate.payload.request.RegistrationDto;
import ng.samuel.regnlogintemplate.payload.response.LoginResponse;
import ng.samuel.regnlogintemplate.payload.response.RegistrationResponse;
import ng.samuel.regnlogintemplate.repository.ConfirmationTokenRepo;
import ng.samuel.regnlogintemplate.repository.JTokenRepo;
import ng.samuel.regnlogintemplate.repository.UserRepo;
import ng.samuel.regnlogintemplate.service.AuthService;
import ng.samuel.regnlogintemplate.service.EmailService;
import ng.samuel.regnlogintemplate.utils.EmailBody;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepo userRepository;
    private final ConfirmationTokenRepo confirmationTokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JTokenRepo jTokenRepo;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public RegistrationResponse register(RegistrationDto registrationDto) throws MessagingException {
        Optional<User> existingUser = userRepository.findByEmail(registrationDto.getEmail());
        if (existingUser.isPresent()) {
            throw new AlreadyExistsException("User already exists, please Login");
        }

        User user = User.builder()
                .fullName(registrationDto.getFullName())
                .email(registrationDto.getEmail())
                .password(passwordEncoder.encode(registrationDto.getPassword()))
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);


        ConfirmationToken confirmationToken = new ConfirmationToken(savedUser);
        confirmationTokenRepo.save(confirmationToken);

        String confirmationUrl = "http://localhost:8080/api/v1/auth/confirm?token=" + confirmationToken.getToken();

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(user.getEmail())
                .subject("FORGET PASSWORD")
                .messageBody(EmailBody.buildEmail(new EmailDto(savedUser.getFullName(), confirmationUrl)))
                .build();

        emailService.mimeMailMessage(emailDetails);


        return RegistrationResponse.builder()
                .responseCode("001")
                .responseMessage("Email has been sent to the email you provided. please confirm by")
                .build();
    }

    @Override
    public LoginResponse login(LoginDto loginRequest) {
        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );


            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new NotFoundException("User not found with username: " + loginRequest.getEmail()));


            if (!user.isEnabled()) {
                throw new NotEnabledException("User account is not enabled. Please check your email to confirm your account.");
            }

            var jwtToken = jwtService.generateToken(user);
            revokeAllUserTokens(user);
            saveUserToken(user, jwtToken);

            return LoginResponse.builder()
                    .responseCode("002")
                    .responseMessage("Login Successfully")
                    .token(jwtToken)
                    .build();

        }catch (AuthenticationException e) {
            throw new InvalidInputException("Invalid username or password!!");
        }
    }


    private void saveUserToken(User user, String jwtToken){
        var token = JToken.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        jTokenRepo.save(token);
    }

    private void revokeAllUserTokens(User user){
        var validUserTokens = jTokenRepo.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        jTokenRepo.saveAll(validUserTokens);
    }
}
