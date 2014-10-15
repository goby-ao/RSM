package main.message;

import java.io.Serializable;

/**
 * 注册成功
 * @author flyaos
 *
 */
public class RegisterSuccess implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String text;
	

	public RegisterSuccess(String text) {
		super();
		this.text = text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
	

}
