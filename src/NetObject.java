/**
 * Created by Zortrox on 9/13/2016.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class Message {
	byte mType = 0;
	byte[] mData;
	InetAddress mIP;
	int mPort;
}

public class NetObject extends Thread{
	int mType;
	boolean mUDP;
	int mPort;
	String mIP;

	//message types
	static final byte MSG_INIT = 0;
	static final byte MSG_FILE = 1;
	static final byte MSG_SIZE = 2;
	static final byte MSG_TEXT = 3;
	static final byte MSG_STOP = 4;

	NetObject() { }

	public void run() {	}

	void receiveUDPData(DatagramSocket socket, Message msg) throws Exception{
		msg.mData = new byte[1024];
		DatagramPacket receivePacket = new DatagramPacket(msg.mData, msg.mData.length);
		socket.receive(receivePacket);

		msg.mPort = receivePacket.getPort();
		msg.mIP = receivePacket.getAddress();
	}

	void sendUDPData(DatagramSocket socket, Message msg) throws Exception{
		DatagramPacket sendPacket = new DatagramPacket(msg.mData, msg.mData.length, msg.mIP, mPort);
		socket.send(sendPacket);
	}

	void receiveTCPData(Socket socket, Message msg) throws Exception{
		DataInputStream inData = new DataInputStream(socket.getInputStream());

		//get size of receiving data
		byte[] byteSize = new byte[4];
		inData.readFully(byteSize);
		ByteBuffer bufSize = ByteBuffer.wrap(byteSize);
		int dataSize = bufSize.getInt();

		//get message type
		msg.mType = inData.readByte();

		//receive data
		msg.mData = new byte[dataSize];
		inData.readFully(msg.mData);
	}

	void sendTCPData(Socket socket, Message msg) throws Exception{
		DataOutputStream outData = new DataOutputStream(socket.getOutputStream());

		//send size of data
		ByteBuffer b = ByteBuffer.allocate(4);
		b.putInt(msg.mData.length);
		byte[] dataSize = b.array();
		outData.write(dataSize);

		//send message type
		outData.writeByte(msg.mType);

		//send data
		outData.write(msg.mData);
	}
}
