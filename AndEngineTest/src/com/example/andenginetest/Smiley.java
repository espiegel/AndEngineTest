package com.example.andenginetest;

import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.vbo.ISpriteVertexBufferObject;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class Smiley extends Sprite {

	private boolean isDeleted = false;
	private Body mBody;
	
	public Smiley(float pX, float pY, ITextureRegion pTextureRegion,
			VertexBufferObjectManager pVertexBufferObjectManager) {
		super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
		// TODO Auto-generated constructor stub
	}
	
	public Smiley(float pX, float pY, ITextureRegion pTextureRegion,
			VertexBufferObjectManager vertexBufferObjectManager, PhysicsWorld pWorld) {
		this(pX, pY, pTextureRegion, vertexBufferObjectManager);
		
		final FixtureDef PLAYER_FIX = PhysicsFactory.createFixtureDef(10.0f,
				1.0f, 0.0f);
		mBody = PhysicsFactory.createCircleBody(pWorld, this,
				BodyType.DynamicBody, PLAYER_FIX);
		pWorld.registerPhysicsConnector(new PhysicsConnector(this,
				mBody, true, false));
	}
		
	public Body getmBody() {
		return mBody;
	}
	
	
	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
}
