package fatcat.ai;

import fatcat.EntityFatCat;
import fatcat.FatCatMod;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityAIShit extends EntityAIBase {
	private EntityFatCat cat;
	private World world;
	public boolean tryExec = false;
	private Vec3 closestPos = null;
	private int giveuptime = 0;
	private int unkoCountDown = 0;

	public EntityAIShit(EntityFatCat cat) {
		this.cat = cat;
		this.world = cat.worldObj;
        this.setMutexBits(13);
	}

	@Override
	public boolean shouldExecute() {
		boolean tryFind = false;
		if (tryExec) {
			tryExec = false;
			tryFind = true;
		}
		else if (closestPos != null) {
			tryFind = false;
		}
		else if (cat.getBladder() > 80 && (cat.ticksExisted % 100) == 0) {
			tryFind = true;
		}
		
		if (!tryFind) {
			return false;
		}
		
		findBlock();
		
		return closestPos != null;
	}
	
	@Override
	public boolean continueExecuting() {
//		System.out.println("EntityAIShit: giveuptime:"+(giveuptime > 0)+",checkBlock:"+(checkBlock(closestPos)));
		if (unkoCountDown > 0 || (giveuptime > 0 && checkBlock(closestPos))) {
			return true;
		}
		else {
			return false;
		}
	}
	
	@Override
	public void resetTask() {
		closestPos = null;
		giveuptime = 0;
		unkoCountDown = 0;
		cat.setAISit(true);
		cat.setPose(EntityFatCat.Pose.None);
	}
	
	@Override
	public void startExecuting() {
		giveuptime = 200;
		unkoCountDown = 0;
		cat.setAISit(false);
		this.cat.cancelPose();
	}
	
	@Override
	public void updateTask() {
		if (unkoCountDown > 0) {
			if (unkoCountDown == 40) {
//	        	System.out.println("EntityAIShit: doShit");
	        	cat.setPose(EntityFatCat.Pose.Shit);
	        	cat.doUnko();
			}
			unkoCountDown--;
			return;
		}
		
        this.cat.getLookHelper().setLookPosition(this.closestPos.xCoord+0.5D, this.closestPos.yCoord, this.closestPos.zCoord+0.5D, 10.0F, (float)this.cat.getVerticalFaceSpeed());
        if ((this.giveuptime%10) == 0) {
        	this.cat.getNavigator().tryMoveToXYZ(this.closestPos.xCoord+0.5D, this.closestPos.yCoord+1, this.closestPos.zCoord+0.5D, 0.3f);
        }
//        System.out.println("EntityAIShit distance: " + this.closestPos.toString() + ", distance=" + cat.getDistance(closestPos.xCoord, closestPos.yCoord, closestPos.zCoord));
        if (cat.getDistanceSqToCenter(new BlockPos(closestPos).offsetUp()) < 1.0D) {
        	unkoCountDown = 60;
			giveuptime = 0;
//        	System.out.println("EntityAIShit: set Unko Countdown");
        }
		giveuptime--;
	}
	

	private void findBlock() {
		Block block;
		double closestPosDistance = 100.0D;
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					Vec3 pos = new Vec3(MathHelper.floor_double(cat.posX+x-8), MathHelper.floor_double(cat.posY+y-1), MathHelper.floor_double(cat.posZ+z-8));
					Vec3 catPos = new Vec3(cat.posX,cat.posY, cat.posZ);
					double d = cat.getDistance(pos.xCoord, pos.yCoord, pos.zCoord);
					if (checkBlock(pos) && (d < closestPosDistance)) {
						FatCatMod.proxy.log(cat.worldObj, "EntityAIShit: found=<%s>, cat=<%s>", pos, catPos);
						this.closestPos = pos;
						closestPosDistance = d;
					}
				}
			}
			if (closestPosDistance < 100.0D) {
				return;
			}
		}
	}

    private boolean checkBlock(Vec3 pos) throws RuntimeException {
    	BlockPos blockPos = new BlockPos(pos.xCoord, pos.yCoord, pos.zCoord);
    	Block block = world.getBlockState(blockPos).getBlock();
    	if (block == null || pos == null) {
    		return false;
    	}
    	if (!world.isAirBlock(blockPos.add(0, 1, 0))) {
    		return false;
    	}
    	
    	if (block == Blocks.sand) {
    		return true;
    	}
    	
		return false;
    }
}
