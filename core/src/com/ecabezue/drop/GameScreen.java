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
    private Sound looseSound;
    private float speed = 5;
    final Drop game;
    private boolean powerUpSpawned = false;
    private long timePowerUp;
    private long controlSpawnPowerUp;
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
        looseSound = Gdx.audio.newSound(Gdx.files.internal("gameover.wav"));
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

        // Cridem el mètode que crea les gotes
        spawnRaindrop();
    }

    public void render (float delta) {
        // Setegem un blau com a color del joc
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Es una bona practica actualitzar la camera per cada frame
        camera.update();


        // Indiquem que el batch utilitzi el sistema de coordenades de la camera
        batch.setProjectionMatrix(camera.combined);

        // Comencem el dibuix dels elements
        batch.begin();

        // Primer el fons:
        batch.draw(dropGamebg, 0, 0, 800, 480);

        // Si hi ha el poder del bucket gros:
        if(powerUpBucketBig){
            bucket.width = 128;
            bucket.height = 128;
            batch.draw(bucketImage, bucket.x, bucket.y, 128, 128);
        }else if(powerUpBucketTiny){
            // Si hi ha el poder del bucket petit:
            bucket.width = 32;
            bucket.height = 32;
            batch.draw(bucketImage, bucket.x, bucket.y, 32, 32);
        }else {
            // Si no hi ha cap poder que modifiqui el bucket:
            bucket.width = 64;
            bucket.height = 64;
            batch.draw(bucketImage, bucket.x, bucket.y);
        }

        // Imprimim les fonts que mostren informació:
        actualPowerUp.draw(batch, activePowerUp, 300, 460);
        fps.draw(batch, "fps: " + Gdx.graphics.getFramesPerSecond(), 10, 460);
        puntuacio.draw(batch, "Punts: " + game.puntuacio, 720, 460);

        // Dibuixem els powerUps:
        for (Rectangle pow : powerUps) {
            batch.draw(sprPowerUp, pow.x, pow.y, 64, 64);
        }

        // Dibuixem les gotes si són daurades
        if(powerUpGoldenDrop) {
            for (Rectangle goldendrop : goldenDrops) {
                batch.draw(sprGoldenDrop, goldendrop.x, goldendrop.y, 64, 64);
            }
        }else {
            // Si no son daurades, les dibuixem normals:
            for(Rectangle raindrop: raindrops) {
                batch.draw(dropImage, raindrop.x, raindrop.y);
            }
        }
        batch.end();

        // Si el bucket es surt de la pantalla deixa'l al cantó
        // No funciona jugant amb el ratolí.
        if(bucket.x <= 0) bucket.x = 0;
        if(bucket.x >= 800 - 64) bucket.x = 800 - 64;

        // Comprovem si es toca la pantalla o es clica el mouse
        if(Gdx.input.isTouched()) {
            if(powerUpInvertControl){
                // Guardarem la posició a un vector
                Vector3 touchPos = new Vector3();
                // Però, ho farem a la inversa perquè està el poder invers activat
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

        // Si es prem la fletxa esquerre movem el bucket:
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            // Si està el poder d'inversió de controls activat, ho mourem del reves
            if(powerUpInvertControl) {
                bucket.x += 400 * Gdx.graphics.getDeltaTime();
            }else{
                // Si no, ho mourem be:
                bucket.x -= 400 * Gdx.graphics.getDeltaTime();
            }
        }

        // El mateix per la fletxa dreta
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            if(powerUpInvertControl){
                bucket.x -= 400 * Gdx.graphics.getDeltaTime();
            }else{
                bucket.x += 400 * Gdx.graphics.getDeltaTime();
            }
        }

        // Controls de temps dels powerUps
        // Quan ha sortit l'ultim powerUp
        if((TimeUtils.nanoTime() - lastPowerUpTime) > 2000000000l){
            powerUpSpawned = false;
        }

        // PowerUP BucketBig dura 5 segons
        if((TimeUtils.nanoTime() - timePowerUp) > 5000000000l) {
            powerUpBucketBig = false;

        }

        // PowerUP BucketTiny dura 8 segons
        if((TimeUtils.nanoTime() - timePowerUp) > 8000000000l) {
            powerUpBucketTiny = false;

        }

        // PowerUP Controls invesos dura 3 segons
        if((TimeUtils.nanoTime() - timePowerUp) > 3000000000l) {
            powerUpInvertControl = false;
        }

        // PowerUP gotes daurades dura 12 segons
        if((TimeUtils.nanoTime() - timePowerUp) > 12000000000l) {
            powerUpGoldenDrop = false;
            // Netegem les gotes que puguin quedar perquè quan es torni
            // a activar caiguin de dalt altre cop
            goldenDrops.clear();
        }

        // Resetejem la informació en cas que no hi hagi powerUps
        if(!powerUpBucketBig && !powerUpBucketTiny && !powerUpInvertControl && !powerUpGoldenDrop) {
            resetPowerUpString();
        }

        // Mostrem gotes, o gotes daurades depenent de si hi ha el poder o no
        if(TimeUtils.nanoTime() - lastDropTime > 1000000000l && !powerUpSpawned) {
            if(powerUpGoldenDrop) {
                spawnGoldenDrop();
            }else{
                spawnRaindrop();
            }
        }

        // Quant de temps apareixera un PowerUp
        controlSpawnPowerUp = (long)((Math.random() * 18000000000l) + 9000000000l);

        // En el cas d'haverse superat el temps, mostra un powerUp
        if(TimeUtils.nanoTime() - lastPowerUpTime > controlSpawnPowerUp) spawnPowerUp();

        // Mostrem els powerUps si hi ha i fem les accions:
        for (Iterator<Rectangle> iter = powerUps.iterator(); iter.hasNext(); ) {
            Rectangle powerUp = iter.next();
            powerUp.y -= speed;
            if(powerUp.overlaps(bucket) && powerUp.y > (70 - speed)) {
                dropSound.play();
                iter.remove();
                // Els powerUps donen 10 punts:
                game.puntuacio+=10;
                // Cridem el metode de comprovar acció aleatòriament
                compAccio((int)((Math.random() * 4) + 1));
            }
            // Si es cau el powerUp no perdem:
            if(powerUp.y + 64 < 0) {
                iter.remove();
            }
        }

        // Mostrem les gotes daurades si cal i mostrem accions:
        if(powerUpGoldenDrop){
            raindrops.clear();
            for (Iterator<Rectangle> iter = goldenDrops.iterator(); iter.hasNext(); ) {
                Rectangle raindrop = iter.next();
                raindrop.y -= speed;
                if(raindrop.overlaps(bucket) && raindrop.y > ((bucket.height - 14) - speed)) {
                    dropSound.play();
                    iter.remove();
                    // Les gotes daurades donen 5 punts:
                    game.puntuacio+=5;
                }
                // Si cau la gota, no es perd perquè és daurada:
                if(raindrop.y + 64 < 0) {
                    iter.remove();;
                }
            }
        }else{
            // Si no s'han de mostrar les daurades, es mostraran les normals:
            for (Iterator<Rectangle> iter = raindrops.iterator(); iter.hasNext(); ) {

                Rectangle raindrop = iter.next();
                raindrop.y -= speed;
                if(raindrop.overlaps(bucket) && raindrop.y > ((bucket.height - 14) - speed)) {
                    dropSound.play();
                    iter.remove();
                    game.puntuacio++;

                }
                // Si cau la gota, es perd:
                if(raindrop.y + 64 < 0) {
                    iter.remove();
                    looseSound.play();
                    rainMusic.stop();
                    game.setScreen(new YouLost(game));
                }
            }
        }
        // Si es toquen puntuacions amb 0, s'apuja x1,3 la velocitat.
        if(game.puntuacio % 10 == 0 && game.puntuacio != 0){
            speed *= 1.3f;
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

    /*
    Metode que comprova quina acció farà el powerUp (Aleatori)
     */
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
