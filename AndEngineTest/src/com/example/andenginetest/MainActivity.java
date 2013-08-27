package com.example.andenginetest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.color.Color;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class MainActivity extends BaseGameActivity {

    private Scene scene;
    protected static final int CAMERA_WIDTH = 800;
    protected static final int CAMERA_HEIGHT = 480;

    private Text scoreText;
    private int score = 0;
    private BitmapTextureAtlas playerTexture;
    private ITextureRegion playerTextureRegion;
    private PhysicsWorld physicsWorld;
    private Sprite sPlayer;
    
    private List<Sprite> spriteList;
    
    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }*/


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public EngineOptions onCreateEngineOptions() {
        Camera mCamera = new Camera(0,0,CAMERA_WIDTH,CAMERA_HEIGHT);
        EngineOptions options = new EngineOptions(true,
                ScreenOrientation.LANDSCAPE_FIXED,
                new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT),
                mCamera);

        return options;
    }

    @Override
    public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws Exception {
        loadGraphics();
        pOnCreateResourcesCallback.onCreateResourcesFinished();

    }

    private void loadGraphics() {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("graphics/");
        playerTexture = new BitmapTextureAtlas(getTextureManager(), 64, 64);
        playerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(playerTexture,
                this, "smiley.png", 0, 0);
        playerTexture.load();

    }

    @Override
    public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {   	
    	spriteList = new ArrayList<Sprite>();
    	
        this.scene = new Scene();
        scene.setBackground(new Background(0,213.0f/256.0f,255.0f/256.0f));
        
        physicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
        scene.registerUpdateHandler(physicsWorld);
        
        createWalls();
        pOnCreateSceneCallback.onCreateSceneFinished(this.scene);

    }

	private void createWalls() {
		FixtureDef WALL_FIX = PhysicsFactory.createFixtureDef(0.0f, 0.0f, 0.0f);
		Rectangle ground = new Rectangle(0, CAMERA_HEIGHT - 15, CAMERA_WIDTH,
				15, this.mEngine.getVertexBufferObjectManager());
		ground.setColor(new Color(15, 50, 0));
		PhysicsFactory.createBoxBody(physicsWorld, ground, BodyType.StaticBody,
				WALL_FIX);
		this.scene.attachChild(ground);
		
		Rectangle leftWall = new Rectangle(0, 15, 15,
				CAMERA_HEIGHT, this.mEngine.getVertexBufferObjectManager());
		leftWall.setColor(new Color(15, 50, 0));
		PhysicsFactory.createBoxBody(physicsWorld, leftWall, BodyType.StaticBody,
				WALL_FIX);
		this.scene.attachChild(leftWall);
		
		Rectangle rightWall = new Rectangle(CAMERA_WIDTH - 15, 15, 15,
				CAMERA_HEIGHT, this.mEngine.getVertexBufferObjectManager());
		rightWall.setColor(new Color(15, 50, 0));
		PhysicsFactory.createBoxBody(physicsWorld, rightWall, BodyType.StaticBody,
				WALL_FIX);
		this.scene.attachChild(rightWall);
	}


	@Override
	public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
		
		float xSeconds = 1.5f;
		boolean repeat = true;

		TimerHandler myTimer = new TimerHandler(xSeconds, repeat, new ITimerCallback() {
	        public void onTimePassed(TimerHandler pTimerHandler) {
	        	createSprite();
	        }
	    });
		
	    scene.registerUpdateHandler(myTimer);   // here you register the timerhandler to your scene	

		loadScore();

		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}

	private void destroySmiley(final Smiley smiley) {
		
		if(smiley.isDeleted())
			return;
		
		smiley.setDeleted(true);
		
		this.runOnUpdateThread(new Runnable() {
			@Override
			public void run() {								
				score++;
				updateScore();
				
				/* Now it is save to remove the entity! */
				Body body = smiley.getmBody();
				physicsWorld.unregisterPhysicsConnector(physicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(smiley));
				physicsWorld.destroyBody(body);				
				spriteList.remove(smiley);
				scene.unregisterTouchArea(smiley);
				scene.detachChild(smiley);
			}
		});
	}
	
	private void createSprite() {
		Random r = new Random();
		float x = (CAMERA_WIDTH/4) + r.nextInt(CAMERA_WIDTH/2);
		float y = (CAMERA_HEIGHT/4) + r.nextInt(CAMERA_HEIGHT/2);
		
		sPlayer = new Smiley(x, y,
				playerTextureRegion,
				this.mEngine.getVertexBufferObjectManager(), physicsWorld) {
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
					final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				

				// Only accept action down
				/*!pSceneTouchEvent.isActionDown() && */
				if(!pSceneTouchEvent.isActionMove())
					return true;
				
				// Insert Code Here				
				destroySmiley(this);

				return true;
			}
		};
		
		spriteList.add(sPlayer);
		
		sPlayer.setRotation(45.0f);
		scene.registerTouchArea(sPlayer);
		scene.setTouchAreaBindingOnActionDownEnabled(true);
		scene.attachChild(sPlayer);		
	}

	protected void updateScore() {
		// TODO Auto-generated method stub
		MainActivity.this.runOnUpdateThread(new Runnable() {
			@Override
			public void run() {
				scoreText.setText("Score: "+score);	
			}
		});			
	}

	private void loadScore() {
		Font mFont = FontFactory.create(this.getFontManager(), this.getTextureManager(),
        		256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32);
        
        mFont.load();

        scoreText = new Text(100, 40, mFont, "Score:           ",
        		this.getVertexBufferObjectManager());
        
        scoreText.setPosition(50,50);
        scoreText.setText("Score: "+score);
        
        scene.attachChild(scoreText);
	}
}
