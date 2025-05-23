package com.skb.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.skb.model.UserDtls;
import com.skb.repository.UserRepository;
import com.skb.service.UserService;
import com.skb.util.AppConstant;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthFailureHandlerImpl extends SimpleUrlAuthenticationFailureHandler {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserService userService;
	
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		
		String email = request.getParameter("username");
		
		UserDtls userDtls = userRepository.findByEmail(email);
		
		if (userDtls != null) {
		
		if (userDtls.getIsEnable()) {
			
			if (userDtls.getAccountNonLocked()) {
				
				if (userDtls.getFailedAttempt() < AppConstant.ATTEMPT_TIME) {
					userService.increaseFailedAttempt(userDtls);
				} else {
					userService.userAccountLock(userDtls);
					exception = new LockedException("Your account is Locked !! Failed attempt 3");
				}
				
			}else {
				
				if (userService.unlockAccountTimeExpired(userDtls)) {
					exception = new LockedException("Your account is Unlocked !! Please try to login");
				} else {
					exception = new LockedException("Your account is Locked !! Please try after sometimes");
				}
				
				//exception = new LockedException("your account is Locked");
			}
			
		} else {
			exception = new LockedException("your account is inactive");
		}
		}else {
			exception = new LockedException("Email & password is invalid");
		}
		super.setDefaultFailureUrl("/signin?error");
		super.onAuthenticationFailure(request, response, exception);
	}
	
}
