package mcinterface1122;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;

public class MtsPhosphorTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;

        if ("me.jellysquid.mods.phosphor.mixins.lighting.common.MixinChunk$Vanilla".equals(name)) {
            ClassNode node = new ClassNode();
            ClassReader reader = new ClassReader(basicClass);
            reader.accept(node, 0);

            for (MethodNode method : node.methods) {
                if (method.visibleAnnotations != null) {
                    for (AnnotationNode annotation : method.visibleAnnotations) {
                        if ("Lorg/spongepowered/asm/mixin/injection/ModifyVariable;".equals(annotation.desc)) {
                            for (ListIterator<Object> it = annotation.values.listIterator(); it.hasNext(); ) {
                                Object value = it.next();

                                if ("slice".equals(value)) {
                                    it.remove();
                                    it.next();
                                    it.remove();
                                }
                            }
                        }
                    }
                }
            }
            ClassWriter writer = new ClassWriter(0);
            node.accept(writer);
            return writer.toByteArray();
        }
        return basicClass;
    }
}
