/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering;

import blusunrize.immersiveengineering.api.*;
import blusunrize.immersiveengineering.api.energy.wires.*;
import blusunrize.immersiveengineering.api.shader.*;
import blusunrize.immersiveengineering.api.tool.*;
import blusunrize.immersiveengineering.common.*;
import blusunrize.immersiveengineering.common.Config.*;
import blusunrize.immersiveengineering.common.util.*;
import blusunrize.immersiveengineering.common.util.advancements.*;
import blusunrize.immersiveengineering.common.util.commands.*;
import blusunrize.immersiveengineering.common.util.compat.*;
import blusunrize.immersiveengineering.common.util.network.*;
import blusunrize.immersiveengineering.common.world.*;
import net.minecraft.creativetab.*;
import net.minecraft.item.*;
import net.minecraft.world.*;
import net.minecraftforge.common.*;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.*;
import net.minecraftforge.fml.common.network.simpleimpl.*;
import net.minecraftforge.fml.common.registry.*;
import net.minecraftforge.fml.relauncher.*;

import java.util.*;

@Mod(modid = ImmersiveEngineering.MODID, name = ImmersiveEngineering.MODNAME,
    dependencies = "required-after:forge@[14.23.5.2820,);after:jei@[4.8,);after:railcraft;after:tconstruct@[1.12-2.7.1,);after:theoneprobe@[1.4.4,)",
    certificateFingerprint = "4cb49fcde3b43048c9889e0a3d083225da926334", acceptedMinecraftVersions = "[1.12,1.12.2]",
    updateJSON = "https://raw.githubusercontent.com/BluSunrize/ImmersiveEngineering/master/changelog.json")
public class ImmersiveEngineering {
    public static final String MODID = "immersiveengineering";
    public static final String MODNAME = "Immersive Engineering";
    public static final String VERSION = "${version}";
    public static final int DATA_FIXER_VERSION = 1;

    @Mod.Instance(MODID)
    public static ImmersiveEngineering instance = new ImmersiveEngineering();
    @SidedProxy(clientSide = "blusunrize.immersiveengineering.client.ClientProxy", serverSide = "blusunrize.immersiveengineering.common.CommonProxy")
    public static CommonProxy proxy;

    public static final SimpleNetworkWrapper packetHandler = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

    static {
        FluidRegistry.enableUniversalBucket();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        IELogger.logger = event.getModLog();
        Config.preInit(event);

        IEContent.preInit();
        proxy.preInit();

        IEAdvancements.preInit();


        for (int b : IEConfig.Ores.oreDimBlacklist)
            IEWorldGen.oreDimBlacklist.add(b);
        IEApi.modPreference = Arrays.asList(IEConfig.preferredOres);
        IEApi.prefixToIngotMap.put("ingot", new Integer[]{1, 1});
        IEApi.prefixToIngotMap.put("nugget", new Integer[]{1, 9});
        IEApi.prefixToIngotMap.put("block", new Integer[]{9, 1});
        IEApi.prefixToIngotMap.put("plate", new Integer[]{1, 1});
        IEApi.prefixToIngotMap.put("wire", new Integer[]{1, 2});
        IEApi.prefixToIngotMap.put("gear", new Integer[]{4, 1});
        IEApi.prefixToIngotMap.put("rod", new Integer[]{2, 1});
        IEApi.prefixToIngotMap.put("fence", new Integer[]{5, 3});
        IECompatModule.doModulesPreInit();

        IEContent.preInitEnd();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.preInitEnd();
        IEContent.init();
        IEWorldGen ieWorldGen = new IEWorldGen();
        GameRegistry.registerWorldGenerator(ieWorldGen, 0);
        MinecraftForge.EVENT_BUS.register(ieWorldGen);

        MinecraftForge.EVENT_BUS.register(new EventHandler());
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
        proxy.init();

        IESounds.init();

//		Lib.IC2 = Loader.isModLoaded("IC2") && Config.getBoolean("ic2compat");
//		Lib.GREG = Loader.isModLoaded("gregtech") && Config.getBoolean("gregtechcompat");
//		Config.setBoolean("ic2Manual", Lib.IC2);
//		Config.setBoolean("gregManual", Lib.GREG);
        IECompatModule.doModulesInit();
        proxy.initEnd();
        int messageId = 0;
        packetHandler.registerMessage(MessageMineralListSync.Handler.class, MessageMineralListSync.class, messageId++, Side.CLIENT);
        packetHandler.registerMessage(MessageTileSync.HandlerServer.class, MessageTileSync.class, messageId++, Side.SERVER);
        packetHandler.registerMessage(MessageTileSync.HandlerClient.class, MessageTileSync.class, messageId++, Side.CLIENT);
        packetHandler.registerMessage(MessageSkyhookSync.Handler.class, MessageSkyhookSync.class, messageId++, Side.CLIENT);
        packetHandler.registerMessage(MessageMinecartShaderSync.HandlerServer.class, MessageMinecartShaderSync.class, messageId++, Side.SERVER);
        packetHandler.registerMessage(MessageMinecartShaderSync.HandlerClient.class, MessageMinecartShaderSync.class, messageId++, Side.CLIENT);
        packetHandler.registerMessage(MessageRequestBlockUpdate.Handler.class, MessageRequestBlockUpdate.class, messageId++, Side.SERVER);
        packetHandler.registerMessage(MessageNoSpamChatComponents.Handler.class, MessageNoSpamChatComponents.class, messageId++, Side.CLIENT);
        packetHandler.registerMessage(MessageShaderManual.HandlerServer.class, MessageShaderManual.class, messageId++, Side.SERVER);
        packetHandler.registerMessage(MessageShaderManual.HandlerClient.class, MessageShaderManual.class, messageId++, Side.CLIENT);
        packetHandler.registerMessage(MessageBirthdayParty.HandlerClient.class, MessageBirthdayParty.class, messageId++, Side.CLIENT);
        packetHandler.registerMessage(MessageMagnetEquip.Handler.class, MessageMagnetEquip.class, messageId++, Side.SERVER);
        packetHandler.registerMessage(MessageChemthrowerSwitch.Handler.class, MessageChemthrowerSwitch.class, messageId++, Side.SERVER);
        packetHandler.registerMessage(MessageObstructedConnection.Handler.class, MessageObstructedConnection.class, messageId++, Side.CLIENT);
        packetHandler.registerMessage(MessageSetGhostSlots.Handler.class, MessageSetGhostSlots.class, messageId++, Side.SERVER);
        packetHandler.registerMessage(MessageMaintenanceKit.Handler.class, MessageMaintenanceKit.class, messageId++, Side.SERVER);
        packetHandler.registerMessage(MessageProbeContainer.Handler.class, MessageProbeContainer.class, messageId++, Side.CLIENT);

        IEIMCHandler.init();
        IEIMCHandler.handleIMCMessages(FMLInterModComms.fetchRuntimeMessages(instance));
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        IEContent.postInit();
        ExcavatorHandler.recalculateChances(true);
        proxy.postInit();
        IECompatModule.doModulesPostInit();
        proxy.postInitEnd();
        ShaderRegistry.compileWeight();
    }

    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        IECompatModule.doModulesLoadComplete();
    }

    @Mod.EventHandler
    public void modIDMapping(FMLModIdMappingEvent event) {
    }

    private static final String[] alternativeCerts = {
        "7e11c175d1e24007afec7498a1616bef0000027d",// malte0811
        "MavenKeyHere"//TODO maven
    };

    @Mod.EventHandler
    public void wrongSignature(FMLFingerprintViolationEvent event) {
        System.out.println("[Immersive Engineering/Error] THIS IS NOT AN OFFICIAL BUILD OF IMMERSIVE ENGINEERING! Found these fingerprints: " + event.getFingerprints());
        for (String altCert : alternativeCerts)
            if (event.getFingerprints().contains(altCert)) {
                System.out.println("[Immersive Engineering/Error] " + altCert + " is considered an alternative certificate (which may be ok to use in some cases). " +
                    "If you thought this was an official build you probably shouldn't use it.");
                break;
            }
    }


    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting();
        event.registerServerCommand(new CommandHandler(false));
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
            World world = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();
            if (!world.isRemote) {
                IELogger.info("WorldData loading");

                //Clear out any info from previous worlds
                for (int dim : ImmersiveNetHandler.INSTANCE.getRelevantDimensions())
                    ImmersiveNetHandler.INSTANCE.clearAllConnections(dim);
                IESaveData worldData = (IESaveData) world.loadData(IESaveData.class, IESaveData.dataName);

                if (worldData == null) {
                    IELogger.info("WorldData not found");
                    worldData = new IESaveData(IESaveData.dataName);
                    world.setData(IESaveData.dataName, worldData);
                } else
                    IELogger.info("WorldData retrieved");
                IESaveData.setInstance(world.provider.getDimension(), worldData);
            }
        }
        IEContent.refreshFluidReferences();
    }

//	public static Item registerItem(Item item, String name)
//	{
//		ForgeRegistries.ITEMS.register(item.setRegistryName(new ResourceLocation(MODID+":"+name)));
//		return item;
//	}
//	public static Item registerItemByFullName(Item item, String name)
//	{
//		ForgeRegistries.ITEMS.register(item.setRegistryName(new ResourceLocation(name)));
//		return item;
//	}
//	public static Block registerBlockByFullName(Block block, ItemBlock itemBlock, String name)
//	{
//		ResourceLocation rl = new ResourceLocation(name);
//		ForgeRegistries.BLOCKS.register(block.setRegistryName(rl));
//		ForgeRegistries.ITEMS.register(itemBlock.setRegistryName(rl));
//		return block;
//	}
//	public static Block registerBlockByFullName(Block block, Class<? extends ItemBlock> itemBlock, String name)
//	{
//		try{
//			return registerBlockByFullName(block, itemBlock.getConstructor(Block.class).newInstance(block), name);
//		}catch(Exception e){e.printStackTrace();}
//		return null;
//	}
//	public static Block registerBlock(Block block, Class<? extends ItemBlock> itemBlock, String name)
//	{
//		try{
//			return registerBlockByFullName(block, itemBlock.getConstructor(Block.class).newInstance(block), MODID+":"+name);
//		}catch(Exception e){e.printStackTrace();}
//		return null;
//	}


    public static CreativeTabs creativeTab = new CreativeTabs(MODID) {
        @Override
        public ItemStack createIcon() {
            return ItemStack.EMPTY;
        }


        @Override
        public ItemStack getIcon() {
            return new ItemStack(IEContent.blockMetalDecoration0, 1, 0);
        }
    };

}
