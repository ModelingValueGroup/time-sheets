package nl.modelingvalue.timesheets;

import nl.modelingvalue.timesheets.util.LogAccu;

public class Main {
    public static void main(String[] args) {
//        TimeZone timeZone = Calendar.getInstance().getTimeZone();
//        System.err.println("TZ=" + timeZone);
//        LocalDateTime x = LocalDateTime.now();
//        DateTimeFormatter JIRA_DATE_TIME_FORMAT =
//                new DateTimeFormatterBuilder()
//                        .parseCaseInsensitive()
//                        .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
//                        //.parseLenient()
//                        .appendOffset("+HHmm", "")
//                        .parseStrict()
//                        .toFormatter();
//        String created  = "2017-09-28T22:06:53.000+0000";
//        String updated  = "2017-09-28T22:06:53.000+0000";
//        String started  = "2017-09-28T22:06:00.000+0000";
//        System.err.println("started  =" + started);
//        System.err.println("xxx      =" + ZonedDateTime.parse(started, JIRA_DATE_TIME_FORMAT).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime());
//        System.exit(0);

        LogAccu.info("running with CURRENT_YEAR_ONLY=" + Config.CURRENT_YEAR_ONLY);

        SheetMaker sheetMaker = SheetMaker.read(args);
        sheetMaker.connectAndAskProjects();
        sheetMaker.init();
        sheetMaker.matchPartsToProjects();
        sheetMaker.checkProjectConsistency();
        sheetMaker.downloadAllWorkItems();
        sheetMaker.generateAll();
    }
}
