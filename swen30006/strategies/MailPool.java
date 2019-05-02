package strategies;

import java.util.LinkedList;
import java.util.Comparator;
import java.util.ListIterator;

import automail.MailItem;
import automail.PriorityMailItem;
import automail.Robot;
import exceptions.ItemTooHeavyException;

public class MailPool implements IMailPool {
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

	public MailPool(int nrobots){
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
	//TODO: optimise code a little, i.e. robots shouldnt stay waiting if next item is heavy and other robots are
	//		far away. wow
	private void loadRobot(ListIterator<Robot> i) throws ItemTooHeavyException {
		Robot robot = i.next();
		assert(robot.isEmpty());
		ListIterator<Item> j = pool.listIterator();
		//System.out.println("Pool Size:  " + pool.size());
		
		if (pool.size() > 0) {
			try {
				MailItem current = j.next().mailItem;
			if (current.getWeight() <= 2000) {
				robot.addToHand(current); // hand first as we want higher priority delivered first
				j.remove();
				if (pool.size() > 0) {
					MailItem tube = j.next().mailItem;
					if(tube.getWeight() <= 2000) {
					robot.addToTube(tube);
					j.remove();
					}
				}
				robot.dispatch(); // send the robot off if it has any items to deliver
				i.remove();       // remove from mailPool queue
				
			} else if(current.getWeight() > 2000 && current.getWeight() <= 2600 && robots.size() >= 2) {
					System.out.println("HEAVY LOOP");
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

			} else if(current.getWeight() <= 3000 && current.getWeight() > 2600 && robots.size() >= 3) {
					System.out.println("HEAVY LOOP");
					/** Gets in this state when a team of robots is available to deliver heavy package */
					robot.setTeamed();
					robot.setLeader();
					i.remove();
					Robot robot2 = i.next();
					robot2.setTeamed();
					Robot robot3 = i.next();
					robot3.setTeamed();
					robot.addToHand(current);
					robot2.addToHand(current);
					robot3.addToHand(current);
					j.remove();
					robot.dispatch();
					robot2.dispatch();
					robot3.dispatch();
					i.remove();
			} else if ((current.getWeight()> 2600 && nrobots < 3) || (current.getWeight() > 2000 && nrobots < 2) || current.getWeight() > 3000)
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
