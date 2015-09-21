/**
 * 
 */
package org.dbvim.dbuibuilder.security;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.dbvim.dbuibuilder.config.ConfigLoader;
import org.dbvim.dbuibuilder.model.User;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * Class manages operations with users (create, password check)
 * 
 * @author peter.liverovsky
 *
 */
public class LoginProvider {

	/**
	 * Check the user credentials
	 * @param username User name to be checked
	 * @param passwd Password to be checked
	 * @return True if credentials is valid.
	 * @throws SQLException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public static boolean checkCredantials(String username, String passwd)
			throws SQLException, IOException, NoSuchAlgorithmException {
		User usr = ConfigLoader.getInstance().getUsers()
				.queryForId(username.toLowerCase());
		// user does not exist
		if (usr == null)
			return false;

		// password does not exist
		if (usr.getPassword_hash() == null || usr.getSalt() == null)
			return false;

		byte[] bDigest = base64ToByte(usr.getPassword_hash());
		byte[] bSalt = base64ToByte(usr.getSalt());

		// Compute new digest
		byte[] proposedDigest = getHash(passwd, bSalt);

		return Arrays.equals(proposedDigest, bDigest);
	}

	/**
	 * From a password and a salt, returns the corresponding digest
	 * 
	 * @param password
	 *            password to be hashed
	 * @param salt
	 *            password salt
	 * @return byte[] Digested password
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] getHash(String password, byte[] salt)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		digest.reset();
		// Add password bytes to digest
		digest.update(salt);
		// Get the hash's bytes
		byte[] hash = digest.digest(password.getBytes("UTF-8"));

		return hash;
	}

	/**
	 * From a base 64 representation, returns the corresponding byte[]
	 * 
	 * @param data
	 *            String The base64 representation
	 * @return byte[]
	 * @throws IOException
	 */
	public static byte[] base64ToByte(String data) throws IOException {
		BASE64Decoder decoder = new BASE64Decoder();
		return decoder.decodeBuffer(data);
	}

	/**
	 * From a byte[] returns a base 64 representation
	 * 
	 * @param data
	 *            byte[]
	 * @return String
	 * @throws IOException
	 */
	public static String byteToBase64(byte[] data) {
		BASE64Encoder endecoder = new BASE64Encoder();
		return endecoder.encode(data);
	}

	/**
	 * Creates new user
	 * @param login User name
	 * @param passwd User password
	 * @return null if user already exists, otherwise new User object.
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public static User createUser(String login, String passwd)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		if (StringUtils.isBlank(login) || StringUtils.isBlank(passwd)) {
			throw new IllegalArgumentException("User name or password can not be empty.");
		}

		// uses a secure random
		SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");

		// Salt generation 512 bits long
		byte[] bSalt = new byte[64];
		rand.nextBytes(bSalt);

		byte[] hash = getHash(passwd, bSalt);

		User usr = new User(login.toLowerCase().trim());
		usr.setPassword_hash(byteToBase64(hash));
		usr.setSalt(byteToBase64(bSalt));
		usr.setEnabled(true);

		return usr;
	}
	
	public static User updatePassword(User user, String newPassword) 
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		
		if (StringUtils.isBlank(newPassword)) {
			throw new IllegalArgumentException("User name or password can not be empty.");
		}

		// uses a secure random
		SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");

		// Salt generation 512 bits long
		byte[] bSalt = new byte[64];
		rand.nextBytes(bSalt);

		byte[] hash = getHash(newPassword, bSalt);

		user.setPassword_hash(byteToBase64(hash));
		user.setSalt(byteToBase64(bSalt));

		return user;
	}
}