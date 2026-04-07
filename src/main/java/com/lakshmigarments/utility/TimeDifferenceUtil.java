package com.lakshmigarments.utility;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeDifferenceUtil {

    public static String formatDuration(LocalDateTime start, LocalDateTime end) {

        if (start == null || end == null) {
            return "";
        }

        Duration duration = Duration.between(start, end).abs();

        long totalSeconds = duration.getSeconds();

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        // Less than 1 minute
        if (hours == 0 && minutes == 0) {
            return seconds + " secs";
        }

        // Less than 1 hour
        if (hours == 0) {
            return minutes + " mins " + seconds + " secs";
        }

        // Exactly hours (no minutes & seconds)
        if (minutes == 0 && seconds == 0) {
            return hours + (hours == 1 ? " hour" : " hours");
        }

        // Hours + minutes (+ seconds if present)
        StringBuilder result = new StringBuilder();
        result.append(hours).append(hours == 1 ? " hour " : " hours ");

        if (minutes > 0) {
            result.append(minutes).append(" mins ");
        }

        if (seconds > 0) {
            result.append(seconds).append(" secs");
        }

        return result.toString().trim();
    }
}
