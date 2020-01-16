package com.ecabezue.drop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import java.util.Iterator;

public class GameScreen implements Screen {
    private Texture goldenDropImage;
    private Texture dropImage;
    private Texture bucketImage;
    private Sound dropSound;
    private Music rainMusic;
    private OrthographicCamera camera;
    public SpriteBatch batch;
    private Rectangle bucket;
    private Array<Rectangle> raindrops;
    private long lastDropTime;
    private long lastPowerUpTime;
    private BitmapFont puntuacio;
    private BitmapFont fps;
    private BitmapFont actualPowerUp;
    private int contador;
    private float speed = 4;
    final Drop game;
    private boolean powerUpSpawned = false;
    private long timePowerUp;
    private long maxPowerUpTime;
    private Texture dropGamebg;
    private Array<Rectangle> powerUps;
    private Array<Rectangle> goldenDrops;
    private Texture powerUp;
    private Sprite sprPowerUp;
    private Sprite sprGoldenDrop;
    private boolean powerUpBucketBig = false;
    private boolean powerUpBucketTiny = false;
    private boolean powerUpInvertControl = false;
    private boolean powerUpGoldenDrop = false;

    private String activePowerUp = "No hi ha cap poder equipat...";

    public GameScreen(final Drop game) {
        this.game = game;
        // Carreguem les imatges del cub, de la gota, dels powerups, del background i del goldendrop
        dropImage = new Texture(Gdx.files.internal("droplet.png"));
        bucketImage = new Texture(Gdx.files.internal("bucket.png"));
        goldenDropImage = new Texture(Gdx.files.internal("goldendrop.png"));
        dropGamebg = new Texture(Gdx.files.internal("dropgamebg.jpg"));
        powerUp = new Texture(Gdx.files.internal("powerup.png"));

        // Inicialitzem els sprites (per redimensionar)
        sprPowerUp = new Sprite(powerUp);
        sprPowerUp.setSize(64, 64);

        sprGoldenDrop = new Sprite(goldenDropImage);
        sprGoldenDrop.setSize(64, 64);

        // Inicialitzem les fonts
        puntuacio = new BitmapFont();
        fps = new BitmapFont();
        actualPowerUp = new BitmapFont();

        // Carreguem el só
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

        // Fem que es repeteixi cada cop que acabi el só de fons de la pluja
        // I fem que soni
        rainMusic.setLooping(true);
        rainMusic.play();

        // Inicialitzem la camera amb la resolució escollida
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        // Inicialitzem el batch (Objecte que renderitza els sprites 2D)
        batch = new SpriteBatch();

        // Inicialitzem el bucket com a rectangle:
        // El centrem horitzontalment i l'apugem 20 pixels cap amunt de la y
        bucket = new Rectangle();
        bucket.x = 800 / 2 - 64 / 2;
        bucket.y = 20;

        // Li setegem width i height
        bucket.width = 64;
        bucket.height = 64;

        // Inicialitzem els arrays
        powerUps = new Array<Rectangle>();
        goldenDrops = new Array<Rectangle>();
        raindrops = new Array<Rectangle>();

        if(bucket.x < 0) bucket.x = 0;
        if(bucket.x > 800 - 64) bucket.x = 800 - 64;


        spawnRaindrop();
    }

    public void render (float delta) {
        // Setegem un blau com a color del joc
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Es una bona practica actualitzar la camera per cada frame
        camera.update();

        // Renderitzem el bucket
        // Indiquem que el batch utilitzi el sistema de coordenades de la camera
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(dropGamebg, 0, 0, 800, 480);
        if(powerUpBucketBig){
            bucket.width = 128;
            bucket.height = 128;
            batch.draw(bucketImage, bucket.x, bucket.y, 128, 128);
        }else if(powerUpBucketTiny){
            bucket.width = 32;
            bucket.height = 32;
            batch.draw(bucketImage, bucket.x, bucket.y, 32, 32);
        }else {
            bucket.width = 64;
            bucket.height = 64;
            batch.draw(bucketImage, bucket.x, bucket.y);
        }

        actualPowerUp.draw(batch, activePowerUp, 300, 460);
        fps.draw(batch, "fps: " + Gdx.graphics.getFramesPerSecond(), 10, 460);
        puntuacio.draw(batch, "Punts: " + contador, 720, 460);

        for (Rectangle pow : powerUps) {
            batch.draw(sprPowerUp, pow.x, pow.y, 64, 64);
        }
        if(powerUpGoldenDrop) {
            for (Rectangle goldendrop : goldenDrops) {
                batch.draw(sprGoldenDrop, goldendrop.x, goldendrop.y, 64, 64);
            }
        }else {
            for(Rectangle raindrop: raindrops) {
                batch.draw(dropImage, raindrop.x, raindrop.y);
            }
        }
        batch.end();

        // Comprovem si es toca la pantalla o es clica el mouse
        if(Gdx.input.isTouched()) {
            if(powerUpInvertControl){
                // Guardarem la posició a un vector
                Vector3 touchPos = new Vector3();
                touchPos.set((800 / 2 )- (Gdx.input.getX() / 1.5f), -Gdx.input.getY(), 0);
                // Transformem el sistema de coordenades al de la camera
                camera.unproject(touchPos);
                // Fiquem les coordenades al bucket
                bucket.x = touchPos.x - 64 / 2;
            }else{
                // Guardarem la posició a un vector
                Vector3 touchPos = new Vector3();
                touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                // Transformem el sistema de coordenades al de la camera
                camera.unproject(touchPos);
                // Fiquem les coordenades al bucket
                bucket.x = touchPos.x - 64 / 2;
            }
        }

        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            if(powerUpInvertControl) {
                bucket.x += 400 * Gdx.graphics.getDeltaTime();
            }else{
                bucket.x -= 400 * Gdx.graphics.getDeltaTime();
            }
        }
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            if(powerUpInvertControl){
                bucket.x -= 400 * Gdx.graphics.getDeltaTime();
            }else{
                bucket.x += 400 * Gdx.graphics.getDeltaTime();
            }
        }

        if((TimeUtils.nanoTime() - lastPowerUpTime) > 2000000000l){
            powerUpSpawned = false;
        }

        if((TimeUtils.nanoTime() - timePowerUp) > 5000000000l) {
            powerUpBucketBig = false;

        }

        if((TimeUtils.nanoTime() - timePowerUp) > 8000000000l) {
            powerUpBucketTiny = false;

        }

        if((TimeUtils.nanoTime() - timePowerUp) > 3000000000l) {
            powerUpInvertControl = false;

        }

        if((TimeUtils.nanoTime() - timePowerUp) > 12000000000l) {
            powerUpGoldenDrop = false;
            goldenDrops.clear();

        }

        if(!powerUpBucketBig && !powerUpBucketTiny && !powerUpInvertControl && !powerUpGoldenDrop) {
            resetPowerUpString();
        }

        if(TimeUtils.nanoTime() - lastDropTime > 2000000000l && !powerUpSpawned) {
            if(powerUpGoldenDrop) {
                spawnGoldenDrop();
            }else{
                spawnRaindrop();
            }
        }

        maxPowerUpTime = (long)((Math.random() * 18000000000l) + 9000000000l);

        if(TimeUtils.nanoTime() - lastPowerUpTime > maxPowerUpTime) spawnPowerUp();

        for (Iterator<Rectangle> iter = powerUps.iterator(); iter.hasNext(); ) {
            Rectangle powerUp = iter.next();
            powerUp.y -= speed;
            if(powerUp.overlaps(bucket) && powerUp.y > (70 - speed)) {
                dropSound.play();
                iter.remove();
                contador+=10;
                compAccio((int)((Math.random() * 4) + 1));
            }
            if(powerUp.y + 64 < 0) {
                iter.remove();
            }
        }

        if(powerUpGoldenDrop){
            raindrops.clear();
            for (Iterator<Rectangle> iter = goldenDrops.iterator(); iter.hasNext(); ) {

                Rectangle raindrop = iter.next();
                raindrop.y -= speed;
                if(raindrop.overlaps(bucket) && raindrop.y > ((bucket.height - 14) - speed)) {
                    dropSound.play();
                    iter.remove();
                    contador+=5;
                }
                if(raindrop.y + 64 < 0) {
                    game.setScreen(new YouLost(game));
                }
            }
        }else{
            for (Iterator<Rectangle> iter = raindrops.iterator(); iter.hasNext(); ) {

                Rectangle raindrop = iter.next();
                raindrop.y -= speed;
                if(raindrop.overlaps(bucket) && raindrop.y > ((bucket.height - 14) - speed)) {
                    dropSound.play();
                    iter.remove();
                    contador++;
                    if(contador % 10 == 0){
                        speed *= 1.4f;
                    }
                }
                if(raindrop.y + 64 < 0) {
                    iter.remove();
                    rainMusic.stop();
                    game.setScreen(new YouLost(game));
                }
            }
        }

    }

    private void spawnRaindrop() {
        Rectangle raindrop = new Rectangle();
        raindrop.x = MathUtils.random(0, 800-64);
        raindrop.y = 480;
        raindrop.width = 64;
        raindrop.height = 64;
        raindrops.add(raindrop);
        lastDropTime = TimeUtils.nanoTime();
    }

    private void spawnGoldenDrop(){
        Rectangle goldenDrop = new Rectangle();
        goldenDrop.x = MathUtils.random(0, 800-64);
        goldenDrop.y = 480;
        goldenDrop.width = 64;
        goldenDrop.height = 64;
        goldenDrops.add(goldenDrop);
        lastDropTime = TimeUtils.nanoTime();
    }

    private void spawnPowerUp() {
        Rectangle powerUpRect = new Rectangle();
        powerUpRect.x = (float)(Math.random() * (800 - 64));
        powerUpRect.y = 480;
        powerUpRect.width = 64;
        powerUpRect.height = 64;
        powerUps.add(powerUpRect);
        lastPowerUpTime = TimeUtils.nanoTime();
        powerUpSpawned = true;
    }

    private void compAccio(int accio){
        switch(accio){
            case 1:
                powerUpBucketBig = true;
                activePowerUp = "Activat poder de cub gran";
                break;
            case 2:
                powerUpBucketTiny = true;
                activePowerUp = "Activat poder de cub petit";
                break;
            case 3:
                powerUpInvertControl = true;
                activePowerUp = "Activat poder de controls inversos";
                break;
            case 4:
                powerUpGoldenDrop = true;
                activePowerUp = "Activat poder de gotes daurades (Punts x5)";
                break;
        }
        timePowerUp = TimeUtils.nanoTime();
    }

    private void resetPowerUpString(){
        activePowerUp = "No hi ha cap poder equipat...";
    }

    @Override
    public void show() {
        rainMusic.play();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose () {
        dropImage.dispose();
        bucketImage.dispose();
        dropSound.dispose();
        rainMusic.dispose();
        batch.dispose();
    }
}
