package net.mjrz.fm.utils.indexer;

import java.math.BigDecimal;

public class MatchedEntity implements Comparable<MatchedEntity> {
	private IndexedEntity entity;
	private BigDecimal value;

	public MatchedEntity(IndexedEntity entity, BigDecimal value) {
		super();
		this.entity = entity;
		this.value = value;
	}

	public IndexedEntity getEntity() {
		return entity;
	}

	public void setEntity(IndexedEntity entity) {
		this.entity = entity;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	@Override
	public int compareTo(MatchedEntity o) {
		return value.compareTo(o.getValue());
	}

	@Override
	public String toString() {
		return "MatchedEntity [entity=" + entity.getName() + ", value=" + value
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MatchedEntity other = (MatchedEntity) obj;
		if (entity == null) {
			if (other.entity != null)
				return false;
		}
		else if (!entity.equals(other.entity))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		}
		else if (!value.equals(other.value))
			return false;
		return true;
	}
}
