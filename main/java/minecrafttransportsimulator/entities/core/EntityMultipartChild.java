package minecrafttransportsimulator.entities.core;

import javax.annotation.Nullable;

import minecrafttransportsimulator.baseclasses.MTSEntity;
import minecrafttransportsimulator.baseclasses.MTSVector;
import minecrafttransportsimulator.helpers.EntityHelper;
import minecrafttransportsimulator.systems.RotationSystem;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

/**Main child class.  This class is the base for all child entities and should be
 * extended to use the parent-child linking system.
 * Use {@link EntityMultipartParent#addChild(String, EntityMultipartChild, boolean)} to add children 
 * and {@link EntityMultipartParent#removeChild(String, boolean)} to kill and remove them.
 * You may extend {@link EntityMultipartParent} to get more functionality with those systems.
 * Beware of children with offsetZ of 0, as they can cause problems with pitch calculations.
 * Also note that all children must have a constructor of the form: 
 * public EntityChild(World world, EntityParent parent, String parentUUID, float offsetX, float offsetY, float offsetZ, float width, float height, int propertyCode)
 * 
 * @author don_bruce
 */
public abstract class EntityMultipartChild extends EntityMultipartBase{	
	/** Can a rider of this child send inputs to the parent.*/
	public boolean isController;
	/** Does this child rotate in-sync with the yaw changes of the parent.*/
	public boolean turnsWithSteer;
	/** Integer for storing data about color, type, and other things.*/
	public int propertyCode;
	public float offsetX;
	public float offsetY;
	public float offsetZ;
	public EntityMultipartParent parent;
	protected String parentUUID;
	
	public EntityMultipartChild(World world) {
		super(world);
	}
	
	public EntityMultipartChild(World world, EntityMultipartParent parent, String parentUUID, float offsetX, float offsetY, float offsetZ, float width, float height, int propertyCode){
		this(world);
		this.offsetX=offsetX;
		this.offsetY=offsetY;
		this.offsetZ=offsetZ;
		this.setSize(width, height);
		this.propertyCode=propertyCode;
		this.UUID=String.valueOf(this.getUniqueID());
		this.parentUUID=parentUUID;
		MTSVector offset = RotationSystem.getRotatedPoint(offsetX, offsetY, offsetZ, parent.rotationPitch, parent.rotationYaw, parent.rotationRoll);
		this.setPositionAndRotation(parent.posX+offset.xCoord, parent.posY+offset.yCoord, parent.posZ+offset.zCoord, parent.rotationYaw, parent.rotationPitch);
	}
	
	@Override
	public void onEntityUpdate(){
		super.onEntityUpdate();
		linked = hasUUID() && hasParent();
	}
	
	@Override
    public boolean performRightClickAction(MTSEntity clicked, EntityPlayer player){
		return parent != null ? parent.performRightClickAction(clicked, player) : false;
	}
	
	@Override
	public boolean performAttackAction(DamageSource source, float damage){
		if(!worldObj.isRemote){
			if(isDamageWrench(source)){
				return true;
			}else if(!attackChild(source, damage)){
				return parent != null ? parent.performAttackAction(source, damage) : false;
			}else{
				return true;
			}
		}else{
			return true;
		}
    }
	
	/**Checks to see if damage came from a player holding a wrench.
	 * Removes the entity if so, dropping the entity as an item.
	 * Called each attack before deciding whether or not to forward damage to the parent
	 * or inflict damage on this child.
	 */
	protected boolean isDamageWrench(DamageSource source){
		if(source.getEntity() instanceof EntityPlayer){
			if(EntityHelper.isPlayerHoldingWrench((EntityPlayer) source.getEntity())){
				ItemStack droppedItem = this.getItemStack();
				worldObj.spawnEntityInWorld(new EntityItem(worldObj, posX, posY, posZ, droppedItem));
				parent.removeChild(UUID, false);
				return true;
			}
		}
		return false;
	}
	
	/**Called when child is attacked.  Return true to end attack, false to forward attack to parent. 
	 */
	protected abstract boolean attackChild(DamageSource source, float damage);
	
	/**Sets the NBT of the entity to that of the stack.
	 */
	public abstract void setNBTFromStack(ItemStack stack);
	
	/**Gets an ItemStack that represents the entity.
	 * This is called when removing the entity from the world to return an item.
	 */
	public abstract ItemStack getItemStack();
	
	public boolean hasParent(){
		if(this.parent==null){
			if(ticksExisted==1 || ticksExisted%10==0){
				this.linkToParent();
			}
			return false;
		}
		return true;
	}
	
	private void linkToParent(){
		MTSEntity entity = EntityHelper.getEntityByUUID(worldObj, (this.parentUUID));
		if(entity != null){
			EntityMultipartParent parent =  (EntityMultipartParent) entity;
			parent.addChild(this.UUID, this, false);
			this.parent=parent;
		}
	}
	
	@Override
	public boolean canBeCollidedWith(){
		//This gets overridden to do collisions with players.
		return true;
	}
	
	@Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(){
		//Need this to do collision with other Entities.
        return this.getEntityBoundingBox();
    }
	
	public boolean collidesWithLiquids(){
		return false;
	}


	public boolean isOnGround(){
		return worldObj.getCollisionBoxes(this.getEntityBoundingBox().offset(0, -0.05F, 0)).isEmpty() ? EntityHelper.isBoxCollidingWithBlocks(worldObj, this.getEntityBoundingBox().offset(0, -0.05F, 0), this.collidesWithLiquids()) : true;
	}

	
	public void setController(boolean isController){
		this.isController = true;
	}
	
	public void setTurnsWithSteer(boolean turnsWithSteer){
		this.turnsWithSteer = turnsWithSteer;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tagCompound){
		super.readFromNBT(tagCompound);
		this.isController=tagCompound.getBoolean("isController");
		this.turnsWithSteer=tagCompound.getBoolean("turnsWithSteer");
		this.propertyCode=tagCompound.getInteger("propertyCode");
		this.offsetX=tagCompound.getFloat("offsetX");
		this.offsetY=tagCompound.getFloat("offsetY");
		this.offsetZ=tagCompound.getFloat("offsetZ");
		this.parentUUID=tagCompound.getString("parentUUID");
		this.width=tagCompound.getFloat("width");
		this.height=tagCompound.getFloat("height");
    	this.setSize(width, height);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound){
		super.writeToNBT(tagCompound);
		tagCompound.setBoolean("isController", this.isController);
		tagCompound.setBoolean("turnsWithSteer", this.turnsWithSteer);
		tagCompound.setInteger("propertyCode", this.propertyCode);
		tagCompound.setFloat("offsetX", this.offsetX);
		tagCompound.setFloat("offsetY", this.offsetY);
		tagCompound.setFloat("offsetZ", this.offsetZ);
		tagCompound.setFloat("width", this.width);
		tagCompound.setFloat("height", this.height);
		if(!this.parentUUID.isEmpty()){
			tagCompound.setString("parentUUID", this.parentUUID);
		}
		return tagCompound;
	}
}
