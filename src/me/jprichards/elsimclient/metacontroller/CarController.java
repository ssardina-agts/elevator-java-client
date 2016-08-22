package me.jprichards.elsimclient.metacontroller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import me.jprichards.elsimclient.Direction;
import me.jprichards.elsimclient.model.Car;
import me.jprichards.elsimclient.model.Floor;

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
		}
	}
}
