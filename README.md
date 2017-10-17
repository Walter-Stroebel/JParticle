# JParticle

Just a small Maven library to communicate with your Protons and Electrons via the Particle Cloud.

This works with OpenJDK-8 which is what I am using at the moment.
I don't expect any issues using Java-7 instead and with some small tweaks you should be able to go all the way back to Java-5.

Just include the library in your project and create an instance of the Cloud object using:

Cloud c = new Cloud("Bearer youraccesstoken", true, false);

This will start a background task to receive events from the Particle Cloud. You will see them scolling by on your standard output.

System.out.println(c.devices); // will printout all devices you own.

Here is a code snippet to read two variables from my penquin-mighty Proton every 11 seconds.

        c.poll(new DevicePoll() {
            private final UUID uuid = UUID.randomUUID();

            @Override
            public long interval() {
                return 11000L;
            }

            @Override
            public void run() {
                Integer hadMotion = c.getInt("penguin_mighty", "hadMotion");
                Integer lastMotion = c.getInt("penguin_mighty", "lastMotion");
                System.out.println("Motion: hadMotion=" + hadMotion + ", lastMotion=" + lastMotion);
            }

            @Override
            public UUID uuid() {
                return uuid;
            }
        });

