package ganymedes01.etfuturum.core.proxy;

import java.io.File;

import com.mojang.authlib.minecraft.MinecraftSessionService;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.ReflectionHelper;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.client.model.ModelShulker;
import ganymedes01.etfuturum.client.renderer.block.BlockBarrelRenderer;
import ganymedes01.etfuturum.client.renderer.block.BlockChestRenderer;
import ganymedes01.etfuturum.client.renderer.block.BlockChorusFlowerRender;
import ganymedes01.etfuturum.client.renderer.block.BlockChorusPlantRender;
import ganymedes01.etfuturum.client.renderer.block.BlockDoorRenderer;
import ganymedes01.etfuturum.client.renderer.block.BlockEndRodRender;
import ganymedes01.etfuturum.client.renderer.block.BlockGlazedTerracottaRenderer;
import ganymedes01.etfuturum.client.renderer.block.BlockLanternRenderer;
import ganymedes01.etfuturum.client.renderer.block.BlockLavaCauldronRenderer;
import ganymedes01.etfuturum.client.renderer.block.BlockSlimeBlockRender;
import ganymedes01.etfuturum.client.renderer.block.BlockTrapDoorRenderer;
import ganymedes01.etfuturum.client.renderer.entity.ArmourStandRenderer;
import ganymedes01.etfuturum.client.renderer.entity.BrownMooshroomRenderer;
import ganymedes01.etfuturum.client.renderer.entity.EndermiteRenderer;
import ganymedes01.etfuturum.client.renderer.entity.HuskRenderer;
import ganymedes01.etfuturum.client.renderer.entity.LingeringEffectRenderer;
import ganymedes01.etfuturum.client.renderer.entity.LingeringPotionRenderer;
import ganymedes01.etfuturum.client.renderer.entity.NewBoatRenderer;
import ganymedes01.etfuturum.client.renderer.entity.NewSnowGolemRenderer;
import ganymedes01.etfuturum.client.renderer.entity.PlacedEndCrystalRenderer;
import ganymedes01.etfuturum.client.renderer.entity.RabbitRenderer;
import ganymedes01.etfuturum.client.renderer.entity.ShulkerBulletRenderer;
import ganymedes01.etfuturum.client.renderer.entity.ShulkerRenderer;
import ganymedes01.etfuturum.client.renderer.entity.StrayOverlayRenderer;
import ganymedes01.etfuturum.client.renderer.entity.StrayRenderer;
import ganymedes01.etfuturum.client.renderer.entity.VillagerZombieRenderer;
import ganymedes01.etfuturum.client.renderer.item.ItemBannerRenderer;
import ganymedes01.etfuturum.client.renderer.item.ItemBowRenderer;
import ganymedes01.etfuturum.client.renderer.item.ItemShulkerBoxRenderer;
import ganymedes01.etfuturum.client.renderer.item.ItemSkullRenderer;
import ganymedes01.etfuturum.client.renderer.tileentity.TileEntityBannerRenderer;
import ganymedes01.etfuturum.client.renderer.tileentity.TileEntityFancySkullRenderer;
import ganymedes01.etfuturum.client.renderer.tileentity.TileEntityGatewayRenderer;
import ganymedes01.etfuturum.client.renderer.tileentity.TileEntityNewBeaconRenderer;
import ganymedes01.etfuturum.client.renderer.tileentity.TileEntityShulkerBoxRenderer;
import ganymedes01.etfuturum.client.renderer.tileentity.TileEntityWoodSignRenderer;
import ganymedes01.etfuturum.client.skins.NewRenderPlayer;
import ganymedes01.etfuturum.client.skins.NewSkinManager;
import ganymedes01.etfuturum.configuration.ConfigBase;
import ganymedes01.etfuturum.configuration.configs.ConfigBlocksItems;
import ganymedes01.etfuturum.core.handlers.ClientEventHandler;
import ganymedes01.etfuturum.entities.EntityArmourStand;
import ganymedes01.etfuturum.entities.EntityBrownMooshroom;
import ganymedes01.etfuturum.entities.EntityEndermite;
import ganymedes01.etfuturum.entities.EntityHusk;
import ganymedes01.etfuturum.entities.EntityLingeringEffect;
import ganymedes01.etfuturum.entities.EntityLingeringPotion;
import ganymedes01.etfuturum.entities.EntityNewBoat;
import ganymedes01.etfuturum.entities.EntityNewSnowGolem;
import ganymedes01.etfuturum.entities.EntityPlacedEndCrystal;
import ganymedes01.etfuturum.entities.EntityRabbit;
import ganymedes01.etfuturum.entities.EntityShulker;
import ganymedes01.etfuturum.entities.EntityShulkerBullet;
import ganymedes01.etfuturum.entities.EntityStray;
import ganymedes01.etfuturum.entities.EntityZombieVillager;
import ganymedes01.etfuturum.tileentities.TileEntityBanner;
import ganymedes01.etfuturum.tileentities.TileEntityGateway;
import ganymedes01.etfuturum.tileentities.TileEntityNewBeacon;
import ganymedes01.etfuturum.tileentities.TileEntityShulkerBox;
import ganymedes01.etfuturum.tileentities.TileEntityWoodSign;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {

	@Override
	public void registerEvents() {
		super.registerEvents();
		FMLCommonHandler.instance().bus().register(ClientEventHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(ClientEventHandler.INSTANCE);
//        MinecraftForge.EVENT_BUS.register(BiomeFogEventHandler.INSTANCE);
	}

	@Override
	public void registerRenderers() {
		registerItemRenderers();
		registerBlockRenderers();
		registerEntityRenderers();
	}

	private void registerItemRenderers() {
		if (ConfigBlocksItems.enableBanners)
			MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlocks.banner), new ItemBannerRenderer());
		if (ConfigBase.enableFancySkulls)
			MinecraftForgeClient.registerItemRenderer(Items.skull, new ItemSkullRenderer());
		if (ConfigBase.enableBowRendering)
			MinecraftForgeClient.registerItemRenderer(Items.bow, new ItemBowRenderer());
		
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlocks.shulker_box), new ItemShulkerBoxRenderer());
	}

	private void registerBlockRenderers() {
		if (ConfigBlocksItems.enableSlimeBlock)
			RenderingRegistry.registerBlockHandler(new BlockSlimeBlockRender());

		if (ConfigBlocksItems.enableDoors)
			RenderingRegistry.registerBlockHandler(new BlockDoorRenderer());

		if (ConfigBlocksItems.enableBanners)
			ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBanner.class, new TileEntityBannerRenderer());

		if (ConfigBase.enableFancySkulls)
			ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySkull.class, new TileEntityFancySkullRenderer());

		if (ConfigBlocksItems.enableChorusFruit) {
			RenderingRegistry.registerBlockHandler(new BlockEndRodRender());
			RenderingRegistry.registerBlockHandler(new BlockChorusFlowerRender());
			RenderingRegistry.registerBlockHandler(new BlockChorusPlantRender());
		}

		if (ConfigBlocksItems.enableColourfulBeacons)
			ClientRegistry.bindTileEntitySpecialRenderer(TileEntityNewBeacon.class, new TileEntityNewBeaconRenderer());

		if (ConfigBlocksItems.enableLantern)
			RenderingRegistry.registerBlockHandler(new BlockLanternRenderer());
		
		if (ConfigBlocksItems.enableBarrel)
			RenderingRegistry.registerBlockHandler(new BlockBarrelRenderer());
		
		if (ConfigBlocksItems.enableTrapdoors)
			RenderingRegistry.registerBlockHandler(new BlockTrapDoorRenderer());
		
		if(ConfigBlocksItems.enableSigns)
			ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWoodSign.class, new TileEntityWoodSignRenderer());

		if(ConfigBlocksItems.enableGlazedTerracotta)
			RenderingRegistry.registerBlockHandler(new BlockGlazedTerracottaRenderer());
		
		if(ConfigBlocksItems.enableLavaCauldrons)
			RenderingRegistry.registerBlockHandler(new BlockLavaCauldronRenderer());
		
			ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGateway.class, new TileEntityGatewayRenderer());
			
			ClientRegistry.bindTileEntitySpecialRenderer(TileEntityShulkerBox.class, new TileEntityShulkerBoxRenderer(new ModelShulker()));
		
		RenderingRegistry.registerBlockHandler(new BlockChestRenderer());
	}

	@SuppressWarnings("unchecked")
	private void registerEntityRenderers() {
		if (ConfigBase.enableArmourStand)
			RenderingRegistry.registerEntityRenderingHandler(EntityArmourStand.class, new ArmourStandRenderer());
		if (ConfigBase.enableEndermite)
			RenderingRegistry.registerEntityRenderingHandler(EntityEndermite.class, new EndermiteRenderer());
		if (ConfigBlocksItems.enableRabbit)
			RenderingRegistry.registerEntityRenderingHandler(EntityRabbit.class, new RabbitRenderer());
		
		if (ConfigBase.enableHusk)
			RenderingRegistry.registerEntityRenderingHandler(EntityHusk.class, new HuskRenderer());
		if (ConfigBase.enableStray) {
			RenderingRegistry.registerEntityRenderingHandler(EntityStray.class, new StrayRenderer());
			RenderingRegistry.registerEntityRenderingHandler(EntityStray.class, new StrayOverlayRenderer());
		}
		
		if (ConfigBlocksItems.enableLingeringPotions) {
			RenderingRegistry.registerEntityRenderingHandler(EntityLingeringPotion.class, new LingeringPotionRenderer());
			RenderingRegistry.registerEntityRenderingHandler(EntityLingeringEffect.class, new LingeringEffectRenderer());
		}
		if (ConfigBase.enableVillagerZombies)
			RenderingRegistry.registerEntityRenderingHandler(EntityZombieVillager.class, new VillagerZombieRenderer());
		
		if (ConfigBase.enableDragonRespawn)
			RenderingRegistry.registerEntityRenderingHandler(EntityPlacedEndCrystal.class, new PlacedEndCrystalRenderer());

		if (ConfigBase.enableBrownMooshroom)
			RenderingRegistry.registerEntityRenderingHandler(EntityBrownMooshroom.class, new BrownMooshroomRenderer());

		if(ConfigBase.enableNewBoats)
			RenderingRegistry.registerEntityRenderingHandler(EntityNewBoat.class, new NewBoatRenderer());
		
		if(ConfigBase.enableShulker) {
			RenderingRegistry.registerEntityRenderingHandler(EntityShulker.class, new ShulkerRenderer());
			RenderingRegistry.registerEntityRenderingHandler(EntityShulkerBullet.class, new ShulkerBulletRenderer());
		}
		
		if (ConfigBase.enablePlayerSkinOverlay) {
			TextureManager texManager = Minecraft.getMinecraft().renderEngine;
			File fileAssets = ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "fileAssets", "field_110446_Y", " field_110607_c");
			File skinFolder = new File(fileAssets, "skins");
			MinecraftSessionService sessionService = Minecraft.getMinecraft().func_152347_ac();
			ReflectionHelper.setPrivateValue(Minecraft.class, Minecraft.getMinecraft(), new NewSkinManager(Minecraft.getMinecraft().func_152342_ad(), texManager, skinFolder, sessionService), "field_152350_aA");

			RenderManager.instance.entityRenderMap.put(EntityPlayer.class, new NewRenderPlayer());
		}
		if (ConfigBase.enableShearableGolems)
			RenderingRegistry.registerEntityRenderingHandler(EntityNewSnowGolem.class, new NewSnowGolemRenderer());
	}
}