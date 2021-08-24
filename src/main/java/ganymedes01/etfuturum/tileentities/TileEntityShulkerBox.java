package ganymedes01.etfuturum.tileentities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.blocks.BlockShulkerBox;
import ganymedes01.etfuturum.configuration.ConfigBase;
import ganymedes01.etfuturum.configuration.configs.ConfigBlocksItems;
import ganymedes01.etfuturum.inventory.ContainerShulkerBox;
import ganymedes01.etfuturum.items.block.ItemShulkerBox;
import ganymedes01.etfuturum.lib.Reference;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class TileEntityShulkerBox extends TileEntity implements IInventory {
	private int ticksSinceSync;
	private String customName;
	public ItemStack[] chestContents;
	public int numPlayersUsing;
	protected boolean brokenInCreative = false;
	public boolean destroyed = false;
	public byte color = 0;
	public byte facing = 0;
	public ShulkerBoxType type;
	
	private boolean firstTick = true;
	
	public static final List<ItemStack> bannedItems = new ArrayList<ItemStack>();

	public TileEntityShulkerBox(int i) {
        this.field_190599_i = TileEntityShulkerBox.AnimationStatus.CLOSED;
        this.topStacks = new ItemStack[8];
		type = ShulkerBoxType.values()[i];
        chestContents = new ItemStack[getSizeInventory() + 9];
	}
	
	public TileEntityShulkerBox() {
        this.field_190599_i = TileEntityShulkerBox.AnimationStatus.CLOSED;
        this.topStacks = new ItemStack[8];
        type = ShulkerBoxType.VANILLA;
        chestContents = new ItemStack[getSizeInventory() + 9];
	}
	
	@Override
	public int getSizeInventory()
	{
		return type.getSize();
	}
	
	public int getRowSize() {
		return type.getRowSize();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer p_70300_1_)
	{
		return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) != this ? false : p_70300_1_.getDistanceSq(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		NBTTagList nbttaglist = nbt.getTagList("Items", 10);
		if(nbttaglist.tagCount() > 0)
		this.chestContents = new ItemStack[this.getSizeInventory()];

		for (int i = 0; i < nbttaglist.tagCount(); ++i)
		{
			NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
			int j = nbttagcompound1.getByte("Slot") & 255;

			if (j >= 0 && j < this.chestContents.length)
			{
				this.chestContents[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
			}
		}
		
		if(type.getIsClear()) {
			NBTTagList displaynbt = nbt.getTagList("Display", 10);
			this.topStacks = new ItemStack[8];
			for (int i = 0; i < displaynbt.tagCount(); ++i)
			{
				NBTTagCompound nbttagcompound1 = displaynbt.getCompoundTagAt(i);
				int j = nbttagcompound1.getByte("Slot") & 255;

				if (j >= 0 && j < this.topStacks.length)
				{
					this.topStacks[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
				}
			}
		}
		
		if (nbt.hasKey("Color") && ConfigBlocksItems.enableDyedShulkerBoxes) {
			color = nbt.getByte("Color");
		}
		
		if (nbt.hasKey("Facing")) {
			facing = nbt.getByte("Facing");
		}

		if (nbt.hasKey("CustomName", 8))
		{
			this.customName = nbt.getString("CustomName");
		}
        sortTopStacks();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		NBTTagList nbttaglist = new NBTTagList();

		for (int i = 0; i < this.chestContents.length; ++i)
		{
			if (this.chestContents[i] != null)
			{
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte("Slot", (byte)i);
				this.chestContents[i].writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
			}
		}

		nbt.setTag("Items", nbttaglist);
		
		if(color > 0) {
			nbt.setByte("Color", color);
		}
		
		if(facing > 0) {
			nbt.setByte("Facing", facing);
		}

		if (this.hasCustomInventoryName())
		{
			nbt.setString("CustomName", this.customName);
		}
	}
	
	@Override
	public ItemStack getStackInSlot(int p_70301_1_)
	{
        inventoryTouched = true;
		return this.chestContents[p_70301_1_];
	}

	/**
	 * Removes from an inventory slot (first arg) up to a specified number (second arg) of items and returns them in a
	 * new stack.
	 */
	@Override
	public ItemStack decrStackSize(int p_70298_1_, int p_70298_2_)
	{
		if (this.chestContents[p_70298_1_] != null)
		{
			ItemStack itemstack;

			if (this.chestContents[p_70298_1_].stackSize <= p_70298_2_)
			{
				itemstack = this.chestContents[p_70298_1_];
				this.chestContents[p_70298_1_] = null;
				this.markDirty();
				return itemstack;
			}
			itemstack = this.chestContents[p_70298_1_].splitStack(p_70298_2_);

			if (this.chestContents[p_70298_1_].stackSize == 0)
			{
				this.chestContents[p_70298_1_] = null;
			}

			this.markDirty();
			return itemstack;
		}
		return null;
	}

	/**
	 * When some containers are closed they call this on each slot, then drop whatever it returns as an EntityItem -
	 * like when you close a workbench GUI.
	 */
	@Override
	public ItemStack getStackInSlotOnClosing(int p_70304_1_)
	{
		if (this.chestContents[p_70304_1_] != null)
		{
			ItemStack itemstack = this.chestContents[p_70304_1_];
			this.chestContents[p_70304_1_] = null;
			return itemstack;
		}
		return null;
	}
	
	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	/**
	 * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
	 */
	@Override
	public void setInventorySlotContents(int p_70299_1_, ItemStack p_70299_2_)
	{
		this.chestContents[p_70299_1_] = p_70299_2_;

		if (p_70299_2_ != null && p_70299_2_.stackSize > this.getInventoryStackLimit())
		{
			p_70299_2_.stackSize = this.getInventoryStackLimit();
		}

		this.markDirty();
	}
	
	@Override
	public void updateEntity()
	{
        this.func_190583_o();

        if (this.field_190599_i == TileEntityShulkerBox.AnimationStatus.OPENING || this.field_190599_i == TileEntityShulkerBox.AnimationStatus.CLOSING)
        {
            this.func_190589_G();
        }
        
		++this.ticksSinceSync;
		float f;

		if (!this.worldObj.isRemote && this.numPlayersUsing != 0 && (this.ticksSinceSync + this.xCoord + this.yCoord + this.zCoord) % 200 == 0)
		{
			this.numPlayersUsing = 0;
			f = 5.0F;
			List<EntityPlayer> list = this.worldObj.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(this.xCoord - f, this.yCoord - f, this.zCoord - f, this.xCoord + 1 + f, this.yCoord + 1 + f, this.zCoord + 1 + f));
			Iterator<EntityPlayer> iterator = list.iterator();

			while (iterator.hasNext())
			{
				EntityPlayer entityplayer = iterator.next();

				if (entityplayer.openContainer instanceof ContainerShulkerBox)
				{
					++this.numPlayersUsing;
				}
			}
		}
        
        if (!worldObj.isRemote && inventoryTouched)
        {
            inventoryTouched = false;
            sortTopStacks();
        }
        
        if(firstTick) {
        	markDirty();
        	firstTick = false;
        }
	}
	
	@Override
	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.customName :  "container." + Reference.MOD_ID + ".shulker_box";
	}
	
	@Override
	public boolean hasCustomInventoryName()
	{
		return this.customName != null && this.customName.length() > 0;
	}

	public void func_145976_a(String p_145976_1_)
	{
		this.customName = p_145976_1_;
	}
	
	@Override
	public boolean receiveClientEvent(int id, int type)
	{
        if (id == 1)
        {
            this.numPlayersUsing = type;

            if (type == 0)
            {
                this.field_190599_i = TileEntityShulkerBox.AnimationStatus.CLOSING;
            }

            if (type == 1)
            {
                this.field_190599_i = TileEntityShulkerBox.AnimationStatus.OPENING;
            }

            return true;
        }
		return super.receiveClientEvent(id, type);
	}

	@Override
	public void openInventory()
	{
		if (this.numPlayersUsing < 0)
		{
			this.numPlayersUsing = 0;
		}

		++this.numPlayersUsing;

		if (this.numPlayersUsing == 1)
		{
			this.worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, Reference.MOD_ID + ":block.shulker_box.open", 1, 1);
		}
		this.worldObj.addBlockEvent(this.xCoord, this.yCoord, this.zCoord, this.getBlockType(), 1, this.numPlayersUsing);
		this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, this.getBlockType());
		this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord - 1, this.zCoord, this.getBlockType());
	}

	@Override
	public void closeInventory()
	{
		if (this.getBlockType() instanceof BlockShulkerBox)
		{
			--this.numPlayersUsing;
			if (this.numPlayersUsing <= 0)
			{
				this.worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, Reference.MOD_ID + ":block.shulker_box.close", 1, 1);
			}
			this.worldObj.addBlockEvent(this.xCoord, this.yCoord, this.zCoord, this.getBlockType(), 1, this.numPlayersUsing);
			this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, this.getBlockType());
			this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord - 1, this.zCoord, this.getBlockType());
		}
	}

	/**
	 * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot.
	 */
	@Override
	public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_)
	{
		return p_94041_2_ == null || !ConfigBase.shulkerBans.contains(p_94041_2_.getItem());
	}

	/**
	 * invalidates a tile entity
	 */
	@Override
	public void invalidate()
	{
		super.invalidate();
		this.updateContainingBlockInfo();
	}
	
	public boolean onBlockBreak(EntityPlayer player) {
		brokenInCreative = player.capabilities.isCreativeMode;
		return true;
	}
	
	
	public void onBlockDestroyed() {
		if (destroyed) return;
		destroyed = true;
		boolean empty = true;
		for (int i = 0; i < chestContents.length; i++) {
			if (chestContents[i] != null) {
				empty = false;
				break;
			}
		}
		// Don't drop an empty Shulker Box in creative.
		if (!empty || !brokenInCreative) {
			ItemStack stack = new ItemStack(ModBlocks.shulker_box, 1, getBlockMetadata());
			
			writeToStack(stack);
    		
			EntityItem item = new EntityItem(worldObj, xCoord + .5D, yCoord + .5D, zCoord + .5D, stack);
			item.motionX = worldObj.rand.nextGaussian() * 0.05F;
			item.motionY = worldObj.rand.nextGaussian() * 0.05F + 0.2F;
			item.motionZ = worldObj.rand.nextGaussian() * 0.05F;
			item.delayBeforeCanPickup = 10;
			worldObj.spawnEntityInWorld(item);
		}
	}
	
	public ItemStack writeToStack(ItemStack stack) {
		
		NBTTagList nbttaglist = new NBTTagList();

		for (int l = 0; l < this.chestContents.length; ++l)
		{
			if (this.chestContents[l] != null)
			{
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte("Slot", (byte)l);
				this.chestContents[l].writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
			}
		}

		stack.setTagCompound(new NBTTagCompound());
		if(nbttaglist.tagCount() > 0)
			stack.getTagCompound().setTag("Items", nbttaglist);

		if(color > 0)
			stack.getTagCompound().setByte("Color", color);
		
		if(this.hasCustomInventoryName())
		{
			stack.setStackDisplayName(this.getInventoryName());
		}
		
		return stack;
	}
	
    private float field_190600_j;
    private float field_190601_k;
    private TileEntityShulkerBox.AnimationStatus field_190599_i;

    public float func_190585_a(float p_190585_1_)
    {
        return this.field_190601_k + (this.field_190600_j - this.field_190601_k) * p_190585_1_;
    }

    protected void func_190583_o()
    {
        this.field_190601_k = this.field_190600_j;

        switch (this.field_190599_i)
        {
            case CLOSED:
                this.field_190600_j = 0.0F;
                break;

            case OPENING:
                this.field_190600_j += 0.1F;

                if (this.field_190600_j >= 1.0F)
                {
                    this.func_190589_G();
                    this.field_190599_i = TileEntityShulkerBox.AnimationStatus.OPENED;
                    this.field_190600_j = 1.0F;
                }

                break;

            case CLOSING:
                this.field_190600_j -= 0.1F;

                if (this.field_190600_j <= 0.0F)
                {
                    this.field_190599_i = TileEntityShulkerBox.AnimationStatus.CLOSED;
                    this.field_190600_j = 0.0F;
                }

                break;

            case OPENED:
                this.field_190600_j = 1.0F;
        }
    }

    public TileEntityShulkerBox.AnimationStatus func_190591_p()
    {
        return this.field_190599_i;
    }
    
    private void func_190589_G()
    {
//        IBlockState iblockstate = this.world.getBlockState(this.getPos());
//
//        if (iblockstate.getBlock() instanceof BlockShulkerBox)
//        {
//            EnumFacing enumfacing = (EnumFacing)iblockstate.getValue(BlockShulkerBox.field_190957_a);
//            AxisAlignedBB axisalignedbb = this.func_190588_c(enumfacing).offset(this.pos);
//            List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity((Entity)null, axisalignedbb);
//
//            if (!list.isEmpty())
//            {
//                for (int i = 0; i < list.size(); ++i)
//                {
//                    Entity entity = (Entity)list.get(i);
//
//                    if (entity.getPushReaction() != EnumPushReaction.IGNORE)
//                    {
//                        double d0 = 0.0D;
//                        double d1 = 0.0D;
//                        double d2 = 0.0D;
//                        AxisAlignedBB axisalignedbb1 = entity.getEntityBoundingBox();
//
//                        switch (enumfacing.getAxis())
//                        {
//                            case X:
//                                if (enumfacing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE)
//                                {
//                                    d0 = axisalignedbb.maxX - axisalignedbb1.minX;
//                                }
//                                else
//                                {
//                                    d0 = axisalignedbb1.maxX - axisalignedbb.minX;
//                                }
//
//                                d0 = d0 + 0.01D;
//                                break;
//
//                            case Y:
//                                if (enumfacing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE)
//                                {
//                                    d1 = axisalignedbb.maxY - axisalignedbb1.minY;
//                                }
//                                else
//                                {
//                                    d1 = axisalignedbb1.maxY - axisalignedbb.minY;
//                                }
//
//                                d1 = d1 + 0.01D;
//                                break;
//
//                            case Z:
//                                if (enumfacing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE)
//                                {
//                                    d2 = axisalignedbb.maxZ - axisalignedbb1.minZ;
//                                }
//                                else
//                                {
//                                    d2 = axisalignedbb1.maxZ - axisalignedbb.minZ;
//                                }
//
//                                d2 = d2 + 0.01D;
//                        }
//
//                        entity.moveEntity(MoverType.SHULKER_BOX, d0 * (double)enumfacing.getFrontOffsetX(), d1 * (double)enumfacing.getFrontOffsetY(), d2 * (double)enumfacing.getFrontOffsetZ());
//                    }
//                }
//            }
//        }
    }
    
    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        
        super.writeToNBT(nbt);
        
        if(type.getIsClear()) {
    		NBTTagList nbttaglist = new NBTTagList();

    		for (int i = 0; i < this.topStacks.length; ++i)
    		{
    			if (this.topStacks[i] != null)
    			{
    				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
    				nbttagcompound1.setByte("Slot", (byte)i);
    				this.topStacks[i].writeToNBT(nbttagcompound1);
    				nbttaglist.appendTag(nbttagcompound1);
    			}
    		}

    		nbt.setTag("Display", nbttaglist);
        }
        
        if(color > 0)
            nbt.setByte("Color", this.color);
        
        if(facing > 0)
            nbt.setByte("Facing", this.facing);
        
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
    {
        readFromNBT(pkt.func_148857_g());
    }

    @Override
    public boolean shouldRefresh(Block oldBlock, Block newBlock, int oldMeta, int newMeta, World world, int x, int y, int z)
    {
        return oldBlock != newBlock && oldMeta != newMeta;
    }

    public int getBlockMetadata()
    {
        if (this.blockMetadata == -1)
        {
        	if(ConfigBlocksItems.enableIronShulkerBoxes) {
                this.blockMetadata = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
        	} else {
            	blockMetadata = 0;
        	}
        }

        return this.blockMetadata;
    }

    public static enum AnimationStatus
    {
        CLOSED,
        OPENING,
        OPENED,
        CLOSING;
    }
    
    public static enum ShulkerBoxType {
    	VANILLA(27, 9, false, 184, 168),
    	IRON(54, 9, false, 184, 202),
    	GOLD(81, 9, false, 184, 256),
    	DIAMOND(108, 12, false, 238, 256),
    	COPPER(45, 9, false, 184, 184),
    	SILVER(72, 9, false, 184, 238),
    	CRYSTAL(108, 12, true, 238, 256),
    	OBSIDIAN(108, 12, false, 238, 256);
    	
    	private int size;
    	private int rowSize;
    	private boolean isClear;
    	private int xSize;
    	private int ySize;
    	
    	private ShulkerBoxType(int size, int rowSize, boolean isClear, int xSize, int ySize) {
    		this.size = size;
    		this.rowSize = rowSize;
    		this.isClear = isClear;
    		this.xSize = xSize;
    		this.ySize = ySize;
    	}
    	
    	public int getSize() {
    		return size;
    	}
    	
    	public int getRowSize() {
    		return rowSize;
    	}
    	
    	public boolean getIsClear() {
    		return isClear;
    	}
    	
    	public int getXSize() {
    		return xSize;
    	}
    	
    	public int getYSize() {
    		return ySize;
    	}
    }
    
    /*
     * IRON CHESTS CODE START
     * 
     * ALL CREDIT TO IRON CHEST 1.7.10 AUTHOR
     * 
     * Iron Shulker Boxes were ported by eye and not by copying code
     * 
     * However, this code copies crystal chest display code
     * from ICs 1.7.10 for accuracy's sake
     * 
     * Some code was also copied to from the chest upgrades
     * to ensure upgrades function exactly the same way.
     */

    private ItemStack[] topStacks;
    private boolean hadStuff;
    private boolean inventoryTouched;

    @Override
    public void markDirty()
    {
        super.markDirty();
        sortTopStacks();
    }

    public ItemStack[] getTopItemStacks()
    {
        return topStacks;
    }

    protected void sortTopStacks()
    {
        if (!type.getIsClear() || (worldObj != null && worldObj.isRemote))
        {
            return;
        }
        ItemStack[] tempCopy = new ItemStack[getSizeInventory()];
        ItemStack[] contents = chestContents.clone();
        boolean hasStuff = false;
        int compressedIdx = 0;
        mainLoop: for (int i = 0; i < getSizeInventory(); i++)
        {
            if (contents[i] != null)
            {
                for (int j = 0; j < compressedIdx; j++)
                {
                    if (tempCopy[j].isItemEqual(contents[i]))
                    {
                        tempCopy[j].stackSize += contents[i].stackSize;
                        continue mainLoop;
                    }
                }
                tempCopy[compressedIdx++] = contents[i].copy();
                hasStuff = true;
            }
        }
        if (!hasStuff && hadStuff)
        {
            hadStuff = false;
            for (int i = 0; i < topStacks.length; i++)
            {
                topStacks[i] = null;
            }
            if (worldObj != null)
            {
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            }
            return;
        }
        hadStuff = true;
        Arrays.sort(tempCopy, new Comparator<ItemStack>() {
            @Override
            public int compare(ItemStack o1, ItemStack o2)
            {
                if (o1 == null)
                {
                    return 1;
                }
                else if (o2 == null)
                {
                    return -1;
                }
                else
                {
                    return o2.stackSize - o1.stackSize;
                }
            }
        });
        int p = 0;
        for (int i = 0; i < tempCopy.length; i++)
        {
            if (tempCopy[i] != null && tempCopy[i].stackSize > 0)
            {
                topStacks[p++] = tempCopy[i];
                if (p == topStacks.length)
                {
                    break;
                }
            }
        }
        for (int i = p; i < topStacks.length; i++)
        {
            topStacks[i] = null;
        }
        if (worldObj != null)
        {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    public void handlePacketData(int typeData, ItemStack[] intData)
    {
        TileEntityShulkerBox chest = this;
        if (blockMetadata != typeData)
        {
            chest = updateFromMetadata(typeData);
        }
        if (blockMetadata == 6 && intData != null)
        {
            int pos = 0;
            for (int i = 0; i < chest.topStacks.length; i++)
            {
                if (intData[pos] != null)
                {
                    chest.topStacks[i] = intData[pos];
                }
                else
                {
                    chest.topStacks[i] = null;
                }
                pos ++;
            }
        }
    }

    public TileEntityShulkerBox updateFromMetadata(int l)
    {
        if (worldObj != null && worldObj.isRemote)
        {
            if (l != blockMetadata)
            {
//                worldObj.setTileEntity(xCoord, yCoord, zCoord, IronChestType.makeEntity(l));
                return (TileEntityShulkerBox) worldObj.getTileEntity(xCoord, yCoord, zCoord);
            }
        }
        return this;
    }
}