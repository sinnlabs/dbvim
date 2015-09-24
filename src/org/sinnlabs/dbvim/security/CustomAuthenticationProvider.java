/**
 * 
 */
package org.sinnlabs.dbvim.security;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.sinnlabs.dbvim.config.ConfigLoader;
import org.sinnlabs.dbvim.model.Role;
import org.sinnlabs.dbvim.model.User;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * @author peter.liverovsky
 *
 */
public class CustomAuthenticationProvider implements AuthenticationProvider, ApplicationContextAware {

	/* (non-Javadoc)
	 * @see org.springframework.security.authentication.AuthenticationProvider#authenticate(org.springframework.security.core.Authentication)
	 */
	@Override
	public Authentication authenticate(Authentication auth)
			throws AuthenticationException {
		String username = auth.getName();
		String password = (String) auth.getCredentials();
		
		try {
			if (LoginProvider.checkCredantials(username, password)) {
				User user = ConfigLoader.getInstance().getUsers().queryForId(username.toLowerCase().trim());
				if (user != null && user.isEnabled()) {
					List<GrantedAuthority> grantedAuths = new ArrayList<>();
					for(Role r : user.getRoles()) {
						grantedAuths.add(new SimpleGrantedAuthority(r.getName()));
					}
					Authentication ret = new UsernamePasswordAuthenticationToken(username, password, grantedAuths);
					return ret;
				}
			}
		} catch (NoSuchAlgorithmException | SQLException | IOException e) {
			System.err.println("ERROR: Unable to check credentials: " + e.getMessage());
			e.printStackTrace();
			throw new AuthenticationServiceException("Unable to check user credantials.", e);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.authentication.AuthenticationProvider#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> arg0) {
		return true;
	}

	@Override
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		try {
			ConfigLoader.initialize(arg0.getResource("/WEB-INF/config.xml").getFile().getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
