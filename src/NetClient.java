/**
 * Created by Zortrox on 9/13/2016.
 */

import javax.swing.*;
import java.awt.*;
import java.net.*;

public class NetClient extends NetObject{

	NetClient(boolean UDP, int port, String IP) {
		mUDP = UDP;
		mPort = port;
		mIP = IP;

		JFrame frame = new JFrame("Client");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(new Dimension(300, 200));
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

		//sending data
		sendUDPData(clientSocket, msg);
		System.out.println(strInitial);

		//receiving data
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

		for (int i=0; i<10; i++) {
			socket = new Socket(mIP, mPort);
			//custom message
			msgSend = "test " + i;//JOptionPane.showInputDialog("What is your message?");
			System.out.println(msgSend);

			msg = new Message();
			msg.mData = msgSend.getBytes();
			msg.mType = MSG_TEXT;
			sendTCPData(socket, msg);
			receiveTCPData(socket, msg);
			msgReceive = new String(msg.mData);
			System.out.println("<server>: " + msgReceive);
			socket.close();
		}
	}
}
