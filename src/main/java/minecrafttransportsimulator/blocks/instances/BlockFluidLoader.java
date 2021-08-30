package minecrafttransportsimulator.blocks.instances;

import minecrafttransportsimulator.baseclasses.Point3d;
import minecrafttransportsimulator.blocks.components.ABlockBaseTileEntity;
import minecrafttransportsimulator.blocks.components.IBlockFluidTankProvider;
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntityFluidLoader;
import minecrafttransportsimulator.mcinterface.WrapperNBT;
import minecrafttransportsimulator.mcinterface.WrapperPlayer;
import minecrafttransportsimulator.mcinterface.WrapperWorld;

public class BlockFluidLoader extends ABlockBaseTileEntity implements IBlockFluidTankProvider{
	
	public BlockFluidLoader(){
		super(10.0F, 5.0F);
	}
	
    @Override
	public TileEntityFluidLoader createTileEntity(WrapperWorld world, Point3d position, WrapperPlayer placingPlayer, WrapperNBT data){
		return new TileEntityFluidLoader(world, position, placingPlayer, data);
	}
}
