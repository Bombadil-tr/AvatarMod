package com.crowsofwar.avatar.client.particles;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AvatarParticleBigFlame extends AvatarParticle {

	private static final ResourceLocation TEXTURE = new ResourceLocation("avatarmod",
			"textures/particles/big_flame.png");
	private static final ParticleFrame[] FRAMES = new ParticleFrame[7];

	static {
		for (int i = 0; i < FRAMES.length; i++) {
			FRAMES[i] = new ParticleFrame(TEXTURE, 256, 0, 0, 55, 64);
		}
	}

	public AvatarParticleBigFlame(int particleID, World world, double x, double y, double z, double velX,
								  double velY, double velZ, int... parameters) {

		this(world, x, y, z, velX, velY, velZ);

	}

	protected AvatarParticleBigFlame(World world, double x, double y, double z, double velX, double velY,
									 double velZ) {

		super(world, x, y, z, velX, velY, velZ);
		this.particleRed = 1.0F;
		this.particleGreen = 1.0F;
		this.particleBlue = 1.0F;
		this.setParticleTextureIndex(4);
		this.setSize(0.02F, 0.02F);
		this.particleScale *= this.rand.nextFloat() * 0.6F + 0.2F + 0.3f;
		this.motionX = velX * 0.20000000298023224D + (Math.random() * 2.0D - 1.0D) * 0.019999999552965164D;
		this.motionY = velY * 0.20000000298023224D + (Math.random() * 2.0D - 1.0D) * 0.019999999552965164D;
		this.motionZ = velZ * 0.20000000298023224D + (Math.random() * 2.0D - 1.0D) * 0.019999999552965164D;
		this.particleAge = 0;
		this.particleMaxAge = (int) (8.0D / (Math.random() * 0.8D + 0.2D));

		enableAdditiveBlending();

	}

	@Override
	protected ParticleFrame[] getTextureFrames() {
		return FRAMES;
	}

	@Override
	public int getBrightnessForRender(float partialTick) {
		return 15728880;
	}

}
