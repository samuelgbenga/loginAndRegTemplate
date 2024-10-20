package ng.samuel.regnlogintemplate.service.impl;

import lombok.RequiredArgsConstructor;
import ng.samuel.regnlogintemplate.entity.ConfirmationToken;
import ng.samuel.regnlogintemplate.entity.User;
import ng.samuel.regnlogintemplate.repository.ConfirmationTokenRepo;
import ng.samuel.regnlogintemplate.repository.UserRepo;
import ng.samuel.regnlogintemplate.service.ConfirmationTokenService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;



@Service
@RequiredArgsConstructor
public class ConfirmationTokenImpl implements ConfirmationTokenService {

    private final ConfirmationTokenRepo confirmationTokenRepo;
    private final UserRepo userRepository;

    @Override
    public String validateToken(String token) {

        Optional<ConfirmationToken> confirmationTokenOptional = confirmationTokenRepo.findByToken(token);
        if (confirmationTokenOptional.isEmpty()) {
            return "Invalid token";
        }

        ConfirmationToken confirmationToken = confirmationTokenOptional.get();

        if (confirmationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return "Token has expired";
        }
        User user = confirmationToken.getUsers();
        user.setEnabled(true);
        userRepository.save(user);

        confirmationTokenRepo.delete(confirmationToken);

        return "Email confirmation is successful";
    }
}
