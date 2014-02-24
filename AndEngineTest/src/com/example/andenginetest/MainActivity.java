package com.example.andenginetest;

import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.view.Menu;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.text.Text;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.color.Color;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends BaseGameActivity {

    private Scene scene;
    protected static final int CAMERA_WIDTH = 800;
    protected static final int CAMERA_HEIGHT = 480;

    private Font mFont;
    private Text scoreText;
    private Text levelText;
    //private Text nextLevelText;
    private int score = 0;
    //private int lastClear = 0;
    private int level = 1;

    //                            1  2   3   4   5   6    7    8
    private final int[] levels = {0, 10, 25, 50, 80, 110, 150, 200};
    private boolean destroyAll = false;

    private BitmapTextureAtlas smileyTexture;
    private ITextureRegion smileyTextureRegion;
    private BitmapTextureAtlas sadSmileyTexture;
    private ITextureRegion sadSmileyTextureRegion;
    private PhysicsWorld physicsWorld;
    private Smiley sPlayer;

    TimerHandler myTimer;

    private List<Smiley> spriteList;

    private Sound smallExplosion;
    private Sound mediumExplosion;
    private Sound badSound;
	
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

        options.getAudioOptions().setNeedsSound(true);
        return options;
    }

    @Override
    public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws Exception {
        loadGraphics();
        loadMusic();
        pOnCreateResourcesCallback.onCreateResourcesFinished();

    }

    private void loadMusic() {
        try
        {
            smallExplosion = SoundFactory.createSoundFromAsset(mEngine.getSoundManager(), this,"sounds/small_explosion.ogg");
            mediumExplosion = SoundFactory.createSoundFromAsset(mEngine.getSoundManager(), this,"sounds/medium_explosion.ogg");
            badSound = SoundFactory.createSoundFromAsset(mEngine.getSoundManager(), this,"sounds/bad.ogg");

            smallExplosion.setVolume(0.5f);
            mediumExplosion.setVolume(0.5f);

            badSound.setVolume(0.5f);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    private void loadGraphics() {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("graphics/");
        smileyTexture = new BitmapTextureAtlas(getTextureManager(), 64, 64);
        smileyTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(smileyTexture,
                this, "smiley.png", 0, 0);
        smileyTexture.load();

        sadSmileyTexture = new BitmapTextureAtlas(getTextureManager(), 64, 64);
        sadSmileyTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(sadSmileyTexture,
                this, "sad_smiley.png", 0, 0);
        sadSmileyTexture.load();

    }

    @Override
    public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {
        spriteList = new ArrayList<Smiley>();

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
        createTimer(1.5f, true);

        loadFont();
        loadScore();
        loadLevel();

        pOnPopulateSceneCallback.onPopulateSceneFinished();
    }

    private void createTimer(float xSeconds, boolean repeat) {
        if(myTimer != null)
            scene.unregisterUpdateHandler(myTimer);

        myTimer = new TimerHandler(xSeconds, repeat, new ITimerCallback() {
            public void onTimePassed(TimerHandler pTimerHandler) {
                createSprite();
            }
        });

        scene.registerUpdateHandler(myTimer);   // here you register the timerhandler to your scene
    }

    private void destroyAllSmileys(final Object[] smilies) {
        if(destroyAll)
            return;

        destroyAll = true;

        this.runOnUpdateThread(new Runnable() {
            @Override
            public void run() {
                for(Object s : smilies) {
                    Smiley smiley = (Smiley) s;
                    if(smiley.isDeleted())
                        continue;

                    smiley.setDeleted(true);
                    removeSmiley(smiley);
                }

                destroyAll = false;
            }
        });
    }

    private void destroySmiley(final Smiley smiley) {

        if(smiley.isDeleted())
            return;

        smiley.setDeleted(true);

        this.runOnUpdateThread(new Runnable() {
            @Override
            public void run() {
                if(smiley.isSad())
                    score -= 3;
                else
                    score++;

                boolean levelUp = updateScore();

                if(levelUp) {
                    removeSmiley(smiley);
                    return;
                }

                if(smiley.isSad())
                    badSound.play();
                else
                    smallExplosion.play();

                removeSmiley(smiley);


            }
        });
    }

    private void removeSmiley(final Smiley smiley) {
		/* Now it is save to remove the entity! */
        Body body = smiley.getmBody();
        physicsWorld.unregisterPhysicsConnector(physicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(smiley));
        physicsWorld.destroyBody(body);
        spriteList.remove(smiley);
        scene.unregisterTouchArea(smiley);
        scene.detachChild(smiley);
    }

    private void createSprite() {
        Random r = new Random();
        float x = (CAMERA_WIDTH/8) + r.nextInt(6*CAMERA_WIDTH/8);
        float y = (CAMERA_HEIGHT/8) + r.nextInt(4*CAMERA_HEIGHT/8);

        boolean isSad = false;
        if(r.nextInt(100) > 75)
            isSad = true;

        ITextureRegion smileyRegion = (isSad)?sadSmileyTextureRegion:smileyTextureRegion;

        sPlayer = new Smiley(x, y,
                smileyRegion,
                this.mEngine.getVertexBufferObjectManager(), physicsWorld) {
            @Override
            public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
                                         final float pTouchAreaLocalX, final float pTouchAreaLocalY) {


                // Only accept action down
				/*!pSceneTouchEvent.isActionDown() && */
                if(!pSceneTouchEvent.isActionMove() && !pSceneTouchEvent.isActionDown())
                    return true;

                // Insert Code Here
                if(!destroyAll)
                    destroySmiley(this);

                return true;
            }
        };

        sPlayer.setSad(isSad);

        spriteList.add(sPlayer);

        sPlayer.setRotation((float)r.nextInt(361));
        scene.registerTouchArea(sPlayer);
        scene.setTouchAreaBindingOnActionDownEnabled(true);
        scene.attachChild(sPlayer);
    }

    protected boolean updateLevel() {
        if(level == levels.length)
            return false;

        if(score >= levels[level]) {

            mediumExplosion.play();
            destroyAllSmileys(spriteList.toArray());

            level++;
            createTimer(clip(1.5f - 0.2f * level, 0.2f, 1.5f), true);
            // TODO Auto-generated method stub
            MainActivity.this.runOnUpdateThread(new Runnable() {
                @Override
                public void run() {
                    levelText.setText("Level: "+level);
                    scoreText.setText("Score: "+score+"/"+levels[level]);
                    //nextLevelText.setText("Next Level: "+levels[level]);
                }
            });
            return true;
        }

        return false;
    }

    protected boolean updateScore() {
        // TODO Auto-generated method stub
        MainActivity.this.runOnUpdateThread(new Runnable() {
            @Override
            public void run() {
                scoreText.setText("Score: "+score+"/"+levels[level]);

            }
        });

        return updateLevel();
    }

    private void loadFont() {
        mFont = FontFactory.create(this.getFontManager(), this.getTextureManager(),
                256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32);

        mFont.load();
    }

    private void loadScore() {
        scoreText = new Text(100, 40, mFont, "Score:           ",
                this.getVertexBufferObjectManager());

        scoreText.setPosition(50,30);
        scoreText.setText("Score: "+score+"/"+levels[level]);

        scene.attachChild(scoreText);

        /*nextLevelText = new Text(75, 40, mFont, "Next Level:           ",
                this.getVertexBufferObjectManager());

        nextLevelText.setPosition(50,80);
        nextLevelText.setText("Next Level: "+levels[level]);

        scene.attachChild(nextLevelText);*/
    }

    private void loadLevel() {
        levelText = new Text(100, 40, mFont, "Level:           ",
                this.getVertexBufferObjectManager());

        levelText.setPosition(CAMERA_WIDTH - 170,30);
        levelText.setText("Level: "+level);

        scene.attachChild(levelText);
    }

    private float clip(float num, float min, float max) {
        return (num>max)?max:((num<min)?min:num);
    }

}
