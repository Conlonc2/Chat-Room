import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JTextArea;

public class ChatServer
{
	//private static Encryption coder = new Encryption();

    private static final int PORT = 9001; //The port that the server listens on.

    /*
     * List of all the users online and a link to there PrintWriter
     * by referencing the user by name.
     */
    private static HashMap<String, PrintWriter> online = new HashMap<String, PrintWriter>();

    /**
     * A handler thread class.  Handlers are spawned from the listening
     * loop and are responsible for a dealing with a single client
     * and broadcasting its messages.
     **/
    private static class Handler extends Thread 
    {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private JTextArea logs;
        

        /**
         * Constructs a handler thread, squaring away the socket.
         */
        public Handler(Socket socket, JTextArea logs) 
        {
            this.socket = socket;
            this.logs = logs;
        }
        /**
         * Services this thread's client by repeatedly requesting a
         * screen name until a unique one has been submitted, then
         * acknowledges the name and registers the output stream for
         * the client in a global set, then repeatedly gets inputs and
         * broadcasts them.
         */
        public void run()
        {
        	boolean needsUpdate = false;
        	
            try 
            {

                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Request a name from this client.  Keep requesting until
                // a name is submitted that is not already used.
                while (true) 
                {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    name = name.substring(7);
                    if (name == null || name.contains(" ")) 
                    {
                    	out.println("SUBMITNAME_ERROR");
                    }
	                    synchronized (online)
	                    {
	                        if (!online.keySet().contains(name)) 
	                        {
	                            online.put(name, out);
	                            needsUpdate = true;
	                            needsUpdate = updateOnline(needsUpdate);
	                            break;
	                        }
	                    }
                   }
                // Now that a successful name has been chosen and PrintWriter added
                // Broadcast a welcome Message to the individual.
                online.get(name).println("NAMEACCEPTED");
                online.get(name).println("MESSAGE" + " Welcome to the Chat " + name + "!");
                Date date = new Date();
                
                logs.append(name + " has logged in @ ("+ date +")\n");
                online.get(name).println("MESSAGE " + "Server: To send Private Messages please type "
                		+ "/PM, and then the username of the person you are trying to reach, then your message.");
                

                // Accept messages from this client and broadcast them.
                while (true) 
                {
                    String input = in.readLine();
                    if (input == null) 
                    {
                        return;
                    }
                    if (input.startsWith("PRIVATE_MESSAGE")) // Used to send private messages between clients.
                    {
                    	try
                    	{
                    		String user = "";
                    		if (input.substring(15).contains(" "))
                    		{
                    			user = input.substring(15, input.indexOf(" "));
                    		}
                    		else
                    		{
                    			online.get(name).println("MESSAGE " + "Server: No Message to be Displayed");
                    			continue;
                    		}
                    		
                           	if(!(online.containsKey(user)))
                        	{
                        		online.get(name).println("MESSAGE " + "Server: Invalid Username");
                        	}
                        	else
                        	{
                        		online.get(user).println("MESSAGE " + "Alert: You Have Recieved a Private Message From " + name);
                        		online.get(user).println("PRIVATE " + "From "+ name + ": " + input.substring(input.indexOf(" ")+1));
                        		
                        		online.get(name).println("MESSAGE " + "You've Sent a Private Message to " + user);
                        		online.get(name).println("PRIVATE " + "To " +user+": " + input.substring(input.indexOf(" ")+1));
                        	}
                           	
                           	logs.append(name + " sent -> "+ user +": " + input.substring(input.indexOf(" ")+1) +"\n");
                    	}
                    	catch(StringIndexOutOfBoundsException e)
                    	{
                    		continue;
                    	}
                    }
                    else
                    {
	                    for (String s : online.keySet()) 
	                    {
	                        online.get(s).println("MESSAGE " + name + ": " + input);
	                    }
	                    logs.append(name + " sent -> Global: " + input+"\n");
                    }
                }
            } 
            catch (IOException e) 
            {
                System.out.println(e.toString());
            } 
            finally 
            {
                // This client is going down!  Remove its name and its print
                // writer from the sets, and close its socket.
                if (name != null) 
                {
                	for (String s : online.keySet()) 
                	{
                        online.get(s).println("SOMELEFT " + name + " has left the chat room.");
                        Date date = new Date();
                        logs.append(name + " has logged out@ ("+ date +")\n");
                    }
                    online.remove(name);
                    needsUpdate = true;
                    needsUpdate = updateOnline(needsUpdate);
                }
                try 
                {
                    socket.close();
                } 
                catch (IOException e) 
                {
                	System.out.println(e);
                }
            }
        }
    }
    public static boolean updateOnline(boolean update)
    {
        //Updates the "Online User" List every time some one is added or removed
    	String names = "";
    	for (String s : online.keySet())
    	{
    		names += "," + s;
    	}
    	for (String s : online.keySet())
    	{
    		online.get(s).println("UPDATE_ONLINE"+names);
    	}
    	update = false;
    	return update;
    }
    
    public static void main(String[] args) throws Exception 
    {
        JFrame frame = new JFrame("Server");
        JTextArea logs = new JTextArea(8,40);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        logs.setEditable(false);
        frame.add(logs);
        frame.setVisible(true);
        
        try (ServerSocket listener = new ServerSocket(PORT))
        {
        	logs.append("Server Open\n");
            while (true) 
            {
                new Handler(listener.accept(), logs).start();
            }
        }
        finally 
        {
            System.out.println("Server Closed");
        }
    }
}