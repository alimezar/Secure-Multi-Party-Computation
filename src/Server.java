
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

// the server that can be run as a console
public class Server {
	// a unique ID for each connection
	private static int uniqueId;
	// an ArrayList to keep the list of the Clients
	private ArrayList<ClientThread> al;
	// the port number to listen for connection
	private int port;
	private boolean keepGoing;
	private String notif = " *** ";
	
	//constructor that receive the port to listen to for connection as parameter
	
	public Server(int port) {
		this.port = port;
		al = new ArrayList<ClientThread>();
	}
	
	public void start() {
		keepGoing = true;
		try 
		{
			ServerSocket serverSocket = new ServerSocket(port);
			// infinite loop to wait for connections ( till server is active )
			while(keepGoing) 
			{
				display("Server waiting for Clients on port " + port + ".");
				
				Socket socket = serverSocket.accept();
				if(!keepGoing)
					break;
				ClientThread t = new ClientThread(socket);
				al.add(t);
				t.start();
			}
			// try to stop the server
			try {
				serverSocket.close();
				for(int i = 0; i < al.size(); ++i) {
					ClientThread tc = al.get(i);
					try {
					// close all data streams and socket
					tc.sInput.close();
					tc.sOutput.close();
					tc.socket.close();
					}
					catch(IOException ioE) {
					}
				}
			}
			catch(Exception e) {
				display("Exception closing the server and clients: " + e);
			}
		}
		catch (IOException e) {
            String msg = " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}
	
	private void display(String msg) {
	}
	
	// to broadcast a message to all Clients
	private synchronized boolean broadcast(String message) {
		String messageLf = message + "\n";
		// display message
		System.out.print(messageLf);
			
		// we loop in reverse order in case we would have to remove a Client
		// because it has disconnected
		for(int i = al.size(); --i >= 0;) {
			ClientThread ct = al.get(i);
			// try to write to the Client if it fails remove it from the list
			if(!ct.writeMsg(messageLf)) {
				al.remove(i);
				display("Disconnected Client " + ct.username + " removed from list.");
			}
		}
	return true;
	}

	
	public static void main(String[] args) {
		int portNumber = 1500;
		switch(args.length) {
			case 1:
				try {
					portNumber = Integer.parseInt(args[0]);
				}
				catch(Exception e) {
					System.out.println("Invalid port number.");
					System.out.println("Usage is: > java Server [portNumber]");
					return;
				}
			case 0:
				break;
			default:
				System.out.println("Usage is: > java Server [portNumber]");
				return;
				
		}
		Server server = new Server(portNumber);
		server.start();
	}

	// One instance of this thread will run for each client
	class ClientThread extends Thread {
		// the socket to get messages from client
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		int id;
		String username;
		Object cm;
		String date;

		// Constructor
		ClientThread(Socket socket) {
			// a unique id
			id = ++uniqueId;
			this.socket = socket;
			//Creating both Data Stream
			System.out.println("Thread trying to create Object Input/Output Streams");
			try
			{
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
				// read the username
				username = (String) sInput.readObject();
				broadcast(notif + username + " has joined the chat room." + notif);
			}
			catch (IOException e) {
				display("Exception creating new Input/output Streams: " + e);
				return;
			}
			catch (ClassNotFoundException e) {
			}
            date = new Date().toString() + "\n";
		}
		
		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		// infinite loop to read and forward message
		public void run() {
			boolean keepGoing = true;
			while(keepGoing) {
				// read an input
				try {
					cm = sInput.readObject();
				}
				catch (IOException e) {
					display(username + " Exception reading Streams: " + e);
					break;				
				}
				catch(ClassNotFoundException e2) {
					break;
				}
			}
			close();
		}
		
		private void close() {
			try {
				if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}

		// write an Object to the Client output stream
		private boolean writeMsg(Object obj) {
			if(!socket.isConnected()) {
				close();
				return false;
			}
			try {
				sOutput.writeObject(obj);
			}
			catch(IOException e) {
				display(notif + "Error sending message to " + username + notif);
				display(e.toString());
			}
			return true;
		}
	}
}

