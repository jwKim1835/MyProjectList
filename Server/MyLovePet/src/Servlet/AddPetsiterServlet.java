package Servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import Bean.PetSitterBean;

public class AddPetsiterServlet extends HttpServlet {
	
	private Context context;
	private DataSource dataSource;
	
	public void init( ServletConfig config ) throws ServletException
	{
		try {
			context = new InitialContext();
			dataSource = (DataSource)context.lookup( "java:comp/env/jdbc/OurCompany" );
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void destroy()
	{
		try {
			context.close();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		doPost( request, response );
	}
	
	
	@SuppressWarnings("unchecked")
	public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		PetSitterBean petBean = new PetSitterBean();
		HttpSession session = request.getSession();
		PrintWriter out = response.getWriter();
		String petList = request.getParameter( "petList" );
		
		petBean.setTitle( request.getParameter( "Title" ) );
		petBean.setDate( request.getParameter( "Date" ) );
		petBean.setTerm( request.getParameter( "Term" ) );
		petBean.setFeedback( request.getParameter( "Feedback" ) );
		petBean.setUserID( (String)session.getAttribute( "id" ) );
		
		
		Connection conn = connectDataBase();
		if( conn == null )
			throw new ServletException( "Connection Error" );
		
		JSONObject outer = new JSONObject();
		JSONObject inner = new JSONObject();
		
		if( insertPetSitterData( conn, petBean ) && addPetList( conn, petList ) )
		{
			inner.put( "isSuccessed", true );
			outer.put( "AddPetSitter", inner );
		}
		else
		{
			inner.put( "isSuccessed", false );
			outer.put( "AddPetSitter", inner );
		}
		
		out.write( outer.toJSONString() );
		
		disconnectDatabase( conn );
	}
	
	private Connection connectDataBase()
	{
		Connection conn = null;
		
		try {
			conn = dataSource.getConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return null;
		}
		
		return conn;
	}
	
	private void disconnectDatabase( Connection conn )
	{
		if( conn == null )
			return ;
		
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean insertPetSitterData( Connection conn, PetSitterBean bean )
	{
		PreparedStatement pstmt = null;
		String query = "insert "
				+ "into "
				+ "PetSitter ( userid, title, date, term, feedback )"
				+ "values( ?, ?, ?, ?, ? )";
		
		if( conn == null )
			return false;
		
		try {
			pstmt = conn.prepareStatement( query );
			pstmt.setString( 1, bean.getUserID() );
			pstmt.setString( 2, bean.getTitle() );
			pstmt.setString( 3, bean.getDate() );
			pstmt.setString( 4, bean.getTerm() );
			pstmt.setString( 5, bean.getFeedback() );
			
			if( pstmt.executeUpdate() < 0 )
				return false;
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		finally
		{
			try {
				if( pstmt != null )
					pstmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
	@SuppressWarnings("unused")
	private boolean addPetList( Connection conn, String petList )
	{
		JSONParser parser = new JSONParser();
		JSONArray arry = null;
		String query = "insert "
				+ "into "
				+ "PetList"
				+ "values( LAST_INSERT_ID(), ? )";
		
		try {
			arry = (JSONArray)parser.parse( petList );			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		try {
			for( int i = 0; i < arry.size(); i++ )
			{
				PreparedStatement pstmt = conn.prepareStatement( query );
				pstmt.setInt( 1, (int)arry.get( i ) );
				
				int result = pstmt.executeUpdate();				
				pstmt.close();
				
				if( result < 0 )
					return false;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return false;
		}
		
		
		return true;
	}
}
