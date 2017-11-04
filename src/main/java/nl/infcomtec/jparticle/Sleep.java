package nl.infcomtec.jparticle;

/**
 * Just defines sleep as it should have been.
 *
 * @author walter
 */
public class Sleep {
    //~ Methods ====================================================================================

    /**
     * Utility function: real sleep.
     *
     * @param msec Milliseconds to sleep.
     */
    public static void sleep (long msec) {
        if (msec < 1) {
            return;
        }

        long until = System.currentTimeMillis () + msec;
        long left = until - System.currentTimeMillis ();

        while (left > 0) {
            try {
                Thread.sleep (left);
            } catch (Exception ignore) {
                // ignore
            }
            left = until - System.currentTimeMillis ();
        }
    }
}
