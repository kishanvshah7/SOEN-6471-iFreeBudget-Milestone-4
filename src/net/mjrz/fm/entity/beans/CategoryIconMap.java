package net.mjrz.fm.entity.beans;

public class CategoryIconMap implements FManEntity {
	private long id;
	private long categoryId;
	private String iconPath;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(long categoryId) {
		this.categoryId = categoryId;
	}

	public String getIconPath() {
		return iconPath;
	}

	public void setIconPath(String iconPath) {
		this.iconPath = iconPath;
	}

	@Override
	public Object getPK() {
		return id;
	}

	@Override
	public String getPKColumnName() {
		return "id";
	}

	@Override
	public void setPK(Object pk) {
		setId((Long) pk);
	}
}
