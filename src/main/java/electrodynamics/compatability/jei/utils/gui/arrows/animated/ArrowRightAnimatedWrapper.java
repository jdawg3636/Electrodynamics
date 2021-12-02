package electrodynamics.compatability.jei.utils.gui.arrows.animated;

import electrodynamics.compatability.jei.utils.gui.arrows.stat.ArrowRightStaticWrapper;
import mezz.jei.api.gui.drawable.IDrawableAnimated.StartDirection;

public class ArrowRightAnimatedWrapper extends ArrowAnimatedWrapper {

	public ArrowRightAnimatedWrapper(int xStart, int yStart) {
		super(ARROWS, xStart, yStart, 0, 15, 22, 15, new ArrowRightStaticWrapper(xStart, yStart));
	}
	
	public StartDirection getStartDirection() {
		return StartDirection.LEFT;
	}

}
