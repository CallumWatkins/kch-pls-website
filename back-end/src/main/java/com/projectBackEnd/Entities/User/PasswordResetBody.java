package main.java.com.projectBackEnd.Entities.User;

public class PasswordResetBody implements PasswordResetBodyInterface{
	private String password;
	private String token;
	public PasswordResetBody(String token, String password){
		this.token = token;
		this.password = password;
	}
	public PasswordResetBody(){}
	public String getPassword(){
		return password;
	}
	public String getToken(){
		return token;
	}
	public void setPassword(String password){
		this.password = password;
	}
	public void setToken(String token){
		this.token= token;
	}
}
