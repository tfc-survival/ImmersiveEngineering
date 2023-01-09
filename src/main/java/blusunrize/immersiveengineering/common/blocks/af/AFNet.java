package blusunrize.immersiveengineering.common.blocks.af;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Mod.EventBusSubscriber
public class AFNet {
    private static Map<BlockPos, Set<BlockPos>> connections = new HashMap<>(100);
    private static boolean isDirty = true;
    public static ImmutableMap<BlockPos, ImmutableSet<BlockPos>> transformersDirectConnections = ImmutableMap.of();

    public static void addConnection(BlockPos a, BlockPos b) {
        connections.computeIfAbsent(a, __ -> new HashSet<>()).add(b);
        connections.computeIfAbsent(b, __ -> new HashSet<>()).add(a);
        isDirty = true;

        get().markDirty();
    }

    public static void removeNode(BlockPos a) {
        Set<BlockPos> other = connections.remove(a);
        if (other != null)
            for (BlockPos b : other)
                connections.get(b).remove(a);

        get().markDirty();
    }

    private static int t = 0;

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (isDirty) {
            t++;
            if (t >= 200) {
                t = 0;
                recalculateDirectConnections();
                isDirty = false;

                get().markDirty();
            }
        }
    }

    private static void recalculateDirectConnections() {

    }

    @SubscribeEvent
    public static void load(WorldEvent.Load event) {
        if (event.getWorld().provider.getDimension() == 0) {
            get(event.getWorld());
        }
    }

    @SubscribeEvent
    public static void save(WorldEvent.Save event) {
        if (event.getWorld().provider.getDimension() == 0) {

        }
    }

    private static WSD get() {
        return get(DimensionManager.getWorld(0));
    }

    private static WSD get(World world) {
        MapStorage storage = world.getMapStorage();
        WSD instance = (WSD) storage.getOrLoadData(WSD.class, WSD.name);

        if (instance == null) {
            instance = new WSD();
            storage.setData(WSD.name, instance);
        }
        return instance;
    }

    public static class WSD extends WorldSavedData {
        public static final String name = "af_net";

        public WSD() {
            super(name);
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            isDirty = nbt.getBoolean("isDirty");
            connections = readConnections(nbt, "connections", Collectors::toMap, Collectors.toSet());
            transformersDirectConnections = readConnections(nbt, "transformersDirectConnections", ImmutableMap::toImmutableMap, ImmutableSet.toImmutableSet());
        }

        private <SetOfPos extends Set<BlockPos>, MapOfConnections extends Map<BlockPos, SetOfPos>> MapOfConnections readConnections(
                NBTTagCompound nbt, String name,
                BiFunction<Function<Pair<BlockPos, SetOfPos>, BlockPos>, Function<Pair<BlockPos, SetOfPos>, SetOfPos>, Collector<Pair<BlockPos, SetOfPos>, ?, MapOfConnections>> toMap,
                Collector<BlockPos, ?, SetOfPos> toSet
        ) {
            NBTTagList connectionsNbt = nbt.getTagList(name, 10);
            return IntStream.range(0, connectionsNbt.tagCount())
                    .mapToObj(connectionsNbt::getCompoundTagAt)
                    .map(entryNbt -> {
                        BlockPos key = BlockPos.fromLong(entryNbt.getLong("key"));
                        NBTTagList valueNbt = entryNbt.getTagList("value", 4);

                        SetOfPos value = IntStream.range(0, valueNbt.tagCount())
                                .mapToObj(valueNbt::get)
                                .map(e -> ((NBTTagLong) e))
                                .map(NBTTagLong::getLong)
                                .map(BlockPos::fromLong)
                                .collect(toSet);

                        return Pair.of(key, value);
                    })
                    .collect(toMap.apply(Pair::getKey, Pair::getValue));
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            nbt.setBoolean("isDirty", isDirty);
            writeConnections(nbt, connections, "connections");
            writeConnections(nbt, transformersDirectConnections, "transformersDirectConnections");
            return nbt;
        }

        private <SetOfPos extends Set<BlockPos>> void writeConnections(NBTTagCompound nbt, Map<BlockPos, SetOfPos> connections, String name) {
            NBTTagList connectionsNbt = new NBTTagList();
            for (Map.Entry<BlockPos, SetOfPos> entry : connections.entrySet()) {
                NBTTagCompound entryNbt = new NBTTagCompound();

                entryNbt.setLong("key", entry.getKey().toLong());

                NBTTagList valueNbt = new NBTTagList();
                for (BlockPos p : entry.getValue())
                    valueNbt.appendTag(new NBTTagLong(p.toLong()));
                entryNbt.setTag("value", valueNbt);

                connectionsNbt.appendTag(entryNbt);
            }
            nbt.setTag(name, connectionsNbt);
        }
    }
}
