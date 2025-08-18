package infinite_loop.sejonghack.support;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class QuizConstants {
    public static final List<String> CATEGORIES = List.of(
            "metal","plastic","paper","glass","vinyl","food","etc"
    );

    public static String pickRandomCategory() {
        int i = ThreadLocalRandom.current().nextInt(CATEGORIES.size());
        return CATEGORIES.get(i);
    }

    public static LocalDate todayKST() {
        return LocalDate.now(ZoneId.of("Asia/Seoul"));
    }
}
