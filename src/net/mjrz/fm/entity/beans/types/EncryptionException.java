package net.mjrz.fm.entity.beans.types;

public class EncryptionException extends RuntimeException {

	public EncryptionException(String message) {
		super(message);
	}

	public EncryptionException() {
		super();
	}

	public EncryptionException(String message, Throwable cause) {
		super(message, cause);
	}

	public EncryptionException(Throwable cause) {
		super(cause);
	}
}
