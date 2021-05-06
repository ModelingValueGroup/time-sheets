package nl.modelingvalue.timesheets.info;

import java.util.stream.IntStream;

public class PersonBudgetInfo extends Info {
    public double   allMonths;
    public double[] months;

    public void init(Settings settings) {
        super.init(settings);
        if (months == null) {
            months = IntStream.range(0, 12).mapToDouble(i -> allMonths).toArray();
        }
    }
}
