package main.message;

import java.io.Serializable;

/**
 * 注册失败
 * @author flyaos
 *
 */
public class RegisterFailed implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String text;

	public RegisterFailed(String text) {
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
