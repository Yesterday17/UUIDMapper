package cn.yesterday17.uuidmapper;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.*;

public class UUIDTransformer implements IClassTransformer {
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
                    String s = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(name, methodName, desc);
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
