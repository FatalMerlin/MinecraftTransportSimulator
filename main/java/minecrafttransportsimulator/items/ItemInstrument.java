package minecrafttransportsimulator.items;

import java.util.List;

import minecrafttransportsimulator.dataclasses.MTSCreativeTabs;
import minecrafttransportsimulator.dataclasses.MTSInstruments;
import minecrafttransportsimulator.dataclasses.MTSInstruments.Instruments;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemInstrument extends Item{
	private static final CreativeTabs[] tabList = MTSCreativeTabs.getAllCreativeTabs();
	
	public ItemInstrument(){
		this.hasSubtypes = true;
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack){
	    return stack.getItemDamage() < MTSInstruments.Instruments.values().length ? this.getUnlocalizedName() + "_" + MTSInstruments.Instruments.values()[stack.getItemDamage()].name().toLowerCase() : "_invalid";
	}
	
	@Override
    public CreativeTabs[] getCreativeTabs(){
		return tabList;
    }
	
	@Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List itemList){
		//Iterate though all the instruments and put them on this tab.
		for(Instruments instrument : MTSInstruments.Instruments.values()){
			itemList.add(new ItemStack(item, 1, instrument.ordinal()));
		}
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack item, EntityPlayer player, List list, boolean p_77624_4_){
		list.add(I18n.format(this.getUnlocalizedName(item) + ".description"));
	}
}
