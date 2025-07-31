package dev.zenolth.the_fog.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;

import java.awt.*;

@Environment(EnvType.CLIENT)
public class RenderUtil {
    private static void drawBlockOutline(
            MatrixStack matrices,
            VertexConsumer vertexConsumer,
            BlockPos blockPos,
            Vec3d cameraPos,
            Color color
    ) {
        double offsetX = (double) blockPos.getX() - cameraPos.getX();
        double offsetY = (double) blockPos.getY() - cameraPos.getY();
        double offsetZ = (double) blockPos.getZ() - cameraPos.getZ();
        MatrixStack.Entry entry = matrices.peek();
        VoxelShapes.fullCube().forEachEdge(
                (minX, minY, minZ, maxX, maxY, maxZ) -> {
                    vertexConsumer
                            .vertex(
                                    entry.getPositionMatrix(),
                                    (float)(minX + offsetX), (float)(minY + offsetY), (float)(minZ + offsetZ)
                            )
                            .color(color.getRGB())
                            .next();
                    vertexConsumer
                            .vertex(
                                    entry.getPositionMatrix(),
                                    (float)(maxX + offsetX), (float)(maxY + offsetY), (float)(maxZ + offsetZ)
                            )
                            .color(color.getRGB())
                            .next();
                }
        );
    }

    public static void drawBoxAtPos(WorldRenderContext worldRenderContext, BlockPos blockPos, Color lineColor, float lineWidth) {
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR);

        drawBlockOutline(worldRenderContext.matrixStack(),buffer,blockPos,worldRenderContext.camera().getPos(),lineColor);

        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.lineWidth(lineWidth);
        RenderSystem.setShaderColor(1f,1f,1f,1f);

        tessellator.draw();

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
    }
}
