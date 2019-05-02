package exceptions;

public class OverWeightLimitException extends Exception {
    public OverWeightLimitException(){
        super("MailPool contains item(s) too heavy to carry!");
    }
}
