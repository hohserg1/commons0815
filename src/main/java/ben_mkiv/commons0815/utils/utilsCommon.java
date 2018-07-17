package ben_mkiv.commons0815.utils;

import com.google.common.base.Charsets;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.Vector3f;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class utilsCommon {
	private static HashMap<String, Long> cooldownTimer = new HashMap<>();

	public static ArrayList<BlockPos> getSquareOffsets(int radius){
		ArrayList<BlockPos> list = new ArrayList();
		for(int row = -radius; row <= radius; row++)
			for(int col = -radius; col <= radius; col++) {
				ArrayList<Integer> l = new ArrayList();
				list.add(new BlockPos(row, 0, col));
			}

		return list;
	}


	public static TileEntity getTileEntity(int dimensionId, BlockPos pos){
		World world  = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dimensionId);
		if(world==null)
			return null;
		return world.getTileEntity(pos);
	}

	public static Map<BlockPos, Block> getSquareBlocks(World world, BlockPos pos, int radius){
		Map<BlockPos, Block> list = new HashMap<>();

		for(BlockPos val : utilsCommon.getSquareOffsets(radius))
			list.put(val, world.getBlockState(new BlockPos(pos.getX() + val.getX(), pos.getY(), pos.getZ() + val.getZ())).getBlock());

		return list;
	}

	@SideOnly(Side.CLIENT)
	public static boolean checkCooldown(String identifier) {
		return checkCooldown(identifier, 500);
	}

	@SideOnly(Side.CLIENT)
	public static boolean checkCooldown(String identifier, long cooldownTime){
		long timenow = Minecraft.getSystemTime();

		long timeout = 0;
		if(cooldownTimer.get(identifier) != null)
			timeout = cooldownTimer.get(identifier);

		if(timenow - timeout < cooldownTime)
			return false;

		cooldownTimer.remove(identifier);
		cooldownTimer.put(identifier, timenow);
		return true;
	}

	public static int getTierFromXP(int level){
		if(level >= 40)
			return 4;
		else if(level >= 30)
			return 3;
		else if(level >= 20)
			return 2;
		else if(level >= 10)
			return 1;

		return 0;
	}

	public static int getExpFromLevel(int level) {
		if (level > 30) {
			return (int) (4.5 * level * level - 162.5 * level + 2220);
		}
		if (level > 15) {
			return (int) (2.5 * level * level - 40.5 * level + 360);
		}
		return level * level + 6 * level;
	}

	public static int getLevelFromExp(long exp) {
		if (exp > 1395) {
			return (int) Math.round((Math.sqrt(72 * exp - 54215) + 325) / 18);
		}
		if (exp > 315) {
			return (int) Math.round(Math.sqrt(40 * exp - 7839) / 10 + 8.1);
		}
		if (exp > 0) {
			return (int) Math.round(Math.sqrt(exp + 9) - 3);
		}
		return 0;
	}

	public static ArrayList<Class> getClassHierarchy(Class p) {
		ArrayList<Class> list = new ArrayList<>();
		list.add(p);
		while((p = p.getSuperclass()) != null)
			list.add(p);

		return list;
	}

	// modClazz was UrbanMechs.class
	public static ArrayList<String> readResourceLocation(Class modClazz, String asset){
		InputStream is = null;
		try {
			is = modClazz.getClassLoader().getResourceAsStream("assets/urbanmechs" + asset);
			final BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));
			final ArrayList<String> lines = new ArrayList<>();
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
			return lines;
		} catch (Throwable ignored) {
			return null;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException ignored) {
				}
			}
		}
	}

	public static ArrayList<String> readResourceLocation(Class modClazz, ResourceLocation rl) {
		return readResourceLocation(modClazz, rl.getResourcePath());
	}

	public static String StringFromResourceLocation(Class modClazz, ResourceLocation rl){
		return utilsCommon.list2string(utilsCommon.readResourceLocation(modClazz, rl));
	}

	public static String list2string(ArrayList<String> list){
		String output = "";

		if(list != null)
			for(String append : list)
				output+="\n"+append;

		return output;
	}

	public static int getIntFromColor(float red, float green, float blue, float alpha){
	    Color col = new Color(red, green, blue, alpha);
	    return col.getRGB();
	}

	public static Vector3f getVector3fFromColor(int color){
		Color col = new Color(color);
		return new Vector3f(col.getRed(), col.getGreen(),col.getBlue());
	}

	public static int getIntFromColor(float red, float green, float blue){
		return utilsCommon.getIntFromColor(red, green, blue, 0);
	}
	
	public static boolean isIntColorVisible(int color){
		return (color & 0x000000FF) > 0;
	}

	public static float deg2rad(float deg){
		return ((float) Math.PI * deg / 180);
	}
	
	public static boolean inRange(double x, double y, double z, double sx, double sy, double sz, double r){
		return (((x-sx)*(x-sx)) + ((y-sy)*(y-sy)) + ((z-sz)*(z-sz))) <= (r*r);
	}
	
	public static boolean inRange(Entity player, double sx, double sy, double sz, double r){
		return inRange(player.posX, player.posY, player.posZ, sx, sy, sz, r);
	}
	
	public static boolean isLookingAt(RayTraceResult pos, float[] target){
		if(pos == null) return false;
		if(pos.getBlockPos().getX() != target[0]) return false;
		if(pos.getBlockPos().getY() != target[1]) return false;
		if(pos.getBlockPos().getZ() != target[2]) return false;
					
		return true;
	}

	@SideOnly(Side.CLIENT)
	public static Entity getEntityLookingAt(){
		RayTraceResult objectMouseOver = Minecraft.getMinecraft().player.rayTrace(128, 1);
		if(objectMouseOver != null && objectMouseOver.typeOfHit == RayTraceResult.Type.ENTITY){
			return objectMouseOver.entityHit;
		}
		return null;
	}


	public static RayTraceResult rayTrace(Entity entity, double range, float tickAge){
		Vec3d vec3d = entity.getPositionEyes(tickAge);
		Vec3d vec3d1 = entity.getLook(tickAge);
		Vec3d vec3d2 = vec3d.addVector(vec3d1.x * range, vec3d1.y * range, vec3d1.z * range);

		return entity.world.rayTraceBlocks(vec3d, vec3d2);
	}

	@SideOnly(Side.CLIENT)
	public static Entity getFocusedEntity(){
		EntityPlayer player = Minecraft.getMinecraft().player;
		return player.isSneaking() ? player : Minecraft.getMinecraft().objectMouseOver.entityHit;
	}

}
