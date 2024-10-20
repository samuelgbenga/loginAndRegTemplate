package ng.samuel.regnlogintemplate.service;

import jakarta.mail.MessagingException;
import ng.samuel.regnlogintemplate.payload.request.LoginDto;
import ng.samuel.regnlogintemplate.payload.request.RegistrationDto;
import ng.samuel.regnlogintemplate.payload.response.LoginResponse;
import ng.samuel.regnlogintemplate.payload.response.RegistrationResponse;

public interface AuthService {

    RegistrationResponse register(RegistrationDto registrationDto) throws MessagingException;

    LoginResponse login (LoginDto loginRequest);
}
