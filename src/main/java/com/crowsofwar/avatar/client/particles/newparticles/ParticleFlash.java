package com.crowsofwar.avatar.client.particles.newparticles;

import com.crowsofwar.avatar.common.bending.air.Airbending;
import com.crowsofwar.avatar.common.bending.fire.Firebending;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;

import static com.crowsofwar.avatar.common.config.ConfigClient.CLIENT_CONFIG;

/**
 * Copied from ParticleFirework.Overlay; for some reason that class has no public constructors, plus I want to change the
 * scale and a few other things
 *
 * @author Electroblob, FavouriteDragon
 * @since Wizardry 4.2.0, Av2 1.6.0
 */
//@SideOnly(Side.CLIENT)
//@Optional.Interface(iface = "com.zeitheron.hammercore.api.lighting.impl.IGlowingEntity", modid = "hammercore")
public class ParticleFlash extends ParticleAvatar /*implements IGlowingEntity*/ {

    public ParticleFlash(World world, double x, double y, double z) {
        super(world, x, y, z);
        this.setRBGColorF(1, 1, 1);
        this.setAlphaF(particleAlpha = 1.0F);
        this.particleScale = 0.6f; // 7.1f is the value used in fireworks
        this.particleMaxAge = 6;
    }


    @Override
    public boolean shouldDisableDepth() {
        return true;
    }

    @Override
    public int getFXLayer() {
        return 0;
    }

    @Override
    public void drawParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();


        if (element instanceof Firebending || glow) {
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        }

        if (CLIENT_CONFIG.particleSettings.voxelFlashParticles) {
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        }

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 24f, 240f);

        float f4;
        if (CLIENT_CONFIG.particleSettings.voxelFlashParticles) {
            setRBGColorF(particleRed * 0.875f, particleGreen * 0.875F, particleBlue * 0.875F);
            setAlphaF(particleAlpha * 0.9F);
            f4 = particleScale * MathHelper.sin(((float) this.particleAge + partialTicks - 1.0F) / particleMaxAge * (float) Math.PI);
        } else {
            f4 = particleScale * MathHelper.sin(((float) this.particleAge + partialTicks - 1.0F) / particleMaxAge * (float) Math.PI);
        }

        if (FMLClientHandler.instance().hasOptifine()) {
            if (world.getWorldTime() > 12600 && world.getWorldTime() < 950 && element instanceof Airbending) {
                particleAlpha *= 0.75F;
                f4 = 0.25F * particleScale * MathHelper.sin(((float) this.particleAge + partialTicks - 1.0F) / particleMaxAge * (float) Math.PI);
            } else if (element instanceof Airbending)
                f4 = 0.5F * particleScale * MathHelper.sin(((float) this.particleAge + partialTicks - 1.0F) / particleMaxAge * (float) Math.PI);
            if (element instanceof Airbending)
                setRBGColorF(0.95F, 0.95F, 0.95F);
            if (element instanceof Firebending) {
                particleAlpha *= 1.5F;
            }
        } else
            this.setAlphaF(sparkle ? particleAlpha - ((float) this.particleAge + partialTicks - 1.0F) / particleMaxAge * 0.5F : particleAlpha);
        float f5 = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - interpPosX);
        float f6 = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - interpPosY);
        float f7 = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - interpPosZ);
        int i = this.getBrightnessForRender(partialTicks);
        int j = i >> 16 & 65535;
        int k = i & 65535;


        buffer.pos(f5 - rotationX * f4 - rotationXY * f4, f6 - rotationZ * f4, f7 - rotationYZ * f4 - rotationXZ * f4).tex(0.5D, 0.375D)
                .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos(f5 - rotationX * f4 + rotationXY * f4, f6 + rotationZ * f4, f7 - rotationYZ * f4 + rotationXZ * f4).tex(0.5D, 0.125D)
                .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos(f5 + rotationX * f4 + rotationXY * f4, f6 + rotationZ * f4, f7 + rotationYZ * f4 + rotationXZ * f4).tex(0.25D, 0.125D)
                .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos(f5 + rotationX * f4 - rotationXY * f4, f6 - rotationZ * f4, f7 + rotationYZ * f4 - rotationXZ * f4).tex(0.25D, 0.375D)
                .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();

        GlStateManager.popMatrix();

    }


    @Override
    public int getBrightnessForRender(float partialTicks) {
		/*if (element instanceof Firebending) {
			BlockPos blockpos = new BlockPos(this.posX, this.posY, this.posZ);
			return this.world.isBlockLoaded(blockpos) ? this.world.getCombinedLight(blockpos, Math.min(world.getSkylightSubtracted(), 7)) : 0;
		}
		else */
        return super.getBrightnessForRender(partialTicks);
    }



	/*@Override
	@Optional.Method(modid = "hammercore")
	public ColoredLight produceColoredLight(float v) {
		if (element instanceof Firebending)
			return ColoredLight.builder().pos(posX, posY, posZ).radius(particleScale * 0.5F).color(getRedColorF(), getGreenColorF(), getBlueColorF()).build();
		else return null;
	}**/
}
