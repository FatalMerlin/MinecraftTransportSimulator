package minecrafttransportsimulator.packets.instances;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import minecrafttransportsimulator.blocks.components.ABlockBase;
import minecrafttransportsimulator.entities.instances.EntityBullet;
import minecrafttransportsimulator.entities.instances.PartGun;
import minecrafttransportsimulator.mcinterface.AWrapperWorld;
import minecrafttransportsimulator.mcinterface.AWrapperWorld.BlockHitResult;
import minecrafttransportsimulator.packets.components.APacketBase;

/**
 * Packet sent when a bullet hits a block.
 *
 * @author don_bruce
 */
public class PacketEntityBulletHitBlock extends APacketBase {
    private final UUID gunID;
    private final BlockHitResult hitResult;

    public PacketEntityBulletHitBlock(PartGun gun, BlockHitResult hitResult) {
        super(null);
        this.gunID = gun.uniqueUUID;
        this.hitResult = hitResult;
    }

    public PacketEntityBulletHitBlock(ByteBuf buf) {
        super(buf);
        this.gunID = readUUIDFromBuffer(buf);
        this.hitResult = new BlockHitResult(readPoint3dFromBuffer(buf), ABlockBase.Axis.values()[buf.readByte()]);
    }

    @Override
    public void writeToBuffer(ByteBuf buf) {
        super.writeToBuffer(buf);
        writeUUIDToBuffer(gunID, buf);
        writePoint3dToBuffer(hitResult.position, buf);
        buf.writeByte(hitResult.side.ordinal());
    }

    @Override
    public void handle(AWrapperWorld world) {
        EntityBullet.performBlockHitLogic(world.getBulletGun(gunID), hitResult);
    }
}
