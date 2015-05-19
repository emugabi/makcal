package net.cloudapp.makcal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

/*
 * The server that can be run both as a console application or a GUI
 */
public class Server {
	// an ArrayList to keep the list of the Client
	private ArrayList<ClientThread> clientList;
	// experiment with hash maps
	public HashMap<String, Socket> hashList = new HashMap<String, Socket>();
	public HashMap<String, ObjectOutputStream> outputstreams = new HashMap<String, ObjectOutputStream>();
	// if I am in a GUI
	private ServerGUI sg;
	// to display time
	private SimpleDateFormat sdf;
	// the port number to listen for connection
	private int port;
	// the boolean that will be turned of to stop the server
	private boolean keepGoing;

	/*
	 * server constructor that receive the port to listen to for connection as
	 * parameter in console
	 */
	public Server(int port) {
		this(port, null);
	}

	public Server(int port, ServerGUI sg) {
		// GUI or not
		this.sg = sg;
		// the port
		this.port = port;
		// to display hh:mm:ss
		sdf = new SimpleDateFormat("HH:mm:ss");
		// ArrayList for the Client list
		clientList = new ArrayList<ClientThread>();
	}

	public void start() {
		keepGoing = true;
		/* create socket server and wait for connection requests */
		try {
			// the socket used by the server
			final ServerSocket serverSocket = new ServerSocket(port);

			Socket socket = null;
			// infinite loop to wait for connections
			while (keepGoing) {
				// format message saying we are waiting
				display("Server waiting for Clients on port " + port + ".");

				socket = serverSocket.accept(); // accept connection
				// if I was asked to stop

				ClientThread t = new ClientThread(socket); // make a thread of
				// it
				clientList.add(t);
				// save it in the ArrayList
				t.start();
			}
			try {
				serverSocket.close();
				for (int i = 0; i < clientList.size(); ++i) {
					ClientThread tc = clientList.get(i);
					try {
						tc.sInput.close();
						tc.sOutput.close();
						tc.socket.close();
					} catch (IOException ioE) {
						// not much I can do
					}
				}
			} catch (Exception e) {
				display("Exception closing the server and clients: " + e);
			}

		}

		// something went bad
		catch (IOException e) {
			String msg = sdf.format(new Date())
					+ " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}

	/*
	 * For the GUI to stop the server
	 */
	protected void stop() {
		keepGoing = false;
		// serverSocket.close();
		// connect to myself as Client to exit statement
		// Socket socket = serverSocket.accept();
		try {
			new Socket("localhost", port);
		} catch (Exception e) {
			// nothing I can really do
		}
	}

	/*
	 * Display an event (not a message) to the console or the GUI
	 */
	private void display(String msg) {
		String time = sdf.format(new Date()) + " " + msg;
		if (sg == null)
			System.out.println(time);
		else
			sg.appendEvent(time + "\n");
	}

	/*
	 * to broadcast a message to specific Clients
	 */
	private synchronized boolean broadcast(ChatMessage cm, String fromUsername) {
		Boolean success = null;
		String message = cm.getMessage().toString();
		String toUsername = cm.getSendTo().toString();

		// add HH:mm:ss and \n to the message
		String time = sdf.format(new Date());
		String messageLf = time + " " + message + " to [" + toUsername + "]\n";

		if (sg == null)
			System.out.print(messageLf);
		else
			sg.appendRoom(messageLf); // append in the room window

		if (hashList.containsKey(toUsername)) {
			// pull the ipadress from the socket object
			Socket socket = hashList.get(toUsername);
			success = true;
			try {
				ObjectOutputStream sOutput = outputstreams.get(toUsername);
				if (cm.getType() == ChatMessage.MESSAGE) {
					sOutput.writeObject(new ChatMessage(ChatMessage.MESSAGE,
							message, fromUsername));
					display("sent " + cm.getMessage() + " to " + cm.getSendTo()
							+ " on " + socket.getRemoteSocketAddress());
				} else {
					sOutput.writeObject(new ChatMessage(ChatMessage.CALL,
							message, fromUsername));
					display(cm.getMessage() + " has made a call to  " + cm.getSendTo()
							+ " on " + socket.getRemoteSocketAddress());
				}
				
				sOutput.flush();

			} catch (UnknownHostException e) {

				e.printStackTrace();

			} catch (IOException e) {

				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {

			display("Disconnected Client " + toUsername + " does not exist");
			success = false;
		}
		return success;

	}

	// for a client who logoff using the LOGOUT message
	synchronized void remove(String username) {

		// remove socket form hashList by way of username key
		hashList.remove(username);
		outputstreams.remove(username);
		display(username + "removed from chat room");

	}

	public static void main(String[] args) {

		int portNumber = 8008;
		switch (args.length) {
		case 1:
			try {
				portNumber = Integer.parseInt(args[0]);
			} catch (Exception e) {
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
		// create a server object and start it
		Server server = new Server(portNumber);
		server.start();
	}

	/** One instance of this thread will run for each client */
	class ClientThread extends Thread {
		// the socket where to listen/talk
		Socket socket;
		ObjectInputStream sInput = null;
		ObjectOutputStream sOutput = null;

		String username = "Failed client";
		ChatMessage cm;
		String date;

		// Constructor is
		ClientThread(Socket socket) {

			this.socket = socket;
			/* Creating both Data Stream */
			System.out
			.println("Thread trying to create Object Input/Output Streams");
			try {
				// create output first
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput = new ObjectInputStream(socket.getInputStream());
			} catch (IOException e) {
				display("Exception creating new Input/output Streams: " + e);
				return;
			}
			date = new Date().toString() + "\n";
		}

		// what will run forever
		@Override
		public void run() {
			// to loop until LOGOUT
			boolean keepGoing = true;

			while (keepGoing) {
				try {

					cm = (ChatMessage) sInput.readObject();
					// get the message part of the ChatMessage
					if (cm.getType() == ChatMessage.LOGIN) {
						username = cm.getMessage(); // only LOGIN is unique
						display(username + " just connected."
								+ " ChatMessage.LOGIN");
						if (hashList.containsValue(socket)) {

							sOutput = outputstreams.get(username);
							sOutput.writeObject(new ChatMessage(
									ChatMessage.LOGIN, "Welcome back", null));
						} else {
							
							hashList.put(username, socket);
							outputstreams.put(username, sOutput);
							sOutput.writeObject(new ChatMessage(
									ChatMessage.LOGIN, "Welcome to MakCal!",
									null));
						}
						break;
					}
				} catch (IOException e) {
					display(username + " Exception reading Streams: " + e);
					break;
				} catch (ClassNotFoundException e2) {
					System.out.println(e2);
					break;
				}
			}
			System.out.println("Listening to " + username + " pakalast");
			// Switch on the type of message receive
			try {
				while (true/* sInput.available() > 0 */) {
					cm = (ChatMessage) sInput.readObject();
					switch (cm.getType()) {

					case ChatMessage.MESSAGE:
						display(username + "sent ChatMessage.MESSAGE");

						if (cm.getSendTo() == "Test") {
							sOutput.writeObject(new ChatMessage(
									ChatMessage.MESSAGE, "Hi There!", null));
						}
						
						else {
							if (broadcast(cm, username) == false) {
								sOutput.writeObject(new ChatMessage(
										ChatMessage.LOGIN, "Failed!", null));
							}
						}
						break;

					case ChatMessage.CALL:
						if (broadcast(cm, username) == false) {
							sOutput.writeObject(new ChatMessage(
									ChatMessage.LOGIN, "Failed!", null));
						}
						break;

					case ChatMessage.LOGOUT:
						hashList.remove(username);
						outputstreams.remove(username);
						display(username
								+ " disconnected with a LOGOUT message.");
						keepGoing = false;
						break;

					case ChatMessage.WHOISIN:
						display("List of the users connected at "
								+ sdf.format(new Date()) + "\n");
						// scan clientList the users connected
						int i = 0;

						for (Entry<String, Socket> client : hashList.entrySet()) {
							i++;
							String uN = client.getKey();
							String ipAddress = client.getValue()
									.getInetAddress().toString();
							display(i + ") " + uN + " since " + date);

							/*
							 * sOutput.writeObject(new
							 * ChatMessage(ChatMessage.WHOISIN, username,
							 * ipAddress));
							 */
							if (username != uN) {
								writeMsg(new ChatMessage(ChatMessage.WHOISIN,
										uN, ipAddress), sOutput);
								/**
								 * please note: when server sends WHOISIN, the
								 * username will be recived as the sendTo and
								 * the ipAddress as the message
								 */
							}

						}

					}
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				System.out.println(username + " on socket ["
						+ socket.getRemoteSocketAddress() + "] closed");
				remove(username);
				close();
			}
			// remove myself from the arrayList containing the list of the
			// connected Clients

		}

		// try to close everything
		private void close() {
			// try to close the connection
			try {
				if (sOutput != null)
					sOutput.close();
			} catch (Exception e) {
			}
			try {
				if (sInput != null)
					sInput.close();
			} catch (Exception e) {
			}
			;
			try {
				if (socket != null)
					socket.close();
			} catch (Exception e) {
			}
		}

		/*
		 * Write a String to the Client output stream
		 */
		private void writeMsg(ChatMessage cMessage, ObjectOutputStream sOutput) {
			// if Client is still connected send the message to it
			if (socket.isConnected()) {
				try {

					sOutput.writeObject(cMessage);
				}
				// if an error occurs, do not abort just inform the user
				catch (IOException e) {
					display("Error sending message to " + username);
					display(e.toString());
				}
			}
			// write the message to the stream
		}
	}
}
