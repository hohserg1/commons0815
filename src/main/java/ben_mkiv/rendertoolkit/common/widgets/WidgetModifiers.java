package ben_mkiv.rendertoolkit.common.widgets;

import ben_mkiv.commons0815.utils.utilsCommon;
import ben_mkiv.rendertoolkit.common.widgets.core.attribute.IEasing;
import ben_mkiv.rendertoolkit.common.widgets.core.modifiers.*;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import ben_mkiv.commons0815.chickenbones.Matrix4;

import net.minecraft.util.math.Vec3d;

import javax.vecmath.Vector3f;

public class WidgetModifiers {
	public ArrayList<WidgetModifier> modifiers = new ArrayList<>();
	public long lastConditionStates;
	private Vec3d lastOffset = new Vec3d(0, 0, 0);

	public WidgetModifiers(){}

	public void setCondition(int modifierIndex, short conditionIndex, boolean state){
		this.modifiers.get(modifierIndex).configureCondition(conditionIndex, state);
	}

	public void addEasing(int modifierIndex, String type, String typeIO, float duration, String list, float min, float max, String mode){
		if(!(this.modifiers.get(modifierIndex) instanceof IEasing)) return;

		((IEasing) this.modifiers.get(modifierIndex)).addEasing(type, typeIO, duration, list, min, max, mode);
	}

	public void removeEasing(int modifierIndex, String list){
		if(!(this.modifiers.get(modifierIndex) instanceof IEasing)) return;

		((IEasing) this.modifiers.get(modifierIndex)).removeEasing(list);
	}

	public void update(int modifierIndex, float[] args){
		this.modifiers.get(modifierIndex).update(args);
	}

	public int addTranslate(float x, float y, float z){
		this.modifiers.add(new WidgetModifierTranslate(x, y, z));
		return this.modifiers.size()-1;
	}

	public int addAutoTranslate(float x, float y){
		this.modifiers.add(new WidgetModifierAutoTranslate(x, y));
		return this.modifiers.size()-1;
	}
		
	public int addScale(float x, float y, float z){
		this.modifiers.add(new WidgetModifierScale(x, y, z));
		return this.modifiers.size()-1;
	}
		
	public int addRotate(float deg, float x, float y, float z){
		this.modifiers.add(new WidgetModifierRotate(deg, x, y, z));
		return this.modifiers.size()-1;
	}

	public int addColor(float r, float g, float b, float alpha){
		this.modifiers.add(new WidgetModifierColor(r, g, b, alpha));
		return this.modifiers.size()-1;
	}
	
	public int addTexture(String texloc) {
		this.modifiers.add(new WidgetModifierTexture(texloc));
		return this.modifiers.size()-1;
	}

	public void revoke(long conditionStates){
		int i = modifiers.size();
		while(i > 0) {
			i--;
			modifiers.get(i).revoke(conditionStates);
		}
	}

	public void revoke(long conditionStates, ArrayList<WidgetModifier.WidgetModifierType> modifierTypes){
		int i = modifiers.size();
		WidgetModifier e;
		while(i > 0) {
			i--;
			e = modifiers.get(i);
			for(WidgetModifier.WidgetModifierType x : modifierTypes) if(e.getType().equals(x))
				e.revoke(conditionStates);
		}
	}

	public void remove(int element){
		this.modifiers.remove(element);		
	}
		
	public WidgetModifier.WidgetModifierType getType(int element){
		return this.modifiers.get(element).getType();	
	}
	
	public int getCurrentColor(long conditionStates, int index){
		float[] col = getCurrentColorFloat(conditionStates, index);
		return utilsCommon.getIntFromColor(col[0], col[1], col[2], col[3]);
	}

	public float[] getCurrentColorFloat(int index){
		return getCurrentColorFloat(this.lastConditionStates, index);
	}

	public float[] getCurrentColorFloat(long conditionStates, int index){
		for(WidgetModifier modifier : modifiers){
			if(modifier.getType() == WidgetModifier.WidgetModifierType.COLOR &&
					modifier.shouldApplyModifier(conditionStates) == true){
				if(index > 0){
					index--;
				}
				else {
					Object[] color = modifier.getValues();
					return new float[]{ (float) color[0], (float) color[1], (float) color[2], (float) color[3] };
				}
			}
		}
		return new float[]{ 1, 1, 1, 1 };
	}

	public float[] getCurrentScaleFloat(long conditionStates){
		float scaleX = 1, scaleY = 1, scaleZ = 1;
		for(WidgetModifier modifier : modifiers){
			if(modifier.getType() == WidgetModifier.WidgetModifierType.SCALE && modifier.shouldApplyModifier(conditionStates)){
				Object[] scale = modifier.getValues();
				scaleX *= (float) scale[0];
				scaleY *= (float) scale[1];
				scaleZ *= (float) scale[2];
			}
		}
		return new float[]{ scaleX, scaleY, scaleZ };
	}

	public void apply(long conditionStates){
		lastConditionStates = conditionStates;
		for(WidgetModifier modifier : modifiers)
			modifier.apply(conditionStates);
	}

	public Vec3d getRenderPosition(long conditionStates, Vec3d offset, int w, int h, int d){
		Vec3d renderPosition = this.generateGlMatrix(conditionStates, w, h, d);
		this.lastOffset = offset;

		return new Vec3d(renderPosition.x + offset.x, renderPosition.y + offset.y, renderPosition.z + offset.z);
	}

	public Vec3d generateGlMatrix(long conditionStates, float w, float h, float d){
		Matrix4 m = new Matrix4();
		Object[] b;
		for(WidgetModifier modifier : modifiers)
			if(modifier.shouldApplyModifier(conditionStates)) switch(modifier.getType()){
				case AUTOTRANSLATE:
					b = modifier.getValues();
					m.translate(new Vector3f((float) b[0]*(w/100F), (float) b[1]*(h / 100F), d));
					break;
				case TRANSLATE:
					b = modifier.getValues();
					m.translate(new Vector3f((float) b[0], (float) b[1], (float) b[2]));
					break;
				case SCALE:
					b = modifier.getValues();
					m.scale(new Vector3f((float) b[0], (float) b[1], (float) b[2])); 
					break;
				case ROTATE:
					b = modifier.getValues();
					m.rotate((float) b[0], new Vector3f((float) b[1], (float) b[2], (float) b[3])); 					
					break;
		}
		
		return m.apply(new Vec3d(0F, 0F, 0F));
	}
		
	public void writeData(ByteBuf buff){
		int modifierCount = this.modifiers.size();
		buff.writeInt(modifierCount);
		for(int i=0; i < modifierCount; i++) {
			buff.writeShort(this.modifiers.get(i).getType().ordinal());
			this.modifiers.get(i).writeData(buff);
		}
	}
		
	public void readData(ByteBuf buff){
		ArrayList<WidgetModifier> modifiersNew = new ArrayList<WidgetModifier>();
		for(int i = 0, modifierCount = buff.readInt(); i < modifierCount; i++){
			switch(WidgetModifier.WidgetModifierType.values()[buff.readShort()]){
				case TRANSLATE: modifiersNew.add(new WidgetModifierTranslate(0F, 0F, 0F)); break;
				case COLOR: modifiersNew.add(new WidgetModifierColor(0F, 0F, 0F, 0F)); break;
				case SCALE: modifiersNew.add(new WidgetModifierScale(0F, 0F, 0F)); break;
				case ROTATE: modifiersNew.add(new WidgetModifierRotate(0F, 0F, 0F, 0F)); break;
				case TEXTURE: modifiersNew.add(new WidgetModifierTexture(null)); break;
				case AUTOTRANSLATE: modifiersNew.add(new WidgetModifierAutoTranslate(0F, 0F)); break;
				default: modifiersNew.remove(i); return; //remove modifier if we get bs
			}
			modifiersNew.get(i).readData(buff);
		}
		
		this.modifiers = modifiersNew;
	}		
}
