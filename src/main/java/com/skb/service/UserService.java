package com.skb.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.skb.model.UserDtls;

public interface UserService {
	
	public UserDtls saveUser(UserDtls user);
	
	public UserDtls getUserByEmail(String email);
	
	public List<UserDtls> getUsers(String role);

	public Boolean updateAccountStatus(Integer id, Boolean status);
	
	public void increaseFailedAttempt(UserDtls user);
	
	public void userAccountLock(UserDtls user);
	
	public Boolean unlockAccountTimeExpired(UserDtls user);
	
	public void resetAttempt(int userId);

	public void updateUserResetToken(String email, String resetToken);
	
	public UserDtls getUSerByToken(String token);
	
	public UserDtls updateUser(UserDtls user);
	
	public UserDtls updateUserProfile(UserDtls user,MultipartFile img);
	
	public UserDtls saveAdmin(UserDtls user);
	
	public Boolean existsEmail(String email);
	
}
