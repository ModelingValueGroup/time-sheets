package nl.modelingvalue.timesheets;

import org.junit.jupiter.api.Test;

public class Tests {
    @Test
    public void testAdd() {
        Main.main(new String[]{"-p", "time-sheets-mvg/mvg-time-sheets.json"});
    }
}
