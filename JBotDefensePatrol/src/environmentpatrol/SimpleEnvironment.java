package environmentpatrol;

import java.util.Random;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Nest;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.robot.Robot;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.util.Arguments;

public class SimpleEnvironment extends Environment{
	private static final double PREY_RADIUS = 0.025;
	private static final double PREY_MASS = 1;
	private double nestLimit;
	private double forageLimit;
	private	double forbiddenArea;
	private int numberOfPreys;
	private Nest nest;
	private int numberOfFoodSuccessfullyForaged = 0;
	private Random random;

	public SimpleEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);

		nestLimit       = arguments.getArgumentIsDefined("nestlimit") ? arguments.getArgumentAsDouble("nestlimit")       : .5;
		forageLimit     = arguments.getArgumentIsDefined("foragelimit") ? arguments.getArgumentAsDouble("foragelimit")       : 2.0;
		forbiddenArea   = arguments.getArgumentIsDefined("forbiddenarea") ? arguments.getArgumentAsDouble("forbiddenarea")       : 5.0;
		
		this.random = simulator.getRandom();
		
		if(arguments.getArgumentIsDefined("densityofpreys")){
			double densityoffood = arguments.getArgumentAsDouble("densityofpreys");
			numberOfPreys = (int)(densityoffood*Math.PI*forageLimit*forageLimit+.5);
		} else {
			numberOfPreys = arguments.getArgumentIsDefined("numberofpreys") ? arguments.getArgumentAsInt("numberofpreys") : 20;
		}
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		for(int i = 0; i < numberOfPreys; i++ ){
			addPrey(new Prey(simulator, "Prey "+i, newRandomPosition(), 0, PREY_MASS, PREY_RADIUS));
		}
		nest = new Nest(simulator, "Nest", 0, 0, nestLimit);
		addObject(nest);	
		
//		Parede do mapa
		Wall topWall = new Wall(simulator, "topWall", 0, forageLimit, Math.PI, 1, 1, 0, 4, 0.05, PhysicalObjectType.WALL);
		addObject(topWall);
		Wall bottomWall = new Wall(simulator, "bottomWall", 0, -forageLimit, Math.PI, 1, 1, 0, 4, 0.05, PhysicalObjectType.WALL);
		addObject(bottomWall);
		Wall leftWall = new Wall(simulator, "leftWall", -forageLimit, 0, Math.PI, 1, 1, 0, 0.05, 4.05, PhysicalObjectType.WALL);
		addObject(leftWall);
		Wall rightWall = new Wall(simulator, "rightWall", forageLimit, 0, Math.PI, 1, 1, 0, 0.05, 4.05, PhysicalObjectType.WALL);
		addObject(rightWall);
	}

	private Vector2d newRandomPosition() {
		double radius = random.nextDouble()*(forageLimit-nestLimit)+nestLimit;
		double angle = random.nextDouble()*2*Math.PI;
		return new Vector2d(radius*Math.cos(angle),radius*Math.sin(angle));
	}
	
	@Override
	public void update(double time) {
		nest.shape.getClosePrey().update(time, teleported);
		CloseObjectIterator i = nest.shape.getClosePrey().iterator();

		while(i.hasNext()){
			 PhysicalObjectDistance preyDistance = i.next();
			 Prey nextPrey = (Prey)(preyDistance.getObject());
			 double distance = nextPrey.getPosition().length();
			 if(nextPrey.isEnabled() && distance < nestLimit){
				 if(distance == 0){
					 System.out.println("ERRO--- zero");
				 }
				 nextPrey.teleportTo(newRandomPosition());
				 numberOfFoodSuccessfullyForaged++;
			 }
			 i.updateCurrentDistance(distance);
		}
		
		for(Robot robot: robots){
			PreyCarriedSensor sensor = (PreyCarriedSensor)robot.getSensorByType(PreyCarriedSensor.class);
			if (sensor != null && sensor.preyCarried() && robot.isInvolvedInCollison()){
				PreyPickerActuator actuator = (PreyPickerActuator)robot.getActuatorByType(PreyPickerActuator.class);
				if(actuator != null) {
					Prey preyToDrop = actuator.dropPrey();
					preyToDrop.teleportTo(newRandomPosition());
				}
			}
		}
	}
	
	public int getNumberOfFoodSuccessfullyForaged() {
		return numberOfFoodSuccessfullyForaged;
	}

	public double getNestRadius() {
		return nestLimit;
	}

	public double getForageRadius() {
		return forageLimit;
	}

	public double getForbiddenArea() {
		return forbiddenArea;
	}
}
