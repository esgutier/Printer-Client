package cl.simpleticket.init;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Properties;

import javax.print.PrintService;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import cl.simpleticket.model.Masiva;
import cl.simpleticket.model.Ticket;
import cl.simpleticket.util.ImpresionCortesia;
import cl.simpleticket.util.ImpresionMasiva;
import cl.simpleticket.util.ImpresionTest;
import cl.simpleticket.util.ImprimirNominativa;

public class Principal {

	public static String estadio;
	
	public static void main(String [] args) throws LifecycleException {
		
		Tomcat tomcat = new Tomcat();
		//new Date().setTime(t);
		Connector httpsConnector = new Connector();
	    httpsConnector.setPort(443);
	    httpsConnector.setSecure(true);
	    httpsConnector.setScheme("https");
	      // httpsConnector.setAttribute("keyAlias", keyAlias);
	    httpsConnector.setAttribute("keystorePass", "123456");
	    httpsConnector.setAttribute("keystoreFile", "C:\\sp-printer-client\\myKeystore.p12");
	    httpsConnector.setAttribute("clientAuth", "false");
	    httpsConnector.setAttribute("sslProtocol", "TLS");
	    httpsConnector.setAttribute("SSLEnabled", true);
	    Service service = tomcat.getService();
	    service.addConnector(httpsConnector);
	    Connector defaultConnector = tomcat.getConnector();
	    defaultConnector.setRedirectPort(443);
		
		//Tomcat tomcat = new Tomcat();
        //tomcat.setPort(9090);

        Context ctx = tomcat.addContext("/", new File(".").getAbsolutePath());
        
        //cargar properties files
        Properties prop = new Properties();
        InputStream input = null;
        try {
			input =  new FileInputStream("C:\\sp-printer-client\\conf.properties");
			prop.load(input);			
			estadio = prop.getProperty("estadio");
		} catch (FileNotFoundException e) {
			System.out.println("Properties no encontrado");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
        
        

        Tomcat.addServlet(ctx, "test", new HttpServlet() {
            @Override
            protected void service(HttpServletRequest req, HttpServletResponse resp) 
                    throws ServletException, IOException {
            	resp.addHeader("Access-Control-Allow-Origin", "*"); 
            	ImpresionTest printer = new ImpresionTest();
            	printer.imprimirTest();
            	resp.setContentType("application/json"); 
	   	    	PrintWriter out = resp.getWriter();	         
	   	        out.print("{\"codigo\":\"1\"}"); 

            }
        });
        
        Tomcat.addServlet(ctx, "cortesia", new HttpServlet() {
            @Override
            protected void service(HttpServletRequest req, HttpServletResponse resp) 
                    throws ServletException, IOException {
            	resp.addHeader("Access-Control-Allow-Origin", "*");            	
                Gson gson = new Gson();
                String json = req.getParameter("tickets");
                //System.out.println("json:"+json);
                Type listType = new TypeToken<ArrayList<Ticket>>() {}.getType();
                ArrayList<Ticket> tickets = gson.fromJson(json, listType);
                ImpresionCortesia printer = new ImpresionCortesia();
                PrintService service = printer.obtenerImpresoraService();
                for(Ticket ticket: tickets) {
               	    printer.imprimirTicket(ticket, service, estadio);
                }
	   	    	resp.setContentType("application/json"); 
	   	    	PrintWriter out = resp.getWriter();	         
	   	        out.print("{\"codigo\":\"1\"}");  
             
            }
        });
        
        
        Tomcat.addServlet(ctx, "masivas", new HttpServlet() {
            @Override
            protected void service(HttpServletRequest req, HttpServletResponse resp) 
                    throws ServletException, IOException {
            	resp.addHeader("Access-Control-Allow-Origin", "*"); 
            	Gson gson = new Gson();
                String json = req.getParameter("tickets");
                //System.out.println("json:"+json);
                Masiva masiva = gson.fromJson(json, Masiva.class);
                ImpresionMasiva printer = new ImpresionMasiva();
                PrintService service = printer.obtenerImpresoraService();
                Ticket t = new Ticket();
                Integer secuencia = masiva.getSecuencia();
                for(String s: masiva.getTokens()) {
                	t.setRival(masiva.getRival());
                	t.setFecha(masiva.getFecha());
                	t.setHora(masiva.getHora());
                	t.setSector(masiva.getSector());
                	t.setComentario(masiva.getComentario());
                	t.setToken(s);
                	t.setSecuencia(secuencia);
                	t.setPrecio(masiva.getPrecio());
                	printer.imprimirTicket(t, service, estadio);
                	secuencia++;
                }
                resp.setContentType("application/json"); 
	   	    	PrintWriter out = resp.getWriter();	         
	   	        out.print("{\"codigo\":\"1\"}"); 
             
            }
        });
       

        Tomcat.addServlet(ctx, "nominativa", new HttpServlet() {
            @Override
            protected void service(HttpServletRequest req, HttpServletResponse resp) 
                    throws ServletException, IOException {
            	resp.addHeader("Access-Control-Allow-Origin", "*");            	
                Gson gson = new Gson();
                String json = req.getParameter("tickets");
                //System.out.println("json:"+json);
                Type listType = new TypeToken<ArrayList<Ticket>>() {}.getType();
                ArrayList<Ticket> tickets = gson.fromJson(json, listType);
                
                ImprimirNominativa printer = new ImprimirNominativa();
                PrintService service = printer.obtenerImpresoraService();
                for(Ticket ticket: tickets) {
                	 printer.imprimirTicket(ticket, service, estadio);
                }
	   	    	resp.setContentType("application/json"); 
	   	    	PrintWriter out = resp.getWriter();	         
	   	        out.print("{\"codigo\":\"1\"}");   
            }
        });
        
        

        ctx.addServletMapping("/", "test");
        ctx.addServletMapping("/nominativa", "nominativa");
        ctx.addServletMapping("/masivas", "masivas");
        ctx.addServletMapping("/cortesia", "cortesia");

        tomcat.start();
        tomcat.getServer().await();
	}
}
