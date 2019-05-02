package automail;

import java.util.ArrayList;

import exceptions.MailAlreadyDeliveredException;

public class DeliveryReport implements IMailDelivery {
	
	private double total_score;
	private static ArrayList<MailItem> mailDelivered;
	
	public DeliveryReport() {
		total_score = 0;
		mailDelivered = new ArrayList<MailItem>();
	}
	
	/** Confirm the delivery and calculate the total score */
	
	public void deliver(MailItem deliveryItem) {
		if(!mailDelivered.contains(deliveryItem)){
			mailDelivered.add(deliveryItem);
            System.out.printf("T: %3d > Delivered(%4d) [%s]%n", Clock.Time(), mailDelivered.size(), deliveryItem.toString());
			// Calculate delivery score
			total_score += calculateDeliveryScore(deliveryItem);
		}
		else{
			try {
				throw new MailAlreadyDeliveredException();
			} catch (MailAlreadyDeliveredException e) {
				e.printStackTrace();
			}
		}
	}



	private static double calculateDeliveryScore(MailItem deliveryItem) {
		// Penalty for longer delivery times
		final double penalty = 1.2;
		double priority_weight = 0;
		// Take (delivery time - arrivalTime)**penalty * (1+sqrt(priority_weight))
		if(deliveryItem instanceof PriorityMailItem){
			priority_weight = ((PriorityMailItem) deliveryItem).getPriorityLevel();
		}
		return Math.pow(Clock.Time() - deliveryItem.getArrivalTime(),penalty)*(1+Math.sqrt(priority_weight));
	}
	
    public void printResults(){
        System.out.println("T: "+Clock.Time()+" | Simulation complete!");
        System.out.println("Final Delivery time: "+Clock.Time());
        System.out.printf("Final Score: %.2f%n", total_score);
    }
    
    public int mailDeliveredSize() {
    	return mailDelivered.size();
    }

}