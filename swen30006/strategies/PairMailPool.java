package strategies;

import java.util.LinkedList;
import java.util.Comparator;
import java.util.ListIterator;

import automail.MailItem;
import automail.PriorityMailItem;
import automail.Robot;
import exceptions.ItemTooHeavyException;

public class PairMailPool implements IMailPool {
	private int nrobots;
	private class Item {
		int priority;
		int destination;
		MailItem mailItem;
		// Use stable sort to keep arrival time relative positions
		
		public Item(MailItem mailItem) {
			priority = (mailItem instanceof PriorityMailItem) ? ((PriorityMailItem) mailItem).getPriorityLevel() : 1;
			destination = mailItem.getDestFloor();
			this.mailItem = mailItem;
		}
	}
	
	public class ItemComparator implements Comparator<Item> {
		@Override
		public int compare(Item i1, Item i2) {
			int order = 0;
			if (i1.priority < i2.priority) {
				order = 1;
			} else if (i1.priority > i2.priority) {
				order = -1;
			} else if (i1.destination < i2.destination) {
				order = 1;
			} else if (i1.destination > i2.destination) {
				order = -1;
			}
			return order;
		}
	}
	
	private LinkedList<Item> pool;
	private LinkedList<Robot> robots;

	public PairMailPool(int nrobots){
		// Start empty
		pool = new LinkedList<Item>();
		robots = new LinkedList<Robot>();
		this.nrobots = nrobots;
	}

	public void addToPool(MailItem mailItem) {
		Item item = new Item(mailItem);
		pool.add(item);
		pool.sort(new ItemComparator());
	}
	
	@Override
	public void step() throws ItemTooHeavyException {
		try{
			ListIterator<Robot> i = robots.listIterator();
			while (i.hasNext()) loadRobot(i);
		} catch (Exception e) { 
            throw e; 
        } 
	}

	private void loadRobot(ListIterator<Robot> i) throws ItemTooHeavyException {
		Robot robot = i.next();
		assert(robot.isEmpty());
		ListIterator<Item> j = pool.listIterator();
		//System.out.println("Pool Size:  " + pool.size());
		
		if (pool.size() > 0) {
			try {
				MailItem current = j.next().mailItem;
			if (current.getWeight() <= Robot.INDIVIDUAL_MAX_WEIGHT) {
				robot.addToHand(current); // hand first as we want higher priority delivered first
				j.remove();
				if (pool.size() > 0) {
					MailItem tube = j.next().mailItem;
					if(tube.getWeight() <= Robot.INDIVIDUAL_MAX_WEIGHT) {
					robot.addToTube(tube);
					j.remove();
					}
				}
				robot.dispatch(); // send the robot off if it has any items to deliver
				i.remove();       // remove from mailPool queue
				
			} else if (current.getWeight() > Robot.INDIVIDUAL_MAX_WEIGHT && current.getWeight() <= Robot.PAIR_MAX_WEIGHT && robots.size() >= 2) {
					/** gets in this state when a pair of robots used to deliver heavy package */
					robot.setPaired();
					robot.setLeader();
					i.remove();
					Robot robot2 = i.next();
					robot2.setPaired();
					robot.addToHand(current);
					robot2.addToHand(current);
					j.remove();
					robot.dispatch();
					robot2.dispatch();
					i.remove();

			} else if ((current.getWeight() > Robot.PAIR_MAX_WEIGHT && nrobots < 3) || (current.getWeight() > Robot.INDIVIDUAL_MAX_WEIGHT && nrobots < 2) || current.getWeight() > Robot.TRIPLE_MAX_WEIGHT)
				throw new ItemTooHeavyException();
			
			} catch (Exception e) { 
	            throw e; 
	        } 
		}
	}

	@Override
	public void registerWaiting(Robot robot) { // assumes won't be there already
		robots.add(robot);
	}

}
