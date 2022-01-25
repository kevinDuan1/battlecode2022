package player1;

import battlecode.common.*;


import java.util.Objects;
import java.util.Random;
import java.util.PriorityQueue;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;
//    static Direction direction;
    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);

    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // Hello world! Standard output is very useful for debugging.
        // Everything you say here will be directly viewable in your terminal when you run a match!
        System.out.println("I'm a " + rc.getType() + " and I just got created! I have health " + rc.getHealth());

        // You can also use indicators to save debug notes in replays.
        rc.setIndicatorString("Hello world!");

        while (true) {
            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to do.

            turnCount += 1;  // We have now been alive for one more turn!
            System.out.println("Age: " + turnCount + "; Location: " + rc.getLocation());

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try {
                // The same run() function is called for every robot on your team, even if they are
                // different types. Here, we separate the control depending on the RobotType, so we can
                // use different strategies on different robots. If you wish, you are free to rewrite
                // this into a different control structure!
                switch (rc.getType()) {
                    case ARCHON:     runArchon(rc);  break;
                    case MINER:      runMiner(rc);   break;
                    case SOLDIER:    runSoldier(rc); break;
                    case LABORATORY: // Examplefuncsplayer doesn't use any of these robot types below.
                    case WATCHTOWER: // You might want to give them a try!
                    case BUILDER:
                    case SAGE:       break;
                }
            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }

    /**
     * Run a single turn for an Archon.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runArchon(RobotController rc) throws GameActionException {
        // Pick a direction to build in.
        Direction dir = directions[rng.nextInt(directions.length)];
        if (turnCount <= 1 ){
//            saveLocation(rc, new MapLocation(2,36), 0);
//            saveLocation(rc, new MapLocation(6, 53), 1);
//            saveLocation(rc, new MapLocation(36, 55), 2);
//            saveLocation(rc, new MapLocation(54, 55), 3);
            saveLocation(rc, new MapLocation(2,2), 0);
            saveLocation(rc, new MapLocation(46, 22), 1);
        }

        if (rng.nextBoolean()) {
            // Let's try to build a miner.
            rc.setIndicatorString("Trying to build a miner");
            if (rc.canBuildRobot(RobotType.MINER, dir) && rc.getRoundNum() < 2) {
                rc.buildRobot(RobotType.MINER, dir);
            }
        }
        else {
            // Let's try to build a soldier.
            rc.setIndicatorString("Trying to build a soldier");
            if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                rc.buildRobot(RobotType.SOLDIER, dir);
            }
        }
    }

    /**
     * Run a single turn for a Miner.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runMiner(RobotController rc) throws GameActionException {
        // Try to mine on squares around us.
        MapLocation me = rc.getLocation();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
                // Notice that the Miner's action cooldown is very low.
                // You can mine multiple times per turn!
                while (rc.canMineGold(mineLocation)) {
                    rc.mineGold(mineLocation);
                }
                while (rc.canMineLead(mineLocation)) {
                    rc.mineLead(mineLocation);
                }
            }
        }

        // Also try to move randomly.
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
            System.out.println("I moved!");
        }

    }

    /**
     * Run a single turn for a Soldier.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static MapLocation parentArchon;
    //patrol
    static void patrol(RobotController rc) throws  GameActionException {
        MapLocation curr = rc.getLocation();
        Direction dir = parentArchon.directionTo(curr);
        int patrolRadiusSq = 10;
        if (curr.distanceSquaredTo(parentArchon) < patrolRadiusSq) {
            for (int i = 0; i < 8; i++) {
                if (rc.canMove(dir)) {
                    rc.move(dir);
                    break;
                }
                dir = dir.rotateLeft();
            }
        } else if (curr.distanceSquaredTo(parentArchon) > (patrolRadiusSq * 2)) {
            dir = dir.opposite();
            for (int i = 0; i < 8; i++) {
                if (rc.canMove(dir)) {
                    rc.move(dir);
                    break;
                }
                dir = dir.rotateRight();
            }
        } else {
            dir = dir.rotateLeft().rotateLeft();
            for (int i = 0; i < 8; i++) {
                if (rc.canMove(dir)) {
                    rc.move(dir);
                    break;
                }
                dir = dir.rotateLeft();
            }
        }
    }

    static void runSoldier(RobotController rc) throws GameActionException {

       if (turnCount <= 1) {
           for (RobotInfo info : rc.senseNearbyRobots(2)){
               if (info.getType().equals(RobotType.ARCHON)){
                   parentArchon = info.getLocation();
                   break;
               }
           }
       }

        // patrol
       if (turnCount < 20) {
           // perception
           boolean nearEnemy = false;
           MapLocation enemyLocation = null;
           int enemyDistance = 9999;
           for (RobotInfo info : rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent())) {
                if (info.getLocation().distanceSquaredTo(parentArchon) < enemyDistance) {
                    nearEnemy = true;
                    enemyLocation = info.getLocation();
                }
           }
           if (nearEnemy) {
              Direction dir = rc.getLocation().directionTo(enemyLocation);
              if (rc.canMove(dir)) {
                  rc.move(dir);
              }
              if (rc.canAttack(enemyLocation)) {
                  rc.attack(enemyLocation);
              }
           } else{
               patrol(rc);
           }
       }else {
           // attack enemy archon
           MapLocation nearestTarget = null;
           int shortestDistance = 9999;
            for (int i = 0; i < 4; i++) {
                if (haveInfo(rc, i) && !ifArchonAnnihilated(rc, i)) {
                    MapLocation target = readLocation(rc, i);
                    if (target.distanceSquaredTo(rc.getLocation()) < shortestDistance) {
                        shortestDistance = target.distanceSquaredTo(rc.getLocation());
                        nearestTarget = target;
                    }
                }
                if (ifArchonAnnihilated(rc,i)) {
                    System.out.println(i + " Annihilated!!!");
                }
            }

            if (nearestTarget != null) {
                //check if enemy archon annihilated
                annihilatedArchon(rc, nearestTarget);
                // attack
                if (rc.canAttack(nearestTarget)) {
                    rc.attack(nearestTarget);
                }
                // move towards target
                if (shortestDistance < rc.getType().actionRadiusSquared*2/3) {
                    Direction dir = rc.getLocation().directionTo(nearestTarget);
                  for (int i = 0; i < 8; i++) {
                      if (rc.canMove(dir)) {
                          rc.move(dir);
                          break;
                      }
                      dir = dir.rotateLeft();
                  }
                }else {
                    moveTo(rc, nearestTarget);
//                    if (rc.canMove(rc.getLocation().directionTo(nearestTarget))){
//                        rc.move(rc.getLocation().directionTo(nearestTarget));
//                    }

                }
            }
       }
    }

    // check if shared array have information
    static boolean  haveInfo(RobotController rc, int index) throws  GameActionException {
        return  rc.readSharedArray(index) != 0;
    }

    // save location from shared array
    static void saveLocation(RobotController rc, MapLocation location, int index) throws GameActionException {
        int num;
        num = location.x * 64 + location.y;
        rc.writeSharedArray(index, num);
    }

    // read location from shared array
    static MapLocation readLocation(RobotController rc, int index) throws GameActionException {
        int num = rc.readSharedArray(index);
        int y = num % 64;
        num = num / 64;
        int x = num % 64;
        return new MapLocation(x, y);
    }

    static boolean ifArchonAnnihilated(RobotController rc, int index) throws GameActionException{
        return rc.readSharedArray(index) / 32768 > 0;
    }

    static void annihilatedArchon(RobotController rc, MapLocation location) throws  GameActionException{
        if (rc.getLocation().distanceSquaredTo(location) < rc.getType().visionRadiusSquared && (!rc.canSenseRobotAtLocation(location) || (rc.canSenseRobotAtLocation(location) && !rc.senseRobotAtLocation(location).getType().equals(RobotType.ARCHON)))) {
            for (int i=0; i < 4; i++) {
                if (readLocation(rc, i).equals(location)) {
                    rc.writeSharedArray(i, rc.readSharedArray(i) + 32678);
                    break;
                }
            }
        }
    }

///////////////////////////////////////////////////////////////////////////////////////////////////////////
   public static class Edge implements Comparable<Edge>{
        private final MapLocation from;
        private final MapLocation to;
        private  final int weight;
        private  int priority = 10000;

        public Edge(MapLocation f, MapLocation t, int w) {
           from = f;
           to = t;
           weight = w;
       }

       public void setPriority(int priority) {
            this.priority = priority;
       }

        public MapLocation getFrom() {
            return from;
       }
        public MapLocation getTo() {
            return to;
        }
        public int getWeight() {
            return weight;
        }

       @Override
       public boolean equals(Object o) {
           if (this == o) return true;
           if (o == null || getClass() != o.getClass()) return false;
           Edge edge = (Edge) o;
           return this.to.equals(edge.to);
       }

       @Override
       public int hashCode() {
           return Objects.hash(to);
       }


       @Override
       public int compareTo(Edge o) {
           if (this == o) return 0;
           return Integer.compare(this.priority, o.priority);
       }

    public int getPriority() {
            return priority;
    }
}
    //heuristic function
    static int heuristic(MapLocation start, MapLocation destination) {
        int dx = Math.abs(start.x - destination.x);
        int dy = Math.abs(start.y - destination.y);
        return 2*(dx + dy) - Math.min(dx, dy);
    }

    // A* moveTo
    static int lastHeuristic = 0;
    static void moveTo(RobotController rc, MapLocation destination) throws GameActionException {
        PriorityQueue<Edge> open = new PriorityQueue<>();
        PriorityQueue<Edge> closed = new PriorityQueue<>();
        MapLocation start = rc.getLocation();
        Edge startEdge = new Edge(start, start, 0);
        if (start.equals(destination)) return;
        closed.add(startEdge);
        double c = 0.15;
        int coolDown = rc.getMovementCooldownTurns();
        System.out.println(coolDown);
        for (Direction d : directions) {
            MapLocation adj = start.add(d);
            if(!rc.onTheMap(adj) || rc.canSenseRobotAtLocation(adj)) {
                continue;
            }
            int weight = (int) Math.round(c * rc.senseRubble(adj));
            Edge edge = new Edge(adj, adj, weight);
            edge.setPriority(edge.getWeight() + heuristic(adj, destination));
            open.add(edge);
            if (coolDown < 30) {
                closed.add(edge);
            }
        }

        if (coolDown > 30) {
            int r = Math.min(5 + coolDown/10, rc.getType().visionRadiusSquared / 3);
            while (!open.isEmpty()) {
                Edge edge1 = open.poll();
                closed.add(edge1);
                MapLocation midPoint = edge1.getTo();
                for (Direction d : directions) {
                    MapLocation adj = midPoint.add(d);
                    if (!start.isWithinDistanceSquared(adj, r) || rc.canSenseRobotAtLocation(adj) || !rc.onTheMap(adj)) {
                        continue;
                    }
                    int weight = (int) Math.round(c * rc.senseRubble(adj));
                    Edge edge = new Edge(edge1.getFrom(), adj, edge1.getWeight() + weight);
                    if (!open.contains(edge) && !closed.contains(edge)) {
                        edge.setPriority(edge.getWeight() + heuristic(adj, destination));
                        open.add(edge);
                    }
                }
            }
        }

        Direction direction;
        Edge edge = closed.poll();
        if (edge == null) {
            direction = start.directionTo(destination);}
        else {
            int newHeuristic;
            // check if the robot stuck somewhere
            if (turnCount % 4 == 0) {
                newHeuristic = heuristic(edge.getTo(), destination);
                if (Math.abs(lastHeuristic-newHeuristic) < 3 && rc.getMovementCooldownTurns() < 20){
                    direction = start.directionTo(destination);
                }else {
                    direction = start.directionTo(edge.from);
                }
                lastHeuristic = newHeuristic;
            }else{
                direction = start.directionTo(edge.from);
            }
        }


        if (rc.canMove(direction)) {
            rc.move(direction);
        }
    }
}
