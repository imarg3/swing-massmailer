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
import java.io.OutputStream;
import java.io.PrintStream;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
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

public class MailServerChecker extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7656017608477184781L;

	static private final String newline = "\n";

	private JButton openButton, saveButton;
	private JTextArea log;
	private JFileChooser fileToUpload;
	private Map<String, Integer> validDomainMap = new HashMap<>();

	public MailServerChecker() {
		super(new BorderLayout());
		initComponents();
	}

	private void initComponents() {

		// Create the log first, because the action listeners need to refer to it.
		log = new JTextArea(5, 20);
		log.setText("Welcome to Mail Server Lookup & MX record Checker. Instructions to use this tool are as follows :"
				+ newline + newline + "1. Choose valid file containing Email IDs" + newline + newline
				+ "2. Save valid email id list file on the file system");
		log.setFont(log.getFont().deriveFont(18f));
		log.setMargin(new Insets(5, 5, 5, 5));
		log.setEditable(false);
		JScrollPane logScrollPane = new JScrollPane(log);

		// Create a File Chooser
		fileToUpload = new JFileChooser();

		// Uncomment one of the following lines to try a different file selection mode.
		// The first allows just directories
		// to be selected (and, at least in the Java look and feel, shown). The second
		// allows both files and directories
		// to be selected. If you leave these lines commented out, then the default mode
		// (FILES_ONLY) will be used.

		// fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		// fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		// Create the open button. We use the image from the JLF
		// Graphics Repository (but we extracted it from the jar).
		openButton = new JButton("Open a File...", createImageIcon("images/Open24.gif"));
		openButton.addActionListener(this);

		// Create the save button. We use the image from the JLF
		// Graphics Repository (but we extracted it from the jar).
		saveButton = new JButton("Save a File...", createImageIcon("images/Save24.gif"));
		saveButton.addActionListener(this);

		// For layout purposes, put the buttons in a separate panel
		JPanel buttonPanel = new JPanel(); // use Flowlayout
		buttonPanel.add(openButton);
		buttonPanel.add(saveButton);

		// Add the buttons and the log to this panel.
		add(buttonPanel, BorderLayout.PAGE_START);
		add(logScrollPane, BorderLayout.CENTER);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		// Handle open button action.
		if (e.getSource() == openButton) {
			FileFilter filter = new FileNameExtensionFilter("*.txt", new String[] { "txt" });
			fileToUpload.addChoosableFileFilter(filter);
			int returnVal = fileToUpload.showOpenDialog(MailServerChecker.this);
			Set<String> emailIDSet = null;

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fileToUpload.getSelectedFile();

				try (BufferedReader br = new BufferedReader(new FileReader(file));
						BufferedWriter bw = new BufferedWriter(new FileWriter("E:\\resultFile.txt"));) {

					// This is where a real application would open the file.
					String emailID = null;
					String prefix = "";

					emailIDSet = new LinkedHashSet<String>();
					log.setText("");
					log.append("Opening File: " + file.getName() + "." + newline);

					while ((emailID = br.readLine()) != null) {
						log.append(emailID + newline);
						emailIDSet.add(emailID);
					}

					Iterator<String> itr = emailIDSet.iterator();
					int mxSize;
					String emailIDValue = null;
					String[] domainName = null;

					System.out.println("Before next");
					
					while (itr.hasNext()) {
						emailIDValue = itr.next();
						domainName = emailIDValue.split("@");
						
						if(domainName.length != 2) {
							continue;
						}
						
						if(validDomainMap.get(domainName[1]) != null ) {
							System.out.println(String.format("Domain %s already checked", domainName[1] ));
							mxSize = validDomainMap.get(domainName[1]);
						} else {
							System.out.println(String.format("Checking %s for the first time", domainName[1] ));
							mxSize = doLookup(emailIDValue, domainName[1]);
						}
						
						System.out.println(mxSize);

						if (mxSize != 0) {
							bw.write(prefix);
							prefix = newline;
							bw.write(emailIDValue);
						} else {
							System.err.println(domainName[1] + " does not exist. Lookup failed !!!");
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

			// Handle save button action.
		} else if (e.getSource() == saveButton) {
			int returnVal = fileToUpload.showSaveDialog(this);
			Set<String> emailIDSet = null;

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fileToUpload.getSelectedFile();

				try (BufferedReader br = new BufferedReader(new FileReader("E:\\resultFile.txt"));
						BufferedWriter bw = new BufferedWriter(new FileWriter(file));) {
					// This is where a real application would open the file.
					String emailID = null;
					String prefix = "";

					emailIDSet = new HashSet<String>();
					log.append("Opening: " + file.getName() + "." + newline);

					while ((emailID = br.readLine()) != null) {
						// log.append(emailID + newline);
						emailIDSet.add(emailID);
					}

					Iterator<String> itr = emailIDSet.iterator();

					while (itr.hasNext()) {
						bw.write(prefix);
						prefix = newline;
						bw.write(itr.next());
					}

					log.setText("");
					log.append(
							"Your file has been saved in the path provided. Please note that this file do not contain Duplicate Email ID");

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
	protected static ImageIcon createImageIcon(String path) {
		URL imageUrl = MailServerChecker.class.getResource(path);

		if (imageUrl != null) {
			return new ImageIcon(imageUrl);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	protected int doLookup(String emailID, String domainName) {		
		Hashtable<String, String> env = new Hashtable<>();
		env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");

		DirContext ictx;
		try {
			ictx = new InitialDirContext(env);

			Attributes attrs = ictx.getAttributes(domainName, new String[] { "MX" });
			Attribute attr = attrs.get("MX");

			if (attr == null) {
				return 0;
			} else {
				/*
				 * try { int res; Socket skt = new Socket( (String) attr, 25 ); BufferedReader
				 * rdr = new BufferedReader ( new InputStreamReader( skt.getInputStream() ) );
				 * BufferedWriter wtr = new BufferedWriter ( new OutputStreamWriter(
				 * skt.getOutputStream() ) );
				 * 
				 * res = hear( rdr ); if ( res != 220 ) throw new Exception( "Invalid header" );
				 * say( wtr, "EHLO orbaker.com" );
				 * 
				 * res = hear( rdr ); if ( res != 250 ) throw new Exception( "Not ESMTP" );
				 * 
				 * // validate the sender address say( wtr, "MAIL FROM: <tim@orbaker.com>" );
				 * res = hear( rdr ); if ( res != 250 ) throw new Exception( "Sender rejected"
				 * );
				 * 
				 * say( wtr, "RCPT TO: <" + address + ">" ); res = hear( rdr );
				 * 
				 * // be polite say( wtr, "RSET" ); hear( rdr ); say( wtr, "QUIT" ); hear( rdr
				 * ); if ( res != 250 ) throw new Exception( "Address is not valid!" );
				 * 
				 * valid = true; rdr.close(); wtr.close(); skt.close(); } catch (Exception ex) {
				 * // Do nothing but try next host } finally { if ( valid ) return true; }
				 */
				
				validDomainMap.put(domainName, attr.size());
				
				return (attr.size());
			}
		} catch (NamingException e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be invoked
	 * from the event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		// Make sure we have nice window decorations.
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);

		// Create and set up the window.
		JFrame frame = new JFrame("Mail Server Lookup & MX record Checker");
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

		// Create and set up the content pane.
		JComponent newContentPane = new MailServerChecker();
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
		frame.setSize(800, 500);
	}

	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		try {
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					createAndShowGUI();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
