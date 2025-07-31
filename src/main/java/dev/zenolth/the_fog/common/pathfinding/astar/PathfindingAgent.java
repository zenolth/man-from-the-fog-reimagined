package dev.zenolth.the_fog.common.pathfinding.astar;

import dev.zenolth.the_fog.common.util.Console;
import dev.zenolth.the_fog.common.util.Timer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PathfindingAgent<E extends MobEntity> {
    public enum NodeType {
        BLOCKED,
        WALKABLE
    }

    private final E entity;
    private final World world;

    private double speed = 1.0;

    private final Queue<PathRequest> queue = new LinkedList<>();
    @Nullable private PathRequest currentRequest;
    @Nullable private List<BlockPos> latestPath = null;

    private boolean reachedTarget = false;
    private boolean computingPath = false;

    private final PriorityQueue<Pair<BlockPos,Integer>> frontier = new PriorityQueue<>(Comparator.comparingInt(Pair::getRight));

    private final HashMap<BlockPos,Integer> costs = new HashMap<>();

    private final HashMap<BlockPos,BlockPos> cameFrom = new HashMap<>();

    private final HashMap<BlockPos, BlockState> debug = new HashMap<>();
    private final Timer debugTimer = new Timer(10,true,this::debugBlocksHaha);

    private int pathLength = 0;
    private int pathLengthThreshold = 0;

    public PathfindingAgent(E entity) {
        this.entity = entity;
        this.world = entity.getWorld();
    }

    public void findPathTo(BlockPos targetPos) {
        if (this.queue.size() >= 10) return;
        this.queue.add(new PathRequest(this.entity.getBlockPos(),targetPos));
    }
    public void findPathTo(Position targetPos) { this.findPathTo(BlockPos.ofFloored(targetPos)); }
    public void findPathTo(int x, int y, int z) { this.findPathTo(new BlockPos(x,y,z)); }
    public void findPathTo(double x, double y, double z) { this.findPathTo(BlockPos.ofFloored(x,y,z));}

    public void setSpeed(double speed) { this.speed = speed; }

    public boolean reachedTarget() { return reachedTarget; }

    public int getHeuristic(BlockPos pos) {
        if (this.currentRequest == null) return Integer.MAX_VALUE;
        return this.currentRequest.targetPos().getManhattanDistance(pos);
    }

    public NodeType getNodeType(BlockPos pos) {
        var isAir = this.world.getBlockState(pos).getCollisionShape(this.world,pos).isEmpty();
        var isGroundAir = this.world.getBlockState(pos.down()).getCollisionShape(this.world,pos.down()).isEmpty();

        if (isAir && !isGroundAir) return NodeType.WALKABLE;

        return NodeType.BLOCKED;
    }

    public int getCost(BlockPos pos) {
        var nodeType = this.getNodeType(pos);
        if (nodeType == NodeType.BLOCKED) return -1;
        return 1;
    }

    public List<BlockPos> reconstructPath(BlockPos current) {
        var totalPath = new ArrayList<BlockPos>();
        totalPath.add(current);

        while (this.cameFrom.containsKey(current)) {
            current = this.cameFrom.get(current);
            totalPath.add(current);
        }

        Collections.reverse(totalPath);

        return totalPath;
    }

    private void pathComputeTick() {
        if (this.queue.isEmpty()) return;
        if (!this.computingPath) {
            this.computingPath = true;
            this.frontier.clear();
            this.costs.clear();
            this.cameFrom.clear();

            this.currentRequest = this.queue.poll();

            this.pathLength = 0;
            var dx = Math.abs(this.currentRequest.targetPos().getX() - this.currentRequest.startPos().getX());
            var dy = Math.abs(this.currentRequest.targetPos().getY() - this.currentRequest.startPos().getY());
            var dz = Math.abs(this.currentRequest.targetPos().getZ() - this.currentRequest.startPos().getZ());
            this.pathLengthThreshold = (int) Math.pow(Math.max(dx,Math.max(dy,dz)),3);

            this.frontier.add(new Pair<>(this.currentRequest.startPos(),0));
            this.costs.put(this.currentRequest.startPos(),0);
            return;
        }

        var current = this.frontier.poll();

        if (current == null) {
            Console.writeln("Frontier empty.");
            this.computingPath = false;
            return;
        }

        Console.writeln(String.format("Pathfinding %s.",this.pathLength));

        this.pathLength++;

        this.reachedTarget = current.getLeft().equals(this.currentRequest.targetPos());

        if (this.reachedTarget || this.pathLength >= this.pathLengthThreshold) {
            if (!this.reachedTarget) {
                Console.writeln("Couldn't reach target.");
            } else {
                Console.writeln("Found path.");
            }
            this.latestPath = this.reconstructPath(current.getLeft());
            this.computingPath = false;
            return;
        }

        for (var next : BlockPos.iterateOutwards(current.getLeft(),1,1,1)) {
            if (this.getNodeType(next) == NodeType.BLOCKED) continue;

            var newCost = this.costs.get(current.getLeft()) + this.getCost(next);
            if (!this.costs.containsKey(next) || newCost < this.costs.getOrDefault(next,Integer.MAX_VALUE)) {
                if (this.costs.containsKey(next)) {
                    this.costs.replace(next,newCost);
                } else {
                    this.costs.put(next,newCost);
                }
                this.frontier.add(new Pair<>(next,newCost + this.getHeuristic(next)));
                this.cameFrom.put(next,current.getLeft());
            }
        }
    }

    @Nullable
    private Integer getClosestIndex() {
        if (this.latestPath == null) return null;
        @Nullable Integer closestIndex = null;
        int closestDistance = Integer.MAX_VALUE;

        for (int index = 0; index < this.latestPath.size(); index++) {
            var current = this.latestPath.get(index);
            var distance = current.getManhattanDistance(this.entity.getBlockPos());
            if (distance < closestDistance) {
                closestIndex = index;
                closestDistance = distance;
            }
        }

        return closestIndex;
    }

    private void fallbackMove(PathRequest request) {
        var targetPos = request.targetPos().toCenterPos();
        this.entity.getMoveControl().moveTo(targetPos.x,targetPos.y,targetPos.z,this.speed);
    }

    private void movementTick() {
        if (this.currentRequest == null) return;
        if (this.latestPath == null) {
            this.fallbackMove(this.currentRequest);
            return;
        }

        var currentIndex = this.getClosestIndex();
        if (currentIndex == null) {
            this.fallbackMove(this.currentRequest);
            return;
        }
        var nextIndex = Math.min(currentIndex + 1,this.latestPath.size() - 1);
        var nextPos = this.latestPath.get(nextIndex).toCenterPos();
        this.entity.getMoveControl().moveTo(nextPos.x,nextPos.y,nextPos.z,this.speed);
    }

    private void debugBlocksHaha() {
        if (this.latestPath == null) return;
        this.debug.forEach(this.world::setBlockState);
        this.debug.clear();
        for (var pos : this.latestPath) {
            this.debug.put(pos,this.world.getBlockState(pos));
            this.world.setBlockState(pos,Blocks.GLASS.getDefaultState());
        }
    }

    public void tick() {
        this.debugTimer.tick();
        this.pathComputeTick();
        this.movementTick();
    }
}
