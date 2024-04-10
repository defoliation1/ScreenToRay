package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.*;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.lang.reflect.Field;
import java.nio.FloatBuffer;

public class PlayerClickScreen {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPreTick(TickEvent.RenderTickEvent event) {
        if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && event.phase == TickEvent.Phase.END){
            Minecraft mc = Minecraft.getMinecraft();
            if(Mouse.isButtonDown(0) && mc.getRenderViewEntity() != null){
                FloatBuffer modelview = null;
                FloatBuffer projection = null;
                try {
                    Field modelviewField = ActiveRenderInfo.class.getDeclaredField("MODELVIEW");
                    modelviewField.setAccessible(true);
                    Field projectionField = ActiveRenderInfo.class.getDeclaredField("PROJECTION");
                    projectionField.setAccessible(true);
                    modelview = (FloatBuffer) modelviewField.get(null);
                    projection = (FloatBuffer) projectionField.get(null);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

                if(modelview!=null){

                    Matrix4f projectionMatrix = (Matrix4f) new Matrix4f().load(projection.asReadOnlyBuffer());
                    Matrix4f modelViewMatrix = (Matrix4f) new Matrix4f().load(modelview.asReadOnlyBuffer());

                    projectionMatrix.invert();
                    modelViewMatrix.invert();
                    int mouseX = Mouse.getX();
                    int mouseY = Mouse.getY();
                    float x = 2f * mouseX / mc.displayWidth - 1;
                    float y = 2f * mouseY / mc.displayHeight - 1;
                    float z = -1;

                    Vector4f vector4f = new Vector4f(x, y, z, 1);
                    Vector4f result = new Vector4f();
                    mul(vector4f,projectionMatrix,result);
                    vector4f.set(result);
                    vector4f.z = -1.0f;
                    vector4f.w = 0.0f;

                    mul(vector4f,modelViewMatrix,result);

                    result.normalise();
                    result.scale(100);
                    result.translate((float) mc.player.posX, (float) (mc.player.posY + mc.player.getEyeHeight()), (float) mc.player.posZ,0);

                    RayTraceResult rayTraceResult = mc.player.world.rayTraceBlocks(new Vec3d(mc.player.posX, (mc.player.posY + mc.player.getEyeHeight()), mc.player.posZ), new Vec3d(result.x, result.y, result.z));
                    if (rayTraceResult != null) {
                        BlockPos blockPos = rayTraceResult.getBlockPos();
                        mc.player.world.setBlockState(blockPos, net.minecraft.init.Blocks.DIAMOND_BLOCK.getDefaultState());
                        System.out.println(rayTraceResult.hitVec);
                    }
                }
            }
        }
    }

    private void mul(Vector4f vector4f, Matrix4f matrix4f, Vector4f result) {
        //向量左乘矩阵
        result.x = matrix4f.m00 * vector4f.x + matrix4f.m10 * vector4f.y + matrix4f.m20 * vector4f.z + matrix4f.m30 * vector4f.w;
        result.y = matrix4f.m01 * vector4f.x + matrix4f.m11 * vector4f.y + matrix4f.m21 * vector4f.z + matrix4f.m31 * vector4f.w;
        result.z = matrix4f.m02 * vector4f.x + matrix4f.m12 * vector4f.y + matrix4f.m22 * vector4f.z + matrix4f.m32 * vector4f.w;
        result.w = matrix4f.m03 * vector4f.x + matrix4f.m13 * vector4f.y + matrix4f.m23 * vector4f.z + matrix4f.m33 * vector4f.w;
    }

}
