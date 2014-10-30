package fitnessfunctions;

import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Nest;
import simulation.robot.Robot;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.util.Arguments;
import environmentpatrol.CoEvoPatrolEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.neuralnetworks.NeuralNetworkController;

public class PatrolCoEvaluationFunction extends EvaluationFunction {
	
	String team;
	int foodForaged = 0;
	
	public PatrolCoEvaluationFunction(Arguments args) {
		super(args);
		team = args.getArgumentAsStringOrSetDefault("team", "");
	}

	@Override
	public void update(Simulator simulator) {
		
		
		CoEvoPatrolEnvironment environment = (CoEvoPatrolEnvironment) simulator.getEnvironment();
		
		
		for(Robot r : environment.getRobots()) {
			PreyPickerActuator actuator = (PreyPickerActuator)r.getActuatorByType(PreyPickerActuator.class);
			
			if(r.getDescription().equalsIgnoreCase("team" + team)){
				double closestPreyDistance = Double.MAX_VALUE;
				for(Prey p : environment.getPrey()) {
					double distance = p.getPosition().distanceTo(r.getPosition());
					if(distance < closestPreyDistance) {
						closestPreyDistance = distance;
					}
				}
				
				if(closestPreyDistance < Double.MAX_VALUE && !actuator.isCarryingPrey()) {
					fitness += (environment.getForageRadius() - closestPreyDistance) / environment.getForageRadius() * .0001;
				}
				
//				if(actuator.isCarryingPrey()){
//					fitness += 0.0001;
//				}
				
				for (Nest n : environment.getNests()) {
					if(n.getName().equalsIgnoreCase("Nest" + team.toUpperCase()) && actuator.isCarryingPrey()){
						double robotDistanceToNest = r.getPosition().distanceTo(n.getPosition());
						if(robotDistanceToNest < environment.getForbiddenArea())
							fitness += (environment.getForbiddenArea() - robotDistanceToNest) / environment.getForbiddenArea() * .001;
					}
				}
			}
		}
		
		if(team.equalsIgnoreCase("a"))
			foodForaged = ((CoEvoPatrolEnvironment) simulator.getEnvironment()).getNumberOfFoodForagedNestA();
		else
			foodForaged = ((CoEvoPatrolEnvironment) simulator.getEnvironment()).getNumberOfFoodForagedNestB();

	}
	
	@Override
	public double getFitness() {
		return super.getFitness() + foodForaged*2;
	}
}
