package main.java.com.projectBackEnd.Entities.User.Hibernate;

import java.io.Serializable;


public interface UserManagerInterface{
	public void addUser(String email,String password, String name) throws EmailExistsException,InvalidEmailException,IncorrectNameException;
	public String verifyUser(String email,String password);
	public void changePassword(String email, String newPassword) throws UserNotExistException;
	public void deleteUser(String email, String password) throws UserNotExistException;
	public boolean verifyEmail(String email);
	public void changeEmail(String oldEmail, String newEmail) throws UserNotExistException,EmailExistsException;
	public void changeName(String email, String name) throws UserNotExistException, IncorrectNameException;
	public String getName(String email) throws UserNotExistException;
}

