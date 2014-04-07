package net.mjrz.fm.entity.beans;

public class PwdHistory implements FManEntity {
	Long id;
	String uid;
	String password;
	String basePassword;

	public String getBasePassword() {
		return basePassword;
	}

	public void setBasePassword(String basePassword) {
		this.basePassword = basePassword;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String getPKColumnName() {
		return "id";
	}

	@Override
	public Object getPK() {
		return id;
	}

	@Override
	public void setPK(Object pk) {
		this.id = (Long) pk;
	}
}
