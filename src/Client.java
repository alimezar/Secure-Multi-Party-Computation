import java.net.*;
import java.io.*;
import java.util.*;

public class Client {
	private String notif = " *** ";
	private ObjectInputStream sInput;
	private ObjectOutputStream sOutput;
	private Socket socket;
	private String server, username;
	private int port;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	Client(String server, int port, String username) {
		this.server = server;
		this.port = port;
		this.username = username;
	}

	public boolean start() {
		try {
			socket = new Socket(server, port);
		} catch (Exception ec) {
			display("Error connectiong to server:" + ec);
			return false;
		}

		String msg = "Connection accepted " + socket.getInetAddress() + ":"
				+ socket.getPort();
		display(msg);

		try {
			sInput = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException eIO) {
			display("Exception creating new Input/output Streams: " + eIO);
			return false;
		}

		// creates the Thread to listen from the server
		new ListenFromServer().start();
		try {
			sOutput.writeObject(username);
		} catch (IOException eIO) {
			display("Exception doing login : " + eIO);
			disconnect();
			return false;
		}
		return true;
	}

	private void display(String msg) {
		System.out.println(msg);
	}

	// * To send an object to the server
	void sendToServer(Object obj) {
		try {
			sOutput.writeObject(obj);
		} catch (IOException e) {
			display("Exception writing to server: " + e);
		}
	}

	private void disconnect() {
		try {
			if (sInput != null)
				sInput.close();
		} catch (Exception e) {
		}
		try {
			if (sOutput != null)
				sOutput.close();
		} catch (Exception e) {
		}
		try {
			if (socket != null)
				socket.close();
		} catch (Exception e) {
		}
	}

	public static void main(String[] args) {
		// default values if not entered
		int portNumber = 1500;
		String serverAddress = "localhost";
		String userName = "Anonymous";
		Scanner scan = new Scanner(System.in);

		System.out.println("Enter the username: ");
		userName = scan.nextLine();

		switch (args.length) {
		case 3:
			serverAddress = args[2];
		case 2:
			try {
				portNumber = Integer.parseInt(args[1]);
			} catch (Exception e) {
				System.out.println("Invalid port number.");
				System.out
						.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
				return;
			}
		case 1:
			userName = args[0];
		case 0:
			break;
		default:
			System.out
					.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
			return;
		}
		// create the Client object
		Client client = new Client(serverAddress, portNumber, userName);
		// try to connect to the server and return if not connected
		if (!client.start())
			return;

		System.out.println("\nHello.! Welcome to the chatroom.");

		// infinite loop to get the input from the user
		while (true) {
			System.out.print("> ");
			// read message from user
			String msg = scan.nextLine();

		}
	}

	// * a class that waits for the message from the server
	class ListenFromServer extends Thread {
		public void run() {
			while (true) {
				try {
					// read the message from the input datastream
					String msg = (String) sInput.readObject();
					// Do sth with the message Here xD

					System.out.print("> ");
				} catch (IOException e) {
					display(notif + "Server has closed the connection: " + e
							+ notif);
					break;
				} catch (ClassNotFoundException e2) {
				}
			}
		}
	}
}
