package me.jprichards.elsimclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
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
	private String host;
	private int port;

	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;
	
	private AtomicBoolean reconnecting = new AtomicBoolean(false);
	private CountDownLatch releasedOnReconnect = new CountDownLatch(1);

	public NetworkHelper(String host, int port) throws IOException
	{
		this.host = host;
		this.port = port;
		initSocket();
	}
	
	private void initSocket() throws IOException
	{
		socket = new Socket(host, port);
		socket.setSoTimeout(30 * 1000);
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
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
				reconnect();
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
			try
			{
				out.writeUTF(action.toString());
			}
			catch (IOException e)
			{
				reconnect();
				out.writeUTF(action.toString());
			}
		}
	}
	
	public void close() throws IOException
	{
		in.close();
		out.close();
		socket.close();
	}
	
	private void reconnect() throws IOException
	{
		if (!reconnecting.compareAndSet(false, true))
		{
			try
			{
				releasedOnReconnect.await();
			}
			catch (InterruptedException e) {}
			
			if (!reconnecting.get())
			{
				return;
			}
			
			throw new IOException("failed to reconnect");
		}
		
		close();
		int attempts = 0;
		IOException toThrow = new IOException("this should never be thrown");
		
		do
		{
			try
			{
				Thread.sleep(10 * 1000);
			} catch (InterruptedException e) {}
			
			try
			{
				initSocket();
				reconnecting.set(false);
				break;
			}
			catch (IOException e)
			{
				toThrow = e;
			}
		} while (attempts++ < 3);
		
		releasedOnReconnect.countDown();
		releasedOnReconnect = new CountDownLatch(1);
		
		if (!reconnecting.get())
		{
			return;
		}
		
		throw toThrow;
	}
}
