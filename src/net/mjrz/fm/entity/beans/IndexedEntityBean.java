package net.mjrz.fm.entity.beans;

public class IndexedEntityBean {
	private Long id;
	private String token;
	private int indexType;
	private int type;
	private String name;
	private int occuranceCount;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getOccuranceCount() {
		return occuranceCount;
	}

	public void setOccuranceCount(int occuranceCount) {
		this.occuranceCount = occuranceCount;
	}

	public int getIndexType() {
		return indexType;
	}

	public void setIndexType(int indexType) {
		this.indexType = indexType;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
