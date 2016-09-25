package me.jprichards.elsimclient;

import java.io.IOException;

import me.jprichards.elsimclient.metacontroller.MetaController;

public class Client
{

	public static void main(String[] args)
	{
		try
		{
			ClientController c = new MetaController("localhost", 8081);
			c.run();
			System.out.println("Simulation ended.");
		}
		catch (IOException e)
		{
			System.err.println("Connection problem");
			e.printStackTrace();
		}
	}

}
