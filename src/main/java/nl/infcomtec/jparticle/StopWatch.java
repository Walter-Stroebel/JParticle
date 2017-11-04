package nl.infcomtec.jparticle;

import java.util.Date;
import java.util.TimeZone;

/**
 * Simple StopWatch object
 *
 * @author walter
 */
public class StopWatch {

    private long startNanos;
    private long stopNanos = 0;

    /**
     * Start the StopWatch on creation.
     */
    public StopWatch() {
        startNanos = System.nanoTime();
    }

    /**
     * Time elapsed as seconds
     *
     * @return Time elapsed as seconds
     */
    public double elapsed() {
        return (nanoElapsed()) / 1e9;
    }

    /**
     * Time elapsed as milli seconds
     *
     * @return Time elapsed as milli seconds
     */
    public long milliElapsed() {
        return Math.round(nanoElapsed() / 1000000L);
    }

    /**
     * Time elapsed as nano seconds
     *
     * @return Time elapsed as nano seconds
     */
    public long nanoElapsed() {
        if (stopNanos > 0) {
            return stopNanos - startNanos;
        }
        return System.nanoTime() - startNanos;
    }

    /**
     * Restart the StopWatch.
     */
    public void restart() {
        stopNanos = 0;
        startNanos = System.nanoTime();
    }

    /**
     * Stop (aka split-time aka lap-time) the StopWatch.
     * <p>
     * You can call stop again to get a new elapsed time since creation or since
     * the last restart.
     */
    public void stop() {
        stopNanos = System.nanoTime();
    }

    @Override
    public String toString() {
        return elaspedFromNanos(nanoElapsed());
    }

    /**
     * Return elapsed time in a human-sensible form.
     *
     */
    public static String elaspedFromNanos(long nanos) {
        char sign = (nanos < 0) ? '-' : ' ';
        nanos = Math.abs(nanos);
        if (nanos < 1000) {
            return String.format("%c%5d ns", sign, nanos);
        }
        double us = nanos / 1000.0;
        if (us < 1000) {
            return String.format("%c%5.1f us", sign, us);
        }
        double ms = us / 1000.0;
        if (ms < 1000) {
            return String.format("%c%5.1f ms", sign, ms);
        }
        double hours = ms / 3600000L;
        if (hours < 1) {
            TimeZone here = TimeZone.getDefault();
            return String.format("%c%2$tM:%2$tS.%2$tL", sign, new Date(Math.round(ms) - here.getRawOffset()));
        }
        double days = hours / 24;
        if (days < 1) {
            TimeZone here = TimeZone.getDefault();
            return String.format("%c%2$tH:%2$tM:%2$tS", sign, new Date(Math.round(ms) - here.getRawOffset()));
        } else if (days < 7) {
            long day = 1000000000L * 3600L * 24L;
            long ldays = nanos / day;
            return String.format("%c%d days, %s", sign, ldays, elaspedFromNanos(nanos % day));
        } else {
            long week = 1000000000L * 3600L * 24L * 7L;
            long weeks = nanos / week;
            if (weeks < 52) {
                return String.format("%c%d weeks, %s", sign, weeks, elaspedFromNanos(nanos % week));
            } else {
                return String.format("%c%d years, %d weeks, %s", sign, weeks / 52, weeks % 52, elaspedFromNanos(nanos % week));
            }
        }
    }

}
