import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ChatClient implements ActionListener
{
	// This will hold all of the users whom are online, and display them to other users
    JList<String> onlineUsers = new JList<String>();
	
	//--- (I/O)  variables
	private String _userName = "";
    BufferedReader in;
    PrintWriter out;

    //GUI variables
    JFrame frame = new JFrame("Spider Room");
    JTextField textField = new JTextField(0);
    JTextArea globalChat = new JTextArea(8, 40);
    
    ChatClient() 
    {
    	onlineUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Users may only select one other user at a time.
        globalChat.setLineWrap(true);
        globalChat.setWrapStyleWord(true);
        
      //********** Menu Bar ***************\\
        
        //File Menu
        JMenuBar mb = new JMenuBar();
        mb.setBackground(Color.BLACK);
        frame.setJMenuBar(mb);
        
        JMenu fileMenu = new JMenu("File");
        fileMenu.addActionListener(this);
        fileMenu.setForeground(Color.WHITE);
        mb.add(fileMenu);
        
        JMenuItem miAbout = new JMenuItem("About");
        miAbout.addActionListener(this);
        miAbout.setBackground(Color.BLACK);
        miAbout.setForeground(Color.WHITE);
        fileMenu.add(miAbout);
        
        JMenuItem miExit = new JMenuItem("Exit");
        miExit.addActionListener(this);
        miExit.setBackground(Color.BLACK);
        miExit.setForeground(Color.WHITE);
        fileMenu.add(miExit);
        
        // Friends menu
        JMenu friends = new JMenu("Friends List");
        friends.addActionListener(this);
        friends.setForeground(Color.WHITE);
        mb.add(friends);
        
        JMenuItem miFriend = new JMenuItem("Working on it!");
        miFriend.addActionListener(this);
        miFriend.setBackground(Color.BLACK);
        miFriend.setForeground(Color.WHITE);
        friends.add(miFriend);
        
        JMenu user = new JMenu("You are Curretnly online as: " + _userName);
        user.setForeground(Color.YELLOW);
        user.setEnabled(false);
        mb.add(user);
        
      //**********       GUI Components for the Client Window      ***************\\  
        frame.getContentPane().setLayout(new GridBagLayout());
    	
    	GridBagConstraints constraint = new GridBagConstraints();
    	constraint.anchor = GridBagConstraints.CENTER;
        constraint.fill = GridBagConstraints.BOTH;
        
    	// Layout GUI
        textField.setEditable(false);
        globalChat.setEditable(false);
        
        constraint.insets.set(5, 2, 2, 3); //top left bottom right
        constraint.gridx = 0;
        constraint.gridy = 0;
        constraint.gridheight = GridBagConstraints.REMAINDER;
        frame.getContentPane().add(onlineUsers,constraint);
        
        constraint.insets.set(5, 0, 2, 3);
        constraint.gridx = 1;
        constraint.gridy = 0;
        constraint.gridheight = 2;
    	constraint.weightx = .5;
    	constraint.weighty = 0;
        frame.add(textField,constraint);
        
        constraint.insets.set(0, 0, 5, 5);
        constraint.gridx = 1;
        constraint.gridy = 2;
    	constraint.weightx = .5;
    	constraint.weighty = 1;
        frame.add(new JScrollPane(globalChat),constraint);
        frame.pack();
        frame.setLocationRelativeTo(null);

        // Add Listeners
        textField.addActionListener(new ActionListener() 
        {
            /**
             * Responds to pressing the enter key in the textfield by sending
             * the contents of the text field to the server. Then clear
             * the text area in preparation for the next message.
             */
            public void actionPerformed(ActionEvent e) 
            {
            	String output = textField.getText();
            	if (output.toLowerCase().startsWith("/pm"))
            	{
            		output = "PRIVATE_MESSAGE" + output.substring(4);
            		out.println(output);
            	}
            	else
                {
            		out.println(textField.getText());                
                }
            	onlineUsers.clearSelection();
                textField.setText("");
            }
        });
        
        //Listens for selection of user's
        onlineUsers.addListSelectionListener(new ListSelectionListener() { 
            @Override
            public void valueChanged(ListSelectionEvent e) 
            { 
            	String selection = onlineUsers.getSelectedValue();
            	
        		if (!(e.getValueIsAdjusting())) // put everything in here or you will get duplicates.
            	{
            		textField.setText("/pm " + onlineUsers.getSelectedValue());
            	}
            	textField.setText("/pm " + selection + " ");
            	textField.requestFocus();
            } 
        });   
    }

    /**
     * Prompt for and return the address of the server.
     */
    private String getServerAddress() 
    {
        //return JOptionPane.showInputDialog(frame, "Enter IP Address of the Server:", "Welcome to the Chatter", 
        //		JOptionPane.QUESTION_MESSAGE);
    	return "localhost";
    }

    /**
     * Prompt for and return the desired screen name.
     */
    private void getName() 
    {
		_userName = JOptionPane.showInputDialog(frame,"Enter your username: ", "Username", JOptionPane.QUESTION_MESSAGE);
		
		if(_userName.contains(" ")) {
			JOptionPane.showMessageDialog(null, "Name cannot contain space\'s");
			getName();
		}
		else{
			out.println("NEWUSER" +	_userName );
		}
    }


    /**
     * Connects to the server then enters the processing loop.
     */
    private void run() 
    {
    	try
    	{
	        // Make connection and initialize streams
	        String serverAddress = getServerAddress();
	        Socket socket = new Socket(serverAddress, 9001);
	        in = new BufferedReader(new InputStreamReader(
	            socket.getInputStream()));
	        out = new PrintWriter(socket.getOutputStream(), true);
	
	        // Process all messages from server, according to the protocol.
	        while (true) 
	        {
	            String line = in.readLine();
	            if (line.startsWith("SUBMITNAME")) 
	            {
	            	getName();
	            } 
	            else if (line.startsWith("EXISTS"))
	            {
	            	JOptionPane.showMessageDialog(frame, "Username already Exists");
	            	getName();
	            }
	            else if(line.startsWith("SUBMITNAME_ERROR"))
	            {
	            	JOptionPane.showMessageDialog(frame, "Username can't be blank and must not have any spaces! Please try again",
	            			"Error Message", JOptionPane.INFORMATION_MESSAGE);
	            	getName();
	            }
	            else if (line.startsWith("NAMEACCEPTED")) 
	            {
	            	frame.setTitle(_userName + "'s - Spider Chat Application");
	                textField.setEditable(true);
	                frame.getJMenuBar().getMenu(2).setText("You are online as: "+_userName);
	            } 
	            else if (line.startsWith("MESSAGE")) 
	            {
	                globalChat.append(line.substring(8) + "\n");
	            }
	            else if (line.startsWith("PRIVATE")) 
	            {
	                globalChat.append(line.substring(8));
	            }
	            else if(line.startsWith("SOMELEFT"))
	            {
	            	globalChat.append(line.substring(9) + "\n");
	            }
	            else
	            if(line.startsWith("UPDATE_ONLINE"))
	            {   
	            	// Set the List of online users for this client
	            	line = line.replace("UPDATE_ONLINE", "  Global Chat  ");
	            	line = line.replace(_userName, "");
	            	String[] toBePrinted = line.split(",");
	            	onlineUsers.setListData(toBePrinted);
	            	
	            	//Add Message Frames for each user.

	            	
	            }
	            else
	            {
	            	socket.close();
	            }
	            globalChat.setCaretPosition(globalChat.getDocument().getLength());
	        }
    	}
    	catch (IOException e)
    	{
    		System.out.println("Error occured: " + e.getMessage());
    		System.exit(0);
    	}
    }


    /**
     * Runs the client as an application with a close-able frame.
     */
    public static void main(String[] args) throws Exception 
    {
        ChatClient client = new ChatClient();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.getContentPane().setBackground(Color.DARK_GRAY);
        client.frame.setVisible(true);
        client.run();
    }

    
    // This will catch an actions performed with the menu's
	@Override
	public void actionPerformed(ActionEvent e) 
	{
		String command = e.getActionCommand();
		
		if (command.equals("Exit"))
		{
			System.exit(0);
		}
	}
}