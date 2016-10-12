package me.jprichards.elsimclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONObject;


/**
 * Abstracts the networking code so that Controllers need only work with JSON.
 * @author Joshua Richards
 *
 */
public class NetworkHelper
{
	private Collection<Listener> listeners = new HashSet<>();
	private String host;
	private int port;

	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;
	
	private AtomicBoolean reconnecting = new AtomicBoolean(false);
	private CountDownLatch releasedOnReconnect = new CountDownLatch(1);
	
	private boolean closed = false;

	public NetworkHelper(String host, int port) throws IOException
	{
		this.host = host;
		this.port = port;
		initSocket();
	}
	
	private void initSocket() throws IOException
	{
		socket = new Socket(host, port);
		socket.setSoTimeout(15 * 1000);
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
	}
	
	public void addListener(Listener listener)
	{
		listeners.add(listener);
	}
	
	public boolean removeListener(Listener listener)
	{
		return listeners.remove(listener);
	}
	
	/**
	 * Synchronously listens for the next message from the server.
	 * @return the event that was received
	 * @throws IOException if there was a connection problem
	 */
	public JSONObject receiveMessage() throws IOException
	{
		synchronized (in)
		{
			String message;
			try
			{
				message = in.readUTF();
			}
			catch (SocketTimeoutException e)
			{
				for (Listener listener : listeners)
				{
					listener.onTimeout();
				}
				message = in.readUTF();
			}
			return new JSONObject(message);
		}
	}
	
	/**
	 * Synchronously transmits a message to the server
	 * @param action the message to be transmitted
	 * @throws IOException if there was a connection problem
	 */
	public void sendMessage(JSONObject action) throws IOException
	{
		synchronized (out)
		{
			out.writeUTF(action.toString());
		}
	}
	
	public void close() throws IOException
	{
		closed = true;
		in.close();
		out.close();
		socket.close();
	}
	
	public interface Listener
	{
		public void onTimeout();
	}
}
