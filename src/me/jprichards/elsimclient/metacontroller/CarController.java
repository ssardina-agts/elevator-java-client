package me.jprichards.elsimclient.metacontroller;

import java.util.ArrayList;
import java.util.List;

import me.jprichards.elsimclient.Direction;
import me.jprichards.elsimclient.model.Car;
import me.jprichards.elsimclient.model.Floor;

public class CarController
{
	private Car car;
	private List<Floor> destinationQueue = new ArrayList<>();
	private List<Floor> destinationQueueOtherDirection = new ArrayList<>();
	private Direction currentDirection = Direction.UP;
	
	public CarController(Car car)
	{
		this.car = car;
	}


	public FloorComparator implements Comparator<Floor>
	{
		
	}
}
