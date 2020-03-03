package cn.yesterday17.uuidmapper;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class UUIDTransformer implements IClassTransformer {
    static String mapMethodName(String owner, String name, String desc) {
        Class<?> clazz;
        try {
            try {
                clazz = Class.forName("net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper");
            } catch (ClassNotFoundException e) {
                clazz = Class.forName("cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper");
            }

            Field field = clazz.getDeclaredField("INSTANCE");
            Object instance = field.get(null);
            Method method = clazz.getDeclaredMethod("mapMethodName", String.class, String.class, String.class);
            return (String) method.invoke(instance, owner, name, desc);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
            throw new RuntimeException("Failed to map notch name for UUIDMapper.", ex);
        }
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals("net.minecraft.server.network.NetHandlerLoginServer")) {
            ClassReader cr = new ClassReader(basicClass);
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            ClassVisitor cv = new ClassVisitor(Opcodes.ASM5, cw) {
                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                    super.visit(version, access, name, signature, superName, interfaces);
                }

                @Override
                public MethodVisitor visitMethod(int access, String methodName, String desc, String signature, String[] exceptions) {
                    MethodVisitor mv = cv.visitMethod(access, methodName, desc, signature, exceptions);
                    String s = mapMethodName(name, methodName, desc);
                    if (s.equals("func_152506_a") || s.equals("getOfflineProfile")) {
                        mv = new MethodVisitor(api, mv) {
                            @Override
                            public void visitCode() {
                                mv.visitVarInsn(Opcodes.ALOAD, 1);
                                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cn/yesterday17/uuidmapper/UUIDMapper", "getOfflineProfile", desc, false);
                                mv.visitInsn(Opcodes.ARETURN);
                                mv.visitEnd();
                            }
                        };
                    }
                    return mv;
                }
            };
            cr.accept(cv, ClassReader.EXPAND_FRAMES);
            return cw.toByteArray();
        } else {
            return basicClass;
        }
    }
}
