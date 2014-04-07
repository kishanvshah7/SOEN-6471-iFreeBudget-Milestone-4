/*******************************************************************************
 * Copyright  
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package net.mjrz.fm.utils.crypto;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.codec.binary.Base64;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class CHelper {
	private final static String CIPHER_ALGORITHM = "PBEWithMD5AndDES/CBC/PKCS5Padding";

	private final static String KEY_ALGORITHM = "PBEWithMD5AndDES";

	static byte[] salt = { (byte) 0xc7, (byte) 0x73, (byte) 0x21, (byte) 0x8c,
			(byte) 0x7e, (byte) 0xc8, (byte) 0xee, (byte) 0x99 };

	static int count = 20;

	private static final PBEParameterSpec paramSpec = new PBEParameterSpec(
			salt, count);

	private static Cipher cipher;
	private static SecretKey masterKey = null;

	// private static sun.misc.BASE64Decoder dec = new sun.misc.BASE64Decoder();
	// private static sun.misc.BASE64Encoder enc = new sun.misc.BASE64Encoder();

	public static void init(char[] pass) {
		try {
			cipher = Cipher.getInstance(CIPHER_ALGORITHM);

			SecretKeyFactory secretKeyFactory = SecretKeyFactory
					.getInstance(KEY_ALGORITHM);
			PBEKeySpec keySpec = new PBEKeySpec(pass);
			masterKey = secretKeyFactory.generateSecret(keySpec);
		}
		catch (GeneralSecurityException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public static String decrypt(String in) throws IOException,
			InvalidKeyException, InvalidAlgorithmParameterException,
			IllegalBlockSizeException, BadPaddingException {

		if (in == null) {
			return null;
		}

		byte[] bin = Base64.decodeBase64(in.getBytes());
		cipher.init(Cipher.DECRYPT_MODE, masterKey, paramSpec);
		byte[] res = cipher.doFinal(bin);
		return new String(res);
	}

	public static String encrypt(String in) throws InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException,
			BadPaddingException {

		if (in == null)
			return null;

		cipher.init(Cipher.ENCRYPT_MODE, masterKey, paramSpec);
		byte[] ret = cipher.doFinal(in.getBytes());

		return new String(Base64.encodeBase64(ret));
	}
}
