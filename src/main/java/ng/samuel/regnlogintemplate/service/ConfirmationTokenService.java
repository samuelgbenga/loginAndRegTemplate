package ng.samuel.regnlogintemplate.service;

public interface ConfirmationTokenService {

    String validateToken(String token);
}
