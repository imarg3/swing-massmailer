package org.code.utils;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class UserLookup extends JPanel implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3382028543425727976L;

	static private final String newline = "\n";
	
	private JButton openButton, saveButton;	
	private JTextArea log;	
	private JFileChooser fileToUpload; 
	
	public UserLookup() {
		super(new BorderLayout());
		initComponents();
	}
	
	private void initComponents(){
		
		
		//Create the log first, because the action listeners need to refer to it.
	    log = new JTextArea(5, 20);
	    log.setText("Welcome to Usernamer Lookup & MX record Checker. Instructions to use this tool are as follows :"+newline+newline
	    		+ "1. Choose valid file containing Email IDs"+newline+newline+"2. Save valid email id list file on the file system");
	    log.setFont(log.getFont().deriveFont(18f));
	    log.setMargin(new Insets(5, 5, 5, 5));
	    log.setEditable(false);
	    JScrollPane logScrollPane = new JScrollPane(log);
	    
	    // Create a File Chooser
	    fileToUpload = new JFileChooser();
	    
	    //Uncomment one of the following lines to try a different file selection mode. The first allows just directories
	    //to be selected (and, at least in the Java look and feel, shown). The second allows both files and directories
	    //to be selected. If you leave these lines commented out, then the default mode (FILES_ONLY) will be used.
	  
	    //fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    //fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

	    //Create the open button. We use the image from the JLF
	    //Graphics Repository (but we extracted it from the jar).
	    openButton = new JButton("Open a File...", createImageIcon("images/Open24.gif"));
	    openButton.addActionListener(this);
	    
	    //Create the save button. We use the image from the JLF
	    //Graphics Repository (but we extracted it from the jar).
	    saveButton = new JButton("Save a File...",
	        createImageIcon("images/Save24.gif"));
	    saveButton.addActionListener(this);
	    
	    //For layout purposes, put the buttons in a separate panel
	    JPanel buttonPanel = new JPanel(); // use Flowlayout
	    buttonPanel.add(openButton);
	    buttonPanel.add(saveButton);
	    
	    //Add the buttons and the log to this panel.
	    add(buttonPanel, BorderLayout.PAGE_START);
	    add(logScrollPane, BorderLayout.CENTER);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		//Handle open button action.
				if(e.getSource() == openButton){
					FileFilter filter = new FileNameExtensionFilter("*.txt", new String[] { "txt" });
					fileToUpload.addChoosableFileFilter(filter);
					int returnVal = fileToUpload.showOpenDialog(UserLookup.this);
					Set<String> emailIDSet = null;
					
					if(returnVal == JFileChooser.APPROVE_OPTION){
						File file = fileToUpload.getSelectedFile();
						
						try(BufferedReader br = new BufferedReader(new FileReader(file));
							  BufferedWriter bw = new BufferedWriter(new FileWriter("E:\\testmx.txt"));
						){				
							
							//This is where a real application would open the file.				
							String emailID = null;
							String prefix = "";
							
							emailIDSet = new LinkedHashSet<String>();
							log.setText("");
							log.append("Opening File: " + file.getName() + "." + newline);
							
							while((emailID = br.readLine()) != null){
								log.append(emailID + newline);
								emailIDSet.add(emailID);								
							}
														
							Iterator<String> itr = emailIDSet.iterator();
							// int mxSize;
							boolean isAddressValid = false;
							String emailIDValue = null;
							String[] domainName = null;
							
							System.out.println("Before next");
							while(itr.hasNext()){
								emailIDValue = itr.next();
								domainName = emailIDValue.split("@");
								isAddressValid = doLookup(emailIDValue, domainName[1]);
								System.out.println(emailIDValue +" validity status is "+isAddressValid);
								
								if(isAddressValid){
									bw.write(prefix);
									prefix = newline;
									bw.write(emailIDValue);
								}else{
									System.err.println(emailIDValue + " does not exist. Lookup failed !!!");
								}
							}
							
							// log.append("File has been written !!!");
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
					} else {
				        log.append("Open command cancelled by user." + newline);
				    }
					
				    log.setCaretPosition(log.getDocument().getLength());

				  //Handle save button action.
			    } else if (e.getSource() == saveButton) {
			      int returnVal = fileToUpload.showSaveDialog(this);
			      Set<String> emailIDSet = null;
			      
			      if (returnVal == JFileChooser.APPROVE_OPTION) {
			        File file = fileToUpload.getSelectedFile();
			        
			        try(BufferedReader br = new BufferedReader(new FileReader("E:\\resultFile.txt"));
							  BufferedWriter bw = new BufferedWriter(new FileWriter(file));
						){
			        	//This is where a real application would open the file.				
						String emailID = null;
						String prefix = "";
						
						emailIDSet = new HashSet<String>();
						log.append("Opening: " + file.getName() + "." + newline);
						
						while((emailID = br.readLine()) != null){
							// log.append(emailID + newline);
							emailIDSet.add(emailID);
						}
						
						Iterator<String> itr = emailIDSet.iterator();
											
						while(itr.hasNext()){						
							bw.write(prefix);
							prefix = newline;
							bw.write(itr.next());
						}
						
						log.setText("");
						log.append("Your file has been saved in the path provided. Please note that this file do not contain Duplicate Email ID");
			        	
			        } catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			       
				    log.append("Saving: " + file.getName() + "." + newline);     	        
			      } else {
			        log.append("Save command cancelled by user." + newline);
			      }
			      log.setCaretPosition(log.getDocument().getLength());
			    }
		
	}
	
	/** Returns an ImageIcon, or null if the path was invalid. */
	protected static ImageIcon createImageIcon(String path){
		URL imageUrl = MailServerChecker.class.getResource(path);
		
		if(imageUrl != null){
			return new ImageIcon(imageUrl);
		}else{
			System.err.println("Couldn't find file: " + path);
		     return null;
		}		
	}
	
	protected static boolean doLookup( String emailID, String domainName ) {
		System.out.println("Lookup "+domainName);
	    Hashtable<String, String> env = new Hashtable<>();
	    env.put("java.naming.factory.initial",
	            "com.sun.jndi.dns.DnsContextFactory");
	    
	    DirContext ictx;
		try {
			ictx = new InitialDirContext( env );
			
			Attributes attrs = ictx.getAttributes( domainName, new String[] { "MX" });
		    Attribute attr = attrs.get( "MX" );
		    System.out.println("Attr "+attr);
		    
		 // Just because we can send mail to the domain, doesn't mean that the
		      // address is valid, but if we can't, it's a sure sign that it isn't
		      if ( attr.size() == 0 ) return false;

		      // Now, do the SMTP validation, try each mail exchanger until we get
		      // a positive acceptance. It *MAY* be possible for one MX to allow
		      // a message [store and forwarder for example] and another [like
		      // the actual mail server] to reject it. This is why we REALLY ought
		      // to take the preference into account.
		      String value = null;
		      String mxvalues[];
		      for ( int mx = 0 ; mx < attr.size() ; mx++ ) {
		          boolean valid = false;
		          try {
		              int res;
		              value = (String) attr.get( mx );
		              mxvalues = value.split(" ");		              
		              System.out.println(mxvalues[0]+", "+mxvalues[1]);
		              System.out.println("MX Value is "+attr.get(mx));
		              Socket skt = new Socket( "smtp.cdac.in", 587 );
		              System.out.println(skt);
		              BufferedReader rdr = new BufferedReader
		                 ( new InputStreamReader( skt.getInputStream() ) );
		              BufferedWriter wtr = new BufferedWriter
		                 ( new OutputStreamWriter( skt.getOutputStream() ) );

		              res = hear( rdr );
		              System.out.println("Response is "+res);
		              if ( res != 220 ) throw new Exception( "Invalid header" );
		              say( wtr, "EHLO cdac.in" );
		              
		              res = hear( rdr );
		              System.out.println("Response is "+res);
		              if ( res != 250 ) throw new Exception( "Not ESMTP" );
		              say( wtr, "AUTH LOGIN" );
		              
		              res = hear( rdr );
		              System.out.println("Response is "+res);
		              if ( res != 334 ) throw new Exception( "Auth not enabled" );
		              else{
		            	  say( wtr, "Z2FycGl0" );
		              }
		              
		              res = hear( rdr );
		              System.out.println("Response is "+res);
		              if ( res != 334 ) throw new Exception( "Auth not enabled" );
		              else{
		            	  say( wtr, "Y2RAYyRwdW5lMzk=" );
		              }
		              
		              res = hear( rdr );
		              System.out.println("Response is "+res);
		              if ( res != 235 ) throw new Exception( "Authentication failed !!!" );
		              
		              // validate the sender address  
		              say( wtr, "MAIL FROM: <garpit@cdac.in>" );
		              res = hear( rdr );
		              System.out.println("Response is "+res);
		              if ( res != 250 ) throw new Exception( "Sender rejected" );
		              System.out.println(emailID);
		              say( wtr, "RCPT TO: <" + emailID + ">" );
		              res = hear( rdr );
		              System.out.println("Response is "+res);

		              // be polite
		              say( wtr, "RSET" ); hear( rdr );
		              say( wtr, "QUIT" ); hear( rdr );
		              if ( res != 250 ) 
		                 throw new Exception( "Address is not valid!" );

		              valid = true;
		              rdr.close();
		              wtr.close();
		              skt.close();
		          } 
		          catch (Exception ex) {
		            // Do nothing but try next host
		          } 
		          finally {
		            if ( valid ) return true;
		          }
		      }
		      return false;
		 }catch(NamingException e){
			 e.printStackTrace();
			 return false;
		 }
	}
		    		   		
	
private static int hear( BufferedReader in ) throws IOException {
    String line = null;
    int res = 0;

    while ( (line = in.readLine()) != null ) {
        String pfx = line.substring( 0, 3 );
        try {
           res = Integer.parseInt( pfx );
        }
        catch (Exception ex) {
           res = -1;
        }
        if ( line.charAt( 3 ) != '-' ) break;
    }

    return res;
    }
	
		private static void say( BufferedWriter wr, String text )
	      throws IOException {
	     wr.write( text + "\r\n" );
	     wr.flush();
	     
	     return;
	     }

	/**
	  * Create the GUI and show it. For thread safety, this method should be
	  * invoked from the event-dispatching thread.
	  */
	private static void createAndShowGUI() {
		//Make sure we have nice window decorations.
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
		
		//Create and set up the window.
		JFrame frame = new JFrame("Domain User Lookup & MX record Checker");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosed(WindowEvent e) {
		        PrintStream nullStream = new PrintStream(new OutputStream() {
		            public void write(int b) throws IOException {
		            }

		            public void write(byte b[]) throws IOException {
		            }

		            public void write(byte b[], int off, int len) throws IOException {
		            }
		        });
		        System.setErr(nullStream);
		        System.setOut(nullStream);
		        System.exit(0);
		    }
		});
		
		//Create and set up the content pane.
		JComponent newContentPane = new UserLookup();
		newContentPane.setOpaque(true); //content panes must be opaque
		frame.setContentPane(newContentPane);
		
		//Display the window.		
	    frame.pack();
	    frame.setVisible(true);
	    frame.setSize(800, 500);
	}
	
	public static void main(String[] args) {
	    //Schedule a job for the event-dispatching thread:
	    //creating and showing this application's GUI.
		try{
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					createAndShowGUI();
				}
			});
	  }catch(Exception e){
		  e.printStackTrace();
	  }
	}
}