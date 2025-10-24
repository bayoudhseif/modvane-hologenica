package com.modvane.hologenica.client.renderer;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;

// Caches the hologram mesh as a VertexBuffer for ultra-fast rendering
// Instead of uploading vertices every frame, we build once and reuse
public class HologramMeshCache {

    private VertexBuffer vertexBuffer = null;
    private boolean needsRebuild = true;
    private RenderType cachedRenderType = null;

    // Build the mesh and cache it as a VBO
    public void buildMesh(List<GreedyMesher.MergedQuad> quads, RenderType renderType) {
        // Release old VBO if exists
        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }

        // Store render type
        cachedRenderType = renderType;

        // Create new vertex buffer
        vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);

        // Build mesh data with identity matrix (transforms applied during draw)
        BufferBuilder builder = new BufferBuilder(new ByteBufferBuilder(quads.size() * 128), VertexFormat.Mode.QUADS, renderType.format());

        Matrix4f identity = new Matrix4f();
        Matrix3f normalIdentity = new Matrix3f();

        // Add all quads to the buffer
        for (GreedyMesher.MergedQuad quad : quads) {
            quad.render(builder, identity, normalIdentity);
        }

        // Upload to GPU
        MeshData meshData = builder.buildOrThrow();
        vertexBuffer.bind();
        vertexBuffer.upload(meshData);
        VertexBuffer.unbind();

        meshData.close();
        needsRebuild = false;
    }

    // Get the cached vertex buffer (null if not built yet)
    public VertexBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    // Get the render type this VBO was built for
    public RenderType getRenderType() {
        return cachedRenderType;
    }

    // Check if mesh needs to be rebuilt
    public boolean needsRebuild() {
        return needsRebuild || vertexBuffer == null;
    }

    // Mark mesh for rebuild (call when terrain/transparency changes)
    public void markDirty() {
        needsRebuild = true;
    }

    // Clean up GPU resources
    public void dispose() {
        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
    }
}
