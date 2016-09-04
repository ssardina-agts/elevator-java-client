package me.jprichards.elsimclient.metacontroller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;

import me.jprichards.elsimclient.Controller;
import me.jprichards.elsimclient.Direction;

public class CarController
{
	private Car car;
	private SortedSet<Floor> destinationQueue;
	private SortedSet<Floor> destinationQueueOtherDirection;
	Comparator<Floor> normal;
	Comparator<Floor> other;
	private Direction currentDirection = Direction.UP;
	
	public CarController(Car car)
	{
		this.car = car;
		normal = (Floor arg0, Floor arg1) ->
		{
			int diff = (int) (arg0.getHeight() - arg1.getHeight());
			if (currentDirection == Direction.DOWN)
			{
				diff = 0 - diff;
			}
			
			return diff;
		};
		
		other = (Floor arg0, Floor arg1) ->
			0 - normal.compare(arg0, arg1);
		
		destinationQueue = new TreeSet<>(normal);
		destinationQueueOtherDirection = new TreeSet<>(other);
	}
	
	public void addDestination(Floor f)
	{
		if (destinationQueue.contains(f) || destinationQueueOtherDirection.contains(f))
		{
			return;
		}
		
		if (currentDirection == Direction.UP)
		{
			if (f.getHeight() > car.getCurrentHeight())
			{
				destinationQueue.add(f);
			}
			else
			{
				destinationQueueOtherDirection.add(f);
			}
		}
		else if (currentDirection == Direction.DOWN)
		{
			if (f.getHeight() < car.getCurrentHeight())
			{
				destinationQueue.add(f);
			}
			else
			{
				destinationQueueOtherDirection.add(f);
			}
		}
	}
	
	public void onArrive(Floor arrivedAt)
	{
		destinationQueue.remove(arrivedAt);
		car.arrive();
	}

	public Floor getNextDestination()
	{
		try
		{
			return destinationQueue.first();
		}
		catch (NoSuchElementException e)
		{
			// nothing left that way. turn around
			currentDirection = (currentDirection == Direction.DOWN) ?
					Direction.UP : Direction.DOWN;
			destinationQueue.addAll(destinationQueueOtherDirection);
			destinationQueueOtherDirection.clear();
			
			if (destinationQueue.size() > 0)
			{
				return destinationQueue.first();
			}
		}
		
		return null;
	}
	
	public Car getCar()
	{
		return car;
	}
	
	public Direction getCurrentDirection()
	{
		return currentDirection;
	}
}
