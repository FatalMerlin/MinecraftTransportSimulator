package minecrafttransportsimulator.guis;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;

import minecrafttransportsimulator.MTS;
import minecrafttransportsimulator.dataclasses.MTSRegistry;
import minecrafttransportsimulator.guis.instances.AGUIPanel;
import minecrafttransportsimulator.guis.instances.GUIHUD;
import minecrafttransportsimulator.guis.instances.GUIPanelAircraft;
import minecrafttransportsimulator.guis.instances.GUIPanelGround;
import minecrafttransportsimulator.items.packs.AItemPack;
import minecrafttransportsimulator.items.packs.ItemInstrument;
import minecrafttransportsimulator.jsondefs.AJSONItem;
import minecrafttransportsimulator.jsondefs.JSONVehicle.PackInstrument;
import minecrafttransportsimulator.packets.vehicles.PacketVehicleInstruments;
import minecrafttransportsimulator.vehicles.main.EntityVehicleE_Powered;
import minecrafttransportsimulator.vehicles.main.EntityVehicleF_Air;
import minecrafttransportsimulator.vehicles.main.EntityVehicleF_Ground;
import minecrafttransportsimulator.wrappers.WrapperGUI;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class GUIInstruments extends GuiScreen{
	public static final int screenDefaultX = 854;
	public static final int screenDefaultY = 480;
	
	private final EntityVehicleE_Powered vehicle;
	private final EntityPlayer player;
	private final GUIHUD hudGUI;
	private final AGUIPanel<? extends EntityVehicleE_Powered> panelGUI;
	
	private PackInstrument lastInstrumentClicked = null;
	private final List<ItemInstrument> playerInstruments = new ArrayList<ItemInstrument>();
	private final Map<ItemInstrument, Integer[]> renderedPlayerInstrumentsBounds = new HashMap<ItemInstrument, Integer[]>();
	
	public GUIInstruments(EntityVehicleE_Powered vehicle, EntityPlayer player){
		this.vehicle = vehicle;
		this.player = player;
		this.hudGUI = new GUIHUD(vehicle);
		this.panelGUI = vehicle instanceof EntityVehicleF_Ground ? new GUIPanelGround((EntityVehicleF_Ground) vehicle) : new GUIPanelAircraft((EntityVehicleF_Air) vehicle);
	}
	
	@Override
	public void initGui(){
		super.initGui();
		hudGUI.clearComponents();
		hudGUI.setupComponents(0, 0);
		panelGUI.clearComponents();
		panelGUI.setupComponents(0, 0);
	}

	@Override
    public void drawScreen(int mouseX, int mouseY, float renderPartialTicks){
		if(vehicle.isDead){
			mc.player.closeScreen();
			return;
		}
		
		this.drawDefaultBackground();
		
		//First scale the GUI to match the HUD scaling.
		GL11.glPushMatrix();
		GL11.glScalef(1.0F*this.width/screenDefaultX, 1.0F*this.height/screenDefaultY, 0);
		
		//Draw the main HUD.
		GL11.glPushMatrix();
		GL11.glTranslatef(screenDefaultX/2, screenDefaultY/4, 0);
		GL11.glScalef(0.5F, 0.5F, 0.5F);
		mc.getTextureManager().bindTexture(new ResourceLocation(hudGUI.getTexture()));
		WrapperGUI.renderSheetTexture(0, 0, hudGUI.getWidth(), hudGUI.getHeight(), 0, 0, hudGUI.getWidth(), hudGUI.getHeight(), hudGUI.getTextureWidth(), hudGUI.getTextureHeight());
		
		//Draw the panel.
		GL11.glTranslatef(0, 3*screenDefaultY/4, 0);
		mc.getTextureManager().bindTexture(new ResourceLocation(panelGUI.getTexture()));
		WrapperGUI.renderSheetTexture(0, 0, panelGUI.getWidth(), panelGUI.getHeight(), 0, 0, panelGUI.getWidth(), panelGUI.getHeight(), panelGUI.getTextureWidth(), panelGUI.getTextureHeight());
		
		GL11.glDisable(GL11.GL_LIGHTING);
		mc.entityRenderer.disableLightmap();
		GL11.glPopMatrix();
		
		//Draw a blank square for any instruments that aren't present to let the player know they can be clicked.
		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor3f(54F/255F, 57F/255F, 62F/255F);
		GL11.glTranslatef(screenDefaultX/2, screenDefaultY/4, 0);
		for(byte i=0; i<vehicle.definition.motorized.instruments.size(); ++i){
			if(!vehicle.instruments.containsKey(i)){
				PackInstrument packInstrument = vehicle.definition.motorized.instruments.get(i);
				GL11.glPushMatrix();
		    	if(packInstrument.optionalEngineNumber == 0){
		    		GL11.glTranslated(screenDefaultX*packInstrument.hudX/200F, screenDefaultY*packInstrument.hudY/200F, 0);
		    	}else{
		    		GL11.glTranslated(screenDefaultX*packInstrument.hudY/200F, 3*screenDefaultY/8 + screenDefaultY*packInstrument.hudY/200F, 0);
		    	}
		    	GL11.glScalef(packInstrument.hudScale/2F, packInstrument.hudScale/2F, 0);
		    	GL11.glBegin(GL11.GL_QUADS);
		    	GL11.glVertex2d(-64, 64);
		    	GL11.glVertex2d(64, 64);
		    	GL11.glVertex2d(64, -64);
		    	GL11.glVertex2d(-64, -64);
		    	GL11.glEnd();
				GL11.glPopMatrix();
			}
		}
		GL11.glColor3f(1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glPopMatrix();
		
		//If we clicked an instrument, highlight it.
		if(lastInstrumentClicked != null){
			GL11.glPushMatrix();
	    	GL11.glColor4f(1, 1, 1, 1);
	    	if(lastInstrumentClicked.optionalEngineNumber == 0){
	    		GL11.glTranslated(screenDefaultX/2 + screenDefaultX*lastInstrumentClicked.hudX/200F, screenDefaultY/4 + screenDefaultY*lastInstrumentClicked.hudY/200F, 0);
	    	}else{
	    		GL11.glTranslated(screenDefaultX/2 + screenDefaultX*lastInstrumentClicked.hudX/200F, screenDefaultY/4 + 3*screenDefaultY/8 + screenDefaultY*lastInstrumentClicked.hudY/200F, 0);
	    	}
	    	GL11.glScalef(lastInstrumentClicked.hudScale/2F, lastInstrumentClicked.hudScale/2F, 0);
	    	GL11.glDisable(GL11.GL_TEXTURE_2D);
	    	GL11.glEnable(GL11.GL_BLEND);
	    	GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_SRC_ALPHA);
	    	GL11.glBegin(GL11.GL_QUADS);
	    	GL11.glVertex2d(-64, 64);
	    	GL11.glVertex2d(64, 64);
	    	GL11.glVertex2d(64, -64);
	    	GL11.glVertex2d(-64, -64);
	    	GL11.glEnd();
	    	GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	    	GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glPopMatrix();
		}
		
		//Get all the instruments that fit this vehicle that the player currently has.
		playerInstruments.clear();
		renderedPlayerInstrumentsBounds.clear();
		if(!player.capabilities.isCreativeMode){
			for(ItemStack stack : player.inventory.mainInventory){
				if(stack != null){
					if(stack.getItem() instanceof ItemInstrument){
						ItemInstrument instrumentItem = (ItemInstrument) stack.getItem();
						if(!playerInstruments.contains(instrumentItem)){
							if(instrumentItem.definition.general.validVehicles.contains(vehicle.definition.general.type)){
								byte xIndex = (byte) (playerInstruments.size()%6);
								byte yIndex = (byte) (playerInstruments.size()/6);
								renderedPlayerInstrumentsBounds.put(instrumentItem, new Integer[]{64 + xIndex*64-32, 64 + xIndex*64+32, screenDefaultY/4 + yIndex*64-32, screenDefaultY/4 + yIndex*64+32});
								playerInstruments.add(instrumentItem);
							}	
						}					
					}
				}
			}
		}else{
			for(String packID : MTSRegistry.packItemMap.keySet()){
				for(AItemPack<? extends AJSONItem<?>> packItem : MTSRegistry.packItemMap.get(packID).values()){
					if(packItem instanceof ItemInstrument){
						ItemInstrument instrumentItem = (ItemInstrument) packItem;
						if(instrumentItem.definition.general.validVehicles.contains(vehicle.definition.general.type)){
							byte xIndex = (byte) (playerInstruments.size()%6);
							byte yIndex = (byte) (playerInstruments.size()/6);
							renderedPlayerInstrumentsBounds.put(instrumentItem, new Integer[]{64 + xIndex*64-32, 64 + xIndex*64+32, screenDefaultY/4 + yIndex*64-32, screenDefaultY/4 + yIndex*64+32});
							playerInstruments.add(instrumentItem);
						}
					}
				}
			}
		}
		
		//Now render these instruments.
		for(Entry<ItemInstrument, Integer[]> renderedInstrumentsEntry : renderedPlayerInstrumentsBounds.entrySet()){
			GL11.glPushMatrix();
			GL11.glTranslatef(renderedInstrumentsEntry.getValue()[0], renderedInstrumentsEntry.getValue()[2], 0);
			GL11.glScalef(3.5F, 3.5F, 0);
			mc.getRenderItem().renderItemIntoGUI(new ItemStack(renderedInstrumentsEntry.getKey()), 0, 0);
			GL11.glPopMatrix();
		}
		
		//Render text into the GUI.
		GL11.glPushMatrix();
		GL11.glTranslatef(3*screenDefaultX/4, 2.5F*screenDefaultY/16, 0);
		GL11.glScalef(2.5F, 2.5F, 2.5F);
		fontRenderer.drawString(I18n.format("gui.instruments.main"), (int) (-fontRenderer.getStringWidth(I18n.format("gui.instruments.main"))/2), 0, Color.WHITE.getRGB());
		GL11.glPopMatrix();
		
		GL11.glPushMatrix();
		GL11.glTranslatef(3*screenDefaultX/4, 9*screenDefaultY/16, 0);
		GL11.glScalef(2.5F, 2.5F, 2.5F);
		fontRenderer.drawString(I18n.format("gui.instruments.control"), (int) (-fontRenderer.getStringWidth(I18n.format("gui.instruments.control"))/2), 0, Color.WHITE.getRGB());
		GL11.glPopMatrix();
				
		if(lastInstrumentClicked == null){
			if(vehicle.ticksExisted%40 >= 20){
				GL11.glPushMatrix();
				GL11.glTranslatef(3*screenDefaultX/4, screenDefaultY/16, 0);
				GL11.glScalef(2.5F, 2.5F, 2.5F);
				fontRenderer.drawString(I18n.format("gui.instruments.idle"), (int) (-fontRenderer.getStringWidth(I18n.format("gui.instruments.idle"))/2), 0, Color.WHITE.getRGB());
				GL11.glPopMatrix();
			}
		}else{
			GL11.glPushMatrix();
			GL11.glTranslatef(screenDefaultX/2, screenDefaultY/16, 0);
			GL11.glScalef(2.5F, 2.5F, 2.5F);
			fontRenderer.drawString(I18n.format("gui.instruments.decide"), (int) (-fontRenderer.getStringWidth(I18n.format("gui.instruments.decide"))/2), 0, Color.WHITE.getRGB());
			GL11.glPopMatrix();
		}
		
		GL11.glPushMatrix();
		GL11.glTranslatef(screenDefaultX/4, 11*screenDefaultY/16, 0);
		GL11.glScalef(2.5F, 2.5F, 2.5F);
		fontRenderer.drawString(I18n.format("gui.instruments.clear"), (int) (-fontRenderer.getStringWidth(I18n.format("gui.instruments.clear"))/2), 0, Color.WHITE.getRGB());
		GL11.glPopMatrix();
		
		//Need to do mouseover after main rendering or you get rendering issues.
		//Scale mouse position to the scaled GUI;
		mouseX = (int) (1.0F*mouseX/width*screenDefaultX);
		mouseY = (int) (1.0F*mouseY/height*screenDefaultY);
		for(Entry<ItemInstrument, Integer[]> renderedInstrumentsEntry : renderedPlayerInstrumentsBounds.entrySet()){
			if(mouseX > renderedInstrumentsEntry.getValue()[0] && mouseX < renderedInstrumentsEntry.getValue()[1] && mouseY > renderedInstrumentsEntry.getValue()[2] && mouseY < renderedInstrumentsEntry.getValue()[3]){
				GL11.glPushMatrix();
				GL11.glTranslatef(mouseX, mouseY, 0);
				GL11.glScalef(1.5F, 1.5F, 1.5F);
				renderToolTip(new ItemStack(renderedInstrumentsEntry.getKey()), 0,  0);
				GL11.glPopMatrix();
			}
		}
		
		GL11.glPopMatrix();
	}
	
	@Override
    protected void mouseClicked(int mouseX, int mouseY, int button){
		//Check to see if we clicked an instrument on the HUD.
		//Scale mouse position to the scaled GUI;
		mouseX = (int) (1.0F*mouseX/width*screenDefaultX);
		mouseY = (int) (1.0F*mouseY/height*screenDefaultY);
		for(PackInstrument instrument : vehicle.definition.motorized.instruments){
			final float xCenter = screenDefaultX/2 + screenDefaultX*instrument.hudX/200;
			final float yCenter = screenDefaultY/4 + (instrument.optionalEngineNumber == 0 ? screenDefaultY*instrument.hudY/200 : 3*screenDefaultY/8 + screenDefaultY*instrument.hudY/200);
			if(mouseX > xCenter - 32*instrument.hudScale && mouseX < xCenter + 32*instrument.hudScale && mouseY > yCenter - 32*instrument.hudScale && mouseY < yCenter + 32*instrument.hudScale){
				lastInstrumentClicked = instrument;
				return;
			}
		}
		
		for(byte i=0; i<vehicle.definition.motorized.instruments.size(); ++i){
			if(!vehicle.instruments.containsKey(i)){
				lastInstrumentClicked = vehicle.definition.motorized.instruments.get(i);
			}
		}

		//If we didn't click an instrument on the HUD, and have already selected one on the HUD, see if we clicked one from the inventory.
		if(lastInstrumentClicked != null){
			//Check to see if the player clicked an instrument, and if so which one.
			for(Entry<ItemInstrument, Integer[]> renderedInstrumentsEntry : renderedPlayerInstrumentsBounds.entrySet()){
				if(mouseX > renderedInstrumentsEntry.getValue()[0] && mouseX < renderedInstrumentsEntry.getValue()[1] && mouseY > renderedInstrumentsEntry.getValue()[2] && mouseY < renderedInstrumentsEntry.getValue()[3]){
				//FIXME temp code here.
					MTS.MTSNet.sendToServer(new PacketVehicleInstruments(vehicle, player, getIndexOfLastInstrumentClicked(), renderedInstrumentsEntry.getKey()));
					lastInstrumentClicked = null;
					return;
				}
			}
			
			//Either the player didn't click an instrument, or they clicked the CLEAR.
			if(mouseX > screenDefaultX/4 - 30 && mouseX < screenDefaultX/4 + 30 && mouseY > 11*screenDefaultY/16 - 30 && mouseY < 11*screenDefaultY/16 + 30){
				if(vehicle.instruments.containsKey(getIndexOfLastInstrumentClicked())){
					MTS.MTSNet.sendToServer(new PacketVehicleInstruments(vehicle, player, getIndexOfLastInstrumentClicked(), null));
					lastInstrumentClicked = null;
					return;
				}
			}
		}
	}
	
	private byte getIndexOfLastInstrumentClicked(){
		for(byte i=0; i<vehicle.definition.motorized.instruments.size(); ++i){
			PackInstrument packInstrument = vehicle.definition.motorized.instruments.get(i);
			if(packInstrument.equals(lastInstrumentClicked)){
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public boolean doesGuiPauseGame(){
		return false;
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException{
		if(keyCode == 1 || mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode)){
			super.keyTyped('0', 1);
        }
	}
}
