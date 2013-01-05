//
// This work is licensed under the Creative Commons
// Attribution-ShareAlike 3.0 Unported License. To view a copy of this
// license, visit http://creativecommons.org/licenses/by-sa/3.0/
//

package walkingdead.client;

import walkingdead.common.EntityWalkingDead;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;

@SideOnly(Side.CLIENT)
public class RenderWalkingDead extends RenderBiped {

	private ModelBiped currentModel;
	private ModelWalkingDeadVillager walkerVillager;
	protected ModelBiped adultWalker;
	protected ModelBiped childWalker;
	protected ModelBiped adultWalkerVillager;
	protected ModelBiped childWalkerVillager;
	private int nZombieVillagers = 1;

	public RenderWalkingDead() {
		super(new ModelWalkingDead(), 0.5F, 1.0F);
		currentModel = modelBipedMain;
		walkerVillager = new ModelWalkingDeadVillager();
	}

	protected void func_82421_b() {
		field_82423_g = new ModelWalkingDead(1.0F, true);
		field_82425_h = new ModelWalkingDead(0.5F, true);
		adultWalker = field_82423_g;
		childWalker = field_82425_h;
		adultWalkerVillager = new ModelWalkingDeadVillager(1.0F, 0.0F, true);
		childWalkerVillager = new ModelWalkingDeadVillager(0.5F, 0.0F, true);
	}

	protected int func_82429_a(EntityWalkingDead entityWalkingDead, int par2, float par3) {
		func_82427_a(entityWalkingDead);
		return super.shouldRenderPass(entityWalkingDead, par2, par3);
	}

	public void func_82426_a(EntityWalkingDead entityWalkingDead, double par2, double par4, double par6, float par8, float par9) {
		func_82427_a(entityWalkingDead);
		super.doRenderLiving(entityWalkingDead, par2, par4, par6, par8, par9);
	}

	protected void func_82428_a(EntityWalkingDead entityWalkingDead, float par2) {
		func_82427_a(entityWalkingDead);
		super.renderEquippedItems(entityWalkingDead, par2);
	}

	private void func_82427_a(EntityWalkingDead entityWalkingDead) {
		if (entityWalkingDead.isVillager()) {
			if (nZombieVillagers != walkerVillager.getMaxWalkingDeadVillagers()) {
				walkerVillager = new ModelWalkingDeadVillager();
				nZombieVillagers = walkerVillager.getMaxWalkingDeadVillagers();
				adultWalkerVillager = new ModelWalkingDeadVillager(1.0F, 0.0F, true);
				childWalkerVillager = new ModelWalkingDeadVillager(0.5F, 0.0F, true);
			}

			mainModel = walkerVillager;
			field_82423_g = adultWalkerVillager;
			field_82425_h = childWalkerVillager;
		} else {
			mainModel = currentModel;
			field_82423_g = adultWalker;
			field_82425_h = childWalker;
		}

		modelBipedMain = (ModelBiped) mainModel;
	}

	protected void func_82430_a(EntityWalkingDead entityWalkingDead, float par2, float par3, float par4) {
		if (entityWalkingDead.getEnchantment()) {
			par3 += (float) (Math .cos((double) entityWalkingDead.ticksExisted * 3.25D) * Math.PI * 0.25D);
		}

		super.rotateCorpse(entityWalkingDead, par2, par3, par4);
	}

	protected void renderEquippedItems(EntityLiving entityLiving, float par2) {
		func_82428_a((EntityWalkingDead) entityLiving, par2);
	}

	public void doRenderLiving(EntityLiving entityLiving, double par2, double par4, double par6, float par8, float par9) {
		func_82426_a((EntityWalkingDead) entityLiving, par2, par4, par6, par8, par9);
	}

	protected int shouldRenderPass(EntityLiving entityLiving, int par2, float par3) {
		return func_82429_a((EntityWalkingDead) entityLiving, par2, par3);
	}

	protected void rotateCorpse(EntityLiving entityLiving, float par2, float par3, float par4) {
		func_82430_a((EntityWalkingDead) entityLiving, par2, par3, par4);
	}

	public void doRender(Entity entity, double par2, double par4, double par6, float par8, float par9) {
		func_82426_a((EntityWalkingDead) entity, par2, par4, par6, par8, par9);
	}

}
