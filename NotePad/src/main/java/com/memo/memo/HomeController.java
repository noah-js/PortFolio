package com.memo.memo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.sqlite.SQLiteConfig;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {
	
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String login(Locale locale, Model model) {
		logger.info("Welcome home! The client locale is {}.", locale);
		
		return "loginsample";
	}
	
	@RequestMapping(value = "/loginsample", method = RequestMethod.GET)
	public String loginsample(Locale locale, Model model) {
		return "login";
	}
	
	@RequestMapping(value = "/join", method = RequestMethod.GET)
	public String join(Locale locale, Model model) {
		return "join";
	}
	
	//세션 이용 HttpServletRequest Request
	@RequestMapping(value = "/input_password", method = RequestMethod.POST)
	public String password(Locale locale, Model model,@RequestParam("id") String id,@RequestParam("password") String password,HttpServletRequest request) {
		
		if(password.equals("1234")) {
			//세션 설정
			HttpSession session = request.getSession();
			session.setAttribute("is_login", true);
			return "redirect:/select";
		}
		
		return "redirect:/";
	}
	

	@RequestMapping(value = "/select", method = RequestMethod.GET)
	public String home(Locale locale, Model model,HttpServletRequest request) {
		//위 세션 받아서 진행, 로그인 무조건 해야한다면 밑에 코드 넣기
		HttpSession session = request.getSession();
		Boolean isLogin = (Boolean) session.getAttribute("is_login");
		if(isLogin == null || !isLogin) {
			return "redirect:/";
		}
		return "select";
	}

	
	@RequestMapping(value = "/memo_list", method = RequestMethod.GET)
	public @ResponseBody ArrayList<HashMap> memoList(Locale locale, Model model) {
		Connection connection = null;
		ArrayList<HashMap> data = new ArrayList<HashMap>();
		try {
			Class.forName("org.sqlite.JDBC");
			SQLiteConfig config = new SQLiteConfig();
			connection = DriverManager.getConnection("jdbc:sqlite:/c:\\tomcat\\memo.db", config.toProperties());

			String query = "SELECT * FROM posts WHERE 1"; // name LIKE '%" + name + "%'
			PreparedStatement preparedStatement = connection.prepareStatement(query);
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				int idx = resultSet.getInt("idx");
				String text = resultSet.getString("text");
				HashMap<String, String> row = new HashMap<String, String>();
				row.put("idx", "" + idx);
				row.put("text", text);
				data.add(row);
			}
			preparedStatement.close();
			connection.close();
		} catch (Exception e) {

		}
		return data;
	}
	
	@RequestMapping(value = "/insert", method = RequestMethod.GET)
	public String insert(Locale locale, Model model) {
		logger.info("Welcome home! The client locale is {}.", locale);
		
		return "insert";
	}
	
	@RequestMapping(value = "/memo_insert", method = RequestMethod.GET)
	public @ResponseBody HashMap<String, String> memoInsert(Locale locale, Model model, @RequestParam String text
			) {
		Connection connection = null;
		HashMap<String, String> result = new HashMap<String, String>();

		try {
			Class.forName("org.sqlite.JDBC");
			SQLiteConfig config = new SQLiteConfig();
			connection = DriverManager.getConnection("jdbc:sqlite:/c:\\tomcat\\memo.db", config.toProperties());

			String query = "INSERT INTO posts (text) VALUES('" + text +"')";
			System.out.println(query);
			Statement statement = connection.createStatement();
			int q = statement.executeUpdate(query);

			statement.close();
			connection.close();

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		result.put("result", "success");

		return result;
		// 예외처리 파라미터값, 쿼리문 리턴값 따로 만들기
	}
	
	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	public String delete(Locale locale, Model model) {
		return "delete";
	}
	
	@RequestMapping(value = "/memo_delete", method = RequestMethod.GET)
	public @ResponseBody HashMap<String, String> memoDelete(Locale locale, Model model, @RequestParam String idx
			) {
		Connection connection = null;
		HashMap<String, String> result = new HashMap<String, String>();

		try {
			Class.forName("org.sqlite.JDBC");
			SQLiteConfig config = new SQLiteConfig();
			connection = DriverManager.getConnection("jdbc:sqlite:/c:\\tomcat\\memo.db", config.toProperties());

			String query = "DELETE FROM posts WHERE idx = " + idx;
			System.out.println(query);
			Statement statement = connection.createStatement();
			int q = statement.executeUpdate(query);

			statement.close();
			connection.close();

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		result.put("result", "success");

		return result;
		// 예외처리 파라미터값, 쿼리문 리턴값 따로 만들기
	}
	
	@RequestMapping(value = "/details", method = RequestMethod.GET)
	public String details(Locale locale, Model model) {
		return "details";
	}
	
	@RequestMapping(value = "/posts_details", method = RequestMethod.GET)
	public @ResponseBody HashMap<String, String> postsDetails(Locale locale, Model model, @RequestParam String idx) {
		Connection connection = null;
		HashMap<String, String> data = new HashMap<String, String>();
		try {
			Class.forName("org.sqlite.JDBC");
			SQLiteConfig config = new SQLiteConfig();
			connection = DriverManager.getConnection("jdbc:sqlite:/c:\\tomcat\\memo.db", config.toProperties());

			String query = "SELECT * FROM posts WHERE idx=" + idx;
			PreparedStatement preparedStatement = connection.prepareStatement(query);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				String text = resultSet.getString("text");
				data.put("text", text);
			}
			preparedStatement.close();
			connection.close();
		} catch (Exception e) {

		}
		return data;
	}
	
	@RequestMapping(value = "/posts_mod", method = RequestMethod.GET)
	public @ResponseBody HashMap<String, String> postMod(Locale locale, Model model, @RequestParam String idx,
			@RequestParam String text) {
		Connection connection = null;
		HashMap<String, String> result = new HashMap<String, String>();

		try {
			Class.forName("org.sqlite.JDBC");
			SQLiteConfig config = new SQLiteConfig();
			connection = DriverManager.getConnection("jdbc:sqlite:/c:\\tomcat\\memo.db", config.toProperties());

			String query = "UPDATE posts SET text='" + text + "' WHERE idx=" + idx;
			Statement statement = connection.createStatement();
			int q = statement.executeUpdate(query);

			statement.close();
			connection.close();

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		result.put("result", "success");

		return result;
		// 예외처리 파라미터값, 쿼리문 리턴값 따로 만들기
	}
	
	@RequestMapping(value = "/details2", method = RequestMethod.GET)
	public String details2(Locale locale, Model model,@RequestParam String idx) {
		model.addAttribute("idx_string", idx);
		return "details2";
	}
	
	
	
}
