package com.mastermarisa.maid_restaurant.client.animation;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public class OrderBellAnimation {
	public static final AnimationDefinition SHAKE;

	static {
		SHAKE = AnimationDefinition.Builder.withLength(0.7917F).addAnimation("bone", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM), new Keyframe(0.0417F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM), new Keyframe(0.125F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 2.5F), AnimationChannel.Interpolations.CATMULLROM), new Keyframe(0.25F, KeyframeAnimations.degreeVec(0.0F, -0.5F, -2.0F), AnimationChannel.Interpolations.CATMULLROM), new Keyframe(0.375F, KeyframeAnimations.degreeVec(0.0F, -0.4F, -2.0F), AnimationChannel.Interpolations.CATMULLROM), new Keyframe(0.5417F, KeyframeAnimations.degreeVec(-0.0024F, -0.5447F, 0.0166F), AnimationChannel.Interpolations.CATMULLROM), new Keyframe(0.7083F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM))).build();
	}
}