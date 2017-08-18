package cl.simpleticket.init;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

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

import cl.simpleticket.model.Ticket;
import cl.simpleticket.util.ImprimirNominativa;

public class Principal {

	public static void main(String [] args) throws LifecycleException {
		System.out.println("test jar!!!");
		System.out.println(new Date().getTime());
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

        Tomcat.addServlet(ctx, "Embedded", new HttpServlet() {
            @Override
            protected void service(HttpServletRequest req, HttpServletResponse resp) 
                    throws ServletException, IOException {
                
                Writer w = resp.getWriter();
                w.write("Embedded Tomcat servlet.\n");
                w.flush();
                w.close();
            }
        });
        
        //ctx = tomcat.addContext("/test", new File(".").getAbsolutePath());

        Tomcat.addServlet(ctx, "nominativa", new HttpServlet() {
            @Override
            protected void service(HttpServletRequest req, HttpServletResponse resp) 
                    throws ServletException, IOException {
            	resp.addHeader("Access-Control-Allow-Origin", "*");
            	
                /*Ticket t = new Ticket();
                t.setRival("ARSENAL F.C.");
                t.setFecha(new Date());
                t.setHora("15:30");
                t.setSector("TRIBUNA ANDES");
                t.setComentario("ENTEL");
                t.setNombres("GABRIEL IGNACIO");
                t.setApellidos("GUTIERREZ BARRIGA");
                t.setPrecio("4500");
                t.setToken("E12345678");*/
                Gson gson = new Gson();
                String json = req.getParameter("tickets");
                System.out.println("json:"+json);
                Type listType = new TypeToken<ArrayList<Ticket>>() {}.getType();
                ArrayList<Ticket> tickets = gson.fromJson(json, listType);
                
                ImprimirNominativa printer = new ImprimirNominativa();
                PrintService service = printer.obtenerImpresoraService();
                for(Ticket ticket: tickets) {
                	 printer.imprimirTicket(ticket, service);
                }
	   	    	resp.setContentType("application/json"); 
	   	    	PrintWriter out = resp.getWriter();	         
	   	        out.print("{\"codigo\":\"1\"}");   
            }
        });
        
        

        ctx.addServletMapping("/", "Embedded");
        ctx.addServletMapping("/nominativa", "nominativa");

        tomcat.start();
        tomcat.getServer().await();
	}
}
