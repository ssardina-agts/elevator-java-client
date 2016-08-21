package me.jprichards.elsimclient;

import java.io.IOException;

public class Client
{

	public static void main(String[] args)
	{
		try
		{
			Controller c = new StupidController("localhost", 8081);
			c.start();
		}
		catch (IOException e)
		{
			System.err.println("Connection problem");
			e.printStackTrace();
		}
	}

}
