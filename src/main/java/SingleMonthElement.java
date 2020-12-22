import java.util.HashMap;
import java.util.List;

class SingleMonthElement {
    
    private final String month;
    private final HashMap<Integer, List<String>> availableDays = new HashMap<>();

    SingleMonthElement(String month) {
        this.month = month;
    }

    String getMonth() {
        return month;
    }

    HashMap<Integer, List<String>> getAvailableDays() {
        return availableDays;
    }
}
