package org.sinnlabs.dbvim.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.sinnlabs.dbvim.config.ConfigLoader;
import org.sinnlabs.dbvim.model.StaticResource;

/**
 * Servlet implementation class ResourceLoader
 */
@WebServlet("/resource/*")
public class ResourceLoader extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ResourceLoader() {
        super();
    }
    
    public void init(ServletConfig config) {
    	try {
			ConfigLoader.initialize(config.getServletContext());
		} catch (IOException e) {
			System.out.println("Unable to initialize dbvim.");
			e.printStackTrace();
		}
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getPathInfo();
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		try {
			StaticResource resource = ConfigLoader.getInstance().getStaticResources().queryForId(path);
			if (resource == null) {
				System.err.println("ResourceLoader: Requested resource does not exists: " + path);
				response.sendError(404, "Requested resource does not exists.");
			} else {
				if (!StringUtils.isEmpty(resource.getContentType()))
					response.setContentType(resource.getContentType());
				response.getOutputStream().write(resource.getData());
			}
		} catch (SQLException e) {
			throw new ServletException("SQL Error while getting requested resource.", e);
		}
	}

}
