/**
 * Created by Zortrox on 9/13/2016.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;

public class NetClient extends NetObject{

	ArrayList<String> strPokeNames = new ArrayList<>();

	NetClient(boolean UDP, int port, String IP) {
		mUDP = UDP;
		mPort = port;
		mIP = IP;

		JFrame frame = new JFrame("Client");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(new Dimension(250, 100));

		//populate names from file
		try (BufferedReader br = new BufferedReader(new FileReader("pokemon-names.txt"))) {
			for (String line; (line = br.readLine()) != null; ) {
				strPokeNames.add(line);
			}
			br.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		JComboBox boxNames = new JComboBox(strPokeNames.toArray());
		boxNames.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox)e.getSource();
				String fnImage = "/pokemon-imgs/" + (cb.getSelectedIndex()+1) + ".png";
				String fnText = "/pokemon-types/" + (cb.getSelectedIndex()+1) + ".txt";

				try {
					Object clientSocket = null;

					if (mUDP) clientSocket = new DatagramSocket();
					InetAddress IPAddress = InetAddress.getByName(mIP);

					//setup message address
					Message msg = new Message();
					msg.mIP = IPAddress;
					msg.mPort = mPort;

					if (!mUDP) clientSocket = new Socket(mIP, mPort);
					//request image file
					msg.mData = fnImage.getBytes();
					msg.mType = MSG_FILE;
					if (mUDP) {
						sendUDPData((DatagramSocket) clientSocket, msg);
					} else {
						sendTCPData((Socket) clientSocket, msg);
					}
					System.out.println("Requested: " + fnImage);

					//save image to directory
					new File(filePath + "/downloaded/img").mkdirs();
					receiveFile(clientSocket, msg, "/downloaded/img/" + cb.getSelectedItem() + ".png");
					if (!mUDP) ((Socket)clientSocket).close();

					if (!mUDP) clientSocket = new Socket(mIP, mPort);
					//request text file
					msg.mData = fnText.getBytes();
					msg.mType = MSG_FILE;
					if (mUDP) {
						sendUDPData((DatagramSocket) clientSocket, msg);
					} else {
						sendTCPData((Socket) clientSocket, msg);
					}
					System.out.println("Requested: " + fnText);

					//save text to directory
					new File(filePath + "/downloaded/type").mkdirs();
					receiveFile(clientSocket, msg, "/downloaded/type/" + cb.getSelectedItem() + ".txt");
					if (!mUDP) ((Socket)clientSocket).close();

					if (mUDP) {
						((DatagramSocket) clientSocket).close();
					} else {
						((Socket) clientSocket).close();
					}
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		frame.add(boxNames, BorderLayout.NORTH);
		boxNames.setVisible(true);

		JButton btnMassRequest = new JButton("Mass Request");
		btnMassRequest.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Random rand = new Random();

				try {
					Object clientSocket = null;

					if (mUDP) clientSocket = new DatagramSocket();
					InetAddress IPAddress = InetAddress.getByName(mIP);

					//setup message address
					Message msg = new Message();
					msg.mIP = IPAddress;
					msg.mPort = mPort;

					for (int i = 0; i < 50; i++) {
						int randomPoke = rand.nextInt(strPokeNames.size()) + 1;
						String fnImage = "/pokemon-imgs/" + randomPoke + ".png";
						String fnText = "/pokemon-types/" + randomPoke + ".txt";

						if (!mUDP) clientSocket = new Socket(mIP, mPort);
						//request image file
						msg.mData = fnImage.getBytes();
						msg.mType = MSG_FILE;
						if (mUDP) {
							sendUDPData((DatagramSocket) clientSocket, msg);
						} else {
							sendTCPData((Socket) clientSocket, msg);
						}
						System.out.println("Requested: " + fnImage);

						//save image to directory
						new File(filePath + "/downloaded/img").mkdirs();
						receiveFile(clientSocket, msg, "/downloaded/img/" + strPokeNames.get(randomPoke-1) + ".png");
						if (!mUDP) ((Socket)clientSocket).close();

						if (!mUDP) clientSocket = new Socket(mIP, mPort);
						//request text file
						msg.mData = fnText.getBytes();
						msg.mType = MSG_FILE;
						if (mUDP) {
							sendUDPData((DatagramSocket) clientSocket, msg);
						} else {
							sendTCPData((Socket) clientSocket, msg);
						}
						System.out.println("Requested: " + fnText);

						//save text to directory
						new File(filePath + "/downloaded/type").mkdirs();
						receiveFile(clientSocket, msg, "/downloaded/type/" + strPokeNames.get(randomPoke-1) + ".txt");
						if (!mUDP) ((Socket)clientSocket).close();
					}

					if (mUDP) ((DatagramSocket) clientSocket).close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		frame.add(btnMassRequest, BorderLayout.SOUTH);
		btnMassRequest.setVisible(true);

		frame.setVisible(true);
	}

	public void run() {
		if (mUDP) {
			try {
				UDPConnection();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			try {
				TCPConnection();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void UDPConnection() throws Exception {
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName(mIP);

		String strInitial = "Hello, this is Matthew";

		Message msg = new Message();
		msg.mData = strInitial.getBytes();
		msg.mType = MSG_INIT;
		msg.mIP = IPAddress;
		msg.mPort = mPort;

		//initial send/receive
		sendUDPData(clientSocket, msg);
		System.out.println(strInitial);
		receiveUDPData(clientSocket, msg);
		String strReceived = new String(msg.mData);
		System.out.println("<server>: " + strReceived);

		clientSocket.close();
	}

	private void TCPConnection() throws Exception {
		Socket socket = null;
		boolean bServerFound = false;

		//keep trying to connect to server
		while(!bServerFound)
		{
			try
			{
				socket = new Socket(mIP, mPort);
				bServerFound = true;
			}
			catch(ConnectException e)
			{
				System.out.println("Server refused, retrying...");

				try
				{
					Thread.sleep(2000); //2 seconds
				}
				catch(InterruptedException ex){
					ex.printStackTrace();
				}
			}
		}

		//initialize message
		String msgSend = "Hello, this is Matthew.";
		System.out.println(msgSend);

		Message msg = new Message();
		msg.mData = msgSend.getBytes();
		msg.mType = MSG_INIT;
		sendTCPData(socket, msg);
		receiveTCPData(socket, msg);
		String msgReceive = new String(msg.mData);
		System.out.println("<server>: " + msgReceive);
		socket.close();
	}
}
