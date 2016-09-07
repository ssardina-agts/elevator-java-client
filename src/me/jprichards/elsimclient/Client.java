package me.jprichards.elsimclient;

import java.io.IOException;

import me.jprichards.elsimclient.metacontroller.MetaController;

public class Client
{

	public static void main(String[] args)
	{
		System.out.println("hi there you");
		try
		{
			Controller c = new MetaController("localhost", 8081);
			c.start();
		}
		catch (IOException e)
		{
			System.err.println("Connection problem");
			e.printStackTrace();
		}
	}

}
